<script setup>
import { computed, shallowRef } from 'vue'
import { ArrowUpRightIcon, FolderIcon, HashtagIcon } from '@heroicons/vue/24/outline'
import UserInfoCard from '@/components/UserInfoCard.vue'
import SkeletonList from '@/components/SkeletonList.vue'
import { getCategories } from '@/api/frontend/category'
import { getTags } from '@/api/frontend/tag'

const props = defineProps({
    kind: { type: String, required: true },
})
const isCategory = computed(() => props.kind === 'category')
const title = computed(() => isCategory.value ? '文章分类' : '文章标签')
const entries = shallowRef([])
const state = shallowRef('loading')

async function load() {
    state.value = 'loading'
    try {
        const response = await (isCategory.value ? getCategories() : getTags())
        entries.value = response.success ? response.data ?? [] : []
        state.value = response.success ? 'ready' : 'failed'
    } catch {
        state.value = 'failed'
    }
}

load()
</script>

<template>
    <main class="site-width site-page">
        <header class="wb-page-heading">
            <h1 class="wb-section-title">{{ title }}</h1>
            <span v-if="state === 'ready'" class="site-count">{{ entries.length }} 个{{ isCategory ? '分类' : '标签' }}</span>
        </header>

        <div class="site-split">
            <SkeletonList v-if="state === 'loading'" />
            <div v-else-if="state === 'failed'" class="wb-panel wb-state" role="status">
                加载失败，请稍后重试。
                <button type="button" class="wb-action" @click="load">重新加载</button>
            </div>
            <p v-else-if="!entries.length" class="wb-panel wb-state">暂无{{ isCategory ? '分类' : '标签' }}</p>
            <ul v-else class="directory-grid">
                <li v-for="item in entries" :key="item.id">
                    <RouterLink :to="{ path: '/' + kind + '/list', query: { id: item.id, name: item.name } }" class="directory-link">
                        <span class="directory-icon">
                            <FolderIcon v-if="isCategory" aria-hidden="true" />
                            <HashtagIcon v-else aria-hidden="true" />
                        </span>
                        <span>{{ item.name }}</span>
                        <ArrowUpRightIcon class="directory-arrow" aria-hidden="true" />
                    </RouterLink>
                </li>
            </ul>
            <aside class="site-rail" aria-label="作者"><UserInfoCard /></aside>
        </div>
    </main>
</template>

<style scoped>
.directory-grid { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); column-gap: clamp(14px, 3vw, 36px); }
.directory-grid li { min-width: 0; border-bottom: 1px solid var(--wb-line); }

.directory-link {
    display: grid;
    grid-template-columns: 35px minmax(0, 1fr) 17px;
    height: 100%;
    min-height: 59px;
    align-items: center;
    gap: 11px;
    padding: 8px 4px;
    color: var(--wb-text);
    font: 700 clamp(1rem, 1.8vw, 1.22rem)/1.35 var(--wb-display);
    overflow-wrap: anywhere;
    transition: color 150ms ease;
}

.directory-link:hover,
.directory-link:focus-visible { color: var(--wb-accent); }
.directory-icon { display: grid; place-items: center; width: 35px; height: 35px; border-radius: 5px; background: var(--wb-accent-soft); color: var(--wb-accent); }
.directory-icon svg { width: 17px; height: 17px; }
.directory-arrow { width: 16px; height: 16px; color: var(--wb-accent); }

@media (max-width: 510px) {
    .directory-grid { grid-template-columns: minmax(0, 1fr); }
}
</style>
