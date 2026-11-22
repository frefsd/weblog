<script setup>
import { computed, shallowRef, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useStore } from 'vuex'
import {
    ArrowRightOnRectangleIcon, Bars3Icon, CommandLineIcon, MagnifyingGlassIcon,
    MoonIcon, SparklesIcon, SunIcon, UserCircleIcon, XMarkIcon,
} from '@heroicons/vue/24/outline'
import SiteSearch from './SiteSearch.vue'
import { showMessage, showModel } from '@/composables/util'
import { useTheme } from '@/composables/theme'

const route = useRoute()
const router = useRouter()
const store = useStore()
const { isDark, toggleTheme } = useTheme()
const searchOpen = shallowRef(false)
const menuOpen = shallowRef(false)
const brandName = computed(() => store.state.setting?.blogName || 'WeBlog')
const signedIn = computed(() => Boolean(store.state.user?.username))
const themeLabel = computed(() => isDark.value ? '切换到浅色模式' : '切换到深色模式')

const navigation = [
    { to: '/', label: '首页' },
    { to: '/category', label: '分类' },
    { to: '/tag', label: '标签' },
    { to: '/archive', label: '归档' },
    { to: '/ai', label: '小智', icon: SparklesIcon },
]

watch(() => route.fullPath, () => {
    menuOpen.value = false
    searchOpen.value = false
})

function isCurrent(path) {
    return route.path === path || (path !== '/' && route.path.startsWith(path + '/'))
}

async function accountAction(command) {
    if (command === 'admin') {
        await router.push('/admin')
        return
    }

    if (command === 'logout') {
        try {
            await showModel('是否确定要退出登录？')
            await store.dispatch('logout')
            showMessage('退出登录成功', 'success')
        } catch {
            // The user cancelled the confirmation dialog.
        }
    }
}
</script>

<template>
    <header class="site-header" @keydown.esc="menuOpen = false">
        <div class="site-width header-bar">
            <RouterLink to="/" class="site-brand" :title="brandName">
                <span class="brand-mark"><CommandLineIcon aria-hidden="true" /></span>
                <span class="brand-name">{{ brandName }}</span>
            </RouterLink>

            <nav id="site-navigation" class="header-nav" :class="{ 'is-open': menuOpen }" aria-label="主导航">
                <RouterLink
                    v-for="{ to, label, icon } in navigation"
                    :key="to"
                    :to="to"
                    class="header-link"
                    :class="{ 'is-current': isCurrent(to) }"
                    @click="menuOpen = false"
                >
                    <component :is="icon" v-if="icon" aria-hidden="true" />
                    {{ label }}
                </RouterLink>
            </nav>

            <div class="header-actions">
                <button type="button" class="wb-action search-button" aria-label="搜索文章" @click="searchOpen = true">
                    <MagnifyingGlassIcon aria-hidden="true" />
                    <span>搜索</span>
                </button>
                <button type="button" class="wb-icon-button theme-button" :aria-label="themeLabel" :title="themeLabel" @click="toggleTheme">
                    <SunIcon v-if="isDark" aria-hidden="true" />
                    <MoonIcon v-else aria-hidden="true" />
                </button>
                <RouterLink v-if="!signedIn" to="/login" class="wb-action account-button" aria-label="登录">
                    <ArrowRightOnRectangleIcon aria-hidden="true" />
                    <span class="account-label">登录</span>
                </RouterLink>
                <el-dropdown v-else trigger="click" @command="accountAction">
                    <button type="button" class="wb-icon-button" aria-label="用户菜单" title="用户菜单">
                        <UserCircleIcon aria-hidden="true" />
                    </button>
                    <template #dropdown>
                        <el-dropdown-menu>
                            <el-dropdown-item command="admin">进入后台</el-dropdown-item>
                            <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
                        </el-dropdown-menu>
                    </template>
                </el-dropdown>
                <button
                    type="button"
                    class="wb-icon-button menu-toggle"
                    aria-controls="site-navigation"
                    :aria-expanded="menuOpen"
                    :aria-label="menuOpen ? '关闭主导航' : '打开主导航'"
                    @click="menuOpen = !menuOpen"
                >
                    <XMarkIcon v-if="menuOpen" aria-hidden="true" />
                    <Bars3Icon v-else aria-hidden="true" />
                </button>
            </div>
        </div>
        <SiteSearch v-model="searchOpen" />
    </header>
</template>

<style scoped>
.site-header {
    position: sticky;
    top: 0;
    z-index: 50;
    border-bottom: 1px solid var(--wb-line);
    background: color-mix(in srgb, var(--wb-bg) 93%, transparent);
    backdrop-filter: blur(16px);
}

.header-bar {
    display: grid;
    grid-template-columns: minmax(0, 1fr) auto minmax(0, 1fr);
    align-items: center;
    min-height: var(--wb-header-height);
    gap: 24px;
}

.site-brand,
.header-actions,
.header-nav {
    display: flex;
    align-items: center;
    min-width: 0;
}

.site-brand {
    justify-self: start;
    gap: 10px;
    max-width: 100%;
    color: var(--wb-text);
    font: 700 1.15rem/1.2 var(--wb-display);
}

.brand-mark {
    display: grid;
    width: 35px;
    height: 35px;
    flex: none;
    place-items: center;
    border: 1px solid color-mix(in srgb, var(--wb-accent) 65%, var(--wb-line));
    border-radius: 6px;
    background: var(--wb-accent-soft);
    color: var(--wb-accent);
}

.brand-mark svg { width: 20px; height: 20px; }

.brand-name {
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
}

.header-nav { gap: 6px; }

.header-link {
    position: relative;
    display: inline-flex;
    align-items: center;
    gap: 4px;
    min-height: var(--wb-header-height);
    padding-inline: clamp(10px, 1.4vw, 18px);
    color: var(--wb-muted);
    font-size: 0.875rem;
    font-weight: 600;
    white-space: nowrap;
}

.header-link svg { width: 16px; height: 16px; }
.header-link:hover { color: var(--wb-text); }
.header-link.is-current { color: var(--wb-accent); }

.header-link.is-current::after {
    position: absolute;
    right: 14px;
    bottom: -1px;
    left: 14px;
    height: 2px;
    background: var(--wb-accent);
    box-shadow: 0 0 14px var(--wb-accent-soft);
    content: "";
}

.header-actions {
    justify-content: flex-end;
    gap: 7px;
}

.account-button { flex: none; }
.menu-toggle { display: none; }

@media (max-width: 920px) {
    .header-bar { grid-template-columns: minmax(0, 1fr) auto; gap: 8px; }

    .header-nav {
        display: none;
        grid-column: 1 / -1;
        grid-row: 2;
        flex-wrap: wrap;
        gap: 6px;
        padding-bottom: 12px;
    }

    .header-nav.is-open { display: flex; }
    .header-link { min-height: 44px; padding-inline: 14px; border-radius: 6px; }
    .header-link.is-current { background: var(--wb-accent-soft); }
    .header-link.is-current::after { content: none; }
    .menu-toggle { display: inline-flex; }
}

@media (max-width: 530px) {
    .header-actions { gap: 4px; }
    .header-actions .wb-action { width: 42px; padding: 0; }
    .search-button span,
    .account-label { display: none; }
    .site-brand { gap: 7px; font-size: 1rem; }
    .brand-mark { width: 30px; height: 30px; }
}

@media (max-width: 360px) {
    .header-actions .wb-action,
    .header-actions .wb-icon-button { width: 36px; }
}
</style>
