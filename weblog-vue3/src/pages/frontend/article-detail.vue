<template>
    <Header></Header>

    <!-- 文章详情 -->
    <div class="container mx-auto max-w-screen-xl mt-8 mb-8 px-4">
        <div class="grid grid-cols-1 lg:grid-cols-4 gap-8">
            <!-- 左边栏 -->
            <div class="col-span-1 lg:col-span-3">
                <template v-if="loading">
                    <SkeletonArticleDetail />
                </template>
                <template v-else>
                <div
                    class="bg-white border border-gray-200 p-8 rounded-xl shadow-lg dark:bg-gray-800 dark:border-gray-700">
                    <!-- 面包屑 -->
                    <nav class="flex mb-4" aria-label="Breadcrumb">
                        <ol class="inline-flex items-center space-x-1 md:space-x-3">
                            <li class="inline-flex items-center">
                                <a @click="router.push('/')"
                                    class="cursor-pointer inline-flex items-center text-sm font-medium text-gray-500 hover:text-blue-600 dark:text-gray-400 dark:hover:text-white">
                                    <svg class="w-3 h-3 mr-2.5" aria-hidden="true" xmlns="http://www.w3.org/2000/svg"
                                        fill="currentColor" viewBox="0 0 20 20">
                                        <path
                                            d="m19.707 9.293-2-2-7-7a1 1 0 0 0-1.414 0l-7 7-2 2a1 1 0 0 0 1.414 1.414L2 10.414V18a2 2 0 0 0 2 2h3a1 1 0 0 0 1-1v-4a1 1 0 0 1 1-1h2a1 1 0 0 1 1 1v4a1 1 0 0 0 1 1h3a2 2 0 0 0 2-2v-7.586l.293.293a1 1 0 0 0 1.414-1.414Z" />
                                    </svg>
                                    首页
                                </a>
                            </li>
                            <li aria-current="page">
                                <div class="flex items-center text-gray-400">
                                    /
                                    <span
                                        class="ml-1 text-sm font-medium text-gray-500 md:ml-4 dark:text-gray-400">正文</span>
                                </div>
                            </li>
                        </ol>
                    </nav>


                    <!-- 文章主体 -->
                    <article>
                        <h1 class="title mt-4 text-4xl md:text-5xl font-bold">{{ article.title }}</h1>
                        <div class="text-gray-500 text-base flex items-center article-mata mt-4">
                            <svg class="inline w-3 h-3 mr-2 text-gray-400 dark:text-white" aria-hidden="true"
                                xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 20 20">
                                <path stroke="currentColor" stroke-linecap="round" stroke-linejoin="round"
                                    stroke-width="2"
                                    d="M5 1v3m5-3v3m5-3v3M1 7h18M5 11h10M2 3h16a1 1 0 0 1 1 1v14a1 1 0 0 1-1 1H2a1 1 0 0 1-1-1V4a1 1 0 0 1 1-1Z" />
                            </svg>
                            发表于 {{ article.updateTime }}

                            <svg class="inline w-3 h-3 ml-5 mr-2 text-gray-400 dark:text-white" aria-hidden="true"
                                xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 18 18">
                                <path stroke="currentColor" stroke-linecap="round" stroke-linejoin="round"
                                    stroke-width="2"
                                    d="M1 5v11a1 1 0 0 0 1 1h14a1 1 0 0 0 1-1V6a1 1 0 0 0-1-1H1Zm0 0V2a1 1 0 0 1 1-1h5.443a1 1 0 0 1 .8.4l2.7 3.6H1Z" />
                            </svg>
                            分类于&nbsp;<a @click="goCatagoryArticleListPage(article.categoryId, article.categoryName)"
                                class="text-gray-500 hover:underline">{{ article.categoryName }}</a>

                            <svg class="inline w-3 h-3 ml-5 mr-2 text-gray-400 dark:text-white" aria-hidden="true"
                                xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 20 14">
                                <g stroke="currentColor" stroke-linecap="round" stroke-linejoin="round"
                                    stroke-width="2">
                                    <path d="M10 10a3 3 0 1 0 0-6 3 3 0 0 0 0 6Z" />
                                    <path d="M10 13c4.97 0 9-2.686 9-6s-4.03-6-9-6-9 2.686-9 6 4.03 6 9 6Z" />
                                </g>
                            </svg> 阅读量 {{ article.readNum }}
                        </div>

                        <div class="article-content mt-8" v-viewer v-html="renderedContent" v-highlight>
                        </div>

                        <!-- 标签 -->
                        <div class="mt-8 mb-6">
                            <div @click="goTagArticleListPage(item.id, item.name)" v-for="(item, index) in article.tags"
                                :key="index"
                                class="inline-block rounded-full bg-green-100 text-green-800 text-sm font-medium mr-3 mb-2 px-2.5 py-0.5 rounded dark:bg-green-900 dark:text-green-300 hover:bg-green-200 hover:text-green-900">
                                # {{ item.name }}
                            </div>
                        </div>
                    </article>
                    <!-- 上下篇 -->
                    <div class="article-footer flex mt-8 pt-6">
                        <div class="cursor-pointer">
                            <a v-if="article.preArticleId" @click="goArticleDetail(article.preArticleId)">
                                <span class="desc">
                                    <svg class="inline w-2 h-2 mr-1 mb-3px text-gray-500 dark:text-white"
                                        aria-hidden="true" xmlns="http://www.w3.org/2000/svg" fill="none"
                                        viewBox="0 0 8 14">
                                        <path stroke="currentColor" stroke-linecap="round" stroke-linejoin="round"
                                            stroke-width="2" d="M7 1 1.3 6.326a.91.91 0 0 0 0 1.348L7 13" />
                                    </svg>
                                    上一篇</span>
                                <span
                                    class="hover:text-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-700 focus:text-blue-700">{{
                                        article.preArticleTitle }}</span>
                            </a>
                        </div>
                        <div class="cursor-pointer">
                            <a v-if="article.nextArticleId" @click="goArticleDetail(article.nextArticleId)">
                                <span class="desc">
                                    下一篇
                                    <svg class="inline w-2 h-2 ml-1 mb-3px text-gray-500 dark:text-white"
                                        aria-hidden="true" xmlns="http://www.w3.org/2000/svg" fill="none"
                                        viewBox="0 0 8 14">
                                        <path stroke="currentColor" stroke-linecap="round" stroke-linejoin="round"
                                            stroke-width="2" d="m1 13 5.7-5.326a.909.909 0 0 0 0-1.348L1 1" />
                                    </svg>
                                </span>
                                <span
                                    class="hover:text-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-700 focus:text-blue-700">{{
                                        article.nextArticleTitle }}</span>
                            </a>
                        </div>
                    </div>
                </div>
                </template>
            </div>
            <!-- 右边栏 -->
            <div class="col-span-1">
                <div class="sticky top-24 space-y-6">
                    <template v-if="loading">
                        <SkeletonSidebar />
                    </template>
                    <template v-else>
                    <UserInfoCard></UserInfoCard>

                    <!-- 文章分类 -->
                    <div
                        class="mb-3 w-full font-medium p-5 bg-white border border-gray-200 rounded-lg dark:bg-gray-800 dark:border-gray-700">
                        <h2 class="mb-2 font-bold text-gray-900 uppercase dark:text-white">分类</h2>
                        <div
                            class="text-sm font-medium text-gray-900 bg-white rounded-lg dark:bg-gray-700 dark:border-gray-600 dark:text-white">
                            <a @click="goCatagoryArticleListPage(item.id, item.name)"
                                v-for="(item, index) in categories" :key="index"
                                class="flex items-end block w-full px-4 py-2 rounded-lg cursor-pointer hover:bg-gray-100 hover:text-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-700 focus:text-blue-700 dark:border-gray-600 dark:hover:bg-gray-600 dark:hover:text-white dark:focus:ring-gray-500 dark:focus:text-white">
                                <svg class="w-4 h-4 mr-2 mb-2px text-gray-800 inline dark:text-white" aria-hidden="true"
                                    xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 21 18">
                                    <path stroke="currentColor" stroke-linecap="round" stroke-linejoin="round"
                                        stroke-width="0.9"
                                        d="M2.539 17h12.476l4-9H5m-2.461 9a1 1 0 0 1-.914-1.406L5 8m-2.461 9H2a1 1 0 0 1-1-1V2a1 1 0 0 1 1-1h5.443a1 1 0 0 1 .8.4l2.7 3.6H16a1 1 0 0 1 1 1v2H5" />
                                </svg>
                                {{ item.name }}
                            </a>
                        </div>
                    </div>

                    <!-- 文章标签 -->
                    <div
                        class="mb-3 w-full font-medium p-5 bg-white border border-gray-200 rounded-lg dark:bg-gray-800 dark:border-gray-700">
                        <h2 class="mb-2 font-bold text-gray-900 uppercase dark:text-white">标签</h2>
                        <div @click="goTagArticleListPage(item.id, item.name)" v-for="(item, index) in tags"
                            :key="index"
                            class="inline-block bg-green-100 text-green-800 text-xs font-medium mr-2 mb-1 px-2.5 py-0.5 rounded hover:bg-green-200 hover:text-green-900 dark:hover:bg-green-800 dark:hover:text-green-300 dark:bg-green-900 dark:text-green-300">
                            {{ item.name }}
                        </div>
                    </div>
                    </template>
                </div>
            </div>
        </div>
    </div>

    <Footer></Footer>
