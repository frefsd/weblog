<script setup>
import { reactive, ref, shallowRef } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeftIcon, ArrowRightIcon, CommandLineIcon } from '@heroicons/vue/24/outline'
import { User, Lock } from '@element-plus/icons-vue'
import { login } from '@/api/admin/user'
import { showMessage } from '@/composables/util'
import { setToken } from '@/composables/auth'
import cityscape from '@/assets/chongqing-night.webp'

const router = useRouter()
const route = useRoute()
const formRef = ref(null)
const loading = shallowRef(false)
const form = reactive({ username: '', password: '' })
const rules = {
    username: [{ required: true, message: '用户名不能为空', trigger: 'blur' }],
    password: [{ required: true, message: '密码不能为空', trigger: 'blur' }],
}

async function onSubmit() {
    if (loading.value) return
    const valid = await formRef.value.validate().catch(() => false)
    if (!valid || loading.value) return

    loading.value = true
    try {
        const result = await login(form.username, form.password)
        if (!result.success) {
            showMessage(result.message, 'error')
            return
        }

        setToken(result.data.token)
        showMessage('登录成功', 'success')
        const redirect = typeof route.query.redirect === 'string' ? router.resolve(route.query.redirect) : null
        const target = redirect?.matched[0]?.path === '/admin' ? redirect.fullPath : '/admin'
        await router.replace(target)
    } catch {
        // The request interceptor provides the error message.
    } finally {
        loading.value = false
    }
}
</script>

<template>
    <main class="login-page">
        <div class="login-scenery">
            <img :src="cityscape" alt="" width="1920" height="1080" fetchpriority="high" />
            <RouterLink to="/" class="login-brand">
                <span><CommandLineIcon aria-hidden="true" /></span> WeBlog
            </RouterLink>
        </div>

        <section class="login-content" aria-labelledby="login-title">
            <div class="login-form-wrap">
                <RouterLink to="/" class="login-back"><ArrowLeftIcon aria-hidden="true" /> 返回博客</RouterLink>
                <h1 id="login-title">登录后台</h1>
                <el-form ref="formRef" class="login-form" :rules="rules" :model="form" label-position="top" @submit.prevent="onSubmit">
                    <el-form-item label="用户名" prop="username">
                        <el-input v-model="form.username" name="username" autocomplete="username" :prefix-icon="User" placeholder="请输入用户名" size="large" clearable />
                    </el-form-item>
                    <el-form-item label="密码" prop="password">
                        <el-input v-model="form.password" name="password" type="password" autocomplete="current-password" :prefix-icon="Lock" placeholder="请输入密码" show-password size="large" />
                    </el-form-item>
                    <el-button type="primary" native-type="submit" :loading="loading" class="login-submit" size="large">
                        {{ loading ? '正在登录…' : '登录' }}
                        <ArrowRightIcon v-if="!loading" aria-hidden="true" />
                    </el-button>
                </el-form>
            </div>
        </section>
    </main>
</template>

<style scoped>
.login-page {
    display: grid;
    grid-template-columns: minmax(0, 1.2fr) minmax(0, 1fr);
    min-height: 100vh;
    min-height: 100dvh;
}

.login-scenery {
    position: relative;
    isolation: isolate;
    background: #162136;
    min-height: 100%;
}

.login-scenery img,
.login-scenery::after {
    position: absolute;
    width: 100%;
    height: 100%;
    inset: 0;
    z-index: -1;
}

.login-scenery img { object-fit: cover; object-position: 66% center; }
.login-scenery::after { background: linear-gradient(150deg, #121625b8 0%, #1021323b 56%, #211934a6); content: ""; }

.login-brand {
    display: inline-flex;
    align-items: center;
    gap: 10px;
    margin: clamp(20px, 4vw, 50px);
    color: #fff;
    font: 730 clamp(1.25rem, 2vw, 1.75rem)/1.2 var(--wb-display);
}

.login-brand span {
    display: grid;
    width: 42px;
    height: 42px;
    place-items: center;
    color: #ffac98;
    border: 1px solid #ffac9866;
    border-radius: 6px;
    background: #1815278a;
}

.login-brand svg { width: 23px; height: 23px; }
.login-content { display: grid; align-items: center; justify-items: center; padding: clamp(24px, 6vw, 76px); }
.login-form-wrap { width: min(100%, 400px); }
.login-back { display: inline-flex; min-height: 38px; align-items: center; gap: 6px; color: var(--wb-dim); font-size: 0.85rem; }
.login-back:hover { color: var(--wb-accent); }
.login-back svg { width: 16px; height: 16px; }
.login-form-wrap h1 { font: 750 clamp(2rem, 4.2vw, 3.3rem)/1.28 var(--wb-display); margin: 12px 0 29px; }
.login-form :deep(.el-form-item__label) { color: var(--wb-text); font-weight: 600; }
.login-form :deep(.el-input__wrapper) { min-height: 48px; }
.login-submit { width: 100%; min-height: 46px; margin-top: 8px; }
.login-submit svg { width: 17px; height: 17px; margin-left: 9px; }

@media (max-width: 740px) {
    .login-page { grid-template-columns: minmax(0, 1fr); grid-template-rows: 176px 1fr; }
    .login-scenery img { object-position: 60% 54%; }
    .login-content { align-items: start; padding-top: 23px; }
}
</style>
