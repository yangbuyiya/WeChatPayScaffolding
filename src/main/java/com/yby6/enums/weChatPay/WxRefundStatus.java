// yangbuyi Copyright (c) https://yby6.com 2023.

package com.yby6.enums.weChatPay;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 退款状态枚举
 */
@AllArgsConstructor
@Getter
public enum WxRefundStatus {

    /**
     * 退款成功
     */
    SUCCESS("SUCCESS"),

    /**
     * 退款关闭
     */
    CLOSED("CLOSED"),

    /**
     * 退款处理中
     */
    PROCESSING("PROCESSING"),

    /**
     * 退款异常
     */
    ABNORMAL("ABNORMAL");

    /**
     * 类型
     */
    private final String type;
}
