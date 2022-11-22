package com.kxmall.market.app.api.api.order.builder;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.kxmall.market.app.api.api.category.CategoryService;
import com.kxmall.market.app.api.api.order.OrderServiceImpl;
import com.kxmall.market.biz.service.orderobserve.OrderObserveAble;
import com.kxmall.market.biz.service.orderobserve.OrderUpdater;
import com.kxmall.market.core.exception.AppServiceException;
import com.kxmall.market.core.exception.ExceptionDefinition;
import com.kxmall.market.core.exception.ServiceException;
import com.kxmall.market.core.util.GeneratorUtil;
import com.kxmall.market.data.domain.*;
import com.kxmall.market.data.dto.UserCouponDTO;
import com.kxmall.market.data.dto.goods.SkuDTO;
import com.kxmall.market.data.dto.order.OrderPriceDTO;
import com.kxmall.market.data.dto.order.OrderRequestDTO;
import com.kxmall.market.data.dto.order.OrderRequestSkuDTO;
import com.kxmall.market.data.enums.OrderStatusType;
import com.kxmall.market.data.mapper.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @description: 订单具体建造者
 * @author: fy
 * @date: 2020/03/13 13:05
 **/
@Component
@SuppressWarnings("all")
public class OrderConcreteBuilder extends OrderBuilder {

    private static final Logger logger = LoggerFactory.getLogger(OrderServiceImpl.class);

    @Autowired
    private SkuMapper skuMapper;

    @Autowired
    private CartMapper cartMapper;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private AddressMapper addressMapper;

    @Resource
    private OrderObserveAble observeAble;

    @Autowired
    private OrderSkuMapper orderSkuMapper;

    @Autowired
    private UserCouponMapper userCouponMapper;

    @Autowired
    private CategoryService categoryService;

    @Value("${com.kxmall.market.machine-no}")
    private String MACHINE_NO;

    @Value("${com.kxmall.market.env}")
    private String ENV;

    /**
     * 1.订单初始创建校验部分
     *
     * @param orderRequest
     * @param userId
     */
    @Override
    public void buildOrderCheckHandlePart(OrderRequestDTO orderRequest, Long userId) throws ServiceException {
        //参数强校验 START
        List<OrderRequestSkuDTO> skuList = orderRequest.getSkuList();
        if (CollectionUtils.isEmpty(skuList) || orderRequest.getTotalPrice() == null) {
            throw new AppServiceException(ExceptionDefinition.PARAM_CHECK_FAILED);
        }
        if (orderRequest.getTotalPrice() <= 0) {
            throw new AppServiceException(ExceptionDefinition.ORDER_PRICE_MUST_GT_ZERO);
        }
    }

