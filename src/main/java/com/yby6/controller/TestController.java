// yangbuyi Copyright (c) https://yby6.com 2023.

package com.yby6.controller;


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

}