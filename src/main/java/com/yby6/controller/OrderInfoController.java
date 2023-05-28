package com.yby6.controller;

import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.yby6.config.R;
import com.yby6.entity.OrderInfo;
import com.yby6.enums.OrderStatus;
import com.yby6.service.OrderInfoService;
import com.yby6.service.WxPayService;
import com.yby6.util.StringUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

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

    @Resource
    private WxPayService wxPayService;

    @ApiOperation("订单列表")
    @GetMapping("/list")
    public R list(@RequestParam(required = false) String payType) {
        LambdaQueryChainWrapper<OrderInfo> order = orderInfoService.lambdaQuery();
        if (StringUtils.isNotBlank(payType)) {
            if (!payType.contains("超时") && !payType.contains("取消") && !"全部".equals(payType)) {
                order.eq(OrderInfo::getOrderStatus, payType);
            } else {
                // 单独处理一下状态名称过长导致前段显示不好看
                if (payType.equals("超时关闭")) {
                    order.eq(OrderInfo::getOrderStatus, OrderStatus.CLOSED.getType());
                }
                if (payType.equals("用户取消")) {
                    order.eq(OrderInfo::getOrderStatus, OrderStatus.CANCEL.getType());
                }
            }
        }
        order.orderByDesc(OrderInfo::getCreateTime);
        return R.ok().data("list", order.list());
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

    @ApiOperation("主动发起订单超时")
    @GetMapping("/order-timeout/{orderNo}")
    public R orderTimeout(@PathVariable String orderNo) {
        try {
            wxPayService.checkOrderStatus(orderNo);
            return R.ok();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
