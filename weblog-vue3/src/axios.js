import axios from "axios";
import { showMessage } from '@/composables/util'
import { getToken } from '@/composables/auth'
import store from "@/store";

// 401/403 防重入锁：防止多个并发 403 触发的链式登出
let isRefreshing = false

const instance = axios.create({
    baseURL: import.meta.env.VITE_APP_BASE_API,
    timeout: 7000  // 请求超时时间（毫秒）：7 秒后无响应自动中断，避免用户无限等待
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
    // 401：认证过期/未登录 → 登出 + 跳转登录页
    if (status == 401) {
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

    // 403：可能是"认证过期"（Spring 默认，空响应体）或"权限不足"（JSON 响应体）
    if (status == 403) {
        // 有 JSON 响应体说明是 GlobalExceptionHandler 返回的"权限不足"（如游客没有 ADMIN 角色）
        const isPermissionDenied = error.response.data &&
                                   typeof error.response.data === 'object' &&
                                   'success' in error.response.data

        if (!isPermissionDenied) {
            // 空响应体 → 认证过期 → 同 401 处理
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
        // 有 JSON 响应体 → 权限不足 → 交给下面的 isSuccess 逻辑展示错误消息，不登出
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