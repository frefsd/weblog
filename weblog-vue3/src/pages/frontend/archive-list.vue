<script setup>
import { computed, shallowRef } from 'vue'
import { ArrowUpRightIcon, ChevronDownIcon, DocumentTextIcon } from '@heroicons/vue/24/outline'
import Header from '@/layouts/components/Header.vue'
import Footer from '@/layouts/components/Footer.vue'
import SkeletonArchive from '@/components/SkeletonArchive.vue'
import { getArchives } from '@/api/frontend/archive'

const archives = shallowRef([])
const state = shallowRef('loading')
const count = computed(() => archives.value.reduce((sum, year) => sum + (year.articleCount || 0), 0))

async function loadArchives() {
    state.value = 'loading'
    try {
        const response = await getArchives()
        archives.value = response.success ? response.data ?? [] : []
        state.value = response.success ? 'ready' : 'failed'
    } catch {
        state.value = 'failed'
    }
}
loadArchives()
</script>

<template>
    <Header />
    <main class="site-width site-page archives-page">
        <header class="wb-page-heading">
            <h1 class="wb-section-title">文章归档</h1>
            <p v-if="state === 'ready'" class="archive-total">{{ archives.length }} 年 · {{ count }} 篇文章</p>
        </header>
        <SkeletonArchive v-if="state === 'loading'" />
        <div v-else-if="state === 'failed'" class="wb-panel wb-state" role="status">
            归档加载失败，请稍后重试。
            <button type="button" class="wb-action" @click="loadArchives">重新加载</button>
        </div>
        <p v-else-if="!archives.length" class="wb-panel wb-state">暂无归档文章</p>
        <div v-else class="archive-years">
            <details v-for="(year, index) in archives" :key="year.year" class="wb-panel archive-year" :open="index === 0">
                <summary>
                    <strong>{{ year.year }}</strong>
                    <span>{{ year.articleCount }} 篇</span>
                    <ChevronDownIcon aria-hidden="true" />
                </summary>
                <section v-for="month in year.months" :key="month.month" class="archive-month">
                    <h2>{{ month.month }} <span>{{ month.articleCount }} 篇</span></h2>
                    <ul>
                        <li v-for="article in month.articles" :key="article.id">
                            <RouterLink :to="{ path: '/article/detail', query: { articleId: article.id } }" class="archive-entry">
                                <span class="entry-cover">
                                    <img v-if="article.titleImage" :src="article.titleImage" alt="" loading="lazy" />
                                    <DocumentTextIcon v-else aria-hidden="true" />
                                </span>
                                <span class="entry-details">
                                    <strong>{{ article.title }}</strong>
                                    <small>{{ [article.createTime, article.categoryName].filter(Boolean).join(' · ') }}</small>
                                </span>
                                <ArrowUpRightIcon class="entry-arrow" aria-hidden="true" />
                            </RouterLink>
                        </li>
                    </ul>
                </section>
            </details>
        </div>
    </main>
    <Footer />
</template>

<style scoped>
.archives-page { max-width: 990px; }
.archive-total,
.archive-year summary span,
.archive-month h2 span { color: var(--wb-dim); font-size: 0.83rem; }
.archive-years { display: grid; gap: 12px; }
.archive-year { overflow: clip; }
.archive-year summary {
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 14px 17px;
    min-height: 56px;
    background: linear-gradient(90deg, var(--wb-accent-soft), transparent 56%);
    cursor: pointer;
    list-style: none;
}
.archive-year summary::-webkit-details-marker { display: none; }
.archive-year summary:focus-visible { outline-offset: -3px; }
.archive-year summary strong { color: var(--wb-accent); font: 700 1.55rem/1 var(--wb-display); }
.archive-year summary svg { width: 17px; height: 17px; margin-left: auto; color: var(--wb-dim); transition: transform 160ms ease; }
.archive-year[open] summary svg { transform: rotate(180deg); }
.archive-month { padding: 12px 12px 16px; border-top: 1px solid var(--wb-line); }
.archive-month h2 { display: flex; align-items: center; gap: 8px; padding: 2px 5px 7px; color: var(--wb-cyan); font-weight: 720; }
.archive-entry { display: flex; align-items: center; gap: 12px; min-height: 58px; padding: 7px 5px; border-radius: 6px; }
.archive-entry:hover { background: var(--wb-raised); }
.entry-cover { display: grid; place-items: center; width: 65px; height: 40px; flex: none; overflow: hidden; border: 1px solid var(--wb-line); border-radius: 5px; background: var(--wb-raised); color: var(--wb-cyan); }
.entry-cover img { width: 100%; height: 100%; object-fit: cover; }
.entry-cover svg,
.entry-arrow { width: 17px; height: 17px; flex: none; }
.entry-details { min-width: 0; display: grid; overflow-wrap: anywhere; }
.entry-details strong { font-size: 0.92rem; }
.entry-details small,
.entry-arrow { color: var(--wb-dim); font-size: 0.78rem; }
.entry-arrow { margin-left: auto; }
</style>