</template>

<script setup>
import Header from '@/layouts/components/Header.vue'
import Footer from '@/layouts/components/Footer.vue'
import UserInfoCard from '@/components/UserInfoCard.vue'
import SkeletonArticleDetail from '@/components/SkeletonArticleDetail.vue'
import SkeletonSidebar from '@/components/SkeletonSidebar.vue'
import { useRoute, useRouter } from 'vue-router';
import { getArticleDetail } from '@/api/frontend/article';
import { ref, reactive, computed } from 'vue'
import { getCategories } from '@/api/frontend/category'
import { getTags } from '@/api/frontend/tag'
import MarkdownIt from 'markdown-it'

const router = useRouter()
const route = useRoute()

const loading = ref(true)
let loadCount = 0
function trackLoaded() {
  loadCount++
  if (loadCount >= 3) {
    loading.value = false
  }
}

// 创建 Markdown 解析器
const md = new MarkdownIt({
    html: true,         // 允许 HTML 标签
    linkify: true,      // 自动将 URL 转换为链接
    typographer: true,  // 启用语言无关的排版美化
    breaks: true,       // 将换行符转换为 <br>
    highlight: function (str, lang) {
        // 如果指定了语言，尝试使用 highlight.js 进行高亮
        if (lang && window.hljs && window.hljs.getLanguage(lang)) {
            try {
                return window.hljs.highlight(str, { language: lang }).value;
            } catch (__) { }
        }

        // 如果没有指定语言或高亮失败，返回转义的代码
        return md.utils.escapeHtml(str);
    }
})

