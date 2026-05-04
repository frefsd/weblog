import { createStore } from 'vuex'
import { getAdminInfo } from '@/api/admin/user'
import { getBlogSettingDetail } from '@/api/frontend/blogsetting'
import { logoutApi } from '@/api/admin/user'
import { removeToken } from '@/composables/auth'

// 创建一个新的 store 实例
const store = createStore({
    state() {
        return {
            // 用户信息
            user: {},
            setting: {},
            menuWidth: "250px"
        }
    },
    mutations: {
        // 设置全局用户信息
        SET_USERINFO(state, user) {
            state.user = user
        },
        // 设置博客设置信息
        SET_BLOG_SETTING(state, setting) {
            state.setting = setting
        },
        // 展开或缩起侧边栏
        HANDLE_MENU_WIDTH(state) {
            state.menuWidth = state.menuWidth == "250px" ? "64px" : "250px"
        }

    },
    actions: {
        // 获取用户登录信息
        getAdminInfo({ commit }) {
            return new Promise((resolve, reject) => {
                getAdminInfo().then(res => {
                    commit('SET_USERINFO', res.data)
                    resolve(res.data)
                }).catch(err => {
                    console.log('获取用户信息失败')
                    reject(err)
                })
            })
        },
        getBlogSetting({ commit }) {
            return new Promise((resolve, reject) => {
                getBlogSettingDetail().then(res => {
                    commit('SET_BLOG_SETTING', res.data)
                    resolve(res.data)
                }).catch(err => {
                    console.log('获取博客设置信息失败')
                    reject(err)
                })
            })
        },
        async logout({ commit }) {
            // 调用后端退出登录接口，清除 Redis 中的 Token
            try {
                await logoutApi()
            } catch (e) {
                console.log('调用退出接口失败，直接清除本地 Token')
            }
            removeToken()
            commit('SET_USERINFO', {})
        }
    }
})

export default store