<template>
  <div class="login-page">
    <ParticleBackground />

    <div class="login-wrapper">
      <div class="login-card">
        <!-- 标题区域 -->
        <div class="card-header">
          <div class="logo-icon">
            <span class="logo-dot"></span>
            <span class="logo-dot"></span>
            <span class="logo-dot"></span>
          </div>
          <h1 class="card-title">
            <span v-for="(char, i) in '欢迎回来'" :key="i" class="char-bounce" :style="{ animationDelay: i * 0.12 + 's' }">{{ char }}</span>
          </h1>
        </div>

        <!-- 表单 -->
        <el-form ref="formRef" :rules="rules" :model="form" class="login-form">
          <el-form-item prop="username">
            <el-input
              v-model="form.username"
              :prefix-icon="User"
              placeholder="用户名"
              size="large"
              clearable
            />
          </el-form-item>
          <el-form-item prop="password">
            <el-input
              v-model="form.password"
              type="password"
              autocomplete="off"
              :prefix-icon="Lock"
              placeholder="密码"
              show-password
              size="large"
              clearable
            />
          </el-form-item>
          <el-form-item>
            <el-button round type="primary" @click="onSubmit" :loading="loading" class="login-btn" size="large">
              {{ loading ? '验证中...' : '登 录' }}
            </el-button>
          </el-form-item>
        </el-form>

        <!-- 底部版本信息 -->
        <div class="card-footer">
          <span>v1.0.0</span>
          <span class="footer-sep">|</span>
          <span>Secure Connection</span>
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
import ParticleBackground from '@/components/ParticleBackground.vue'

const router = useRouter()
const store = useStore()

const form = reactive({
  username: '',
  password: '',
})

const rules = {
  username: [{ required: true, message: '用户名不能为空', trigger: 'blur' }],
  password: [{ required: true, message: '密码不能为空', trigger: 'blur' }],
}

const formRef = ref(null)
const loading = ref(false)

const onSubmit = () => {
  formRef.value.validate((valid) => {
    if (!valid) return false
    loading.value = true
    login(form.username, form.password)
      .then(res => {
        if (res.success == true) {
          showMessage('登录成功', 'success')
          setToken(res.data.token)
          router.push('/admin')
        } else {
          showMessage(res.message, 'error')
        }
      }).finally(() => {
        loading.value = false
      })
  })
}

function onKeyUp(e) {
  if (e.key == 'Enter') onSubmit()
}

onMounted(() => document.addEventListener('keyup', onKeyUp))
onBeforeUnmount(() => document.removeEventListener('keyup', onKeyUp))
</script>

<style scoped>
.login-page {
  position: relative;
  width: 100%;
  height: 100vh;
  background: #0a0a0f;
  overflow: hidden;
}

.login-wrapper {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  padding: 24px;
}

/* ===== 卡片 ===== */
.login-card {
  width: 100%;
  max-width: 400px;
  padding: 48px 40px 32px;
  background: rgba(12, 12, 24, 0.75);
  backdrop-filter: blur(24px);
  -webkit-backdrop-filter: blur(24px);
  border: 1px solid rgba(0, 230, 200, 0.15);
  border-radius: 20px;
  box-shadow:
    0 0 40px rgba(0, 230, 200, 0.06),
    inset 0 0 40px rgba(0, 230, 200, 0.02);
  animation: cardIn 0.6s ease-out;
}

@keyframes cardIn {
  from {
    opacity: 0;
    transform: translateY(30px) scale(0.96);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

/* ===== 标题区域 ===== */
.card-header {
  text-align: center;
  margin-bottom: 36px;
}

.logo-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  margin-bottom: 20px;
}

.logo-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #00e6c8;
  box-shadow: 0 0 10px rgba(0, 230, 200, 0.6);
  animation: dotPulse 2s ease-in-out infinite;
}

.logo-dot:nth-child(2) { animation-delay: 0.3s; }
.logo-dot:nth-child(3) { animation-delay: 0.6s; }

@keyframes dotPulse {
  0%, 100% { opacity: 0.4; transform: scale(0.8); }
  50% { opacity: 1; transform: scale(1.2); }
}

.card-title {
  font-family: ui-monospace, SFMono-Regular, 'SF Mono', Menlo, Consolas, monospace;
  font-size: 24px;
  font-weight: 700;
  color: #e0e0e0;
  letter-spacing: 6px;
  margin: 0;
  text-shadow: 0 0 20px rgba(0, 230, 200, 0.3);
}

.char-bounce {
  display: inline-block;
  opacity: 0;
  animation: charIn 0.4s ease-out forwards;
}

@keyframes charIn {
  0% {
    opacity: 0;
    transform: translateY(12px) scale(0.8);
  }
  60% {
    opacity: 1;
    transform: translateY(-4px) scale(1.1);
  }
  100% {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

/* ===== 表单 ===== */
.login-form {
  width: 100%;
}

/* Element Plus 输入框覆盖 */
.login-form :deep(.el-input__wrapper) {
  background: rgba(255, 255, 255, 0.04) !important;
  border: 1px solid rgba(0, 230, 200, 0.12) !important;
  border-radius: 10px;
  box-shadow: none !important;
  padding: 4px 12px;
  transition: all 0.3s ease;
}

.login-form :deep(.el-input__wrapper:hover) {
  border-color: rgba(0, 230, 200, 0.3) !important;
}

.login-form :deep(.el-input__wrapper.is-focus) {
  border-color: #00e6c8 !important;
  box-shadow: 0 0 0 2px rgba(0, 230, 200, 0.08), 0 0 20px rgba(0, 230, 200, 0.04) !important;
}

.login-form :deep(.el-input__inner) {
  color: #e0e0e0 !important;
  font-size: 14px;
  height: 44px;
}

.login-form :deep(.el-input__inner::placeholder) {
  color: rgba(255, 255, 255, 0.25) !important;
}

.login-form :deep(.el-input__prefix) {
  color: rgba(0, 230, 200, 0.4) !important;
}

.login-form :deep(.el-input__prefix .icon) {
  font-size: 16px;
}

.login-form :deep(.el-input__clear) {
  color: rgba(255, 255, 255, 0.3) !important;
}

.login-form :deep(.el-input__suffix) {
  color: rgba(0, 230, 200, 0.4) !important;
}

/* 表单项间距 */
.login-form :deep(.el-form-item) {
  margin-bottom: 24px;
}

/* 按钮 */
.login-btn {
  width: 100%;
  height: 46px;
  font-size: 15px;
  font-weight: 600;
  letter-spacing: 4px;
  background: linear-gradient(135deg, #00d4aa, #00b4d8) !important;
  border: none !important;
  border-radius: 10px !important;
  color: #0a0a0f !important;
  transition: all 0.3s ease !important;
  box-shadow: 0 0 20px rgba(0, 212, 170, 0.2) !important;
}

.login-btn:hover {
  box-shadow: 0 0 40px rgba(0, 212, 170, 0.4) !important;
  transform: translateY(-1px);
}

.login-btn:active {
  transform: translateY(0);
}

.login-btn.is-loading {
  opacity: 0.85;
}

/* ===== 底部 ===== */
.card-footer {
  margin-top: 32px;
  text-align: center;
  font-size: 11px;
  color: rgba(255, 255, 255, 0.15);
  font-family: ui-monospace, SFMono-Regular, 'SF Mono', Menlo, Consolas, monospace;
  letter-spacing: 1px;
}

.footer-sep {
  margin: 0 8px;
}
</style>
