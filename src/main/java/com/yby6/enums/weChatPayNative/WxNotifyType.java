package com.yby6.enums.weChatPayNative;

import lombok.Getter;

/**
 * Native回调枚举
 * 商户服务接收的回调 API 接口
 */
@Getter
public enum WxNotifyType {

    /**
     * 支付通知 v3
     * /v1/play/callback
     * /api/wx-pay/native/notify
     */
    NATIVE_NOTIFY("/api/wx-pay/native/notify"),

    /**
     * 支付通知 v2
     */
    NATIVE_NOTIFY_V2("/api/wx-pay-v2/native/notify"),

    /**
     * 退款结果通知
     */
    REFUND_NOTIFY("/api/wx-pay/refunds/notify");

    /**
     * 类型
     */
    final String type;

    WxNotifyType(String s) {
        this.type = s;
    }

}
