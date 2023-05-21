package com.yby6.controller.alipay;

import com.alipay.api.AlipayApiException;
import com.ijpay.alipay.AliPayApiConfig;


/**
 * ../
 *
 * @author Yang Shuai
 * Create By 2023/05/21
 */
public abstract class AbstractAliPayApiController {
	/**
	 * 获取支付宝配置
	 *
	 * @return {@link AliPayApiConfig} 支付宝配置
	 * @throws AlipayApiException 支付宝 Api 异常
	 */
	public abstract AliPayApiConfig getApiConfig() throws AlipayApiException;

}
