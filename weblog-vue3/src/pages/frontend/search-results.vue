<template>
    <div class="min-h-screen bg-gray-50 dark:bg-gray-900">
        <!-- 头部 -->
        <Header />

        <!-- 搜索页面主内容 -->
        <div class="container mx-auto max-w-screen-2xl mt-8 mb-12 px-4">
            <!-- 搜索框 -->
            <div class="mb-8">
                <div class="relative max-w-2xl mx-auto">
                    <div class="absolute inset-y-0 left-0 flex items-center pl-4 pointer-events-none">
                        <svg class="w-5 h-5 text-gray-500 dark:text-gray-400" fill="none" stroke="currentColor"
                            viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                d="m19 19-4-4m0-7A7 7 0 1 1 1 8a7 7 0 0 1 14 0Z" />
                        </svg>
                    </div>
                    <input type="text" v-model="keyword" @keyup.enter="performSearch"
                        class="block w-full p-4 pl-12 text-lg text-gray-900 border border-gray-300 rounded-xl bg-white focus:ring-blue-500 focus:border-blue-500 dark:bg-gray-800 dark:border-gray-700 dark:text-white dark:focus:ring-blue-500 dark:focus:border-blue-500"
                        placeholder="输入关键词搜索文章...">
                    <button @click="performSearch"
                        class="absolute right-2.5 bottom-2.5 px-6 py-2 text-white bg-blue-600 hover:bg-blue-700 focus:ring-4 focus:outline-none focus:ring-blue-300 font-medium rounded-lg text-lg dark:bg-blue-600 dark:hover:bg-blue-700 dark:focus:ring-blue-800">
                        搜索
                    </button>
                </div>
            </div>

            <!-- 搜索结果 -->
            <div v-if="hasSearched">
                <!-- 搜索结果头部 -->
                <div class="mb-6">
                    <h1 class="text-3xl font-bold text-gray-900 dark:text-white">
                        搜索结果
                    </h1>
                    <div class="mt-2 text-gray-600 dark:text-gray-400">
                        <span v-if="isLoading">正在搜索...</span>
                        <span v-else>
                            找到 <span class="font-bold text-blue-600 dark:text-blue-400">{{ total }}</span> 条关于
                            <span class="font-bold text-gray-900 dark:text-white">"{{ keyword }}"</span> 的结果
                        </span>
                    </div>
                </div>

                <!-- 加载状态 -->
                <div v-if="isLoading" class="py-12 text-center">
                    <div class="inline-block animate-spin rounded-full h-12 w-12 border-b-4 border-blue-600"></div>
                    <p class="mt-4 text-gray-600 dark:text-gray-400">正在搜索相关文章...</p>
                </div>

                <!-- 搜索结果列表 -->
                <div v-else>
                    <div v-if="articles.length > 0">
                        <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
                            <div v-for="article in articles" :key="article.id"
                                @click="$router.push(`/article/detail?articleId=${article.id}`)"
                                class="bg-white dark:bg-gray-800 rounded-xl shadow-md hover:shadow-lg transition-shadow duration-300 cursor-pointer overflow-hidden">
                                <div class="p-6">
                                    <h2
                                        class="text-xl font-bold text-gray-900 dark:text-white mb-3 hover:text-blue-600 dark:hover:text-blue-400"
                                        v-html="highlightKeyword(article.title, keyword)">
                                    </h2>
                                    <p v-if="article.description"
                                        class="text-gray-600 dark:text-gray-300 mb-4 line-clamp-2"
                                        v-html="highlightKeyword(article.description, keyword)">
                                    </p>
                                    <div class="flex items-center text-sm text-gray-500 dark:text-gray-400">
                                        <span>{{ article.createTime }}</span>
                                        <span class="mx-2">•</span>
                                        <span>阅读 {{ article.readNum || 0 }}</span>
                                        <span class="mx-2">•</span>
                                        <span v-if="article.categoryName"
                                            class="px-2 py-1 bg-gray-100 dark:bg-gray-700 rounded">
                                            {{ article.categoryName }}
                                        </span>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <!-- 分页 -->
                        <div v-if="total > 0" class="mt-10 flex justify-center">
                            <nav class="flex items-center space-x-2">
                                <button @click="goToPage(current - 1)" :disabled="current <= 1"
                                    class="px-4 py-2 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-lg hover:bg-gray-100 disabled:opacity-50 disabled:cursor-not-allowed dark:bg-gray-800 dark:text-gray-400 dark:border-gray-600 dark:hover:bg-gray-700">
                                    上一页
                                </button>

                                <template v-for="page in pageNumbers" :key="page">
                                    <button v-if="page === '...'" disabled
                                        class="px-3 py-2 text-gray-500 dark:text-gray-400">
                                        ...
                                    </button>
                                    <button v-else @click="goToPage(page)" :class="[
                                        'px-4 py-2 text-sm font-medium rounded-lg',
                                        current === page
                                            ? 'bg-blue-600 text-white'
                                            : 'text-gray-700 bg-white border border-gray-300 hover:bg-gray-100 dark:bg-gray-800 dark:text-gray-400 dark:border-gray-600 dark:hover:bg-gray-700'
                                    ]">
                                        {{ page }}
                                    </button>
                                </template>

                                <button @click="goToPage(current + 1)" :disabled="current >= totalPages"
                                    class="px-4 py-2 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-lg hover:bg-gray-100 disabled:opacity-50 disabled:cursor-not-allowed dark:bg-gray-800 dark:text-gray-400 dark:border-gray-600 dark:hover:bg-gray-700">
                                    下一页
                                </button>
                            </nav>
                        </div>
                    </div>

                    <!-- 无结果 -->
                    <div v-else class="py-16 text-center">
                        <svg class="w-20 h-20 mx-auto text-gray-300 dark:text-gray-600" fill="none"
                            stroke="currentColor" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5"
                                d="M9.172 16.172a4 4 0 015.656 0M9 10h.01M15 10h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                        </svg>
                        <h3 class="mt-6 text-2xl font-medium text-gray-900 dark:text-white">没有找到相关文章</h3>
                        <p class="mt-2 text-gray-600 dark:text-gray-400">尝试其他关键词或浏览分类</p>
                        <div class="mt-6">
                            <button @click="$router.push('/')"
                                class="px-6 py-3 text-white bg-blue-600 hover:bg-blue-700 rounded-lg font-medium">
                                返回首页
                            </button>
                        </div>
                    </div>
                </div>
            </div>

            <!-- 热门搜索提示 -->
            <div v-else class="max-w-2xl mx-auto mt-12">
                <h2 class="text-xl font-bold text-gray-900 dark:text-white mb-4">热门搜索</h2>
                <div class="flex flex-wrap gap-3">
                    <span v-for="tag in popularTags" :key="tag" @click="setKeywordAndSearch(tag)"
                        class="px-4 py-2 bg-gray-100 dark:bg-gray-800 text-gray-700 dark:text-gray-300 rounded-lg hover:bg-gray-200 dark:hover:bg-gray-700 cursor-pointer transition-colors">
                        {{ tag }}
                    </span>
                </div>
                <p class="mt-6 text-gray-600 dark:text-gray-400 text-sm">
                    提示：支持模糊搜索，例如输入 "spring" 可以搜索到包含 "Spring Boot" 的文章
                </p>
            </div>
        </div>

        <!-- 底部 -->
        <Footer />
    </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import Header from '@/layouts/components/Header.vue'
