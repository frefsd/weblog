<script setup>
import { computed, shallowRef } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { MagnifyingGlassIcon } from '@heroicons/vue/24/outline'
import Header from '@/layouts/components/Header.vue'
import Footer from '@/layouts/components/Footer.vue'
import ArticleCard from './components/ArticleCard.vue'
import SkeletonCard from '@/components/SkeletonCard.vue'
import { searchArticles } from '@/api/frontend/article'

const route = useRoute()
const router = useRouter()
const query = shallowRef(typeof route.query.q === 'string' ? route.query.q : '')
const keyword = computed(() => typeof route.query.q === 'string' ? route.query.q.trim() : '')
const state = shallowRef('initial')
const articles = shallowRef([])
const total = shallowRef(0)
const current = shallowRef(1)
const size = 10

async function loadPage(page, scroll = false) {
    if (!keyword.value) return
    state.value = 'loading'
    if (scroll) document.getElementById('search-results-heading')?.scrollIntoView()

    try {
        const response = await searchArticles(keyword.value, page, size)
        if (!response.success) {
            state.value = 'failed'
            return
        }
        articles.value = response.data.records ?? []
        total.value = response.data.total ?? 0
        current.value = response.data.current ?? page
        state.value = 'ready'
    } catch {
        state.value = 'failed'
    }
}

function submitSearch() {
    const value = query.value.trim()
    if (!value) return
    if (value === keyword.value) loadPage(1)
    else router.push({ path: '/search', query: { q: value } })
}

loadPage(1)
</script>

<template>
    <Header />
    <main class="site-width site-page">
        <header id="search-results-heading" class="wb-page-heading">
            <h1 class="wb-section-title">搜索文章</h1>
            <form class="results-search" role="search" @submit.prevent="submitSearch">
                <el-input v-model="query" :prefix-icon="MagnifyingGlassIcon" aria-label="搜索关键词" placeholder="输入标题或摘要关键词" size="large" maxlength="120" clearable />
                <el-button type="primary" native-type="submit" size="large" :disabled="!query.trim()">搜索</el-button>
            </form>
        </header>

        <p v-if="state === 'ready'" class="result-count" role="status">“{{ keyword }}” 共 {{ total }} 条结果</p>
        <div v-if="state === 'loading'" class="wb-article-grid search-grid" aria-label="正在加载搜索结果">
            <SkeletonCard v-for="index in 4" :key="index" />
        </div>
        <div v-else-if="state === 'failed'" class="wb-panel wb-state" role="status">
            文章搜索失败，请稍后重试。
            <button type="button" class="wb-action" @click="loadPage(current)">重试</button>
        </div>
        <p v-else-if="state === 'initial'" class="wb-panel wb-state">输入关键词，搜索博客文章。</p>
        <p v-else-if="!articles.length" class="wb-panel wb-state">没有找到匹配的文章。</p>
        <div v-else class="wb-article-grid search-grid">
            <ArticleCard v-for="article in articles" :key="article.id" :article="article" :keyword="keyword" />
        </div>

        <el-pagination v-if="total > size" class="wb-pagination" layout="prev, pager, next" :total="total" :current-page="current" :page-size="size" :pager-count="5" :disabled="state === 'loading'" @current-change="loadPage($event, true)" />
    </main>
    <Footer />
</template>

<style scoped>
#search-results-heading { scroll-margin-top: calc(var(--wb-header-height) + 20px); }
.results-search { display: grid; grid-template-columns: minmax(0, 1fr) auto; gap: 8px; width: min(100%, 450px); }
.result-count { margin-bottom: 12px; color: var(--wb-muted); font-size: 0.88rem; overflow-wrap: anywhere; }

@media (min-width: 1000px) { .search-grid { grid-template-columns: repeat(3, minmax(0, 1fr)); } }
</style>
