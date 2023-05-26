// yangbuyi Copyright (c) https://yby6.com 2023.

package com.yby6.service;

import com.yby6.entity.OrderInfo;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface OrderInfoService extends IService<OrderInfo> {

    /**
     * 创建订单
     * @param productId 商品
     */
    OrderInfo createOrderByProductId(Long productId, String nickName);

    /**
     * 查询超过五分钟的未支付的订单
     */
    List<OrderInfo> getNoPayOrderByDuration(int minutes);
}
