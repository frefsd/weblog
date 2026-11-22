<script setup>
import { computed, reactive, ref, shallowRef } from 'vue'
import { useRouter } from 'vue-router'
import { useStore } from 'vuex'
import { useFullscreen } from '@vueuse/core'
import {
    ArrowPathIcon, ArrowsPointingInIcon, ArrowsPointingOutIcon, Bars3Icon, ChevronDownIcon,
    ChevronDoubleLeftIcon, ChevronDoubleRightIcon, HomeIcon, MoonIcon, SunIcon, UserCircleIcon, XMarkIcon,
} from '@heroicons/vue/24/outline'
import { showModel, showMessage } from '@/composables/util'
import { useTheme } from '@/composables/theme'
import { updateAdminPassword } from '@/api/admin/user'

defineProps({
    compact: { type: Boolean, default: false },
    navigationOpen: { type: Boolean, default: false },
})
const emit = defineEmits(['toggle-compact', 'toggle-navigation'])
const store = useStore()
const router = useRouter()
const { isFullscreen, isSupported: fullscreenSupported, toggle: toggleFullscreen } = useFullscreen()
const { isDark, toggleTheme } = useTheme()
const themeLabel = computed(() => isDark.value ? '切换到浅色模式' : '切换到深色模式')
const fullscreenLabel = computed(() => isFullscreen.value ? '退出全屏' : '全屏')
const username = computed(() => store.state.user?.username || '')
const dialogOpen = shallowRef(false)
const saving = shallowRef(false)
const formRef = ref(null)
const form = reactive({ oldPassword: '', newPassword: '', rePassword: '' })
const rules = {
    oldPassword: [{ required: true, message: '请输入当前密码', trigger: 'blur' }],
    newPassword: [{ required: true, message: '请输入新密码', trigger: 'blur' }],
    rePassword: [{ required: true, message: '请再次输入新密码', trigger: 'blur' }],
}

function refresh() {
    window.location.reload()
}

async function savePassword() {
    if (saving.value) return
    const valid = await formRef.value.validate().catch(() => false)
    if (!valid || saving.value) return
    if (form.newPassword !== form.rePassword) {
        showMessage('两次输入的密码不一致', 'warning')
        return
    }

    saving.value = true
    try {
        const response = await updateAdminPassword({ username: username.value, ...form })
        if (!response.success) {
            showMessage(response.message, 'warning')
            return
        }
        dialogOpen.value = false
        await store.dispatch('logout')
        await router.push('/login')
        showMessage('密码已更新，请重新登录', 'success')
    } catch {
        // The request interceptor provides the error message.
    } finally {
        saving.value = false
    }
}

function resetPasswordForm() {
    Object.assign(form, { oldPassword: '', newPassword: '', rePassword: '' })
    formRef.value?.clearValidate()
}

async function handleCommand(command) {
    if (command === 'updatePassword') {
        resetPasswordForm()
        dialogOpen.value = true
    } else if (command === 'logout') {
        try {
            await showModel('是否确定要退出登录？')
            await store.dispatch('logout')
            await router.push('/login')
            showMessage('退出登录成功', 'success')
        } catch {
            // The user cancelled the confirmation dialog.
        }
    }
}
</script>