const article = reactive({
    title: '',
    content: '',
    updateTime: '',
    readNum: 0,
    categoryId: null,
    categoryName: '',
    preArticleId: null,
    preArticleTitle: '',
    nextArticleId: null,
    nextArticleTitle: '',
    tags: [],
})

// 计算属性：将 Markdown 转换为 HTML
const renderedContent = computed(() => {
    return md.render(article.content || '')
})

function queryArticleDetail(articleId) {
    console.log('调用获取详情接口...' + route.query.articleId)
    getArticleDetail(articleId).then((e) => {
        article.title = e.data.title
        article.content = e.data.content
        article.updateTime = e.data.updateTime
        article.categoryId = e.data.categoryId
        article.categoryName = e.data.categoryName
        article.readNum = e.data.readNum
        article.tags = e.data.tags
        if (e.data.preArticle) {
            console.log('上一篇...')
            console.log(e.data.preArticle)
            article.preArticleId = e.data.preArticle.id
            article.preArticleTitle = e.data.preArticle.title
        } else {
            article.preArticleId = null
        }

        if (e.data.nextArticle) {
            article.nextArticleId = e.data.nextArticle.id
            article.nextArticleTitle = e.data.nextArticle.title
        } else {
            article.nextArticleId = null
        }
    }).finally(() => trackLoaded())
}
queryArticleDetail(route.query.articleId);

