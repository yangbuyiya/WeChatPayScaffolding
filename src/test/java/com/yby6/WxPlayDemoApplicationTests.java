// yangbuyi Copyright (c) https://yby6.com 2023.

package com.yby6;

import cn.hutool.core.util.ArrayUtil;
import com.yby6.config.WxPayConfig;
import com.yby6.util.OrderNoUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.security.PrivateKey;
import java.time.Duration;
import java.time.Instant;

@SpringBootTest
class WxPlayDemoApplicationTests {
    @Resource
    private WxPayConfig wxPayConfig;

    @Resource
    private CloseableHttpClient wxPayClient;

    @Resource
    private CloseableHttpClient wxPayNoSignClient;



    /**
     * 获取商户的私钥
     */
    @Test
    void testGetPrivateKey() {

        System.out.println(OrderNoUtils.getOrderNo());



        //获取私钥路径
        String privateKeyPath = wxPayConfig.getPrivateKeyPath();

        //获取私钥
        PrivateKey privateKey = wxPayConfig.getPrivateKey(privateKeyPath);

        System.out.println(privateKey);

        System.out.println(wxPayClient);
        System.out.println(wxPayNoSignClient);


    }
}
