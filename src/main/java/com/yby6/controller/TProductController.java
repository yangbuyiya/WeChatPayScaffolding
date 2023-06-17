// yangbuyi Copyright (c) https://yby6.com 2023.

package com.yby6.controller;

import com.yby6.config.R;
import com.yby6.service.TProductService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 产品表控制层
 *
 * @author yangs
 */

@RestController
@RequestMapping("/api/product")
public class TProductController {
    /**
     * 服务对象
     */
    @Resource
    private TProductService tProductServiceImpl;

    @GetMapping("list")
    public R selectOne() {
        return R.ok().data("productList",tProductServiceImpl.lambdaQuery().list());
    }

}
