<script setup>
import { computed, onBeforeUnmount, ref, shallowRef, watch } from 'vue'
import { ArrowLeftIcon, ArrowRightIcon, CalendarDaysIcon, EyeIcon, FolderIcon } from '@heroicons/vue/24/outline'
import { useRoute } from 'vue-router'
import PublicLayout from '@/layouts/PublicLayout.vue'
import UserInfoCard from '@/components/UserInfoCard.vue'
import SkeletonArticleDetail from '@/components/SkeletonArticleDetail.vue'
import ArticleBody from './components/ArticleBody.vue'
import ReadingProgress from './components/ReadingProgress.vue'
import TopicLinks from './components/TopicLinks.vue'
import { getArticleDetail } from '@/api/frontend/article'
import { getCategories } from '@/api/frontend/category'
import { getTags } from '@/api/frontend/tag'
import { applyArticleSeo } from '@/composables/seo'

const route = useRoute()
const articleId = computed(() => route.params.id)
const articleElement = ref(null)
const state = shallowRef('loading')
const article = shallowRef(null)
const categories = shallowRef([])
const tags = shallowRef([])
const relatedArticles = computed(() => [
    { label: '上一篇', icon: ArrowLeftIcon, article: article.value?.preArticle },
    { label: '下一篇', icon: ArrowRightIcon, article: article.value?.nextArticle },
].filter(item => item.article?.id))

let loadToken = 0

async function loadArticle() {
    const token = ++loadToken
    const id = articleId.value
    if (typeof id !== 'string' || !/^\d+$/.test(id)) {
        state.value = 'missing'
        return
    }

    state.value = 'loading'
    try {
        const response = await getArticleDetail(id)
        if (token !== loadToken) return
        article.value = response.success ? response.data : null
        state.value = article.value ? 'ready' : 'missing'
        if (article.value) applyArticleSeo(article.value)
    } catch {
        if (token !== loadToken) return
        state.value = 'failed'
    }
}

watch(articleId, loadArticle, { immediate: true })
// 离开详情后，旧请求不能覆盖新页面的标题和 canonical。
onBeforeUnmount(() => { loadToken += 1 })

getCategories().then(response => { if (response.success) categories.value = response.data ?? [] }).catch(() => {})
getTags().then(response => { if (response.success) tags.value = response.data ?? [] }).catch(() => {})
</script>

<template>
    <PublicLayout>
        <ReadingProgress v-if="state === 'ready'" :target="articleElement" />
        <main class="site-width site-page reading-page" :aria-busy="state === 'loading'">
            <nav class="reading-nav" aria-label="面包屑">
                <RouterLink to="/"><ArrowLeftIcon aria-hidden="true" /> 文章列表</RouterLink>
            </nav>

            <SkeletonArticleDetail v-if="state === 'loading'" />
            <div v-else-if="state !== 'ready'" class="wb-panel wb-state" role="status">
                <p>{{ state === 'missing' ? '这篇文章不存在或已删除。' : '文章加载失败，请稍后重试。' }}</p>
                <button v-if="state === 'failed'" type="button" class="wb-action" @click="loadArticle">重新加载</button>
                <RouterLink v-else to="/" class="wb-action">返回首页</RouterLink>
            </div>

            <template v-else>
                <article ref="articleElement" class="reading-article">
                    <header class="reading-heading">
                        <RouterLink
                            v-if="article.categoryId"
                            class="reading-category"
                            :to="{ path: '/category/list', query: { id: article.categoryId, name: article.categoryName } }"
                        ><FolderIcon aria-hidden="true" /> {{ article.categoryName }}</RouterLink>
                        <h1>{{ article.title }}</h1>
                        <div class="reading-meta">
                            <span v-if="article.updateTime"><CalendarDaysIcon aria-hidden="true" /> 更新于 {{ article.updateTime }}</span>
                            <span v-if="article.readNum != null"><EyeIcon aria-hidden="true" /> {{ article.readNum }} 次阅读</span>
                        </div>
                    </header>
                    <ArticleBody :content="article.content || ''" />
                    <div v-if="article.tags?.length" class="reading-tags">
                        <RouterLink v-for="tag in article.tags" :key="tag.id" :to="{ path: '/tag/list', query: { id: tag.id, name: tag.name } }" class="wb-chip"># {{ tag.name }}</RouterLink>
                    </div>
                </article>

                <nav v-if="relatedArticles.length" class="related-articles" aria-label="相邻文章">
                    <RouterLink v-for="item in relatedArticles" :key="item.label" :to="{ name: 'ArticleDetail', params: { id: item.article.id } }" class="adjacent-article">
                        <span><component :is="item.icon" aria-hidden="true" /> {{ item.label }}</span>
                        <strong>{{ item.article.title }}</strong>
                    </RouterLink>
                </nav>
            </template>

            <aside class="reading-extras" aria-label="作者与主题">
                <UserInfoCard />
                <TopicLinks title="分类" path="/category" :entries="categories" />
                <TopicLinks title="标签" path="/tag" :entries="tags" />
            </aside>
        </main>
    </PublicLayout>
