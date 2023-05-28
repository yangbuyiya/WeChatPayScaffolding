// yangbuyi Copyright (c) https://yby6.com 2023.

package com.yby6.task;

import com.yby6.entity.OrderInfo;
import com.yby6.entity.RefundInfo;
import com.yby6.service.OrderInfoService;
import com.yby6.service.RefundInfoService;
import com.yby6.service.WxPayService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@Component
public class WxPayTask {

    @Resource
    private OrderInfoService orderInfoService;

    @Resource
    private WxPayService wxPayService;

    @Resource
    private RefundInfoService refundInfoService;

    /**
     * 秒 分 时 日 月 周
     * 以秒为例
     * *：每秒都执行
     * 1-3：从第1秒开始执行，到第3秒结束执行
     * 0/3：从第0秒开始，每隔3秒执行1次
     * 1,2,3：在指定的第1、2、3秒执行
     * ?：不指定
     * 日和周不能同时制定，指定其中之一，则另一个设置为?
     */
    //@Scheduled(cron = "0/3 * * * * ?")
    public void task1() {
        log.info("task1 被执行......");
    }

    /**
     * 查询创建超过10分钟，并且未支付的订单
     */
    @Scheduled(cron = "0 0/5 * * * ? ")
    public void orderConfirm() throws Exception {
        log.info("orderConfirm 被执行......");

        List<OrderInfo> orderInfoList = orderInfoService.getNoPayOrderByDuration(10);

        for (OrderInfo orderInfo : orderInfoList) {
            String orderNo = orderInfo.getOrderNo();
            log.warn("超时订单 ===> {}", orderNo);

            //核实订单状态：调用微信支付查单接口
            wxPayService.checkOrderStatus(orderNo);
        }
    }


    /**
     * 查询创建超过10分钟，并且未成功的退款单
     */
    @Scheduled(cron = "0 0/5 * * * ? ")
    public void refundConfirm() throws Exception {
        log.info("refundConfirm 被执行......");

        // 找出申请退款超过5分钟并且未成功的退款单
        List<RefundInfo> refundInfoList = refundInfoService.getNoRefundOrderByDuration(10);

        for (RefundInfo refundInfo : refundInfoList) {
            String refundNo = refundInfo.getRefundNo();
            log.warn("超时未退款的退款单号 ===> {}", refundNo);

            // 核实订单状态：调用微信支付查询退款接口
            wxPayService.checkRefundStatus(refundNo);
        }
    }

}
