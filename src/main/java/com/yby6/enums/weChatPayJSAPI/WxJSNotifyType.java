// yangbuyi Copyright (c) https://yby6.com 2023.

package com.yby6.enums.weChatPayJSAPI;

import lombok.Getter;

/**
 * JS回调枚举
 * 商户服务接收的回调 API 接口
 */
@Getter
public enum WxJSNotifyType {

    /**
     * 支付通知 v3
     * /v1/play/callback
     * /api/wx-pay/native/notify
     */
    NATIVE_NOTIFY("/api/wx-pay/jsapi/notify"),
    /**
     * 退款结果通知
     */
    REFUND_NOTIFY("/api/wx-pay/jsapi/refunds/notify");

    /**
     * 类型
     */
    final String type;

    WxJSNotifyType(String s) {
        this.type = s;
    }

}
