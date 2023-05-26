// yangbuyi Copyright (c) https://yby6.com 2023.

package com.yby6.service.impl;

import cn.hutool.core.map.MapUtil;
import com.google.gson.Gson;
import com.yby6.entity.PaymentInfo;
import com.yby6.enums.PayType;
import com.yby6.mapper.PaymentInfoMapper;
import com.yby6.service.PaymentInfoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yby6.util.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * 付款信息服务impl
 *
 * @author yangs
 * @date 2023/03/29
 */
@Service
@Slf4j
public class PaymentInfoServiceImpl extends ServiceImpl<PaymentInfoMapper, PaymentInfo> implements PaymentInfoService {

    /**
     * 创建付款信息
     *
     * @param plainText 纯文本
     */
    @Override
    @Transactional
    public void createPaymentInfo(String plainText) {
        log.info("记录支付日志: {}", plainText);
        Map<String, Object> plainTextMap = JsonUtils.toObject(plainText, Map.class);

        //订单号
        String orderNo = (String) plainTextMap.get("out_trade_no");
        //业务编号
        String transactionId = (String) plainTextMap.get("transaction_id");
        //支付类型
        String tradeType = (String) plainTextMap.get("trade_type");
        //交易状态
        String tradeState = (String) plainTextMap.get("trade_state");
        //用户实际支付金额
        Map<String, Object> amount = (Map<String, Object>) plainTextMap.get("amount");

        Integer payerTotal = MapUtil.getInt(amount, "payer_total");

        PaymentInfo paymentInfo = buildInfo(orderNo, transactionId, tradeType, tradeState, payerTotal);
        paymentInfo.setContent(plainText);

        baseMapper.insert(paymentInfo);
    }


    @Override
    @Transactional
    public void createPaymentInfoV2(Map<String, String> plainTextMap) {
        log.info("记录V2支付日志: {}", plainTextMap);

        //订单号
        String orderNo = plainTextMap.get("out_trade_no");
        //业务编号
        String transactionId = plainTextMap.get("transaction_id");
        //支付类型
        String tradeType = plainTextMap.get("trade_type");
        //交易状态
        String tradeState = plainTextMap.get("return_code");
        //用户实际支付金额
        Integer payerTotal = Integer.valueOf(plainTextMap.get("total_fee"));

        PaymentInfo paymentInfo = buildInfo(orderNo, transactionId, tradeType, tradeState, payerTotal);
        paymentInfo.setContent(new Gson().toJson(plainTextMap));
        baseMapper.insert(paymentInfo);
    }


    /**
     * 建立信息
     *
     * @param orderNo       订单没有
     * @param transactionId 事务id
     * @param tradeType     贸易类型
     * @param tradeState    贸易国家
     * @param payerTotal    付款人总
     * @return {@link PaymentInfo}
     */
    private PaymentInfo buildInfo(String orderNo, String transactionId, String tradeType, String tradeState, Integer payerTotal) {
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setOrderNo(orderNo);
        paymentInfo.setPaymentType(PayType.WXPAY.getType());
        paymentInfo.setTransactionId(transactionId);
        paymentInfo.setTradeType(tradeType);
        paymentInfo.setTradeState(tradeState);
        paymentInfo.setPayerTotal(payerTotal);
        return paymentInfo;
    }
}
