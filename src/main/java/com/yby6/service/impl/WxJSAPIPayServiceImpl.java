// yangbuyi Copyright (c) https://yby6.com 2023.

package com.yby6.service.impl;

import com.yby6.domain.weChatJSAPi.CallBackResource;
import com.yby6.entity.OrderInfo;
import com.yby6.enums.OrderStatus;
import com.yby6.service.OrderInfoService;
import com.yby6.service.PaymentInfoService;
import com.yby6.service.WxJSAPIPayService;
import com.yby6.util.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Yang Shuai
 * {@code @create} 2023/5/24:9:43
 * {@code @desc} |
 **/
@Service
@Slf4j
public class WxJSAPIPayServiceImpl implements WxJSAPIPayService {

    private final ReentrantLock lock = new ReentrantLock();
    @Resource
    private OrderInfoService orderInfoService;
    @Resource
    private PaymentInfoService paymentInfoService;

    /**
     * jsapi回调
     */
    @Override
    public void processOrder(String plainText) {
        final CallBackResource data = JsonUtils.toObject(plainText, CallBackResource.class);


        log.info("处理订单");
        // 微信特别提醒：
        // 在对业务数据进行状态检查和处理之前，
        // 要采用数据锁进行并发控制，以避免函数重入造成的数据混乱.

        // 尝试获取锁：
        // 成功获取则立即返回true，获取失败则立即返回false。不必一直等待锁的释放.
        if (lock.tryLock()) {
            try {
                // 处理重复的通知
                // 接口调用的幂等性：无论接口被调用多少次，产生的结果是一致的。
                OrderInfo orderInfo = orderInfoService.lambdaQuery().eq(OrderInfo::getOrderNo, (data.getOutTradeNo())).one();

                if (null != orderInfo && !OrderStatus.NOTPAY.getType().equals(orderInfo.getOrderStatus())) {
                    log.info("重复的通知,已经支付成功啦");
                    return;
                }

                // 模拟通知并发
                //TimeUnit.SECONDS.sleep(5);

                // 更新订单状态
                orderInfoService.lambdaUpdate().eq(OrderInfo::getOrderNo, data.getOutTradeNo()).set(OrderInfo::getOrderStatus, OrderStatus.SUCCESS.getType()).update();
                log.info("更新订单状态,订单号：{},订单状态：{}", data.getOutTradeNo(), OrderStatus.SUCCESS);
                // 记录支付日志
                paymentInfoService.createPaymentInfo(plainText);
            } finally {
                // 要主动释放锁
                lock.unlock();
            }
        }
    }
    /*==========================================================================*/
}