const goArticleDetail = (articleId) => {
    console.log('跳转详情页' + articleId)
    router.push({ path: '/article/detail', query: { articleId: articleId } })
    queryArticleDetail(articleId)
}


// 获取分类
const categories = ref([])
getCategories().then((e) => {
    console.log('获取分类数据')
    console.log(e)
    categories.value = e.data
}).finally(() => trackLoaded())

// 获取标签
const tags = ref([])
getTags().then((e) => {
    console.log('获取标签数据')
    console.log(e)
    tags.value = e.data
}).finally(() => trackLoaded())

const goCatagoryArticleListPage = (id, name) => {
    router.push({ path: '/category/list', query: { id: id, name: name } })
}

const goTagArticleListPage = (id, name) => {
    router.push({ path: '/tag/list', query: { id: id, name: name } })
}

</script>

<style scoped>
.title {
    padding-bottom: 24px;
    margin-bottom: 0;
    line-height: 1.4;
    word-wrap: break-word;
    font-weight: 800;
    color: #1a202c;
}

.article-mata {
    margin-bottom: 32px;
}

:deep(pre) {
    background: #21252b;
    color: #f8f8f2;
    border-radius: 5px;
    padding: 10px 0 0;
    font-size: 17px;
    padding-left: 15px;
}

:deep(pre code.hljs) {
    display: block;
    overflow-x: auto;
    padding: 1em;
    padding-left: 0 !important;
    padding-top: 25px !important;
}

:deep(pre:before) {
    background: #fc625d;
    border-radius: 50%;
    box-shadow: 20px 0 #fdbc40, 40px 0 #35cd4b;
    content: ' ';
    height: 10px;
    margin-top: 5px;
    position: absolute;
    width: 10px;
}

:deep(.article-content p) {
    letter-spacing: 0.4px;
    margin: 0 0 28px 0;
    line-height: 1.8;
    color: #2d3748;
    font-size: 18px;
    font-weight: 400;
    word-break: normal;
    word-wrap: break-word;
    font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, "Noto Sans", sans-serif, "Apple Color Emoji", "Segoe UI Emoji", "Segoe UI Symbol", "Noto Color Emoji";
}

:deep(.article-content h1) {
    margin: 48px 0 24px 0;
    color: #1a202c;
    font-size: 36px;
    font-weight: 800;
    line-height: 1.3;
    border-bottom: 2px solid #e2e8f0;
    padding-bottom: 16px;
}

:deep(.article-content h2) {
    margin: 40px 0 20px 0;
    color: #2d3748;
    font-size: 30px;
    font-weight: 700;
    line-height: 1.4;
    border-bottom: 1px solid #e2e8f0;
    padding-bottom: 12px;
}

:deep(.article-content h3) {
    margin: 32px 0 16px 0;
    color: #4a5568;
    font-size: 24px;
    font-weight: 600;
    line-height: 1.4;
}

:deep(.article-content h4) {
    margin: 28px 0 14px 0;
    color: #4a5568;
    font-size: 20px;
    font-weight: 600;
    line-height: 1.4;
}

:deep(.article-content h5, .article-content h6) {
    margin: 24px 0 12px 0;
    color: #4a5568;
    font-size: 18px;
    font-weight: 600;
    line-height: 1.4;
}

