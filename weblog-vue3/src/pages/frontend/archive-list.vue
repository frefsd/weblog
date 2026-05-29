<template>
    <Header></Header>

    <div class="max-w-3xl mx-auto mt-10 mb-16 px-4">
        <!-- 页面头部 -->
        <div class="text-center mb-14">
            <div
                class="inline-flex items-center justify-center w-16 h-16 rounded-2xl bg-gradient-to-br from-blue-500 to-purple-600 mb-5 text-3xl text-white shadow-lg shadow-blue-500/25">
                📦
            </div>
            <h1 class="text-3xl font-bold tracking-tight text-gray-900 dark:text-white">文章归档</h1>
            <p class="mt-2 text-sm text-gray-500 dark:text-gray-400">记录每一篇文字的时光印记</p>

            <!-- 统计 -->
            <div class="flex justify-center gap-8 mt-5" v-if="!loading">
                <div class="text-center">
                    <div class="text-2xl font-bold text-blue-600 dark:text-blue-400">{{ yearCount }}</div>
                    <div class="text-xs text-gray-400 dark:text-gray-500 mt-0.5">年份</div>
                </div>
                <div class="text-center">
                    <div class="text-2xl font-bold text-blue-600 dark:text-blue-400">{{ monthCount }}</div>
                    <div class="text-xs text-gray-400 dark:text-gray-500 mt-0.5">月份</div>
                </div>
                <div class="text-center">
                    <div class="text-2xl font-bold text-blue-600 dark:text-blue-400">{{ totalArticleCount }}</div>
                    <div class="text-xs text-gray-400 dark:text-gray-500 mt-0.5">文章</div>
                </div>
            </div>
        </div>

        <!-- 骨架屏 -->
        <template v-if="loading">
            <SkeletonArchive />
        </template>

        <!-- 时间线主体 -->
        <template v-else>
            <div class="relative pl-11 sm:pl-12">
                <!-- 时间线竖线 -->
                <div class="absolute left-[18px] sm:left-5 top-2 bottom-2 w-0.5 bg-gray-200 dark:bg-gray-700 rounded-sm"></div>

                <!-- 年份区块 -->
                <div v-for="(yearItem, yi) in archives" :key="yi" class="relative mb-12 last:mb-0">
                    <!-- 年份头部（可点击折叠） -->
                    <div class="relative flex items-center gap-3 mb-6 cursor-pointer select-none"
                        @click="yearItem._collapsed = !yearItem._collapsed">
                        <!-- 年份圆点 -->
                        <div
                            class="absolute left-[-27px] sm:left-[-30px] top-1/2 -translate-y-1/2 w-3.5 h-3.5 bg-blue-500 dark:bg-blue-400 rounded-full border-[3px] border-white dark:border-gray-900 ring-[3px] ring-blue-500 dark:ring-blue-400 z-10">
                        </div>
                        <span class="text-2xl sm:text-[26px] font-extrabold tracking-tight text-gray-800 dark:text-white">
                            {{ yearItem.year }}
                        </span>
                        <span
                            class="text-xs font-medium text-gray-400 dark:text-gray-500 bg-gray-100 dark:bg-gray-800 px-2.5 py-0.5 rounded-full">
                            {{ yearItem.articleCount }} 篇
                        </span>
                        <svg class="w-3 h-3 text-gray-400 dark:text-gray-500 transition-transform duration-300"
                            :class="{ '-rotate-90': yearItem._collapsed }" fill="none" stroke="currentColor"
                            viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="3" d="M19 9l-7 7-7-7" />
                        </svg>
                    </div>

                    <!-- 月份列表 -->
                    <template v-if="!yearItem._collapsed">
                        <div v-for="(monthItem, mi) in yearItem.months" :key="mi" class="mb-7 last:mb-0">
                            <!-- 月份子标题 -->
                            <div
                                class="flex items-center gap-3 mb-3 px-4 py-2 bg-white dark:bg-gray-800 border border-gray-200 dark:border-gray-700 rounded-lg">
                                <span class="text-sm font-semibold text-gray-700 dark:text-gray-300">
                                    {{ monthItem.month }}
                                </span>
                                <span
                                    class="text-[11px] text-gray-400 dark:text-gray-500 bg-gray-100 dark:bg-gray-700 px-2 py-px rounded-full">
                                    {{ monthItem.articleCount }} 篇
                                </span>
                            </div>

                            <!-- 文章列表 -->
                            <ul class="space-y-0.5">
                                <li v-for="article in monthItem.articles" :key="article.id"
                                    class="flex items-center gap-3 sm:gap-4 px-3 sm:px-4 py-3 rounded-lg cursor-pointer transition-all duration-200 border border-transparent hover:bg-gray-50 dark:hover:bg-gray-800/60 hover:border-gray-200 dark:hover:border-gray-700 hover:translate-x-1"
                                    @click="goArticleDetail(article.id)">
                                    <!-- 日期圆点 -->
                                    <div
                                        class="flex-shrink-0 w-2 h-2 rounded-full bg-gray-300 dark:bg-gray-600 group-hover:bg-blue-500 transition-colors">
                                    </div>

                                    <!-- 封面图 -->
                                    <img v-if="article.titleImage"
                                        class="flex-shrink-0 w-16 sm:w-20 h-10 sm:h-12 rounded-lg object-cover bg-gray-100 dark:bg-gray-700"
                                        :src="article.titleImage" :alt="article.title" />
                                    <div v-else
                                        class="flex-shrink-0 w-16 sm:w-20 h-10 sm:h-12 rounded-lg bg-gradient-to-br from-indigo-100 to-purple-100 dark:from-indigo-900/30 dark:to-purple-900/30 flex items-center justify-center text-lg text-indigo-500 dark:text-indigo-400">
                                        📝
                                    </div>

                                    <!-- 文章信息 -->
                                    <div class="flex-1 min-w-0">
                                        <div
                                            class="text-sm sm:text-[15px] font-medium text-gray-900 dark:text-white truncate transition-colors group-hover:text-blue-600 dark:group-hover:text-blue-400">
                                            {{ article.title }}
                                        </div>
                                        <div class="flex items-center gap-2 sm:gap-3 mt-1 text-xs text-gray-400 dark:text-gray-500">
                                            <span class="inline-flex items-center gap-1">
                                                <svg class="w-3 h-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                                        d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
                                                </svg>
                                                {{ article.createTime }}
                                            </span>
                                            <span v-if="article.categoryName"
                                                class="inline-block px-1.5 py-px rounded text-[11px] font-medium bg-gray-100 dark:bg-gray-700 text-gray-500 dark:text-gray-400">
                                                {{ article.categoryName }}
                                            </span>
                                        </div>
                                    </div>
                                </li>
                            </ul>
                        </div>
                    </template>
                </div>
            </div>
        </template>
    </div>

    <Footer></Footer>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import Header from '@/layouts/components/Header.vue'
import Footer from '@/layouts/components/Footer.vue'
import SkeletonArchive from '@/components/SkeletonArchive.vue'
import { getArchives } from '@/api/frontend/archive'

const router = useRouter()
const loading = ref(true)
const archives = ref([])

// 跳转文章详情
const goArticleDetail = (articleId) => {
    router.push({ path: '/article/detail', query: { articleId } })
}

// 统计数据
const yearCount = computed(() => archives.value.length)
const monthCount = computed(() => archives.value.reduce((sum, y) => sum + (y.months?.length || 0), 0))
const totalArticleCount = computed(() => archives.value.reduce((sum, y) => sum + (y.articleCount || 0), 0))

// 获取归档数据
getArchives()
    .then((res) => {
        if (res.success) {
            // 默认展开最近一年，旧年份折叠
            archives.value = (res.data || []).map((year, index) => ({
                ...year,
                _collapsed: index > 0
            }))
        }
    })
    .finally(() => {
        loading.value = false
    })
</script>