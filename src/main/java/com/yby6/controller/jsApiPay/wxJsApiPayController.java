// yangbuyi Copyright (c) https://yby6.com 2023.

package com.yby6.controller.jsApiPay;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.ContentType;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.wechat.pay.contrib.apache.httpclient.auth.Verifier;
import com.yby6.config.R;
import com.yby6.config.WxPayConfig;
import com.yby6.domain.weChatJSAPi.CallBackResource;
import com.yby6.entity.OrderInfo;
import com.yby6.enums.weChatPayJSAPI.WxJSApiType;
import com.yby6.enums.weChatPayJSAPI.WxJSNotifyType;
import com.yby6.service.OrderInfoService;
import com.yby6.service.WxJSAPIPayService;
import com.yby6.util.HttpUtils;
import com.yby6.util.JsonUtils;
import com.yby6.util.WechatPay2ValidatorForRequest;
import com.yby6.util.WxSignUtil;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * 微信支付V3 小程序对接支付
 *
 * @author Yang Shuai
 * {@code @create} 2023/6/16:14:44
 * {@code @desc} |
 **/
@Slf4j
@RestController
@RequestMapping("/api/wx-pay/jsapi")
@RequiredArgsConstructor
public class wxJsApiPayController {

    private final CloseableHttpClient wxPayClient;
    private final WxPayConfig wxPayConfig;
    private final OrderInfoService orderInfoService;
    private final WxJSAPIPayService wxJSAPIPayService;

    private final Verifier verifier;

    @SneakyThrows
    @ApiOperation("调用统一下单API，生成支付二维码")
    @PostMapping("{productId}")
    public R jsPayPay(@PathVariable Long productId, @RequestParam("openId") String openId) {

        String[] arr = openId.split("\\|");
        String openIdTep = arr[0];
        String nickName = arr.length > 1 ? arr[1] : "PC用户";

        // 生成订单
        OrderInfo orderInfo = orderInfoService.createOrderByProductId(productId, nickName);
        String prepayId = orderInfo.getCodeUrl(); // prepayId
        if (StrUtil.isNotEmpty(prepayId) && "未支付".equals(orderInfo.getOrderStatus())) {
            log.info("订单已存在，JSAPI已保存");
            Map<String, Object> map = WxSignUtil.jsApiCreateSign(prepayId);
            log.info("唤起小程序支付参数:{}", map);
            return R.ok().data("wx", map);
        }


        HttpPost httpPost = new HttpPost(wxPayConfig.getDomain().concat(WxJSApiType.JSAPI_PAY.getType()));
        Map<String, Object> paramsMap = new HashMap<>(14);
        paramsMap.put("mchid", wxPayConfig.getMchId());
        paramsMap.put("appid", wxPayConfig.getAppid());
        paramsMap.put("description", orderInfo.getTitle() + "-" + nickName);
        paramsMap.put("out_trade_no", orderInfo.getOrderNo());
        paramsMap.put("notify_url", wxPayConfig.getNotifyDomain().concat(WxJSNotifyType.NATIVE_NOTIFY.getType()));
        Map<String, Object> amountMap = new HashMap<>();
        amountMap.put("total", orderInfo.getTotalFee());
        amountMap.put("currency", "CNY");
        // 设置金额
        paramsMap.put("amount", amountMap);
        paramsMap.put("payer", new HashMap<String, Object>() {{
            put("openid", openIdTep);
        }});
        //将参数转换成json字符串
        JSONObject jsonObject = JSONUtil.parseObj(paramsMap);
        log.info("请求参数 ===> {0}" + jsonObject);
        StringEntity entity = new StringEntity(jsonObject.toString(), "utf-8");
        entity.setContentType("application/json");
        httpPost.setEntity(entity);
        httpPost.setHeader("Accept", "application/json");

        CloseableHttpResponse response = wxPayClient.execute(httpPost);
        String bodyAsString = EntityUtils.toString(response.getEntity());
        JSONObject object = JSONUtil.parseObj(bodyAsString);
        response.close();
        prepayId = object.getStr("prepay_id");
        Map<String, Object> map = WxSignUtil.jsApiCreateSign(prepayId);
        return R.ok().data("wx", map);
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
            //处理通知参数
            String body = HttpUtils.readData(request);
            log.info("支付通知密文: {} ", body);


            //签名的验证
            WechatPay2ValidatorForRequest wechatPay2ValidatorForRequest = new WechatPay2ValidatorForRequest(verifier, body);
            if (!wechatPay2ValidatorForRequest.validate(request)) {
                log.error("通知验签失败");
                //失败应答
                response.setStatus(500);
                map.put("code", "ERROR");
                map.put("message", "通知验签失败");
                return map;
            }

            response.setStatus(200);
            map.put("code", "SUCCESS");
            map.put("message", "SUCCESS");
            // 处理订单
            final CallBackResource decrypt = WxSignUtil.decryptFromResource(body, CallBackResource.class);
            wxJSAPIPayService.processOrder(JsonUtils.toJsonStr(decrypt));
            response.setHeader("Content-type", ContentType.JSON.toString());
            response.getOutputStream().write(JSONUtil.toJsonStr(map).getBytes(StandardCharsets.UTF_8));
            response.flushBuffer();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }


}
