package com.yby6.util;

import cn.hutool.extra.spring.SpringUtil;
import com.wechat.pay.contrib.apache.httpclient.util.AesUtil;
import com.yby6.config.WxPayConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * 微信小程序验签和解密报文
 */
@Slf4j
@Component
public class WxSignUtil<T> {

    protected static final SecureRandom RANDOM = new SecureRandom();

    /**
     * 微信调起支付参数
     * 返回参数如有不理解 请访问微信官方文档
     *
     * @param prepayId 微信下单返回的prepay_id
     * @return 当前调起支付所需的参数
     */
    public static Map<String, Object> jsApiCreateSign(String prepayId) {
        if (StringUtils.isNotBlank(prepayId)) {
            final WxPayConfig wxPayConfig = SpringUtil.getBean(WxPayConfig.class);
            final String appid = wxPayConfig.getAppid();
            // 加载签名
            String timeStamp = String.valueOf(System.currentTimeMillis() / 1000);
            String nonceStr = String.valueOf(System.currentTimeMillis());
            String packageStr = "prepay_id=" + prepayId;
            String packageSign = sign(buildMessage(appid, timeStamp, nonceStr, packageStr).getBytes(StandardCharsets.UTF_8), wxPayConfig.getPrivateKey(wxPayConfig.getPrivateKeyPath()));
            Map<String, Object> packageParams = new HashMap<>(6);
            packageParams.put("appId", appid);
            packageParams.put("timeStamp", timeStamp);
            packageParams.put("nonceStr", nonceStr);
            packageParams.put("package", packageStr);
            packageParams.put("signType", "RSA");
            packageParams.put("paySign", packageSign);
            return packageParams;
        }
        return null;
    }

    /**
     * 生成签名
     * <p>
     * 小程序appId
     * 时间戳
     * 随机字符串
     * 订单详情扩展字符串
     */
    public static String sign(byte[] message, PrivateKey privateKey) {
        try {
            Signature sign = Signature.getInstance("SHA256withRSA");
            sign.initSign(privateKey); // 加载商户私钥
            sign.update(message); // UTF-8
            return Base64.getEncoder().encodeToString(sign.sign());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("当前Java环境不支持SHA256withRSA", e);
        } catch (SignatureException e) {
            throw new RuntimeException("签名计算失败", e);
        } catch (InvalidKeyException e) {
            throw new RuntimeException("无效的私钥", e);
        }
    }

    /**
     * 生成随机字符串 微信底层的方法
     */
    protected static String generateNonceStr() {
        char[] nonceChars = new char[32];
        for (int index = 0; index < nonceChars.length; ++index) {
            nonceChars[index] = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".charAt(RANDOM.nextInt("0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".length()));
        }
        return new String(nonceChars);
    }

    /**
     * 按照前端签名文档规范进行排序，\n是换行
     *
     * @param appId     appId
     * @param timestamp 时间
     * @param nonceStr  随机字符串
     * @param prepayIds prepay_id
     */
    public static String buildMessage(String appId, String timestamp, String nonceStr, String prepayIds) {
        return appId + "\n" + timestamp + "\n" + nonceStr + "\n" + prepayIds + "\n";
    }


    /**
     * 解密 对称解密
     *
     * @param plainText 秘文
     * @return {@link String}
     */
    public static <T> T decryptFromResource(String plainText, Class<T> clazz) {
        Map<String, Object> bodyMap = JsonUtils.toObject(plainText, Map.class);
        log.info("密文解密");
        final WxPayConfig wxPayConfig = SpringUtil.getBean(WxPayConfig.class);
        //通知数据拿到 resource 节点
        Map<String, String> resourceMap = (Map) bodyMap.get("resource");
        //数据密文
        String ciphertext = resourceMap.get("ciphertext");
        //随机串
        String nonce = resourceMap.get("nonce");
        //附加数据
        String associatedData = resourceMap.get("associated_data");
        log.info("密文 ===> {}", ciphertext);
        AesUtil aesUtil = new AesUtil(wxPayConfig.getApiV3Key().getBytes(StandardCharsets.UTF_8));
        // 使用key、nonce和associated_data，对数据密文resource.ciphertext进行解密，得到JSON形式的资源对象
        String resource;
        try {
            resource = aesUtil.decryptToString(associatedData.getBytes(StandardCharsets.UTF_8), nonce.getBytes(StandardCharsets.UTF_8), ciphertext);
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
        log.info("明文 ===> {}", resource);
        return JsonUtils.toObject(resource, clazz);
    }


}


