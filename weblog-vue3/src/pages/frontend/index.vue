<script setup>
import { computed, shallowRef } from 'vue'
import { useStore } from 'vuex'
import { ArrowRightIcon } from '@heroicons/vue/24/outline'
import Header from '@/layouts/components/Header.vue'
import Footer from '@/layouts/components/Footer.vue'
import UserInfoCard from '@/components/UserInfoCard.vue'
import SkeletonCard from '@/components/SkeletonCard.vue'
import GameWidget from './components/GameWidget.vue'
import BlogHero from './components/BlogHero.vue'
import ArticleCard from './components/ArticleCard.vue'
import TopicLinks from './components/TopicLinks.vue'
import { getIndexArticles } from '@/api/frontend/index'
import { getCategories } from '@/api/frontend/category'
import { getTags } from '@/api/frontend/tag'

const store = useStore()
const articles = shallowRef([])
const current = shallowRef(1)
const total = shallowRef(0)
const pageSize = shallowRef(10)
const loading = shallowRef(true)
const failed = shallowRef(false)
const categories = shallowRef([])
const tags = shallowRef([])
const introduction = computed(() => store.state.setting?.introduction || '')
const topics = computed(() => [
    { title: '分类', path: '/category', entries: categories.value },
    { title: '标签', path: '/tag', entries: tags.value },
])

async function loadArticles(page, scroll = false) {
    loading.value = true
    failed.value = false
    if (scroll) document.getElementById('latest-articles')?.scrollIntoView()

    try {
        const response = await getIndexArticles({ current: page, size: pageSize.value })
        if (!response.success) {
            failed.value = true
            return
        }
        articles.value = response.data.records ?? []
        current.value = response.data.current
        total.value = response.data.total
        pageSize.value = response.data.size
    } catch {
        failed.value = true
    } finally {
        loading.value = false
    }
}

loadArticles(1)
getCategories().then(result => { if (result.success) categories.value = result.data ?? [] }).catch(() => {})
getTags().then(result => { if (result.success) tags.value = result.data ?? [] }).catch(() => {})
</script>

<template>
    <Header />
    <main class="site-width site-page home-page">
        <BlogHero :introduction="introduction" />

        <div class="site-split">
            <section id="latest-articles" class="articles-section" tabindex="-1" :aria-busy="loading" aria-labelledby="articles-heading">
                <header class="section-head">
                    <h2 id="articles-heading" class="wb-section-title">文章列表</h2>
                    <RouterLink to="/archive" class="all-articles">
                        文章归档 <ArrowRightIcon aria-hidden="true" />
                    </RouterLink>
                </header>
                <div v-if="loading" class="wb-article-grid" aria-label="正在加载文章">
                    <SkeletonCard v-for="index in 4" :key="index" />
                </div>
                <div v-else-if="failed" class="wb-panel wb-state" role="status">
                    <p>文章加载失败，请稍后重试。</p>
                    <button type="button" class="wb-action" @click="loadArticles(current)">重新加载</button>
                </div>
                <div v-else-if="!articles.length" class="wb-panel wb-state">暂无文章</div>
                <div v-else class="wb-article-grid">
                    <ArticleCard v-for="article in articles" :key="article.id" :article="article" />
                </div>
                <el-pagination
                    v-if="total > pageSize"
                    class="wb-pagination"
                    layout="prev, pager, next"
                    :total="total"
                    :current-page="current"
                    :page-size="pageSize"
                    :pager-count="5"
                    :disabled="loading"
                    @current-change="loadArticles($event, true)"
                />
            </section>

            <aside class="site-rail" aria-label="作者与主题">
                <UserInfoCard />
                <TopicLinks v-for="topic in topics" :key="topic.path" v-bind="topic" />
                <GameWidget />
            </aside>
        </div>
    </main>
    <Footer />
</template>

<style scoped>
.home-page {
    display: grid;
    gap: clamp(26px, 3.6vw, 44px);
    padding-top: clamp(16px, 2.7vw, 38px);
}

.articles-section { min-width: 0; scroll-margin-top: calc(var(--wb-header-height) + 18px); }
.section-head {
    display: flex;
    justify-content: space-between;
    align-items: center;
    gap: 12px;
}
.section-head { margin-bottom: 16px; }
.all-articles { display: inline-flex; align-items: center; gap: 6px; color: var(--wb-dim); font-size: 0.83rem; font-weight: 600; white-space: nowrap; min-height: 36px; }
.all-articles:hover { color: var(--wb-accent); }
.all-articles svg { width: 15px; height: 15px; }
</style>