:deep(.article-content h3) {
    font-size: 20px;
    margin-top: 40px;
    margin-bottom: 16px;
    font-weight: 600;
}

:deep(.image-caption) {
    min-width: 20%;
    max-width: 80%;
    min-height: 43px;
    display: block;
    padding: 10px;
    margin: 0 auto;
    /* border-bottom: 1px solid #eee; */
    font-size: 13px;
    color: #999;
    text-align: center;
}

:deep(code:not(pre code)) {
    padding: 2px 4px;
    margin: 0 2px;
    font-size: 95% !important;
    border-radius: 4px;
    color: rgb(41, 128, 185);
    background-color: rgba(27, 31, 35, 0.05);
    font-family: Operator Mono, Consolas, Monaco, Menlo, monospace;
}

:deep(pre code) {
    display: block;
    font-size: 95% !important;
    background-color: rgba(27, 31, 35, 0.05);
    font-family: Operator Mono, Consolas, Monaco, Menlo, monospace;
    /* color: #fff; */
}

:deep(article ul) {
    padding-left: 40px;
}

:deep(article ul li) {
    list-style-type: disc;
    padding-top: 8px;
    padding-bottom: 8px;
    font-size: 18px;
    line-height: 1.6;
}



:deep(blockquote) {
    border-left: 4px solid #4299e1;
    quotes: none;
    background: #f7fafc;
    color: #4a5568;
    font-size: 18px;
    margin: 32px 0;
    padding: 32px;
    position: relative;
    border-radius: 8px;
    line-height: 1.7;
}

:deep(blockquote p:last-child) {
    margin-bottom: 0;
}

:deep(table tr) {
    background-color: #fff;
    border-top: 1px solid #c6cbd1;
}

:deep(table) {
    border-collapse: collapse;
    margin-bottom: 1rem;
}

:deep(table th) {
    padding: 12px 16px;
    border: 1px solid #e2e8f0;
    background: #f7fafc;
    font-weight: 600;
    font-size: 16px;
}

:deep(table td) {
    padding: 12px 16px;
    border: 1px solid #e2e8f0;
    font-size: 16px;
}

:deep(.article-content a) {
    color: #167bc2;
}

:deep(.article-content h2) {
    /* margin: 1em auto; */
    font-size: 22px;
    line-height: 1.5;
    font-weight: bold;
    font-synthesis: style;
    /* border-bottom: 1px solid rgba(0,0,0,.1); */
    padding-bottom: 16px;
    /* border-left: 3px solid #167bc2; */
    padding-bottom: 0;
    font-size: 24px;
    margin-top: 40px;
    margin-bottom: 26px;
    line-height: 140%;
    border-bottom: 1px solid #e5e5e5;
    padding-bottom: 15px;
}

:deep(.article-content svg) {
    display: inline;
}

:deep(.article-content img) {
    position: relative;
    max-width: 100%;
    overflow: hidden;
    display: block;
    margin: 0 auto;
    cursor: -webkit-zoom-in;
    cursor: zoom-in;
}

:deep(strong) {
    color: rgb(52, 152, 219);
}

:deep(table tr:nth-child(2n)) {
    background-color: #f6f8fa;
}

.el-breadcrumb__inner a {
    font-weight: 400;
    color: #606266;
}

.el-breadcrumb__inner a:hover {
    font-weight: 400;
    color: #606266;
    text-decoration: underline;
}

.article-footer {
    border-top: 1px solid #e4e7ed;
    background-color: var(--el-fill-color-blank);
    justify-content: space-between;
    padding-top: 1rem;
}

.desc {
    display: block;
    font-size: 12px;
    color: rgba(60, 60, 60, .7);
    ;
}

.cursor-pointer {
    cursor: pointer;
}

.category-item:hover {
    text-decoration: underline;
    cursor: pointer;
}

.tag-item:hover {
    cursor: pointer;
}

.el-tag:hover {
    background-color: var(--el-color-info-light-8);
}
</style>