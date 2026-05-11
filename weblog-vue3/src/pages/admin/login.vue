<template>
    <div>
        <div class="grid grid-cols-6 h-screen">
            <div class="col-span-6 md:col-span-3 sm:col-span-6">
                <div
                    class="login-container-left relative overflow-hidden h-full w-full bg-[#001428] animate__animated animate__bounceInLeft">
                    <img src="@/assets/an.jpeg" class="absolute inset-0 w-full h-full object-cover z-0"
                        alt="Background">
                    <div class="absolute inset-0 bg-black/40 z-10"></div>
                    <div class="absolute inset-0 z-20 flex justify-center items-center flex-col p-4 text-center">
                        <div class="max-w-lg">
                            <h2 class="font-bold text-4xl mb-7 text-white drop-shadow-xl">Weblog 博客登录</h2>
                            <p class="text-white text-lg drop-shadow-md opacity-90">一款由 Spring Boot + Mybaits Plus + Vue
                                3.2 + Vite 4 开发的前后端分离博客。</p>
                        </div>
                    </div>

                </div>
            </div>
            <!-- 右边栏 -->
            <div class="col-span-6 px-3 md:col-span-3 sm:col-span-6 login-right-panel">
                <div
                    class="login-container-right flex justify-center items-center flex-col animate__animated animate__bounceInRight animate__fast">
                    <h2 class="font-bold text-3xl text-gray-800 mt-5">欢迎回来</h2>
                    <div class="flex items-center justify-center my-5 text-gray-400 space-x-2">
                        <span class="h-[1px] w-16 bg-gray-200"></span>
                        <span>账号密码登录</span>
                        <span class="h-[1px] w-16 bg-gray-200"></span>
                    </div>
                    <div class="login-card">
                        <el-form ref="formRef" :rules="rules" :model="form" class="w-[300px]">
                            <el-form-item prop="username">
                                <el-input v-model="form.username" :prefix-icon="User" placeholder="请输入用户名" size="large"
                                    clearable />
                            </el-form-item>
                            <el-form-item prop="password">
                                <el-input v-model="form.password" type="password" autocomplete="off" :prefix-icon="Lock"
                                    placeholder="请输入密码" show-password size="large" clearable />
                            </el-form-item>
                            <el-form-item>

                                <el-button round type="primary" @click="onSubmit" :loading="loading"
                                    class="w-[300px] login-btn mt-4" size="large">
                                    登 录
                                </el-button>

                            </el-form-item>
                        </el-form>
                    </div>

                </div>
            </div>
        </div>

    </div>
</template>

<script setup>
import { reactive, ref, onMounted, onBeforeUnmount } from 'vue'
import { login } from '@/api/admin/user';
import { showMessage } from '@/composables/util'
import { useRouter } from 'vue-router'
import { useStore } from 'vuex'
import { setToken } from '@/composables/auth'
import { User, Lock } from '@element-plus/icons-vue'

const router = useRouter()
const store = useStore()

const form = reactive({
    username: '',
    password: '',
})

const rules = {
    username: [
        {
            required: true,
            message: '用户名不能为空',
            trigger: 'blur'
        }
    ],
    password: [
        {
            required: true,
            message: '密码不能为空',
            trigger: 'blur',
        },
    ]
}


const formRef = ref(null)
const loading = ref(false)

const onSubmit = () => {
    // 登录表单验证
    formRef.value.validate((valid) => {
        if (!valid) {
            console.log('验证不通过')
            return false
        }
        loading.value = true
        login(form.username, form.password)
            .then(res => {
                if (res.success == true) {
                    // 提示成功
                    let message = res.message
                    showMessage('登录成功', 'success')
                    // notification('登录成功')

                    let token = res.data.token
                    // 存储 token
                    setToken(token)

                    // 跳转到后台页面
                    router.push('/admin')
                } else {
                    let message = res.message
                    showMessage(message, 'error')
                }
            }).finally(() => {
                loading.value = false
            })
    })
}

function onKeyUp(e) {
    console.log(e)
    if (e.key == 'Enter') {
        onSubmit()
    }
}

// 添加键盘监听
onMounted(() => {
    console.log('添加键盘监听')
    document.addEventListener('keyup', onKeyUp)
})

// 移除键盘监听
onBeforeUnmount(() => {
    document.removeEventListener('keyup', onKeyUp)
})

</script>

<style>
:deep([type='text']:focus) {
    border-color: transparent !important;
}

.login-container {
    height: 100vh;
    width: 100%;
}

.login-container-left {
    height: 100%;
    background: #001428;
    color: #fff;
}

.login-container-right {
    height: 100%;
}

.login-right-panel {
    background: linear-gradient(135deg, #e0e7ff 0%, #f0e6ff 100%);
}

html.dark .login-right-panel {
    background: linear-gradient(135deg, #1a1a2e 0%, #16213e 100%);
}

.login-card {
    background: rgba(255, 255, 255, 0.7);
    backdrop-filter: blur(16px);
    -webkit-backdrop-filter: blur(16px);
    border: 1px solid rgba(255, 255, 255, 0.35);
    border-radius: 16px;
    padding: 32px;
    box-shadow: 0 8px 32px rgba(0, 0, 0, 0.08);
}

html.dark .login-card {
    background: rgba(30, 30, 50, 0.75);
    border: 1px solid rgba(255, 255, 255, 0.08);
}

.login-image {
    height: 550px;
}

.login-btn {}
</style>