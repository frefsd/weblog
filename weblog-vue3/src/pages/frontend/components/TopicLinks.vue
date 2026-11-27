<script setup>
import { ArrowRightIcon } from '@heroicons/vue/24/outline'

defineProps({
    title: { type: String, required: true },
    path: { type: String, required: true },
    entries: { type: Array, default: () => [] },
})
</script>

<template>
    <section class="wb-panel topic-panel">
        <div class="topic-head">
            <h2>{{ title }}</h2>
            <RouterLink :to="path" class="topic-all" :aria-label="'查看全部' + title">
                全部 <ArrowRightIcon aria-hidden="true" />
            </RouterLink>
        </div>
        <div v-if="entries.length" class="topics">
            <RouterLink v-for="item in entries.slice(0, 12)" :key="item.id" :to="{ path: path + '/list', query: { id: item.id, name: item.name } }" class="wb-chip">
                {{ item.name }}
            </RouterLink>
        </div>
        <p v-else class="topic-empty">暂无{{ title }}</p>
    </section>
</template>

<style scoped>
.topic-panel { padding: 15px 17px 17px; }
.topic-head { display: flex; justify-content: space-between; align-items: center; gap: 12px; }
.topic-head h2 { font: 700 1.1rem/1.3 var(--wb-display); }
.topic-all { display: inline-flex; min-height: 44px; align-items: center; gap: 6px; color: var(--wb-dim); font-size: 0.82rem; white-space: nowrap; transition: color 150ms ease; }
.topic-all:hover { color: var(--wb-accent); }
.topic-all svg { width: 15px; height: 15px; }
.topics { display: flex; flex-wrap: wrap; gap: 6px; margin-top: 6px; }
.topic-empty { font-size: 0.82rem; color: var(--wb-dim); }
</style>
