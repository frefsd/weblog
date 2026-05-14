import axios from "axios";
import { showMessage } from '@/composables/util'
import { getToken } from '@/composables/auth'
import store from "@/store";

// 401/403 防重入锁：防止多个并发 403 触发的链式登出
let isRefreshing = false

const instance = axios.create({
    baseURL: import.meta.env.VITE_APP_BASE_API,
    timeout: 7000
});

// 添加请求拦截器
instance.interceptors.request.use(function (config) {
    // 在发送请求之前做些什么
    const token = getToken()
    //console.log('统一添加 token: ' + token)

    // 统一添加请求头 Token
    if (token) {
        config.headers['Authorization'] = 'Bearer ' + token
    }

    return config;
}, function (error) {
    // 对请求错误做些什么
    return Promise.reject(error);
});

// 添加响应拦截器
instance.interceptors.response.use(function (response) {
    // 对响应数据做点什么
    return response.data;
}, function (error) {
    // 防止报错：如果网络断了，error.response 可能是 undefined
    if (!error.response) {
        showMessage('网络错误，请检查服务器', 'error')
        return Promise.reject(error);
    }
    let status = error.response.status
    // 401/403：登录过期或无权访问 → 自动登出
    // 使用防重入锁避免链式反应（logoutApi 也会走这个拦截器）
    if (status == 401 || status == 403) {
        if (!isRefreshing) {
            isRefreshing = true
            showMessage('登录已过期，请重新登录', 'error')
            store.dispatch('logout').finally(() => {
                isRefreshing = false
                window.location.href = '/login'
            })
        }
        return Promise.reject(error);
    }
    let isSuccess = error.response.data.success
    if (!isSuccess) {
        let message = error.response.data.message || '请求失败'
        showMessage(message, 'error')
    }

    return Promise.reject(error);
});


// 暴露
export default instance