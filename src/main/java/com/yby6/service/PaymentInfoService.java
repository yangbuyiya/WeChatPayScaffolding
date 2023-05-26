// yangbuyi Copyright (c) https://yby6.com 2023.

package com.yby6.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yby6.entity.PaymentInfo;
import com.yby6.entity.Product;

import java.util.Map;

public interface PaymentInfoService extends IService<PaymentInfo> {

    void createPaymentInfo(String plainText);

    void createPaymentInfoV2(Map<String, String> notifyMap);

}