    /**
     * 2.订单价格处理部分
     *
     * @param orderDO
     * @param orderRequest
     * @param userId
     */
    @Override
    public OrderPriceDTO buildOrderPriceHandlePart(OrderDO orderDO, OrderRequestDTO orderRequest, Long userId) throws ServiceException {
        List<OrderRequestSkuDTO> skuList = orderRequest.getSkuList();
        Long groupShopId = orderRequest.getGroupShopId();
        Integer groupShopPrice = null;
        //商品价格
        int skuPrice = 0;
        int skuOriginalPrice = 0;
        //稍后用于优惠券作用范围校验
        Map<Long, Integer> categoryPriceMap = new HashMap<>();
        //稍后用于插入OrderSku
        SkuDTO skuDTO;
        Map<Long, SkuDTO> skuIdDTOMap = new HashMap<>();
        for (OrderRequestSkuDTO orderRequestSkuDTO : skuList) {
            //每个地方仓库的价格不一样，所以需要查询各自商户设置的价格
            skuDTO = skuMapper.getSkuDTOBySkuIdAndStorageId(orderRequestSkuDTO.getSkuId(), orderRequest.getStorageId());
            skuIdDTOMap.put(skuDTO.getId(), skuDTO);
            if (skuDTO == null) {
                throw new AppServiceException(ExceptionDefinition.ORDER_SKU_NOT_EXIST);
            }
            if (skuDTO.getStockDTO().getStock() < orderRequestSkuDTO.getNum()) {
                throw new AppServiceException(ExceptionDefinition.ORDER_SKU_STOCK_NOT_ENOUGH);
            }
            int p = 0;
            if (groupShopId != null && groupShopPrice != null) {
                p = groupShopPrice;
            } else {
                p = skuDTO.getStockDTO().getPrice() * orderRequestSkuDTO.getNum();
            }
            skuPrice += p;
            skuOriginalPrice += skuDTO.getSpuDO().getOriginalPrice() * orderRequestSkuDTO.getNum();
            List<Long> categoryFamily = categoryService.getCategoryFamily(skuDTO.getCategoryId());
            for (Long cid : categoryFamily) {
                Integer price = categoryPriceMap.get(cid);
                if (price == null) {
                    price = p;
                } else {
                    price += p;
                }
                categoryPriceMap.put(cid, price);
            }
        }
//                  这层判断会提交不了订单
//                if (skuPric!= orderRequest.getTotalPrice()) {
//                    throw new AppServiceException(ExceptionDefinition.ORDER_PRICE_CHECK_FAILED);
//                }
        //优惠券折扣价格
        int couponPrice = 0;
        //优惠券校验
        UserCouponDTO userCouponFromFront = orderRequest.getCoupon();
        //参数强校验 END
        //???是否校验actualPrice??强迫校验？
        int actualPrice = skuPrice - couponPrice;
        OrderPriceDTO orderPriceDTO = new OrderPriceDTO();
        orderPriceDTO.setGroupShopId(groupShopId);
        orderPriceDTO.setActualPrice(actualPrice);
        if (couponPrice != 0) {
            orderPriceDTO.setCouponPrice(couponPrice);
            orderPriceDTO.setCouponId(orderRequest.getCoupon().getCouponDO().getId());
        }
        orderPriceDTO.setFreightPrice(0);
        orderPriceDTO.setSkuTotalPrice(skuPrice);
        orderPriceDTO.setSkuIdDTOMap(skuIdDTOMap);
        orderPriceDTO.setSkuOriginalTotalPrice(skuOriginalPrice);
        return orderPriceDTO;
    }

    /**
     * 3.构建订单部分
     *
     * @param orderDO
     * @param orderRequest
     * @param channel
     * @param userId
     */
    @Override
    public void buildOrderHandlePart(OrderDO orderDO, OrderPriceDTO orderPriceDTO, OrderRequestDTO orderRequest, String channel, Long userId) throws ServiceException {
        orderDO.setSkuTotalPrice(orderPriceDTO.getSkuTotalPrice());
        orderDO.setSkuOriginalTotalPrice(orderPriceDTO.getSkuOriginalTotalPrice());
        orderDO.setChannel(channel);
        orderDO.setActualPrice(orderPriceDTO.getActualPrice());
        orderDO.setGroupShopId(orderPriceDTO.getGroupShopId());
        Integer couponPrice = orderPriceDTO.getCouponPrice();
        if (couponPrice != null && couponPrice != 0) {
            orderDO.setCouponId(orderPriceDTO.getCouponId());
            orderDO.setCouponPrice(couponPrice);
        }
        Date now = new Date();
        orderDO.setMono(orderRequest.getMono());
        orderDO.setFreightPrice(orderPriceDTO.getFreightPrice());
        orderDO.setOrderNo(GeneratorUtil.genOrderId(MACHINE_NO, ENV));
        orderDO.setUserId(userId);
        orderDO.setStatus(OrderStatusType.UNPAY.getCode());
        orderDO.setGmtUpdate(now);
        orderDO.setGmtCreate(now);
        orderDO.setGmtPredict(orderRequest.getGmtPredict());
        if (orderRequest.getAddressId() != null) {
            AddressDO addressDO = addressMapper.selectById(orderRequest.getAddressId());
            if (!userId.equals(addressDO.getUserId())) {
                throw new AppServiceException(ExceptionDefinition.ORDER_ADDRESS_NOT_BELONGS_TO_YOU);
            }
            orderDO.setConsignee(addressDO.getConsignee());
            orderDO.setPhone(addressDO.getPhone());
            orderDO.setProvince(addressDO.getProvince());
            orderDO.setCity(addressDO.getCity());
            orderDO.setCounty(addressDO.getCounty());
            orderDO.setAddress(addressDO.getAddress());
            //添加预计送达时间
            orderDO.setPredictTime(orderRequest.getPredictTime());
            orderDO.setStoreId(orderRequest.getStorageId());
        }
        orderMapper.insert(orderDO);
    }

