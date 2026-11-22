<script setup>
import { shallowRef, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useCookies } from '@vueuse/integrations/useCookies'
import { ChevronDownIcon } from '@heroicons/vue/24/outline'

const route = useRoute()
const router = useRouter()
const cookie = useCookies()
const dashboard = { title: '仪表盘', path: '/admin' }
const storedTabs = cookie.get('tabList')
const tabs = shallowRef([dashboard])

if (Array.isArray(storedTabs)) {
    const savedTabs = storedTabs.flatMap(item => {
        if (typeof item?.path !== 'string' || item.path === dashboard.path) return []
        const resolved = router.resolve(item.path)
        if (resolved.matched[0]?.path !== '/admin') return []
        return [{ title: resolved.meta.title, path: resolved.path }]
    })
    tabs.value = [dashboard, ...new Map(savedTabs.map(tab => [tab.path, tab])).values()]
}

watch(tabs, (value) => cookie.set('tabList', value, { path: '/admin' }))
watch(() => route.path, (path) => {
    if (route.matched[0]?.path !== '/admin') return
    if (!tabs.value.some(tab => tab.path === path)) {
        tabs.value = [...tabs.value, { title: route.meta.title, path }]
    }
}, { immediate: true })

function activate(path) {
    if (path !== route.path) router.push(path)
}

function closeTab(path) {
    if (path === dashboard.path) return
    const index = tabs.value.findIndex(tab => tab.path === path)
    if (path === route.path) {
        activate((tabs.value[index + 1] || tabs.value[index - 1] || dashboard).path)
    }
    tabs.value = tabs.value.filter(tab => tab.path !== path)
}

function closeTabs(command) {
    if (command === 'others') {
        tabs.value = tabs.value.filter(tab => tab.path === dashboard.path || tab.path === route.path)
    } else if (command === 'all') {
        tabs.value = [dashboard]
        activate(dashboard.path)
    }
}
</script>

<template>
    <div class="admin-tabs">
        <el-tabs :model-value="route.path" type="card" @tab-change="activate" @tab-remove="closeTab">
            <el-tab-pane v-for="tab in tabs" :key="tab.path" :label="tab.title" :name="tab.path" :closable="tab.path !== dashboard.path" />
        </el-tabs>
        <el-dropdown trigger="click" @command="closeTabs">
            <button type="button" class="wb-icon-button tabs-actions" aria-label="管理页面标签" title="管理页面标签">
                <ChevronDownIcon aria-hidden="true" />
            </button>
            <template #dropdown>
                <el-dropdown-menu>
                    <el-dropdown-item command="others">关闭其他</el-dropdown-item>
                    <el-dropdown-item command="all">关闭全部</el-dropdown-item>
                </el-dropdown-menu>
            </template>
        </el-dropdown>
    </div>
</template>

<style scoped>
.admin-tabs {
    display: flex;
    min-width: 0;
    align-items: center;
    gap: 10px;
    padding: 6px clamp(12px, 2.4vw, 28px);
    border-top: 1px solid var(--wb-line);
    background: color-mix(in srgb, var(--wb-bg) 67%, var(--wb-panel));
}

.admin-tabs :deep(.el-tabs) { flex: 1; min-width: 0; }
.admin-tabs :deep(.el-tabs__header) { margin: 0; height: auto; border: 0; }
.admin-tabs :deep(.el-tabs__nav) { border: 0; gap: 4px; }
.admin-tabs :deep(.el-tabs__item) {
    height: 33px;
    line-height: 31px;
    padding: 0 12px;
    border: 1px solid transparent;
    border-radius: 5px;
    color: var(--wb-muted);
    font-size: 0.8rem;
}
.admin-tabs :deep(.el-tabs__item.is-active) { border-color: var(--wb-line); background: var(--wb-panel); color: var(--wb-accent); }
.admin-tabs :deep(.el-tabs__item:hover) { color: var(--wb-accent); }
.admin-tabs :deep(.el-tabs__nav-prev),
.admin-tabs :deep(.el-tabs__nav-next) { line-height: 33px; }
.tabs-actions { min-height: 33px; width: 34px; border-color: transparent; }
.tabs-actions svg { width: 14px; }
</style>
