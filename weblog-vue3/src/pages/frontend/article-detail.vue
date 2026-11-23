<script setup>
import { computed, shallowRef, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ArrowLeftIcon, ArrowRightIcon, CalendarDaysIcon, EyeIcon, FolderIcon } from '@heroicons/vue/24/outline'
import MarkdownIt from 'markdown-it'
import hljs from 'highlight.js'
import 'highlight.js/styles/tokyo-night-dark.css'
import DOMPurify from 'dompurify'
import Header from '@/layouts/components/Header.vue'
import Footer from '@/layouts/components/Footer.vue'
import UserInfoCard from '@/components/UserInfoCard.vue'
import SkeletonArticleDetail from '@/components/SkeletonArticleDetail.vue'
import TopicLinks from './components/TopicLinks.vue'
import { getArticleDetail } from '@/api/frontend/article'
import { getCategories } from '@/api/frontend/category'
import { getTags } from '@/api/frontend/tag'
import { applyArticleSeo } from '@/composables/seo'

const route = useRoute()
// 路由改为 /article/:id，同时用 computed 保证"上一篇/下一篇"跳转时能重新加载
const articleId = computed(() => route.params.id)
const state = shallowRef('loading')
const article = shallowRef(null)
const categories = shallowRef([])
const tags = shallowRef([])
const markdown = new MarkdownIt({ html: true, linkify: true, typographer: true, breaks: true })
const renderedContent = computed(() => DOMPurify.sanitize(markdown.render(article.value?.content || ''), {
    USE_PROFILES: { html: true },
}))
const vHighlight = {
    mounted(element) {
        for (const block of element.querySelectorAll('pre code')) {
            hljs.highlightElement(block)
        }
    },
}
const relatedArticles = computed(() => [
    { label: '上一篇', icon: ArrowLeftIcon, article: article.value?.preArticle },
    { label: '下一篇', icon: ArrowRightIcon, article: article.value?.nextArticle },
].filter(item => item.article?.id))

// 请求序号：快速切换"上一篇/下一篇"时，旧请求可能后到并覆盖新文章
let loadToken = 0

async function loadArticle() {
    const id = articleId.value
    if (typeof id !== 'string' || !/^\d+$/.test(id)) {
        state.value = 'missing'
        return
    }

    const token = ++loadToken
    state.value = 'loading'
    try {
        const response = await getArticleDetail(id)
        if (token !== loadToken) return
        article.value = response.success ? response.data : null
        state.value = article.value ? 'ready' : 'missing'
        // 用文章自身的标题与摘要覆盖通用 SEO 信息
        if (article.value) applyArticleSeo(article.value)
    } catch {
        if (token !== loadToken) return
        state.value = 'failed'
    }
}

watch(articleId, loadArticle, { immediate: true })
getCategories().then(response => { if (response.success) categories.value = response.data ?? [] }).catch(() => {})
getTags().then(response => { if (response.success) tags.value = response.data ?? [] }).catch(() => {})
</script>

<template>
    <Header />
    <main class="site-width site-page site-split">
        <div class="reading-area">
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
                <article class="wb-panel reading-panel">
                    <header class="reading-heading">
                        <RouterLink
                            v-if="article.categoryId"
                            class="wb-chip reading-category"
                            :to="{ path: '/category/list', query: { id: article.categoryId, name: article.categoryName } }"
                        ><FolderIcon aria-hidden="true" /> {{ article.categoryName }}</RouterLink>
                        <h1>{{ article.title }}</h1>
                        <div class="reading-meta">
                            <span v-if="article.updateTime"><CalendarDaysIcon aria-hidden="true" /> 更新于 {{ article.updateTime }}</span>
                            <span v-if="article.readNum != null"><EyeIcon aria-hidden="true" /> {{ article.readNum }} 次阅读</span>
                        </div>
                    </header>
                    <div v-viewer v-highlight class="article-content" v-html="renderedContent"></div>
                    <div v-if="article.tags?.length" class="reading-tags">
                        <RouterLink v-for="tag in article.tags" :key="tag.id" :to="{ path: '/tag/list', query: { id: tag.id, name: tag.name } }" class="wb-chip"># {{ tag.name }}</RouterLink>
                    </div>
                </article>

                <nav v-if="relatedArticles.length" class="related-articles" aria-label="相邻文章">
                    <RouterLink v-for="item in relatedArticles" :key="item.label" :to="{ name: 'ArticleDetail', params: { id: item.article.id } }" class="wb-panel adjacent-article">
                        <span><component :is="item.icon" aria-hidden="true" /> {{ item.label }}</span>
                        <strong>{{ item.article.title }}</strong>
                    </RouterLink>
                </nav>
            </template>
        </div>
        <aside class="site-rail reading-rail" aria-label="作者与主题">
            <UserInfoCard />
            <TopicLinks title="分类" path="/category" :entries="categories" />
            <TopicLinks title="标签" path="/tag" :entries="tags" />
        </aside>
    </main>
    <Footer />
