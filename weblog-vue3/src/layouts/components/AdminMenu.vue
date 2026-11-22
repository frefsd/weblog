<script setup>
import { useRoute } from 'vue-router'
import {
    AdjustmentsHorizontalIcon, BellAlertIcon, ChartBarSquareIcon, ClipboardDocumentListIcon,
    ClockIcon, CommandLineIcon, DocumentTextIcon, FolderIcon, TagIcon,
} from '@heroicons/vue/24/outline'

defineProps({
    compact: { type: Boolean, default: false },
})
const emit = defineEmits(['navigate'])
const route = useRoute()

const groups = [
    {
        label: '内容管理',
        items: [
            { label: '仪表盘', icon: ChartBarSquareIcon, path: '/admin' },
            { label: '文章管理', icon: DocumentTextIcon, path: '/admin/article/list' },
            { label: '分类管理', icon: FolderIcon, path: '/admin/category/list' },
            { label: '标签管理', icon: TagIcon, path: '/admin/tag/list' },
            { label: '博客设置', icon: AdjustmentsHorizontalIcon, path: '/admin/blog/setting' },
        ],
    },
    {
        label: '运维监控',
        items: [
            { label: '实时日志', icon: ClipboardDocumentListIcon, path: '/admin/monitor/log' },
            { label: '告警规则', icon: BellAlertIcon, path: '/admin/monitor/rule' },
            { label: '告警记录', icon: ClockIcon, path: '/admin/monitor/alert' },
        ],
    },
]
</script>

<template>
    <nav class="admin-menu" :class="{ 'menu-compact': compact }" aria-label="后台导航">
        <RouterLink class="admin-brand" to="/admin" aria-label="WeBlog 后台首页" title="WeBlog 后台首页" @click="emit('navigate')">
            <span class="admin-brand-icon"><CommandLineIcon aria-hidden="true" /></span>
            <span class="menu-label">
                <strong>WeBlog</strong>
                <small>管理后台</small>
            </span>
        </RouterLink>
        <section v-for="group in groups" :key="group.label" class="menu-group" :aria-label="group.label">
            <h2 class="menu-label">{{ group.label }}</h2>
            <RouterLink
                v-for="{ label, icon, path } in group.items"
                :key="path"
                :to="path"
                class="menu-link"
                :class="{ 'menu-current': path === route.path }"
                :aria-label="label"
                :title="compact ? label : undefined"
                @click="emit('navigate')"
            >
                <component :is="icon" aria-hidden="true" />
                <span class="menu-label">{{ label }}</span>
            </RouterLink>
        </section>
    </nav>
</template>

<style scoped>
.admin-menu { padding: 0 12px 18px; }
.admin-brand { min-height: 66px; display: flex; align-items: center; gap: 10px; padding-inline: 6px; border-bottom: 1px solid var(--wb-line); color: var(--wb-text); }
.admin-brand strong,
.admin-brand small { display: block; }
.admin-brand strong { font: 700 1.15rem/1.1 var(--wb-display); }
.admin-brand small { margin-top: 2px; color: var(--wb-dim); font-size: 0.7rem; }
.admin-brand-icon { display: grid; width: 34px; height: 34px; flex: none; place-items: center; border: 1px solid color-mix(in srgb, var(--wb-accent) 55%, var(--wb-line)); border-radius: 6px; background: var(--wb-accent-soft); color: var(--wb-accent); }
.admin-brand-icon svg { width: 19px; height: 19px; }
.menu-group { padding-top: 17px; }
.menu-group + .menu-group { margin-top: 12px; border-top: 1px solid var(--wb-line); }
.menu-group h2 { padding: 0 10px 8px; color: var(--wb-dim); font-size: 0.76rem; font-weight: 650; }
.menu-link {
    position: relative;
    display: flex;
    min-height: 43px;
    align-items: center;
    gap: 10px;
    padding-inline: 10px;
    border: 1px solid transparent;
    border-radius: 6px;
    color: var(--wb-muted);
    font-size: 0.88rem;
    font-weight: 600;
    white-space: nowrap;
    transition: color 160ms ease, background-color 160ms ease;
}
.menu-link svg { width: 18px; height: 18px; flex: none; }
.menu-link:hover { color: var(--wb-text); background: var(--wb-raised); }
.menu-link.menu-current { color: var(--wb-accent); background: var(--wb-accent-soft); border-color: color-mix(in srgb, var(--wb-accent) 23%, var(--wb-line)); }

@media (min-width: 861px) {
    .menu-compact { padding-inline: 8px; }
    .menu-compact .admin-brand,
    .menu-compact .menu-link { justify-content: center; padding-inline: 0; }
    .menu-compact .menu-label { display: none; }
}
</style>
