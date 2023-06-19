// yangbuyi Copyright (c) https://yby6.com 2023.

package com.yby6.controller.jsApiPay;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.ContentType;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.ijpay.core.IJPayHttpResponse;
import com.ijpay.core.enums.RequestMethodEnum;
import com.ijpay.core.kit.HttpKit;
import com.ijpay.core.kit.WxPayKit;
import com.ijpay.core.utils.DateTimeZoneUtil;
import com.ijpay.wxpay.WxPayApi;
import com.ijpay.wxpay.enums.WxDomainEnum;
import com.ijpay.wxpay.enums.v3.BasePayApiEnum;
import com.ijpay.wxpay.model.v3.Amount;
import com.ijpay.wxpay.model.v3.Payer;
import com.ijpay.wxpay.model.v3.UnifiedOrderModel;
import com.yby6.config.R;
import com.yby6.config.WxPayConfig;
import com.yby6.entity.OrderInfo;
import com.yby6.enums.weChatPayJSAPI.WxJSNotifyType;
import com.yby6.service.OrderInfoService;
import com.yby6.service.WxJSAPIPayService;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * IJPay JSAPI 支付 v3
 * <p>IJPay 微信支付 v3 接口示例</p>
 * <a href="https://gitee.com/javen205/IJPay?_from=gitee_search">开源仓库</a>
 * @author yang shuai
 * @date 2022/11/19
 */

@RestController
@RequestMapping("/api/wx-pay/jsapi/IJPay")
@Slf4j
public class wxJSApiPayByIJPayController {

    @Resource
    private WxPayConfig wxPayConfig;
    @Resource
    private WxJSAPIPayService wxJSAPIPayService;
    @Resource
    private OrderInfoService orderInfoService;

    @ApiOperation("调用统一下单API，生成支付二维码")
    @PostMapping("{productId}")
    public R jsPayPay(@PathVariable Long productId, @RequestParam String openId) {
        log.info("发起支付请求 v3, 返回支付二维码连接和订单号");

        try {
            String[] arr = openId.split("\\|");
            openId = arr[0];
            String nickName = arr.length > 1 ? arr[1] : "微信用户";

            log.info("生成订单 这里我就不进行订单已经存在的判断了反正订单失效时间3分钟");
            // 生成订单
            OrderInfo orderInfo = orderInfoService.createOrderByProductId(productId, nickName);
            String codeUrl = orderInfo.getCodeUrl();
            if (StrUtil.isNotEmpty(codeUrl)) {
                log.info("订单已存在，JSAPI已保存");
                Map<String, String> map = WxPayKit.jsApiCreateSign(wxPayConfig.getAppid(), codeUrl, wxPayConfig.getPrivateKeyPath());
                log.info("唤起小程序支付参数:{}", map);
                return R.ok().data("wx", map);
            }

            // 订单超时时间  五分钟过期
            String timeExpire = DateTimeZoneUtil.dateToTimeZone(System.currentTimeMillis() + 1000 * 60 * 3);
            UnifiedOrderModel unifiedOrderModel = new UnifiedOrderModel().setAppid(wxPayConfig.getAppid()).setMchid(wxPayConfig.getMchId()).setDescription(orderInfo.getTitle() + "-" + nickName).setOut_trade_no(orderInfo.getOrderNo()).setTime_expire(timeExpire) // 订单过期时间
                    .setAttach("杨不易呀 https://yby6.com").setNotify_url(wxPayConfig.getNotifyDomain().concat(WxJSNotifyType.NATIVE_NOTIFY.getType())).setAmount(new Amount().setTotal(orderInfo.getTotalFee())).setPayer(new Payer().setOpenid(openId));
            final String toJsonStr = JSONUtil.toJsonStr(unifiedOrderModel);
            log.info("统一下单参数 " + toJsonStr);
            IJPayHttpResponse response = WxPayApi.v3(RequestMethodEnum.POST, WxDomainEnum.CHINA.toString(), BasePayApiEnum.JS_API_PAY.toString(), wxPayConfig.getMchId(), wxPayConfig.getMchSerialNo(), null, wxPayConfig.getPrivateKeyPath(), toJsonStr);

            log.info("统一下单响应 " + response);

            String body = response.getBody();
            JSONObject jsonObject = JSONUtil.parseObj(body);
            String prepayId = jsonObject.getStr("prepay_id");
            Map<String, String> map = WxPayKit.jsApiCreateSign(wxPayConfig.getAppid(), prepayId, wxPayConfig.getPrivateKeyPath());
            log.info("唤起小程序支付参数:{}", map);
            // 保存预支付ID
            orderInfoService.lambdaUpdate().eq(OrderInfo::getOrderNo, orderInfo.getOrderNo()).set(OrderInfo::getCodeUrl, prepayId).update();
            return R.ok().data("wx", map);
        } catch (Exception e) {
            log.error("统一下单异常:{}", e.getMessage());
            throw new RuntimeException("统一下单异常，请稍后再试!");
        }
    }

    /**
     * jsapi支付
     * 微信支付成功回调
     *
     * @return {@link R}
     */
    @ApiOperation("支付通知->微信支付通过支付通知接口将用户支付成功消息通知给商户")
    @PostMapping("/notify")
    public Map<String, String> transactionCallBack(HttpServletRequest request, HttpServletResponse response) {
        Map<String, String> map = new HashMap<>(12);
        try {
            String timestamp = request.getHeader("Wechatpay-Timestamp");
            String nonce = request.getHeader("Wechatpay-Nonce");
            String serialNo = request.getHeader("Wechatpay-Serial");
            String signature = request.getHeader("Wechatpay-Signature");

            log.info("timestamp:" + timestamp + " nonce:" + nonce + " serialNo:" + serialNo + " signature:" + signature);
            String result = HttpKit.readData(request);
            log.info("支付通知密文 " + result);

            //处理通知参数


            // 需要通过证书序列号查找对应的证书，verifyNotify 中有验证证书的序列号
            String plainText = WxPayKit.verifyNotify(serialNo, result, signature, nonce, timestamp, wxPayConfig.getApiV3Key(), wxPayConfig.getPlatformCertPath());

            log.info("支付通知明文 " + plainText);

            if (StrUtil.isNotEmpty(plainText)) {
                response.setStatus(200);
                map.put("code", "SUCCESS");
                map.put("message", "SUCCESS");
                // 处理订单
                wxJSAPIPayService.processOrder(plainText);
            } else {
                response.setStatus(500);
                map.put("code", "ERROR");
                map.put("message", "签名错误");
            }
            response.setHeader("Content-type", ContentType.JSON.toString());
            response.getOutputStream().write(JSONUtil.toJsonStr(map).getBytes(StandardCharsets.UTF_8));
            response.flushBuffer();
        } catch (Exception e) {
            log.error("支付通知:{}", e.getMessage());
            throw new RuntimeException("支付通知，请稍后再试!");
        }
        return map;
    }
}