import Footer from '@/layouts/components/Footer.vue'
import { searchArticles } from '@/api/frontend/article'
import { highlightKeyword } from '@/composables/util'

const route = useRoute()
const router = useRouter()

// 搜索状态
const keyword = ref('')
const articles = ref([])
const total = ref(0)
const current = ref(1)
const size = ref(10)
const isLoading = ref(false)
const hasSearched = ref(false)

// 热门标签
const popularTags = ref(['Spring Boot', 'MySQL', 'Vue.js', 'Java', 'Docker', '微服务', 'Redis', 'Linux'])

// 计算总页数
const totalPages = computed(() => {
    return Math.ceil(total.value / size.value)
})

// 分页页码计算
const pageNumbers = computed(() => {
    const pages = []
    const maxVisible = 5

    if (totalPages.value <= maxVisible) {
        for (let i = 1; i <= totalPages.value; i++) {
            pages.push(i)
        }
    } else {
        if (current.value <= 3) {
            for (let i = 1; i <= 4; i++) pages.push(i)
            pages.push('...')
            pages.push(totalPages.value)
        } else if (current.value >= totalPages.value - 2) {
            pages.push(1)
            pages.push('...')
            for (let i = totalPages.value - 3; i <= totalPages.value; i++) pages.push(i)
        } else {
            pages.push(1)
            pages.push('...')
            pages.push(current.value - 1)
            pages.push(current.value)
            pages.push(current.value + 1)
            pages.push('...')
            pages.push(totalPages.value)
        }
    }

    return pages
})

// 从URL获取搜索关键词
onMounted(() => {
    if (route.query.q) {
        keyword.value = decodeURIComponent(route.query.q)
        performSearch()
    }
})

// 监听路由变化
watch(() => route.query.q, (newQuery) => {
    if (newQuery) {
        keyword.value = decodeURIComponent(newQuery)
        current.value = 1
        performSearch()
    }
})

// 执行搜索
const performSearch = async () => {
    if (!keyword.value.trim()) {
        return
    }

    // 更新URL
    router.push({ path: '/search', query: { q: encodeURIComponent(keyword.value.trim()) } })

    // 执行搜索
    isLoading.value = true
    hasSearched.value = true

    try {
        const response = await searchArticles(keyword.value.trim(), current.value, size.value)
        if (response && response.data) {
            articles.value = response.data.records || []
            total.value = response.data.total || 0
        } else {
            articles.value = []
            total.value = 0
        }
    } catch (error) {
        console.error('搜索失败:', error)
        articles.value = []
        total.value = 0
    } finally {
        isLoading.value = false
    }
}

// 设置关键词并搜索
const setKeywordAndSearch = (tag) => {
    keyword.value = tag
    current.value = 1
    performSearch()
}

// 跳转到指定页
const goToPage = (page) => {
    if (page < 1 || page > totalPages.value || page === current.value) {
        return
    }
    current.value = page
    performSearch()
}
</script>

<style scoped>
.line-clamp-2 {
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
}
</style>