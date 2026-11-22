<script setup>
import { computed, shallowRef } from 'vue'
import { useRoute } from 'vue-router'
import { ArrowLeftIcon } from '@heroicons/vue/24/outline'
import ArticleCard from './ArticleCard.vue'
import TopicLinks from './TopicLinks.vue'
import SkeletonCard from '@/components/SkeletonCard.vue'
import UserInfoCard from '@/components/UserInfoCard.vue'
import { getCategories, getCategoryArticles } from '@/api/frontend/category'
import { getTags, getTagArticles } from '@/api/frontend/tag'

const props = defineProps({
    kind: { type: String, required: true },
})
const route = useRoute()
const topicId = route.query.id
const isCategory = computed(() => props.kind === 'category')
const labels = computed(() => isCategory.value
    ? { heading: '分类文章', back: '所有分类', related: '标签', relatedPath: '/tag' }
    : { heading: '标签文章', back: '所有标签', related: '分类', relatedPath: '/category' })
const name = computed(() => typeof route.query.name === 'string' && route.query.name.trim() ? route.query.name : labels.value.heading)
const entries = shallowRef([])
const articles = shallowRef([])
const total = shallowRef(0)
const current = shallowRef(1)
const pageSize = shallowRef(10)
const state = shallowRef('loading')

async function loadPage(page, scroll = false) {
    if (typeof topicId !== 'string' || !/^\d+$/.test(topicId)) {
        state.value = 'missing'
        return
    }
    state.value = 'loading'
    if (scroll) document.getElementById('topic-articles')?.scrollIntoView()

    const request = { current: page, size: pageSize.value }
    try {
        const response = isCategory.value
            ? await getCategoryArticles({ ...request, categoryId: topicId })
            : await getTagArticles({ ...request, tagId: topicId })
        if (!response.success) {
            state.value = 'failed'
            return
        }
        const data = response.data
        articles.value = data.records ?? []
        total.value = data.total
        current.value = data.current
        pageSize.value = data.size
        state.value = 'ready'
    } catch {
        state.value = 'failed'
    }
}

loadPage(1)
const loadRelated = isCategory.value ? getTags : getCategories
loadRelated().then(result => { if (result.success) entries.value = result.data ?? [] }).catch(() => {})
</script>

<template>
    <main class="site-width site-page">
        <header id="topic-articles" class="wb-page-heading topic-heading">
            <div>
                <RouterLink :to="'/' + kind" class="topic-back"><ArrowLeftIcon aria-hidden="true" /> {{ labels.back }}</RouterLink>
                <h1 class="wb-section-title">{{ name }}</h1>
            </div>
            <span v-if="state === 'ready'" class="topic-count">{{ total }} 篇文章</span>
        </header>
        <div class="site-split">
            <section aria-label="文章列表" :aria-busy="state === 'loading'">
                <div v-if="state === 'loading'" class="wb-article-grid">
                    <SkeletonCard v-for="index in 4" :key="index" />
                </div>
                <div v-else-if="state === 'failed'" class="wb-panel wb-state" role="status">
                    文章加载失败，请稍后重试。
                    <button type="button" class="wb-action" @click="loadPage(current)">重新加载</button>
                </div>
                <p v-else-if="state === 'missing'" class="wb-panel wb-state">请选择一个{{ isCategory ? '分类' : '标签' }}。</p>
                <p v-else-if="!articles.length" class="wb-panel wb-state">这个主题暂时没有文章。</p>
                <div v-else class="wb-article-grid">
                    <ArticleCard v-for="article in articles" :key="article.id" :article="article" />
                </div>
                <el-pagination v-if="total > pageSize" class="wb-pagination" layout="prev, pager, next" :total="total" :current-page="current" :page-size="pageSize" :pager-count="5" :disabled="state === 'loading'" @current-change="loadPage($event, true)" />
            </section>

            <aside class="site-rail" aria-label="作者与相关主题">
                <UserInfoCard />
                <TopicLinks :title="labels.related" :path="labels.relatedPath" :entries="entries" />
            </aside>
        </div>
    </main>
</template>

<style scoped>
.topic-heading { scroll-margin-top: calc(var(--wb-header-height) + 20px); }
.topic-count { color: var(--wb-dim); font-size: 0.85rem; }
.topic-back { display: inline-flex; align-items: center; gap: 6px; min-height: 38px; margin-bottom: 8px; color: var(--wb-dim); font-size: 0.85rem; }
.topic-back svg { width: 16px; height: 16px; }
.topic-back:hover { color: var(--wb-accent); }
</style>
