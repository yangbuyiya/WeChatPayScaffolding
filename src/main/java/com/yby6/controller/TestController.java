package com.yby6.controller;


import cn.dev33.satoken.util.SaTokenConsts;
import com.yby6.config.R;
import com.yby6.config.WxPayConfig;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/api/test")
public class TestController {

    @Resource
    private WxPayConfig wxPayConfig;

    @GetMapping("/mchid")
    public R getWxPayConfig(){

        String mchId = wxPayConfig.getMchId();
        return R.ok().data("mchId", mchId);
    }


    // 浏览器访问测试： http://localhost:8081
    @RequestMapping({"/", "/index"})
    public String index() {
        return "<br />"
                + "<h1 style='text-align: center;'>资源页 （登录后才可进入本页面） </h1>"
                + "<hr/>"
                + "<p style='text-align: center;'> Sa-Token " + SaTokenConsts.VERSION_NO + " </p>";
    }
}