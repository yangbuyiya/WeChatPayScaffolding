import request from '@/utils/request'

export default{
	//查询订单列表
	list(payType) {
		return request({
			url: '/api/order-info/list',
			method: 'get',
			params: {
				payType
			}
		})
	},

	queryOrderStatus(orderNo) {
		return request({
			url: '/api/order-info/query-order-status/' + orderNo,
			method: 'get'
		})
	}
}
