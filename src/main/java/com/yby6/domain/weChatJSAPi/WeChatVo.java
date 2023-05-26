// yangbuyi Copyright (c) https://yby6.com 2023.

package com.yby6.domain.weChatJSAPi;

import lombok.Data;

import java.time.OffsetDateTime;

/**
 * 微信
 *
 * @author yang shuai
 * @date 2023/3/7
 */
@Data
public class WeChatVo {

    /**
     * 交易订单
     */
    private String outTradeNo;
    /**
     * 开放id
     */
    private String openId;
    /**
     * 支付金额（分）
     */
    private Integer total;
    /**
     * 订单描述
     */
    private String desc;
    /**
     * 异步回调地址
     */
    private String notifyUrl;
    /**
     * 时间到期
     */
    private OffsetDateTime timeExpire;
}