// yangbuyi Copyright (c) https://yby6.com 2023.

import request from '@/utils/request'

export function productList() {
  return request({
    url: '/api/product/list',
    method: 'get'
  })
}
