package com.yby6.domain.weChatJSAPi;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * jsapi 下单请求参数
 * @author yang shuai
 * @date 2022/11/19
 */
@NoArgsConstructor
@Data
public class Transactions {


    @JsonProperty("mchid")
    private String mchid;
    @JsonProperty("out_trade_no")
    private String outTradeNo;
    @JsonProperty("appid")
    private String appid;
    @JsonProperty("description")
    private String description;
    @JsonProperty("notify_url")
    private String notifyUrl;
    @JsonProperty("amount")
    private AmountDTO amount;
    @JsonProperty("payer")
    private PayerDTO payer;

    /**
     * 金额实体
     */
    @NoArgsConstructor
    @Data
    public static class AmountDTO {
        @JsonProperty("total")
        private Integer total;
        @JsonProperty("currency")
        private String currency;
    }

    /**
     * 支付者
     */
    @NoArgsConstructor
    @Data
    public static class PayerDTO {
        @JsonProperty("openid")
        private String openid;
    }
}
