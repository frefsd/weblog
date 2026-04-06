import router from '@/router/index'
import { getToken } from '@/composables/auth'
import { showMessage } from '@/composables/util'
import store from '@/store'
import { showPageLoading, hidePageLoading } from '@/composables/util'


// 全局前置守卫
router.beforeEach(async (to, from, next) => {
    console.log('全局前置守卫 >>>>')
    showPageLoading()
    const token = getToken()

    if (token) {
        try {
            // 注意：如果 token 过期，这个 dispatch 可能会报错或被后端拒绝
            // 如果你的 getAdminInfo 接口在 token 过期时会返回 401，
            // 那么 axios 拦截器会捕获到，并触发上面的 logout。
            await store.dispatch('getAdminInfo')
        } catch (e) {
            // 如果获取信息失败（比如 token 无效），强制登出
            await store.dispatch('logout')
            next({ path: '/login', query: { redirect: to.fullPath } })
            return
        }
    }

    // 加载博客设置（前后台都需要）
    await store.dispatch('getBlogSetting')


    //后台请求逻辑处理
    //未登录，强制跳转登录页
    if (!token && to.path.startsWith('/admin')) {
        showMessage('请先登录', 'warning')
        next({ path: '/login', query: { redirect: to.fullPath } })
        return
    }

    // 3. 已登录访问登录页 -> 踢回首页或上一页
    if (token && to.path == '/login') {
        next({ path: from.query.redirect || '/' })
        return
    }
    next()
})

router.afterEach((to, from) => {
    // 设置页面标题
    let title = (to.meta.title ? to.meta.title : '') + ' - WeBlog'
    document.title = title

    hidePageLoading()
})
