// yangbuyi Copyright (c) https://yby6.com 2023.

package com.yby6.util;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Map;

/**
 * @author yang shuai
 * @date 2022/11/14
 */

@Slf4j
@Component
public class SendWechatServerInterface {
    @Resource
    private CloseableHttpClient wxPayClient;

    public static CloseableHttpResponse sendWeChart(Map<String, Object> paramsMap, String url) throws IOException {
        SendWechatServerInterface anInterface = new SendWechatServerInterface();
        return anInterface.send(paramsMap, url);
    }


    public CloseableHttpResponse send(Map<String, Object> paramsMap, String url) throws IOException {
        Gson gson = new Gson();
        HttpPost httpPost = new HttpPost(url);
        String jsonParams = gson.toJson(paramsMap);
        log.info("请求参数, {}", jsonParams);
        StringEntity entity = new StringEntity(jsonParams, "utf-8");
        entity.setContentType("application/json");
        httpPost.setEntity(entity);
        httpPost.setHeader("Accept", "application/json");
        return wxPayClient.execute(httpPost);
    }

}
