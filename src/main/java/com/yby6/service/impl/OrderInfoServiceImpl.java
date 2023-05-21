package com.yby6.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yby6.domain.TProduct;
import com.yby6.entity.OrderInfo;
import com.yby6.enums.OrderStatus;
import com.yby6.mapper.OrderInfoMapper;
import com.yby6.service.OrderInfoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yby6.service.TProductService;
import com.yby6.util.OrderNoUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

@RequiredArgsConstructor
@Service
public class OrderInfoServiceImpl extends ServiceImpl<OrderInfoMapper, OrderInfo> implements OrderInfoService {

    private final TProductService tProductService;

    /**
     * 创建订单
     *
     * @param productId 商品
     */
    @Override
    @Transactional
    public OrderInfo createOrderByProductId(Long productId) {
        // 查找已存在但未支付的订单
        OrderInfo orderInfo = this.lambdaQuery().eq(OrderInfo::getProductId, productId).eq(OrderInfo::getOrderStatus, OrderStatus.NOTPAY.getType()).one();
        if (orderInfo != null) {
            return orderInfo;
        }
        // 根据商品ID 获取 商品信息
        TProduct product = tProductService.lambdaQuery().eq(TProduct::getId, productId).one();
        // 创建订单信息
        orderInfo = new OrderInfo();
        orderInfo.setTitle(product.getTitle());
        orderInfo.setOrderNo(OrderNoUtils.getOrderNo());
        orderInfo.setProductId(productId);
        orderInfo.setTotalFee(product.getPrice()); // 分
        orderInfo.setOrderStatus(OrderStatus.NOTPAY.getType());
        save(orderInfo);
        return orderInfo;
    }

    @Override
    public List<OrderInfo> getNoPayOrderByDuration(int minutes) {
        // 获取当前时间未来的minutes时间 早于五分钟之前
        Instant instant = Instant.now().minus(Duration.ofMinutes(minutes));

        QueryWrapper<OrderInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("order_status", OrderStatus.NOTPAY.getType());
        queryWrapper.le("create_time", instant);

        return baseMapper.selectList(queryWrapper);
    }
}
