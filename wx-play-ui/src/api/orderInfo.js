import request from '@/utils/request'

export default{
 
  //查询订单列表
  list() {
    return request({
      url: '/api/order-info/list',
      method: 'get'
    })
  },

  queryOrderStatus(orderNo) {
    return request({
      url: '/api/order-info/query-order-status/' + orderNo,
      method: 'get'
    })
  }
}
