package com.kxmall.market.admin.controller;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.github.binarywang.wxpay.bean.notify.WxPayNotifyResponse;
import com.github.binarywang.wxpay.bean.notify.WxPayOrderNotifyResult;
import com.github.binarywang.wxpay.exception.WxPayException;
import com.github.binarywang.wxpay.service.WxPayService;
import com.kxmall.market.biz.service.notify.AdminNotifyBizService;
import com.kxmall.market.biz.service.order.OrderBizService;
import com.kxmall.market.biz.service.user.UserBizService;
import com.kxmall.market.data.domain.OrderDO;
import com.kxmall.market.data.domain.OrderSkuDO;
import com.kxmall.market.data.dto.order.OrderDTO;
import com.kxmall.market.data.enums.OrderStatusType;
import com.kxmall.market.plugin.core.inter.IPluginPaySuccess;
import com.kxmall.market.plugin.core.manager.PluginsManager;
import com.kxmall.market.data.mapper.OrderMapper;
import com.kxmall.market.data.mapper.OrderSkuMapper;
import com.kxmall.market.data.mapper.SpuMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;

/**
 *
 * @author admin
 * @date 2019/7/10
 */
@RestController
@RequestMapping("/cb")
public class CallbackController {

    private static final Logger logger = LoggerFactory.getLogger(CallbackController.class);

    @Autowired
    private OrderBizService orderBizService;

    @Autowired
    private UserBizService userBizService;

    @Autowired
    private SpuMapper spuMapper;

    @Autowired
    private OrderSkuMapper orderSkuMapper;

    @Autowired
    private WxPayService wxPayService;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private PluginsManager pluginsManager;

    @Autowired
    private AdminNotifyBizService adminNotifyBizService;

    @RequestMapping("/wxpay")
    @Transactional(rollbackFor = Exception.class)
    public Object wxpay(@RequestBody String body) throws Exception {
        WxPayOrderNotifyResult result = null;
        try {
            result = wxPayService.parseOrderNotifyResult(body);
        } catch (WxPayException e) {
            logger.error("[微信解析回调请求] 异常", e);
            return WxPayNotifyResponse.fail(e.getMessage());
        }
        logger.info("处理腾讯支付平台的订单支付");
        logger.info(JSONObject.toJSONString(result));

        /* 之前传过去的我们系统的订单ID */
        String orderNo = result.getOutTradeNo();
        String payId = result.getTransactionId();

        List<OrderDO> orderDOList = orderMapper.selectList(
                new EntityWrapper<OrderDO>()
                        .eq("order_no", orderNo));

        if (CollectionUtils.isEmpty(orderDOList)) {
            return WxPayNotifyResponse.fail("订单不存在 orderNo=" + orderNo);
        }

        OrderDO order = orderDOList.get(0);

        // 检查这个订单是否已经处理过
        if (order.getStatus() != OrderStatusType.UNPAY.getCode()) {
            return WxPayNotifyResponse.success("订单已经处理成功!");
        }

        Integer totalFee = result.getTotalFee();

        // 检查支付订单金额
        if (!totalFee.equals(order.getActualPrice())) {
            return WxPayNotifyResponse.fail(order.getOrderNo() + " : 支付金额不符合 totalFee=" + totalFee);
        }

        //**************** 在此之前都没有 数据库修改 操作 所以前面是直接返回错误的 **********************//

        OrderDO updateOrderDO = new OrderDO();
        updateOrderDO.setPayId(payId);
        updateOrderDO.setPayChannel("WX");
        updateOrderDO.setPayPrice(totalFee);
        updateOrderDO.setGmtPay(new Date());
        updateOrderDO.setGmtUpdate(order.getGmtPay());
        if (order.getGroupShopId() != null) {
            updateOrderDO.setStatus(OrderStatusType.GROUP_SHOP_WAIT.getCode());
        } else {
            updateOrderDO.setStatus(OrderStatusType.WAIT_PREPARE_GOODS.getCode());
        }
        orderBizService.changeOrderStatus(orderNo, OrderStatusType.UNPAY.getCode(), updateOrderDO);
        List<OrderSkuDO> orderSkuDOList = orderSkuMapper.selectList(
                new EntityWrapper<OrderSkuDO>()
                        .eq("order_no", orderNo));
        orderSkuDOList.forEach(item -> {
            //增加销量
            spuMapper.incSales(item.getSpuId(), item.getNum());
        });

        OrderDTO orderDTO = new OrderDTO();
        BeanUtils.copyProperties(order, orderDTO);
        orderDTO.setPayChannel(updateOrderDO.getPayChannel());
        orderDTO.setSkuList(orderSkuDOList);

        List<IPluginPaySuccess> plugins = pluginsManager.getPlugins(IPluginPaySuccess.class);
        if (!CollectionUtils.isEmpty(plugins)) {
            String formId = userBizService.getValidFormIdByUserId(orderDTO.getUserId()).getFormId();
            for (IPluginPaySuccess paySuccess : plugins) {
                orderDTO = paySuccess.invoke(orderDTO, formId);
            }
        }
        //通知管理员发货
//        OrderDTO finalOrderDTO = orderDTO;
//        GlobalExecutor.execute(() -> {
//            adminNotifyBizService.newOrder(finalOrderDTO);
//        });


        return WxPayNotifyResponse.success("支付成功");
    }

}
