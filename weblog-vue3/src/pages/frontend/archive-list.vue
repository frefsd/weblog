<script setup>
import { computed, shallowRef } from 'vue'
import { ArrowUpRightIcon, ChevronDownIcon } from '@heroicons/vue/24/outline'
import PublicLayout from '@/layouts/PublicLayout.vue'
import SkeletonArchive from '@/components/SkeletonArchive.vue'
import { getArchives } from '@/api/frontend/archive'

const archives = shallowRef([])
const state = shallowRef('loading')
const count = computed(() => archives.value.reduce((sum, year) => sum + (year.articleCount || 0), 0))
const years = computed(() => [...archives.value]
    .sort((first, second) => Number(second.year) - Number(first.year))
    .map(year => ({
        ...year,
        months: [...(year.months ?? [])]
            .sort((first, second) => parseInt(second.month, 10) - parseInt(first.month, 10))
            .map(month => ({
                ...month,
                articles: [...(month.articles ?? [])]
                    .sort((first, second) => (second.createTime ?? '').localeCompare(first.createTime ?? '')),
            })),
    })))

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
    <PublicLayout>
        <main class="site-width site-page archives-page" :aria-busy="state === 'loading'">
            <header class="wb-page-heading">
                <h1 class="wb-section-title">文章归档</h1>
                <p v-if="state === 'ready'" class="site-count">{{ archives.length }} 年 · {{ count }} 篇文章</p>
            </header>

            <SkeletonArchive v-if="state === 'loading'" />
            <div v-else-if="state === 'failed'" class="wb-panel wb-state" role="status">
                归档加载失败，请稍后重试。
                <button type="button" class="wb-action" @click="loadArchives">重新加载</button>
            </div>
            <p v-else-if="!years.length" class="wb-panel wb-state">暂无归档文章</p>
            <div v-else class="archive-years">
                <details v-for="(year, index) in years" :key="year.year" class="archive-year" :open="index === 0">
                    <summary>
                        <strong>{{ year.year }}</strong>
                        <span class="site-count">{{ year.articleCount }} 篇</span>
                        <ChevronDownIcon aria-hidden="true" />
                    </summary>
                    <section v-for="month in year.months" :key="month.month" class="archive-month">
                        <header class="archive-month-label">
                            <h2>{{ month.month }}</h2>
                            <span class="site-count">{{ month.articleCount }} 篇</span>
                        </header>
                        <ul class="archive-entries">
                            <li v-for="article in month.articles" :key="article.id">
                                <RouterLink :to="{ name: 'ArticleDetail', params: { id: article.id } }" class="archive-entry">
                                    <time v-if="article.createTime" :datetime="year.year + '-' + article.createTime" class="entry-date">{{ article.createTime }}</time>
                                    <span class="entry-details">
                                        <strong>{{ article.title }}</strong>
                                        <small v-if="article.categoryName">{{ article.categoryName }}</small>
                                    </span>
                                    <ArrowUpRightIcon class="entry-arrow" aria-hidden="true" />
                                </RouterLink>
                            </li>
                        </ul>
                    </section>
                </details>
            </div>
        </main>
    </PublicLayout>
</template>

<style scoped>
.archives-page { max-width: 1020px; }
.archive-year { border-bottom: 1px solid var(--wb-line); }
.archive-year summary {
    display: flex;
    align-items: baseline;
    gap: 13px;
    min-height: 58px;
    padding-block: 10px;
    cursor: pointer;
    list-style: none;
}

.archive-year summary::-webkit-details-marker { display: none; }
.archive-year summary:focus-visible { outline-offset: -1px; }
.archive-year summary strong { color: var(--wb-accent); font: 700 clamp(2.1rem, 4vw, 3.15rem)/1.1 var(--wb-display); }
.archive-year summary svg { align-self: center; width: 19px; height: 19px; margin-inline-start: auto; color: var(--wb-accent); transition: transform 160ms ease; }
.archive-year[open] summary svg { transform: rotate(180deg); }

.archive-month {
    display: grid;
    grid-template-columns: minmax(63px, 0.2fr) minmax(0, 1fr);
    gap: clamp(12px, 3.7vw, 50px);
    padding-block: clamp(13px, 2vw, 22px);
    border-top: 1px solid var(--wb-line);
}

.archive-month-label { padding-top: 8px; }
.archive-month-label h2 { font: 700 clamp(1.15rem, 2vw, 1.55rem)/1.25 var(--wb-display); }
.archive-entries { min-width: 0; }
.archive-entries li + li { border-top: 1px solid var(--wb-line); }
.archive-entry {
    display: grid;
    grid-template-columns: 3.8em minmax(0, 1fr) auto;
    align-items: center;
    gap: clamp(8px, 2vw, 21px);
    min-height: 50px;
    padding: 7px 0;
    transition: color 150ms ease;
}

.archive-entry:hover,
.archive-entry:focus-visible { color: var(--wb-accent); }
.entry-date { color: var(--wb-dim); font: 0.81rem/1.4 var(--wb-mono); }
.entry-details { display: grid; grid-column: 2; min-width: 0; overflow-wrap: anywhere; }
.entry-details strong { font: 700 clamp(1.05rem, 1.6vw, 1.26rem)/1.4 var(--wb-display); }
.entry-details small { color: var(--wb-dim); font-size: 0.78rem; }
.entry-arrow { grid-column: 3; width: 17px; height: 17px; justify-self: end; color: var(--wb-accent); }

@media (max-width: 510px) {
    .archive-month { grid-template-columns: minmax(0, 1fr); gap: 5px; }
    .archive-month-label { display: flex; align-items: baseline; gap: 9px; padding: 0; }
    .archive-entry { grid-template-columns: 3.15em minmax(0, 1fr) auto; }
}
</style>
