// yangbuyi Copyright (c) https://yby6.com 2023.

import request from '@/utils/request'

// 登录方法获取openId
export function loginOrRegister(data) {
    return request({
        'url': '/api/wx-pay/jsapi/loginOrRegister',
        'method': 'post',
        'data': data
    })
}

// 统一JSAPI下单
export function jsapi(productId, openId) {
    return request({
        'url': `/api/wx-pay/jsapi/${productId}`,
        'method': 'post',
		'params': {
			"openId" : openId
		}
    })
}

// 取消订单
export function cancel(orderNo) {
    return request({
        url: '/api/wx-pay/cancel/' + orderNo,
        method: 'post'
    })
}

// 申请退款

export function refunds(orderNo, reason, refundNo) {
    return request({
        url: '/api/wx-pay/refunds/' + orderNo + '/' + refundNo + "/" + reason,
        method: 'post'
    })
}
