<template>
    <Header></Header>

    <div class="container mx-auto max-w-screen-xl mt-8 px-4">
        <div class="grid grid-cols-1 lg:grid-cols-4 gap-8">
            <!-- 左边栏 -->
            <div class="col-span-1 lg:col-span-3">
                <!-- 文章列表 -->
                <div v-if="loading" class="grid grid-cols-1 md:grid-cols-2 gap-6">
                    <SkeletonCard v-for="i in 6" :key="i" />
                </div>
                <div v-else class="grid grid-cols-1 md:grid-cols-2 gap-6">

                    <div v-for="(article, index) in articles" :key="index"
                        class="group bg-white/70 backdrop-blur-sm border border-white/30 rounded-xl shadow-md hover:-translate-y-1.5 hover:shadow-xl hover:border-white/60 transition-all duration-300 dark:bg-gray-800/75 dark:border-gray-600/30 dark:hover:border-gray-500/50">
                        <div class="relative overflow-hidden rounded-t-xl">
                            <a @click="goArticleDetail(article.id)" class="cursor-pointer">
                                <img class="h-56 w-full object-cover group-hover:scale-105 transition-transform duration-500" :src="article.titleImage" />
                            </a>
                            <span v-if="article.isTop === 1"
                                class="absolute top-3 left-3 z-10 text-xs font-semibold px-2 py-0.5 rounded bg-yellow-300/90 text-yellow-800 shadow-sm">置顶</span>
                        </div>
                        <div class="p-6">
                            <!-- 标签 -->
                            <div @click="goTagArticleListPage(item.id, item.name)" v-for="(item, index) in article.tags"
                                :key="index"
                                class="mb-3 inline-block bg-green-100 text-green-800 text-xs font-medium mr-2 px-2.5 py-0.5 rounded hover:bg-green-200 hover:text-green-900 dark:hover:bg-green-800 dark:hover:text-green-300 dark:bg-green-900 dark:text-green-300">
                                {{ item.name }}
                            </div>
                            <a @click="goArticleDetail(article.id)" class="cursor-pointer">
                                <h2
                                    class="mb-3 text-2xl font-bold tracking-tight text-gray-900 dark:text-white hover:text-blue-600 transition-colors duration-200">
                                    {{
                                        article.title }}</h2>
                            </a>
                            <p class="mb-4 font-normal text-gray-600 dark:text-gray-300 text-lg leading-relaxed">{{
                                article.description }}</p>
                            <!-- meta 信息 -->
                            <p class="text-gray-400 text-sm flex items-center article-mata">
                                <svg class="inline w-3 h-3 mr-2 text-gray-400 dark:text-white" aria-hidden="true"
                                    xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 20 20">
                                    <path stroke="currentColor" stroke-linecap="round" stroke-linejoin="round"
                                        stroke-width="2"
                                        d="M5 1v3m5-3v3m5-3v3M1 7h18M5 11h10M2 3h16a1 1 0 0 1 1 1v14a1 1 0 0 1-1 1H2a1 1 0 0 1-1-1V4a1 1 0 0 1 1-1Z" />
                                </svg>
                                {{ article.createTime }}

                                <svg class="inline w-3 h-3 ml-5 mr-2 text-gray-400 dark:text-white" aria-hidden="true"
                                    xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 18 18">
                                    <path stroke="currentColor" stroke-linecap="round" stroke-linejoin="round"
                                        stroke-width="2"
                                        d="M1 5v11a1 1 0 0 0 1 1h14a1 1 0 0 0 1-1V6a1 1 0 0 0-1-1H1Zm0 0V2a1 1 0 0 1 1-1h5.443a1 1 0 0 1 .8.4l2.7 3.6H1Z" />
                                </svg>
                                <a @click="goCatagoryArticleListPage(article.category.id, article.category.name)"
                                    class="text-gray-400 hover:underline">{{ article.category.name }}</a>
                            </p>
                        </div>
                    </div>
                </div>

                <!-- 分页 -->
                <nav aria-label="Page navigation example" v-if="total > 0" class="mt-10">
                    <ul class="flex items-center justify-center mt-10 mb-10 -space-x-px h-10 text-base">
                        <li>
                            <a v-if="current > 1" @click="getArticles(current - 1)"
                                class="flex items-center justify-center px-4 h-10 ml-0 leading-tight text-gray-500 bg-white border border-gray-300 rounded-l-lg hover:bg-gray-100 hover:text-gray-700 dark:bg-gray-800 dark:border-gray-700 dark:text-gray-400 dark:hover:bg-gray-700 dark:hover:text-white">
                                <span class="sr-only">Previous</span>
                                <svg class="w-3 h-3" aria-hidden="true" xmlns="http://www.w3.org/2000/svg" fill="none"
                                    viewBox="0 0 6 10">
                                    <path stroke="currentColor" stroke-linecap="round" stroke-linejoin="round"
                                        stroke-width="2" d="M5 1 1 5l4 4" />
                                </svg>
                            </a>
                            <a v-else @click="getArticles(current)"
                                class="cursor-not-allowed flex items-center justify-center px-4 h-10 ml-0 leading-tight text-gray-500 bg-white border border-gray-300 rounded-l-lg hover:bg-gray-100 hover:text-gray-700 dark:bg-gray-800 dark:border-gray-700 dark:text-gray-400 dark:hover:bg-gray-700 dark:hover:text-white">
                                <span class="sr-only">Previous</span>
                                <svg class="w-3 h-3" aria-hidden="true" xmlns="http://www.w3.org/2000/svg" fill="none"
                                    viewBox="0 0 6 10">
                                    <path stroke="currentColor" stroke-linecap="round" stroke-linejoin="round"
                                        stroke-width="2" d="M5 1 1 5l4 4" />
                                </svg>
                            </a>
                        </li>
                        <li v-for="page in pages" :key="page">
                            <a @click="getArticles(page)"
                                class="flex items-center border-gray-300 justify-center px-4 h-10 leading-tight bg-white border dark:bg-gray-800 dark:border-gray-700 dark:text-gray-400 dark:hover:bg-gray-700 dark:hover:text-white"
                                :class="[page == current ? 'text-blue-600 bg-blue-50 hover:bg-blue-100 hover:text-blue-700' : 'text-gray-500  hover:bg-gray-100 hover:text-gray-700']">

                                {{ page }}
                            </a>
                        </li>
                        <li>
                            <a v-if="current < pages" @click="getArticles(current + 1)"
                                class="flex items-center justify-center px-4 h-10 leading-tight text-gray-500 bg-white border border-gray-300 rounded-r-lg hover:bg-gray-100 hover:text-gray-700 dark:bg-gray-800 dark:border-gray-700 dark:text-gray-400 dark:hover:bg-gray-700 dark:hover:text-white">
                                <span class="sr-only">Next</span>
                                <svg class="w-3 h-3" aria-hidden="true" xmlns="http://www.w3.org/2000/svg" fill="none"
                                    viewBox="0 0 6 10">
                                    <path stroke="currentColor" stroke-linecap="round" stroke-linejoin="round"
                                        stroke-width="2" d="m1 9 4-4-4-4" />
                                </svg>
                            </a>
                            <a v-else="current == pages" @click="getArticles(current)"
                                class="cursor-not-allowed flex items-center justify-center px-4 h-10 leading-tight text-gray-500 bg-white border border-gray-300 rounded-r-lg hover:bg-gray-100 hover:text-gray-700 dark:bg-gray-800 dark:border-gray-700 dark:text-gray-400 dark:hover:bg-gray-700 dark:hover:text-white">
                                <span class="sr-only">Next</span>
                                <svg class="w-3 h-3" aria-hidden="true" xmlns="http://www.w3.org/2000/svg" fill="none"
                                    viewBox="0 0 6 10">
                                    <path stroke="currentColor" stroke-linecap="round" stroke-linejoin="round"
                                        stroke-width="2" d="m1 9 4-4-4-4" />
                                </svg>
                            </a>
                        </li>
                    </ul>
                </nav>


            </div>
            <!-- 右边栏 -->
            <div class="col-span-1">
                <div class="sticky top-24 space-y-6">
                    <template v-if="loading">
                        <SkeletonSidebar />
                    </template>
                    <template v-else>
                        <UserInfoCard></UserInfoCard>

                        <GameWidget></GameWidget>

                        <!-- 文章分类 -->
                        <div
                            class="mb-3 w-full font-medium p-5 bg-white border border-gray-200 rounded-lg dark:bg-gray-800 dark:border-gray-700">
                            <h2 class="mb-2 font-bold text-gray-900 uppercase dark:text-white">分类</h2>
                            <div @click="goCatagoryArticleListPage(item.id, item.name)" v-for="(item, index) in categories"
                                :key="index"
                                class="inline-block bg-blue-100 text-blue-800 text-xs font-medium mr-2 mb-1 px-2.5 py-0.5 rounded hover:bg-blue-200 hover:text-blue-900 dark:hover:bg-blue-800 dark:hover:text-blue-300 dark:bg-blue-900 dark:text-blue-300">
                                {{ item.name }}
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
import GameWidget from '@/pages/frontend/components/GameWidget.vue'
import SkeletonCard from '@/components/SkeletonCard.vue'
import SkeletonSidebar from '@/components/SkeletonSidebar.vue'
import { useRouter } from 'vue-router'
import { reactive, ref } from 'vue'
import { getIndexArticles } from '@/api/frontend/index'
import { getCategories } from '@/api/frontend/category'
import { getTags } from '@/api/frontend/tag'

