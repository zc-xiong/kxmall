package com.kxmall.market.app.api.quartz;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.kxmall.market.biz.service.order.OrderBizService;
import com.kxmall.market.data.component.LockComponent;
import com.kxmall.market.data.domain.OrderDO;
import com.kxmall.market.data.domain.SpuDO;
import com.kxmall.market.data.enums.GroupShopAutomaticRefundType;
import com.kxmall.market.data.enums.OrderStatusType;
import com.kxmall.market.data.enums.StatusType;
import com.kxmall.market.data.mapper.OrderMapper;
import com.kxmall.market.data.mapper.SpuMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.CollectionUtils;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by admin on 2019/7/21.
 */
@Component
@EnableScheduling
public class CheckQuartz {

    private static final Logger logger = LoggerFactory.getLogger(CheckQuartz.class);
    private static final String ORDER_STATUS_LOCK = "ORDER_STATUS_QUARTZ_LOCK";
    private static final String GROUP_SHOP_START_LOCK = "GROUP_SHOP_START_LOCK";
    private static final String GROUP_SHOP_END_LOCK = "GROUP_SHOP_END_LOCK";
    private static final String GROUP_SHOP_LOCK_LOCK = "GROUP_SHOP_LOCK_LOCK";
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderBizService orderBizService;
    @Autowired
    private SpuMapper spuMapper;
    @Autowired
    private LockComponent lockComponent;
    @Autowired
    private TransactionTemplate transactionTemplate;

    /**
     * 订单状态定时轮训
     */
    @Scheduled(cron = "0 * * * * ?")
    public void checkOrderStatus() {
        if (lockComponent.tryLock(ORDER_STATUS_LOCK, 15)) {
            try {
                Date now = new Date();
                List<String> nos = orderMapper.selectExpireOrderNos(OrderStatusType.UNPAY.getCode(), new Date(now.getTime() - 1000l * 60 * 15));
                if (!CollectionUtils.isEmpty(nos)) {
                    nos.forEach(no -> {
                        try {
                            OrderDO updateOrderDO = new OrderDO();
                            updateOrderDO.setStatus(OrderStatusType.CANCELED_SYS.getCode());
                            updateOrderDO.setGmtUpdate(now);
                            orderBizService.changeOrderStatus(no, OrderStatusType.UNPAY.getCode(), updateOrderDO);
                        } catch (Exception e) {
                            logger.error("[未付款检测] 异常", e);
                        }
                    });
                }
                //15分钟执行一次
                long minutes = (now.getTime() / (1000 * 60));
                if (minutes % 15 == 0) {
                    List<String> waitConfirmNos = orderMapper.selectExpireOrderNos(OrderStatusType.WAIT_CONFIRM.getCode(), new Date(now.getTime() - 1000l * 60 * 60 * 24 * 7));
                    waitConfirmNos.forEach(item -> {
                        try {
                            OrderDO updateOrderDO = new OrderDO();
                            updateOrderDO.setStatus(OrderStatusType.WAIT_APPRAISE.getCode());
                            updateOrderDO.setGmtUpdate(now);
                            orderBizService.changeOrderStatus(item, OrderStatusType.WAIT_CONFIRM.getCode(), updateOrderDO);
                        } catch (Exception e) {
                            logger.error("[未确认检测] 异常", e);
                        }
                    });
                }
            } catch (Exception e) {
                logger.error("[订单状态检测定时任务] 异常", e);
            } finally {
                lockComponent.release(ORDER_STATUS_LOCK);
            }
        }
    }
}
