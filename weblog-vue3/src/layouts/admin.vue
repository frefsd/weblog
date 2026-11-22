<script setup>
import { computed, shallowRef, watch } from 'vue'
import { useRoute } from 'vue-router'
import { useStore } from 'vuex'
import AdminHeader from '@/layouts/components/AdminHeader.vue'
import AdminMenu from '@/layouts/components/AdminMenu.vue'
import AdminTagList from '@/layouts/components/AdminTagList.vue'
import AdminFooter from '@/layouts/components/AdminFooter.vue'

const route = useRoute()
const store = useStore()
const compact = computed(() => store.state.menuWidth === '64px')
const navigationOpen = shallowRef(false)

watch(() => route.path, () => { navigationOpen.value = false })
</script>

<template>
    <div class="admin-app" :class="{ 'is-compact': compact }" @keydown.esc="navigationOpen = false">
        <aside id="admin-navigation" class="admin-sidebar" :class="{ 'navigation-open': navigationOpen }">
            <AdminMenu :compact="compact" @navigate="navigationOpen = false" />
        </aside>

        <div class="admin-body">
            <div class="admin-chrome">
                <AdminHeader
                    :compact="compact"
                    :navigation-open="navigationOpen"
                    @toggle-compact="store.commit('HANDLE_MENU_WIDTH')"
                    @toggle-navigation="navigationOpen = !navigationOpen"
                />
                <AdminTagList />
            </div>

            <main class="admin-workspace">
                <header v-if="route.path !== '/admin'" class="wb-page-heading">
                    <h1 class="wb-section-title">{{ route.meta.title }}</h1>
                </header>
                <router-view />
            </main>

            <AdminFooter />
        </div>
    </div>
</template>

<style>
.admin-app {
    display: grid;
    min-height: 100vh;
    min-height: 100dvh;
    grid-template-columns: var(--admin-sidebar-width, 222px) minmax(0, 1fr);
}

.admin-app.is-compact { --admin-sidebar-width: 66px; }

.admin-sidebar {
    position: sticky;
    top: 0;
    height: 100vh;
    height: 100dvh;
    align-self: start;
    border-right: 1px solid var(--wb-line);
    background: var(--wb-panel);
    overflow-y: auto;
    overscroll-behavior: contain;
}

.admin-body { min-width: 0; display: flex; flex-direction: column; }

.admin-chrome {
    position: sticky;
    top: 0;
    z-index: 35;
    background: var(--wb-panel);
    border-bottom: 1px solid var(--wb-line);
}

.admin-workspace {
    flex: 1;
    width: min(100%, 1620px);
    min-width: 0;
    margin-inline: auto;
    padding: clamp(16px, 2.7vw, 36px);
}

.admin-workspace .el-card {
    color: var(--wb-text);
    border-color: var(--wb-line);
    box-shadow: none;
}

.admin-workspace .el-card__body {
    min-width: 0;
    overflow-wrap: anywhere;
}

.admin-workspace .el-card__header { font-weight: 650; }
.admin-workspace .el-table {
    --el-table-header-bg-color: var(--wb-raised);
    --el-table-header-text-color: var(--wb-text);
    --el-table-row-hover-bg-color: var(--wb-hover);
    --el-table-border-color: var(--wb-line);
    --el-table-current-row-bg-color: var(--wb-accent-soft);
    --el-table-text-color: var(--wb-muted);
    width: 100%;
    border: 1px solid var(--wb-line);
    border-radius: 7px;
}

.admin-workspace .el-table th.el-table__cell {
    padding-block: 12px;
    font-size: 0.83rem;
    font-weight: 700;
}

.admin-workspace .el-table .el-button.is-link { padding: 4px 0; }
.admin-workspace .el-table .cell { overflow-wrap: anywhere; }
.admin-workspace .el-pagination { max-width: 100%; flex-wrap: wrap; gap: 5px 0; }
.admin-workspace .el-pagination__jump { margin-left: 12px; }
.admin-filter-card { margin-bottom: 16px; }
.admin-workspace .el-form.admin-filters { align-items: end; margin-bottom: -13px; }
.admin-filters .el-form-item__label { color: var(--wb-text); font-weight: 600; }
.admin-filters .filter-input { width: 210px; max-width: 100%; }
.admin-filters .filter-select { width: 154px; max-width: 100%; }
.admin-filters .filter-date { width: 278px; max-width: 100%; }
.filter-actions .el-form-item__content { display: flex; gap: 8px; }
.filter-actions .el-button + .el-button { margin-left: 0; }

.admin-workspace .el-form--inline { display: flex; flex-wrap: wrap; align-items: start; gap: 2px 12px; }
.admin-workspace .el-form--inline .el-form-item { margin-right: 0; }
.admin-workspace .el-form--inline .el-form-item__content { min-width: 0; }

@media (max-width: 860px) {
    .admin-app { display: block; }
    .admin-sidebar {
        position: fixed;
        display: none;
        top: 62px;
        left: 0;
        z-index: 45;
        width: min(260px, calc(100% - 24px));
        height: auto;
        max-height: calc(100dvh - 62px);
        border: 1px solid var(--wb-line);
        border-top: 0;
        border-radius: 0 0 var(--wb-radius) 0;
        box-shadow: var(--wb-shadow);
    }
    .admin-sidebar.navigation-open { display: block; }
}

@media (max-width: 540px) {
    .admin-workspace .el-card__body { padding: 14px !important; }
    .admin-workspace .el-form--inline .el-form-item { flex-wrap: wrap; }
    .admin-workspace .el-date-editor { max-width: 100%; }
}

@media (max-width: 680px) {
    .weblog-range-popper .el-picker-panel.el-date-range-picker {
        width: min(350px, calc(100vw - 24px));
        max-height: min(54dvh, 510px);
        overflow-y: auto;
    }

    .weblog-range-popper .el-date-range-picker .el-picker-panel__sidebar {
        position: static;
        width: 100%;
        display: flex;
        flex-wrap: wrap;
        gap: 2px;
        padding: 5px;
        border-bottom: 1px solid var(--wb-line);
        border-right: 0;
    }

    .weblog-range-popper .el-date-range-picker .el-picker-panel__shortcut { width: auto; }
    .weblog-range-popper .el-date-range-picker .el-picker-panel__body { margin-left: 0; min-width: 0; }
    .weblog-range-popper .el-date-range-picker .el-date-range-picker__content {
        display: block;
        float: none;
        width: 100%;
        padding: 10px 12px;
    }
    .weblog-range-popper .el-date-range-picker .el-date-range-picker__content.is-left {
        border-right: 0;
        border-bottom: 1px solid var(--wb-line);
    }
}
</style>
