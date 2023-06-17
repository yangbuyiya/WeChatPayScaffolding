// yangbuyi Copyright (c) https://yby6.com 2023.

package com.yby6.controller.jsApiPay;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.yby6.config.R;
import com.yby6.config.WxPayConfig;
import com.yby6.domain.weChatJSAPi.LoginUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 路由
 *
 * @author Yang Shuai
 * Create By 2023/6/17
 */

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/wx-pay")
public class wxJsApiRouteController {
    private final WxPayConfig wxPayConfig;

    /**
     * 微信小程序登录接口 (登录or注册)
     *
     * @param loginUser 必填 code
     */
    @PostMapping("/jsapi/loginOrRegister")
    public R loginOrRegister(@RequestBody LoginUser loginUser) {
        String openId = getOpenId(loginUser.getCode());
        // 存到redis
        return R.ok().data("openId", openId);
    }

    /**
     * 获取微信唯一凭证
     *
     * @param code 代码
     * @return {@link String}
     */
    private String getOpenId(String code) {
        String url = "https://api.weixin.qq.com/sns/jscode2session";
        Map<String, Object> map = new HashMap<>();
        map.put("appId", wxPayConfig.getAppid());
        map.put("secret", wxPayConfig.getSecret());
        map.put("js_code", code);
        map.put("grant_type", "authorization_code");
        String post = HttpUtil.post(url, map);
        log.info("微信返回： {}", post);
        JSONObject obj = JSONUtil.parseObj(post);
        String openid = obj.getStr("openid");
        if (StringUtils.isNoneBlank(openid)) {
            return openid;
        }
        throw new RuntimeException("临时登录凭证错误");
    }

}
