// yangbuyi Copyright (c) https://yby6.com 2023.

package com.yby6.controller.nativePay;

import com.github.wxpay.sdk.WXPayUtil;
import com.yby6.config.R;
import com.yby6.config.WxPayConfig;
import com.yby6.entity.OrderInfo;
import com.yby6.enums.OrderStatus;
import com.yby6.service.OrderInfoService;
import com.yby6.service.PaymentInfoService;
import com.yby6.service.WxPayService;
import com.yby6.util.HttpUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 网站微信支付APIv2
 *
 * @author Yang Shuai
 * Create By 2023/05/21
 */

@RestController
@RequestMapping("/api/wx-pay-v2")
@Api(tags = "网站微信支付APIv2")
@Slf4j
public class WxPayV2Controller {

    @Resource
    private WxPayService wxPayService;

    @Resource
    private WxPayConfig wxPayConfig;

    @Resource
    private OrderInfoService orderInfoService;

    @Resource
    private PaymentInfoService paymentInfoService;

    private final ReentrantLock lock = new ReentrantLock();

    @ApiOperation("调用统一下单API，生成支付二维码")
    @PostMapping("/native/{productId}")
    public R createNative(@PathVariable Long productId, HttpServletRequest request) throws Exception {

        log.info("发起支付请求 v2");

        String remoteAddr = request.getRemoteAddr();
        Map<String, Object> map = wxPayService.nativePayV2(productId, remoteAddr);
        return R.ok().setData(map);
    }

    @ApiOperation("支付通知 -> 微信支付通过支付通知接口将用户支付成功消息通知给商户")
    @PostMapping("/native/notify")
    public String wxNotify(HttpServletRequest request) throws Exception {

        System.out.println("微信发送的回调");
        Map<String, String> returnMap = new HashMap<>();//应答对象

        //处理通知参数
        String body = HttpUtils.readData(request);

        //验签
        if (!WXPayUtil.isSignatureValid(body, wxPayConfig.getPartnerKey())) {
            log.error("通知验签失败");
            //失败应答
            returnMap.put("return_code", "FAIL");
            returnMap.put("return_msg", "验签失败");
            // 应答微信通知签名错误
            return WXPayUtil.mapToXml(returnMap);
        }

        // 解析xml数据
        Map<String, String> notifyMap = WXPayUtil.xmlToMap(body);
        //判断通信和业务是否成功
        if (!"SUCCESS".equals(notifyMap.get("return_code")) || !"SUCCESS".equals(notifyMap.get("result_code"))) {
            log.error("失败");
            //失败应答
            returnMap.put("return_code", "FAIL");
            returnMap.put("return_msg", "失败");
            // 应答微信解析错误
            return WXPayUtil.mapToXml(returnMap);
        }

        //获取商户订单号
        String orderNo = notifyMap.get("out_trade_no");
        OrderInfo orderInfo = orderInfoService.lambdaQuery().eq(OrderInfo::getOrderNo, orderNo).one();
        //并校验返回的订单金额是否与商户侧的订单金额一致
        if (orderInfo != null && orderInfo.getTotalFee() != Long.parseLong(notifyMap.get("total_fee"))) {
            log.error("金额校验失败");
            //失败应答
            returnMap.put("return_code", "FAIL");
            returnMap.put("return_msg", "金额校验失败");
            return WXPayUtil.mapToXml(returnMap);
        }

        //处理订单
        if (lock.tryLock()) {
            try {
                //处理重复的通知
                //接口调用的幂等性：无论接口被调用多少次，产生的结果是一致的。
                String orderStatus = orderInfoService.lambdaQuery().select(OrderInfo::getOrderStatus).eq(OrderInfo::getOrderNo, orderNo).one().getOrderStatus();
                if (OrderStatus.NOTPAY.getType().equals(orderStatus)) {
                    // 更新订单状态
                    orderInfoService.lambdaUpdate().eq(OrderInfo::getOrderNo, orderNo).set(OrderInfo::getOrderStatus, OrderStatus.SUCCESS.getType()).update();
                    //记录支付日志
                     paymentInfoService.createPaymentInfoV2(notifyMap);
                }
            } finally {
                //要主动释放锁
                lock.unlock();
            }
        }

        returnMap.put("return_code", "SUCCESS");
        returnMap.put("return_msg", "OK");
        String returnXml = WXPayUtil.mapToXml(returnMap);
        log.info("支付成功，已应答");
        return returnXml;
    }
}