// yangbuyi Copyright (c) https://yby6.com 2023.

import axios from 'axios'
import { ElMessage } from 'element-plus'

// 创建axios实例
const service = axios.create({
    baseURL: '/wx-api', // 部署 api 的 base_url
    // baseURL: 'https://lzys522.cn/wx-api', // 部署 api 的 base_url
    // baseURL: 'https://34i33045l8.oicp.vip', // api 的 base_url
    timeout: 20000 // 请求超时时间
})

// request拦截器
service.interceptors.request.use(config => {
    return config
}, error => {
    // Do something with request error
    Promise.reject(error)
})

// response 拦截器
service.interceptors.response.use(response => {

    const res = response.data
    if (res.code < 0) {
        ElMessage({
            message: res.message, type: 'error', duration: 5 * 1000
        })

        return Promise.reject('error')
    } else {
        return response.data
    }
}, error => {
    ElMessage({
        message: error.message, type: 'error', duration: 5 * 1000
    })
    return Promise.reject(error)
})

export default service
