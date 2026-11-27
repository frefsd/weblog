<script setup>
import { computed, ref, shallowRef } from 'vue'
import { ChevronRightIcon } from '@heroicons/vue/24/outline'
import { getIndexArticles } from '@/api/frontend/index'
import SkeletonCard from '@/components/SkeletonCard.vue'
import ArticleCard from './ArticleCard.vue'

const section = ref(null)
const articles = shallowRef([])
const current = shallowRef(1)
const total = shallowRef(0)
const pageSize = shallowRef(10)
const state = shallowRef('loading')
const leadArticle = computed(() => current.value === 1 ? articles.value[0] : null)
const highlights = computed(() => leadArticle.value ? articles.value.slice(1, 3) : [])
const remainingArticles = computed(() => leadArticle.value ? articles.value.slice(3) : articles.value)

async function loadArticles(page, scroll = false) {
    state.value = 'loading'
    current.value = page
    if (scroll) section.value?.scrollIntoView()

    try {
        const response = await getIndexArticles({ current: page, size: pageSize.value })
        if (!response.success) {
            state.value = 'failed'
            return
        }

        const data = response.data
        articles.value = data.records ?? []
        current.value = data.current ?? page
        total.value = data.total
        pageSize.value = data.size
        state.value = 'ready'
    } catch {
        state.value = 'failed'
    }
}

loadArticles(1)
</script>

<template>
    <section id="latest-articles" ref="section" class="articles-section" tabindex="-1" :aria-busy="state === 'loading'" aria-labelledby="articles-heading">
        <header class="section-head">
            <h2 id="articles-heading" class="wb-section-title">最新文章</h2>
            <RouterLink to="/archive" class="archive-link">
                文章归档 <ChevronRightIcon aria-hidden="true" />
            </RouterLink>
        </header>

        <template v-if="state === 'loading'">
            <div v-if="current === 1" class="headlines headlines--split" aria-label="正在加载文章">
                <SkeletonCard lead />
                <div class="headline-rows">
                    <SkeletonCard v-for="index in 2" :key="index" />
                </div>
            </div>
            <div v-else class="site-article-list article-rows" aria-label="正在加载文章">
                <SkeletonCard v-for="index in 4" :key="index" />
            </div>
        </template>
        <div v-else-if="state === 'failed'" class="wb-panel wb-state" role="status">
            <p>文章加载失败，请稍后重试。</p>
            <button type="button" class="wb-action" @click="loadArticles(current)">重新加载</button>
        </div>
        <p v-else-if="!articles.length" class="wb-panel wb-state">暂无文章</p>
        <template v-else>
            <div v-if="leadArticle" class="headlines" :class="{ 'headlines--split': highlights.length }">
                <ArticleCard :article="leadArticle" heading-tag="h3" lead />
                <div v-if="highlights.length" class="headline-rows">
                    <ArticleCard v-for="article in highlights" :key="article.id" :article="article" heading-tag="h3" />
                </div>
            </div>
            <div v-if="remainingArticles.length" class="site-article-list article-rows" :class="{ 'after-headlines': leadArticle }">
                <ArticleCard v-for="article in remainingArticles" :key="article.id" :article="article" heading-tag="h3" />
            </div>
        </template>

        <el-pagination
            v-if="total > pageSize"
            class="wb-pagination"
            layout="prev, pager, next"
            :total="total"
            :current-page="current"
            :page-size="pageSize"
            :pager-count="5"
            :disabled="state === 'loading'"
            @current-change="loadArticles($event, true)"
        />
    </section>
</template>

<style scoped>
.articles-section { min-width: 0; scroll-margin-top: calc(var(--wb-header-height) + 14px); }
.section-head { display: flex; align-items: center; gap: clamp(10px, 2vw, 26px); margin-bottom: clamp(16px, 2.2vw, 29px); }
.section-head::after {
    content: "";
    order: 1;
    height: 1px;
    flex: 1;
    background: color-mix(in srgb, var(--wb-accent) 61%, var(--wb-line));
}

.archive-link {
    order: 2;
    display: inline-flex;
    align-items: center;
    gap: 6px;
    min-height: 44px;
    color: var(--wb-accent);
    font: 700 clamp(0.9rem, 1.4vw, 1.2rem)/1.2 var(--wb-display);
    white-space: nowrap;
}

.archive-link svg { width: 15px; height: 15px; transition: transform 150ms ease; }
.archive-link:hover,
.archive-link:focus-visible { text-decoration: underline; text-underline-offset: 4px; }
@media (hover: hover) and (pointer: fine) and (prefers-reduced-motion: no-preference) {
    .archive-link:hover svg { transform: translateX(2px); }
}

.headlines {
    --lead-cover-ratio: 3.5;
    display: grid;
    align-items: start;
    max-width: 760px;
}
.headlines--split {
    max-width: none;
    grid-template-columns: minmax(0, 0.96fr) minmax(0, 1fr);
    gap: clamp(18px, 2vw, 32px);
}

.headline-rows {
    display: grid;
    align-content: start;
    min-width: 0;
    border-inline-start: 1px solid var(--wb-line);
    padding-inline-start: clamp(18px, 2vw, 32px);
}

.headline-rows > :first-child { padding-block-start: 0; border-top: 0; }
.headline-rows > :last-child { padding-block-end: 0; }
.article-rows { grid-template-columns: repeat(2, minmax(0, 1fr)); column-gap: clamp(24px, 4vw, 58px); }
.after-headlines { margin-top: clamp(18px, 2.7vw, 34px); }

@media (max-width: 900px) {
    .article-rows { grid-template-columns: minmax(0, 1fr); }
}

@media (max-width: 760px) {
    .headlines--split { grid-template-columns: minmax(0, 1fr); gap: 14px; }
    .headline-rows { border: 0; padding: 0; }
    .headline-rows > :first-child { border-top: 1px solid var(--wb-line); padding-block-start: 13px; }
}

@media (max-width: 640px) {
    .headlines { --lead-cover-ratio: 2.66; }
}
</style>
