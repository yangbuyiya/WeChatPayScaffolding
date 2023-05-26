// yangbuyi Copyright (c) https://yby6.com 2023.

package com.yby6.domain.weChatJSAPi;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ../
 *
 * @author Yang Shuai
 * Create By 2023/05/24
 */
@NoArgsConstructor
@Data
public class CallBackResource {

    /**
     * mchid
     */
    @JsonProperty("mchid")
    private String mchid;
    /**
     * appid
     */
    @JsonProperty("appid")
    private String appid;
    /**
     * 贸易没有
     */
    @JsonProperty("out_trade_no")
    private String outTradeNo;
    /**
     * 交易订单id
     */
    @JsonProperty("transaction_id")
    private String transactionId;
    /**
     * 贸易类型
     */
    @JsonProperty("trade_type")
    private String tradeType;
    /**
     * 贸易国家
     */
    @JsonProperty("trade_state")
    private String tradeState;
    /**
     * 贸易国家desc
     */
    @JsonProperty("trade_state_desc")
    private String tradeStateDesc;
    /**
     * 银行类型
     */
    @JsonProperty("bank_type")
    private String bankType;
    /**
     * 附加
     */
    @JsonProperty("attach")
    private String attach;
    /**
     * 成功时间
     */
    @JsonProperty("success_time")
    private String successTime;
    /**
     * 付款人
     */
    @JsonProperty("payer")
    private PayerDTO payer;
    /**
     * 量
     */
    @JsonProperty("amount")
    private AmountDTO amount;

    /**
     * ../
     *
     * @author Yang Shuai
     * Create By 2023/05/24
     */
    @NoArgsConstructor
    @Data
    public static class PayerDTO {
        /**
         * openid
         */
        @JsonProperty("openid")
        private String openid;
    }

    /**
     * ../
     *
     * @author Yang Shuai
     * Create By 2023/05/24
     */
    @NoArgsConstructor
    @Data
    public static class AmountDTO {
        /**
         * 总
         */
        @JsonProperty("total")
        private Integer total;
        /**
         * 付款人总
         */
        @JsonProperty("payer_total")
        private Integer payerTotal;
        /**
         * 货币
         */
        @JsonProperty("currency")
        private String currency;
        /**
         * 支付货币
         */
        @JsonProperty("payer_currency")
        private String payerCurrency;
    }
}
