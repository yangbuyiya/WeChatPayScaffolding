// yangbuyi Copyright (c) https://yby6.com 2023.

package com.yby6.config;

import com.wechat.pay.contrib.apache.httpclient.WechatPayHttpClientBuilder;
import com.wechat.pay.contrib.apache.httpclient.auth.PrivateKeySigner;
import com.wechat.pay.contrib.apache.httpclient.auth.ScheduledUpdateCertificatesVerifier;
import com.wechat.pay.contrib.apache.httpclient.auth.WechatPay2Credentials;
import com.wechat.pay.contrib.apache.httpclient.auth.WechatPay2Validator;
import com.wechat.pay.contrib.apache.httpclient.util.PemUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.impl.client.CloseableHttpClient;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;


@Configuration
@PropertySource("classpath:wxpay.properties") //读取配置文件
@ConfigurationProperties(prefix = "wxpay") //读取wxpay节点
@Data //使用set方法将wxpay节点中的值填充到当前类的属性中
@Slf4j
public class WxPayConfig {

    // 商户号
    private String mchId;

    // 商户API证书序列号
    private String mchSerialNo;

    // 商户私钥文件
    private String privateKeyPath;

    // 微信平台证书
    private String platformCertPath;

    // APIv3密钥
    private String apiV3Key;

    // APPID
    private String appid;

    // app secret
    private String secret;

    // 微信服务器地址
    private String domain;

    // 接收结果通知地址
    private String notifyDomain;

    // APIv2密钥
    private String partnerKey;


    /**
     * 获取商户的私钥文件
     */
    public PrivateKey getPrivateKey(String filename) {

        try {
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream(filename);
            if (inputStream == null) {
                throw new FileNotFoundException("私钥文件不存在");
            }
            return PemUtil.loadPrivateKey(inputStream);
        } catch (Exception e) {
            throw new RuntimeException("私钥文件不存在", e);
        }
    }

    /**
     * 获取签名验证器
     */
    @Bean
    public ScheduledUpdateCertificatesVerifier getVerifier() {

        log.info("构建签名验证器");
        log.info("mchId: {}", mchId);
        log.info("mchSerialNo: {}", mchSerialNo);
        log.info("privateKeyPath: {}", privateKeyPath);
        log.info("apiV3Key: {} , {}", apiV3Key , apiV3Key.length());
        log.info("appid: {}", appid);
        log.info("domain: {}", domain);
        log.info("notifyDomain: {}", notifyDomain);
        log.info("partnerKey: {} , {}", partnerKey , partnerKey.length());

        //获取商户私钥
        PrivateKey privateKey = getPrivateKey(privateKeyPath);

        //私钥签名对象
        PrivateKeySigner privateKeySigner = new PrivateKeySigner(mchSerialNo, privateKey);

        //身份认证对象
        WechatPay2Credentials wechatPay2Credentials = new WechatPay2Credentials(mchId, privateKeySigner);

        // 使用定时更新的签名验证器，不需要传入证书 -> 它会帮助我们下载最新的证书不会进行过期
        return new ScheduledUpdateCertificatesVerifier(wechatPay2Credentials, apiV3Key.getBytes(StandardCharsets.UTF_8));
    }


    /**
     * 获取http请求对象
     */
    @Bean(name = "wxPayClient")
    public CloseableHttpClient getWxPayClient(ScheduledUpdateCertificatesVerifier verifier) {

        log.info("获取httpClient");

        //获取商户私钥
        PrivateKey privateKey = getPrivateKey(privateKeyPath);

        WechatPayHttpClientBuilder builder = WechatPayHttpClientBuilder.create()
                // 设置商户信息
                .withMerchant(mchId, mchSerialNo, privateKey)
                // 设置验签器
                .withValidator(new WechatPay2Validator(verifier));
        // ... 接下来，你仍然可以通过builder设置各种参数，来配置你的HttpClient

        // 通过WechatPayHttpClientBuilder构造的HttpClient，会自动的处理签名和验签，并进行证书自动更新
        return builder.build();
    }

    /**
     * 获取HttpClient，无需进行应答签名验证，跳过验签的流程
     */
    @Bean(name = "wxPayNoSignClient")
    public CloseableHttpClient getWxPayNoSignClient() {

        //获取商户私钥
        PrivateKey privateKey = getPrivateKey(privateKeyPath);

        //用于构造HttpClient
        WechatPayHttpClientBuilder builder = WechatPayHttpClientBuilder.create()
                //设置商户信息
                .withMerchant(mchId, mchSerialNo, privateKey)
                //无需进行签名验证、通过withValidator((response) -> true)实现
                .withValidator((response) -> true);

        // 通过WechatPayHttpClientBuilder构造的HttpClient，会自动的处理签名和验签，并进行证书自动更新
        CloseableHttpClient httpClient = builder.build();

        log.info("== getWxPayNoSignClient END ==");

        return httpClient;
    }

//    @Bean
//    public Config getConfig () {
//        // 初始化商户配置
////        return new RSAConfig.Builder()
////                .merchantId(mchId)
////                // 使用 com.wechat.pay.java.core.util 中的函数从本地文件中加载商户私钥，商户私钥会用来生成请求的签名
////                .privateKeyFromPath(privateKeyPath)
////                .merchantSerialNumber(mchSerialNo)
////                .wechatPayCertificatesFromPath(privateKeyPath)
////                .build();
//
//        return new RSAAutoCertificateConfig.Builder()
//                        .merchantId(mchId)
//                        .privateKey(getPrivateKey(privateKeyPath))
//                        .merchantSerialNumber(mchSerialNo)
//                        .apiV3Key(apiV3Key)
//                        .build();
//    }

}
