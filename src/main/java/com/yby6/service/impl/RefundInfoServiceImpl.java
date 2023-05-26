// yangbuyi Copyright (c) https://yby6.com 2023.

package com.yby6.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.gson.Gson;
import com.yby6.entity.OrderInfo;
import com.yby6.entity.RefundInfo;
import com.yby6.enums.weChatPay.WxRefundStatus;
import com.yby6.mapper.RefundInfoMapper;
import com.yby6.service.OrderInfoService;
import com.yby6.service.RefundInfoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yby6.util.OrderNoUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RefundInfoServiceImpl extends ServiceImpl<RefundInfoMapper, RefundInfo> implements RefundInfoService {

    private final OrderInfoService orderInfoService;


    @Override
    public void updateRefund(String content) {
        //将json字符串转换成Map
        Gson gson = new Gson();
        Map<String, String> resultMap = gson.fromJson(content, HashMap.class);

        //根据退款单编号修改退款单
        QueryWrapper<RefundInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("refund_no", resultMap.get("out_refund_no"));

        //设置要修改的字段
        RefundInfo refundInfo = new RefundInfo();

        refundInfo.setRefundId(resultMap.get("refund_id"));//微信支付退款单号

        //查询退款和申请退款中的返回参数
        if (resultMap.get("status") != null) {
            refundInfo.setRefundStatus(resultMap.get("status"));//退款状态
            refundInfo.setContentReturn(content);//将全部响应结果存入数据库的content字段
        }

        //退款回调中的回调参数
        if (resultMap.get("refund_status") != null) {
            refundInfo.setRefundStatus(resultMap.get("refund_status"));//退款状态
            refundInfo.setContentNotify(content);//将全部响应结果存入数据库的content字段
        }

        //更新退款单
        baseMapper.update(refundInfo, queryWrapper);
    }

    @Override
    public RefundInfo createRefundByOrderNo(String orderNo, String reason) {
        //根据订单号获取订单信息
        OrderInfo orderInfo = this.orderInfoService.lambdaQuery().eq(OrderInfo::getOrderNo, orderNo).one();

        //根据订单号生成退款订单
        RefundInfo refundInfo = new RefundInfo();
        refundInfo.setOrderNo(orderNo);//订单编号
        refundInfo.setRefundNo(OrderNoUtils.getRefundNo());//退款单编号
        refundInfo.setTotalFee(orderInfo.getTotalFee());//原订单金额(分)
        refundInfo.setRefund(orderInfo.getTotalFee());//退款金额(分)
        refundInfo.setReason(reason);//退款原因

        //保存退款订单
        baseMapper.insert(refundInfo);

        return refundInfo;
    }

    @Override
    public List<RefundInfo> getNoRefundOrderByDuration(int minutes) {
        // minutes 分钟之前的时间 查询 当前时间减去 minutes时间 = 要查询的时间之内的退款订单
        Instant instant = Instant.now().minus(Duration.ofMinutes(minutes));

        return this.lambdaQuery().eq(RefundInfo::getRefundStatus, WxRefundStatus.PROCESSING.getType())
                .le(RefundInfo::getCreateTime, instant).list();
    }
}
