package com.yby6.controller;

import com.yby6.config.R;
import com.yby6.domain.TProduct;
import com.yby6.service.TProductService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * 产品表控制层
 *
 * @author yangs
 */
@CrossOrigin
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