<template>
    <header class="admin-header">
        <button
            type="button"
            class="wb-icon-button desktop-toggle"
            :aria-label="compact ? '展开侧栏' : '收起侧栏'"
            :title="compact ? '展开侧栏' : '收起侧栏'"
            @click="emit('toggle-compact')"
        >
            <ChevronDoubleRightIcon v-if="compact" aria-hidden="true" />
            <ChevronDoubleLeftIcon v-else aria-hidden="true" />
        </button>
        <button
            type="button"
            class="wb-icon-button mobile-toggle"
            aria-controls="admin-navigation"
            :aria-expanded="navigationOpen"
            :aria-label="navigationOpen ? '关闭后台导航' : '打开后台导航'"
            @click="emit('toggle-navigation')"
        >
            <XMarkIcon v-if="navigationOpen" aria-hidden="true" />
            <Bars3Icon v-else aria-hidden="true" />
        </button>

        <RouterLink to="/admin" class="workspace-label">工作台</RouterLink>

        <div class="admin-actions">
            <RouterLink to="/" class="wb-action visit-link" aria-label="查看博客" title="查看博客">
                <HomeIcon aria-hidden="true" /> <span>查看博客</span>
            </RouterLink>
            <button type="button" class="wb-icon-button refresh" aria-label="刷新页面" title="刷新页面" @click="refresh">
                <ArrowPathIcon aria-hidden="true" />
            </button>
            <button v-if="fullscreenSupported" type="button" class="wb-icon-button fullscreen" :aria-label="fullscreenLabel" :title="fullscreenLabel" @click="toggleFullscreen">
                <ArrowsPointingInIcon v-if="isFullscreen" aria-hidden="true" />
                <ArrowsPointingOutIcon v-else aria-hidden="true" />
            </button>
            <button type="button" class="wb-icon-button" :aria-label="themeLabel" :title="themeLabel" @click="toggleTheme">
                <SunIcon v-if="isDark" aria-hidden="true" />
                <MoonIcon v-else aria-hidden="true" />
            </button>
            <el-dropdown trigger="click" @command="handleCommand">
                <button type="button" class="wb-action admin-account" aria-label="账号设置" title="账号设置">
                    <UserCircleIcon aria-hidden="true" />
                    <span>{{ username || '账号' }}</span>
                    <ChevronDownIcon class="chevron" aria-hidden="true" />
                </button>
                <template #dropdown>
                    <el-dropdown-menu>
                        <el-dropdown-item command="updatePassword">修改密码</el-dropdown-item>
                        <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
                    </el-dropdown-menu>
                </template>
            </el-dropdown>
        </div>
    </header>

    <el-dialog v-model="dialogOpen" title="修改密码" width="480px" append-to-body @closed="resetPasswordForm">
        <el-form ref="formRef" :rules="rules" :model="form" label-position="top" @submit.prevent="savePassword">
            <el-form-item label="用户名">
                <el-input :model-value="username" disabled autocomplete="username" />
            </el-form-item>
            <el-form-item label="当前密码" prop="oldPassword">
                <el-input v-model="form.oldPassword" type="password" autocomplete="current-password" show-password />
            </el-form-item>
            <el-form-item label="新密码" prop="newPassword">
                <el-input v-model="form.newPassword" type="password" autocomplete="new-password" show-password />
            </el-form-item>
            <el-form-item label="确认密码" prop="rePassword">
                <el-input v-model="form.rePassword" type="password" autocomplete="new-password" show-password />
            </el-form-item>
            <button type="submit" class="sr-only" tabindex="-1">保存密码</button>
        </el-form>
        <template #footer>
            <el-button @click="dialogOpen = false">取消</el-button>
            <el-button type="primary" :loading="saving" @click="savePassword">保存密码</el-button>
        </template>
    </el-dialog>
</template>

<style scoped>
.admin-header {
    display: flex;
    min-height: 62px;
    align-items: center;
    gap: 11px;
    padding-inline: clamp(12px, 2.4vw, 28px);
}

.workspace-label { font: 700 1rem/1.1 var(--wb-display); white-space: nowrap; }
.workspace-label:hover { color: var(--wb-accent); }
.admin-actions { margin-left: auto; min-width: 0; display: flex; align-items: center; gap: 6px; }
.admin-header .wb-action { min-height: 38px; }
.admin-header .wb-icon-button { min-height: 38px; width: 38px; }
.admin-account span { max-width: 13ch; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.admin-account .chevron { width: 11px; }
.mobile-toggle { display: none; }

@media (max-width: 860px) {
    .desktop-toggle { display: none; }
    .mobile-toggle { display: inline-flex; }
}
@media (max-width: 535px) {
    .admin-header { gap: 7px; }
    .admin-actions { gap: 4px; }
    .admin-actions .fullscreen,
    .admin-actions .refresh { display: none; }
    .visit-link span,
    .admin-account span,
    .admin-account .chevron { display: none; }
    .admin-header .wb-action { width: 42px; padding: 0; }
}
@media (pointer: coarse) {
    .admin-header .wb-icon-button,
    .admin-header .wb-action { min-height: 44px; }
}
</style>