const router = useRouter()

const loading = ref(true)
let loadCount = 0
function trackLoaded() {
  loadCount++
  if (loadCount >= 3) {
    loading.value = false
  }
}

const goArticleDetail = (articleId) => {
    console.log('跳转详情页' + articleId)
    router.push({ path: '/article/detail', query: { articleId: articleId } })
}

const articles = ref([])
// 当前页码
const current = ref(1)
const total = ref(0)
const size = ref(10)
const pages = ref(0)

// 获取分页数据
function getArticles(currentNo) {
    console.log('获取分页数据')
    getIndexArticles({ current: currentNo, size: size.value })
        .then((res) => {
            console.log(res)
            if (res.success == true) {
                articles.value = res.data.records
                current.value = res.data.current
                total.value = res.data.total
                size.value = res.data.size
                pages.value = res.data.pages
            }
        })
        .finally(() => trackLoaded())
}
getArticles(current.value)

// 获取分类
const categories = ref([])
getCategories()
  .then((e) => {
    console.log('获取分类数据')
    console.log(e)
    categories.value = e.data
  })
  .finally(() => trackLoaded())

// 获取标签
const tags = ref([])
getTags()
  .then((e) => {
    console.log('获取标签数据')
    console.log(e)
    tags.value = e.data
  })
  .finally(() => trackLoaded())


const goCatagoryArticleListPage = (id, name) => {
    router.push({ path: '/category/list', query: { id: id, name: name } })
}

const goTagArticleListPage = (id, name) => {
    console.log('跳转 id' + id)
    router.push({ path: '/tag/list', query: { id: id, name: name } })
}

</script>

<style>
.article-img {
    height: 100%;
}

.two-line-clamp {
    display: -webkit-box;
    -webkit-box-orient: vertical;
    -webkit-line-clamp: 2;
    overflow: hidden;
}

.el-menu--horizontal .el-menu-item:not(.is-disabled):focus,
.el-menu--horizontal .el-menu-item:not(.is-disabled):hover {
    outline: 0;
    color: var(--el-menu-text-color);
    ;
    background-color: #fff;
    border-bottom: 2px solid #409eff;
    ;
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

.cursor-pointer {
    cursor: pointer;
}
</style>