    /**
     * 4.更新优惠券部分
     *
     * @param orderDO
     */
    @Override
    public void buildCoupontHandlePart(OrderDO orderDO) throws ServiceException {
        //扣除用户优惠券
        if (orderDO.getCouponId() != null) {
            UserCouponDO updateUserCouponDO = new UserCouponDO();
            updateUserCouponDO.setId(orderDO.getCouponId());
            updateUserCouponDO.setGmtUsed(new Date());
            updateUserCouponDO.setOrderId(orderDO.getId());
            userCouponMapper.updateById(updateUserCouponDO);
        }
    }

    /**
     * 5.订单商品SKU部分
     */
    @Override
    public void buildOrderSkuHandlePart(OrderDO orderDO, OrderPriceDTO orderPriceDTO, OrderRequestDTO orderRequest) {
        SkuDTO skuDTO;
        OrderSkuDO orderSkuDO;
        Date now = new Date();
        Map<Long, SkuDTO> skuIdDTOMap = orderPriceDTO.getSkuIdDTOMap();
        List<OrderSkuDO> orderSkuDOList = new ArrayList<>();
        List<OrderRequestSkuDTO> skuList = orderRequest.getSkuList();
        for (OrderRequestSkuDTO orderRequestSkuDTO : skuList) {
            orderSkuDO = new OrderSkuDO();
            skuDTO = skuIdDTOMap.get(orderRequestSkuDTO.getSkuId());
            orderSkuDO.setBarCode(skuDTO.getBarCode());
            orderSkuDO.setTitle(skuDTO.getTitle());
            orderSkuDO.setUnit(skuDTO.getUnit());
            orderSkuDO.setSpuTitle(skuDTO.getSpuTitle());
            orderSkuDO.setImg(skuDTO.getImg() == null ? skuDTO.getSpuImg() : skuDTO.getImg());
            orderSkuDO.setNum(orderRequestSkuDTO.getNum());
            orderSkuDO.setOriginalPrice(skuDTO.getSpuDO().getOriginalPrice());
            orderSkuDO.setPrice(skuDTO.getStockDTO().getPrice());
            orderSkuDO.setSkuId(skuDTO.getId());
            orderSkuDO.setSpuId(skuDTO.getSpuId());
            orderSkuDO.setOrderNo(orderDO.getOrderNo());
            orderSkuDO.setOrderId(orderDO.getId());
            orderSkuDO.setGmtCreate(now);
            orderSkuDO.setGmtUpdate(now);
            orderSkuDOList.add(orderSkuDO);
            //扣除库存
            skuMapper.decSkuStock(orderRequestSkuDTO.getSkuId(), orderRequestSkuDTO.getNum());
        }
        orderSkuMapper.insertBatch(orderSkuDOList);
    }

    /**
     * 6.购物车部分
     *
     * @param orderRequest
     * @param userId
     */
    @Override
    public void buildCartHandlePart(OrderRequestDTO orderRequest, Long userId) {
        List<OrderRequestSkuDTO> skuList = orderRequest.getSkuList();
        if (!StringUtils.isEmpty(orderRequest.getTakeWay())) {
            String takeWay = orderRequest.getTakeWay();
            if ("cart".equals(takeWay)) {
                //扣除购物车
                List<Long> skuIds = skuList.stream().map(item -> item.getSkuId()).collect(Collectors.toList());
                cartMapper.delete(new EntityWrapper<CartDO>().in("sku_id", skuIds).eq("user_id", userId));
            }
            //直接购买传值为 "buy"
        }
    }

    /**
     * 7.触发订单创建完成后通知事件部分
     *
     * @param orderDO
     */
    @Override
    public void buildNotifyHandlePart(OrderDO orderDO) {
        OrderUpdater updater = new OrderUpdater(orderDO);
        observeAble.notifyObservers(updater);
    }
}