</template>

<style scoped>
.reading-area { min-width: 0; }
.reading-nav { margin-bottom: 10px; color: var(--wb-muted); font-size: 0.86rem; }
.reading-nav a,
.reading-meta span,
.adjacent-article span { display: inline-flex; align-items: center; gap: 7px; }
.reading-nav a { min-height: 34px; }
.reading-nav a:hover { color: var(--wb-accent); }
.reading-nav svg,
.reading-meta svg,
.reading-category svg,
.adjacent-article svg { width: 16px; height: 16px; flex: none; }

.reading-panel { padding: clamp(17px, 4.6vw, 56px); }
.reading-heading { padding-bottom: 25px; border-bottom: 1px solid var(--wb-line); }
.reading-category { gap: 6px; color: var(--wb-cyan); }
.reading-heading h1 { margin: 14px 0; font: 750 clamp(1.85rem, 4.1vw, 3.3rem)/1.24 var(--wb-display); letter-spacing: -0.038em; overflow-wrap: anywhere; }
.reading-meta { display: flex; flex-wrap: wrap; gap: 6px 20px; color: var(--wb-muted); font-size: 0.82rem; }
.reading-tags { display: flex; flex-wrap: wrap; gap: 7px; padding-top: 18px; border-top: 1px solid var(--wb-line); }
.reading-rail { position: sticky; top: calc(var(--wb-header-height) + 18px); }
.related-articles { display: grid; grid-template-columns: repeat(2, minmax(0,1fr)); gap: 10px; margin-top: 12px; }
.adjacent-article { display: grid; gap: 4px; padding: 13px 15px; overflow-wrap: anywhere; }
.adjacent-article span { color: var(--wb-muted); font-size: 0.78rem; }
.adjacent-article:hover { border-color: var(--wb-accent); color: var(--wb-accent); }

.article-content { max-width: 76ch; margin: 26px auto; font-size: clamp(1rem, 1.55vw, 1.09rem); line-height: 1.85; overflow-wrap: anywhere; }
.article-content :deep(p),
.article-content :deep(ul),
.article-content :deep(ol) { margin-block: 0 1.05em; }
.article-content :deep(h1),
.article-content :deep(h2),
.article-content :deep(h3),
.article-content :deep(h4),
.article-content :deep(h5),
.article-content :deep(h6) { margin-block: 1.45em 0.62em; font-weight: 750; line-height: 1.4; }
.article-content :deep(h1) { font-size: 1.75em; }
.article-content :deep(h2) { font-size: 1.4em; padding-bottom: 0.25em; border-bottom: 1px solid var(--wb-line); }
.article-content :deep(h3) { font-size: 1.18em; }
.article-content :deep(ul),
.article-content :deep(ol) { padding-left: 1.5em; }
.article-content :deep(ul) { list-style: disc; }
.article-content :deep(ol) { list-style: decimal; }
.article-content :deep(a) { color: var(--wb-cyan); text-decoration: underline; text-underline-offset: 3px; }
.article-content :deep(blockquote) { margin: 1.2em 0; padding: 0.8em 1em; border-left: 3px solid var(--wb-accent); background: var(--wb-raised); color: var(--wb-muted); }
.article-content :deep(blockquote p:last-child) { margin: 0; }
.article-content :deep(code) { font-family: var(--wb-mono); font-size: 0.9em; }
.article-content :deep(:not(pre) > code) { color: var(--wb-accent); background: var(--wb-accent-soft); border-radius: 3px; padding: 0.1em 0.3em; }
.article-content :deep(pre) { overflow-x: auto; padding: 1em; border-radius: 7px; border: 1px solid #27344a; background: #101827; color: #e4eafa; margin: 1.25em 0; line-height: 1.65; }
.article-content :deep(pre code) { padding: 0; background: transparent; }
.article-content :deep(table) { display: block; width: max-content; max-width: 100%; overflow-x: auto; margin: 1.2em 0; border-collapse: collapse; }
.article-content :deep(th),
.article-content :deep(td) { padding: 0.4em 0.7em; border: 1px solid var(--wb-line); }
.article-content :deep(th) { background: var(--wb-raised); }
.article-content :deep(img) { display: block; height: auto; max-width: 100%; border-radius: 7px; margin: 1.2em auto; cursor: zoom-in; }
.article-content :deep(hr) { margin: 1.6em 0; border-color: var(--wb-line); }

@media (max-width: 940px) { .reading-rail { position: static; } }
@media (max-width: 520px) { .related-articles { grid-template-columns: minmax(0,1fr); } }
</style>
