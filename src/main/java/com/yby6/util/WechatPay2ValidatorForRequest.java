// yangbuyi Copyright (c) https://yby6.com 2023.

package com.yby6.util;


import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.wechat.pay.contrib.apache.httpclient.auth.Verifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.DateTimeException;
import java.time.Duration;
import java.time.Instant;

import static com.wechat.pay.contrib.apache.httpclient.constant.WechatPayHttpHeaders.*;

/**
 * 模仿微信验证签名，自定义支付通知API验证签名，针对通知请求的签名验证
 */
public class WechatPay2ValidatorForRequest {

    protected static final Logger log = LoggerFactory.getLogger(WechatPay2ValidatorForRequest.class);
    /**
     * 应答超时时间，单位为分钟
     */
    protected static final long RESPONSE_EXPIRED_MINUTES = 5;
    protected final Verifier verifier;
    protected final String body;
    protected final String requestId;

    /**
     * 微信验证器
     *
     * @param verifier 验证器
     * @param body     微信回调的body
     */
    public WechatPay2ValidatorForRequest(Verifier verifier, String body) {
        this.verifier = verifier;
        final JSONObject object = JSONUtil.parseObj(body);
        this.requestId = object.getStr("id"); // requestId 请求id
        this.body = body;
    }

    /**
     * 参数错误
     *
     * @param message 消息
     * @return {@link IllegalArgumentException}
     */
    protected static IllegalArgumentException parameterError(String message, Object... args) {
        message = String.format(message, args);
        return new IllegalArgumentException("parameter error: " + message);
    }

    /**
     * 验证失败
     *
     * @param message 消息
     * @return {@link IllegalArgumentException}
     */
    protected static IllegalArgumentException verifyFail(String message, Object... args) {
        message = String.format(message, args);
        return new IllegalArgumentException("signature verify fail: " + message);
    }

    /**
     * 验证
     *
     * @param request 请求
     * @return boolean 是否成功
     * @throws IOException ioexception
     */
    public final boolean validate(HttpServletRequest request) throws IOException {
        try {
            // 调用验证回调参数
            validateParameters(request);

            // 验签字符串
            String message = buildMessage(request);
            String serial = request.getHeader(WECHAT_PAY_SERIAL);
            // 签名
            String signature = request.getHeader(WECHAT_PAY_SIGNATURE);

            // 进行验证
            if (!verifier.verify(serial, message.getBytes(StandardCharsets.UTF_8), signature)) {
                throw verifyFail("serial=[%s] message=[%s] sign=[%s], request-id=[%s]", serial, message, signature, request.getHeader(REQUEST_ID));
            }
        } catch (IllegalArgumentException e) {
            log.warn(e.getMessage());
            return false;
        }
        return true;
    }

    /**
     * 验证参数
     *
     * @param request 请求
     */
    protected final void validateParameters(HttpServletRequest request) {

        // NOTE: ensure HEADER_WECHAT_PAY_TIMESTAMP at last
        String[] headers = {WECHAT_PAY_SERIAL, WECHAT_PAY_SIGNATURE, WECHAT_PAY_NONCE, WECHAT_PAY_TIMESTAMP};

        // 这些头必须存在否则直接是伪造
        String header = null;
        for (String headerName : headers) {
            header = request.getHeader(headerName);
            if (header == null) {
                throw parameterError("empty [%s], request-id=[%s]", headerName, requestId);
            }
        }

        // 循环完毕直接默认被赋值是时间戳
        String timestampStr = header;
        try {
            // 验证过期应答
            Instant responseTime = Instant.ofEpochSecond(Long.parseLong(timestampStr));
            // 拒绝过期应答
            if (Duration.between(responseTime, Instant.now()).abs().toMinutes() >= RESPONSE_EXPIRED_MINUTES) {
                throw parameterError("timestamp=[%s] expires, request-id=[%s]", timestampStr, requestId);
            }
        } catch (DateTimeException | NumberFormatException e) {
            throw parameterError("invalid timestamp=[%s], request-id=[%s]", timestampStr, requestId);
        }
    }

    /**
     * 构建验证签名消息
     * 参考文档：<a href="https://pay.weixin.qq.com/wiki/doc/apiv3/wechatpay/wechatpay4_1.shtml">参考文档</a>
     * <p>
     * 构造验签名串
     * 首先，商户先从应答中获取以下信息。
     * <p>
     * HTTP头Wechatpay-Timestamp 中的应答时间戳。
     * HTTP头Wechatpay-Nonce 中的应答随机串。
     * 应答主体（response Body），需要按照接口返回的顺序进行验签，错误的顺序将导致验签失败。
     * 然后，请按照以下规则构造应答的验签名串。签名串共有三行，行尾以\n 结束，包括最后一行。\n为换行符（ASCII编码值为0x0A）。
     * 若应答报文主体为空（如HTTP状态码为204 No Content），最后一行仅为一个\n换行符。
     * <p>
     * ***********************************
     * 应答时间戳\n
     * 应答随机串\n
     * 应答报文主体\n
     * ***********************************
     * <p>
     *
     * @param request 请求
     * @return {@link String}
     * @throws IOException ioexception
     */
    protected final String buildMessage(HttpServletRequest request) throws IOException {
        String timestamp = request.getHeader(WECHAT_PAY_TIMESTAMP);
        String nonce = request.getHeader(WECHAT_PAY_NONCE);
        return timestamp + "\n" + nonce + "\n" + body + "\n";
    }
}
