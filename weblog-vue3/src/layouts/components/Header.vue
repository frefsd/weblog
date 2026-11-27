<script setup>
import { computed, nextTick, ref, shallowRef, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useStore } from 'vuex'
import {
    ArrowRightOnRectangleIcon, Bars3Icon, MagnifyingGlassIcon,
    MoonIcon, SunIcon, UserCircleIcon, XMarkIcon,
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
const navigationElement = ref(null)
const menuButton = ref(null)
const brandName = computed(() => store.state.setting?.blogName || 'WeBlog')
const signedIn = computed(() => Boolean(store.state.user?.username))
const themeLabel = computed(() => isDark.value ? '切换到浅色模式' : '切换到深色模式')

const navigation = [
    { to: '/', label: '首页' },
    { to: '/category', label: '分类' },
    { to: '/tag', label: '标签' },
    { to: '/archive', label: '归档' },
    { to: '/ai', label: '小智' },
]

watch(() => route.fullPath, () => {
    menuOpen.value = false
    searchOpen.value = false
})

function isCurrent(path) {
    return route.path === path || (path !== '/' && route.path.startsWith(path + '/'))
}

function closeMenu() {
    if (!menuOpen.value) return
    menuOpen.value = false
    menuButton.value?.focus()
}

async function toggleMenu() {
    if (menuOpen.value) {
        closeMenu()
        return
    }
    menuOpen.value = true
    await nextTick()
    navigationElement.value?.querySelector('a')?.focus()
}

function openSearch() {
    menuOpen.value = false
    searchOpen.value = true
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
    <header class="site-header" @keydown.esc="closeMenu">
        <div class="site-width header-bar">
            <RouterLink to="/" class="site-brand" :title="brandName">
                <span class="brand-name">{{ brandName }}</span>
            </RouterLink>

            <nav id="site-navigation" ref="navigationElement" class="header-nav" :class="{ 'is-open': menuOpen }" aria-label="主导航">
                <RouterLink
                    v-for="{ to, label } in navigation"
                    :key="to"
                    :to="to"
                    class="header-link"
                    :class="{ 'is-current': isCurrent(to) }"
                    @click="closeMenu"
                >
                    {{ label }}
                </RouterLink>
            </nav>

            <div class="header-actions">
                <button type="button" class="header-icon search-button" aria-label="搜索文章" title="搜索文章" @click="openSearch">
                    <MagnifyingGlassIcon aria-hidden="true" />
                </button>
                <button type="button" class="header-icon theme-button" :aria-label="themeLabel" :title="themeLabel" @click="toggleTheme">
                    <SunIcon v-if="isDark" aria-hidden="true" />
                    <MoonIcon v-else aria-hidden="true" />
                </button>
                <RouterLink v-if="!signedIn" to="/login" class="header-icon" aria-label="登录" title="登录">
                    <ArrowRightOnRectangleIcon aria-hidden="true" />
                </RouterLink>
                <el-dropdown v-else trigger="click" popper-class="public-account-menu" @command="accountAction">
                    <button type="button" class="header-icon" aria-label="用户菜单" title="用户菜单">
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
                    ref="menuButton"
                    type="button"
                    class="header-icon menu-toggle"
                    aria-controls="site-navigation"
                    :aria-expanded="menuOpen"
                    :aria-label="menuOpen ? '关闭主导航' : '打开主导航'"
                    @click="toggleMenu"
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
    border-bottom: 1px solid #fff0db25;
    background: linear-gradient(112deg, #0d202c, var(--wb-nav-bg));
    color: var(--wb-nav-text);
}

.header-bar {
    display: grid;
    grid-template-columns: minmax(0, 1fr) auto minmax(0, 1fr);
    align-items: center;
    min-height: calc(var(--wb-header-height) - 1px);
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
    max-width: 100%;
    color: var(--wb-nav-text);
    font: 700 clamp(1.2rem, 2vw, 1.8rem)/1.2 var(--wb-display);
    transition: color 150ms ease;
}

.brand-name {
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
}

.header-nav { gap: clamp(10px, 2vw, 30px); }

.header-link {
    position: relative;
    display: inline-flex;
    align-items: center;
    min-height: calc(var(--wb-header-height) - 1px);
    padding-inline: clamp(8px, 1.2vw, 17px);
    color: var(--wb-nav-text);
    font: 600 clamp(0.98rem, 1.3vw, 1.18rem)/1.2 var(--wb-display);
    white-space: nowrap;
    transition: color 150ms ease, background-color 150ms ease;
}

.site-brand:hover,
.header-link:hover,
.header-link.is-current { color: var(--wb-nav-accent); }

.header-link::after {
    content: "";
    position: absolute;
    inset-inline: 8px;
    bottom: 4px;
    height: 2px;
    background: var(--wb-nav-accent);
    transform: scaleX(0);
    transition: transform 180ms ease;
}

.header-link:hover::after,
.header-link:focus-visible::after,
.header-link.is-current::after { transform: scaleX(1); }

.header-actions { justify-content: flex-end; gap: 4px; }

.header-icon {
    display: grid;
    width: 44px;
    height: 44px;
    flex: none;
    place-items: center;
    border-radius: 5px;
    color: var(--wb-nav-text);
    transition: background-color 150ms ease, color 150ms ease;
}

.header-icon svg { width: 23px; height: 23px; }
.header-icon:hover { background: #fff0db14; color: var(--wb-nav-accent); }
.site-header :where(.site-brand, .header-link, .header-icon):focus-visible { outline-color: var(--wb-nav-accent); }
.header-link:focus-visible { outline-offset: -4px; }
.menu-toggle { display: none; }

@media (max-width: 920px) {
    .header-bar { grid-template-columns: minmax(0, 1fr) auto; gap: 6px; }

    .header-nav {
        position: absolute;
        display: none;
        inset: 100% 0 auto;
        flex-wrap: wrap;
        gap: 4px 8px;
        padding: 10px var(--wb-gutter);
        background: var(--wb-nav-bg);
        border-bottom: 1px solid #fff0db25;
        box-shadow: 0 14px 24px #0003;
    }

    .header-nav.is-open { display: flex; }
    .header-link { min-height: 44px; padding-inline: 14px; border-radius: 6px; }
    .header-link.is-current { background: #fff0db14; }
    .header-link::after { content: none; }
    .menu-toggle { display: grid; }
}

@media (max-width: 530px) {
    .header-actions { gap: 0; }
    .header-icon svg { width: 21px; height: 21px; }
}
</style>
