package com.yby6.service;

import com.yby6.domain.weChart.tradebill.tradeBillResponse;

import java.security.GeneralSecurityException;
import java.util.Map;

/**
 * @author yang shuai
 * @date 2022/11/13
 */

public interface WxPayService {
    /**
     * 统一调用下单API，生成支付二维码
     * @param productId 商户ID
     */
    Map<String, Object> nativePay(Long productId) throws Exception;

    void processOrder(Map<String, Object> bodyMap) throws GeneralSecurityException, InterruptedException;

    void cancelOrder(String orderNo) throws Exception;

    String queryOrder(String orderNo) throws Exception;

    void checkOrderStatus(String orderNo) throws Exception;

    void refund(String orderNo, String reason, String refundsNo) throws Exception;

    String queryRefund(String refundNo) throws Exception;

    void checkRefundStatus(String refundNo) throws Exception;

    void processRefund(Map<String, Object> bodyMap) throws Exception;

    tradeBillResponse queryBill(String billDate, String type) throws Exception;

    String downloadBill(String billDate, String type) throws Exception;

    Map<String, Object> nativePayV2(Long productId, String remoteAddr) throws Exception;
}