</template>

<style scoped>
.reading-page { max-width: 810px; }
.reading-article { min-width: 0; }
.reading-nav { margin-bottom: clamp(17px, 3vw, 28px); color: var(--wb-dim); font-size: 0.88rem; }
.reading-nav a,
.reading-category,
.reading-meta span,
.adjacent-article span { display: inline-flex; align-items: center; gap: 7px; }
.reading-nav a { min-height: 40px; transition: color 150ms ease; }
.reading-nav a:hover,
.reading-category { color: var(--wb-accent); }
.reading-category { min-height: 36px; font-size: 0.88rem; font-weight: 650; }
.reading-category:hover { text-decoration: underline; text-underline-offset: 4px; }

.reading-nav svg,
.reading-meta svg,
.reading-category svg,
.adjacent-article svg { width: 16px; height: 16px; flex: none; }

.reading-heading { padding-bottom: clamp(16px, 3vw, 29px); border-bottom: 1px solid var(--wb-line); }
.reading-heading h1 {
    margin: 7px 0 14px;
    font: 700 clamp(2.05rem, 5.2vw, 3.95rem)/1.13 var(--wb-display);
    letter-spacing: -0.025em;
    overflow-wrap: anywhere;
}

.reading-meta { display: flex; flex-wrap: wrap; gap: 6px 20px; color: var(--wb-dim); font-size: 0.85rem; font-variant-numeric: tabular-nums; }
.reading-tags { display: flex; flex-wrap: wrap; gap: 7px; padding-block: 16px; border-top: 1px solid var(--wb-line); }
.related-articles { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 12px 27px; margin-top: 18px; }
.adjacent-article {
    display: grid;
    min-height: 75px;
    align-content: center;
    gap: 4px;
    padding: 11px 3px;
    border-block: 1px solid var(--wb-line);
    overflow-wrap: anywhere;
    transition: color 150ms ease;
}

.adjacent-article span { color: var(--wb-dim); font-size: 0.8rem; }
.adjacent-article strong { font: 700 clamp(1rem, 1.8vw, 1.32rem)/1.4 var(--wb-display); }
.adjacent-article:hover,
.adjacent-article:focus-visible { color: var(--wb-accent); }
.reading-extras { display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); align-items: start; gap: 14px; margin-top: clamp(24px, 5vw, 56px); }

@media (max-width: 660px) {
    .reading-extras { grid-template-columns: minmax(0, 1fr); }
}

@media (max-width: 480px) {
    .related-articles { grid-template-columns: minmax(0, 1fr); }
}

@media (pointer: coarse) {
    .reading-nav a,
    .reading-category { min-height: 44px; }
}
</style>
