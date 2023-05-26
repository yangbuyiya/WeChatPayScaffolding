// yangbuyi Copyright (c) https://yby6.com 2023.

package com.yby6.enums.weChatPayJSAPI;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * JSAPI 接口枚举
 *
 * @author yang shuai
 * @date 2022/11/19
 */
@AllArgsConstructor
@Getter
public enum WxJSApiType {

    /**
     * jsapi 下单
     * POST
     */
    JSAPI_PAy("/v3/pay/transactions/jsapi");


    /**
     * 类型
     */
    private final String type;
}
