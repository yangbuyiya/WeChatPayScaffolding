// yangbuyi Copyright (c) https://yby6.com 2023.

package com.yby6.controller;

import com.yby6.config.R;
import com.yby6.entity.OrderInfo;
import com.yby6.enums.OrderStatus;
import com.yby6.service.OrderInfoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author yang shuai
 * @date 2022/11/13
 */

@CrossOrigin //开放前端的跨域访问
@Api(tags = "商品订单管理")
@RestController
@RequestMapping("/api/order-info")
public class OrderInfoController {

    @Resource
    private OrderInfoService orderInfoService;

    @ApiOperation("订单列表")
    @GetMapping("/list")
    public R list() {
        List<OrderInfo> list = orderInfoService.lambdaQuery().orderByDesc(OrderInfo::getCreateTime).list();
        return R.ok().data("list", list);
    }

    /**
     * 查询本地订单状态
     */
    @ApiOperation("查询本地订单状态")
    @GetMapping("/query-order-status/{orderNo}")
    public R queryOrderStatus(@PathVariable String orderNo) {
        String orderStatus = orderInfoService.lambdaQuery().eq(OrderInfo::getOrderNo, (orderNo)).one().getOrderStatus();
        if (OrderStatus.SUCCESS.getType().equals(orderStatus)) {
            return R.ok().setMessage("支付成功"); //支付成功
        }

        return R.ok().setCode(101).setMessage("支付中......");
    }


}
