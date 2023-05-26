// yangbuyi Copyright (c) https://yby6.com 2023.

package com.yby6.service;

import com.yby6.entity.RefundInfo;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface RefundInfoService extends IService<RefundInfo> {

    void updateRefund(String bodyAsString);

    RefundInfo createRefundByOrderNo(String orderNo, String reason);

    List<RefundInfo> getNoRefundOrderByDuration(int minutes);

}
