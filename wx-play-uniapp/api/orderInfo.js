// yangbuyi Copyright (c) https://yby6.com 2023.

import request from "@/utils/request";

export default {
  //查询订单列表
  list(payType) {
    return request({
      url: "/api/order-info/list",
      method: "get",
      params: {
        payType,
      },
    });
  },

  queryOrderStatus(orderNo) {
    return request({
      url: "/api/order-info/query-order-status/" + orderNo,
      method: "get",
    });
  },

  orderTimeout(orderNo) {
    return request({
      url: "/api/order-info/order-timeout/" + orderNo,
      method: "get",
    });
  },
};
