<template>
    <Header></Header>

    <div class="container mx-auto max-w-screen-xl mt-8 px-4">
        <div class="grid grid-cols-1 lg:grid-cols-4 gap-8">
            <!-- 左边栏 -->
            <div class="col-span-1 lg:col-span-3">
                <template v-if="loading">
                    <SkeletonArchive />
                </template>
                <template v-else>
                <div v-for="(item, index) in archives" :key="index"
                    class="p-5 mb-4 border border-gray-200 rounded-lg bg-white dark:bg-gray-800 dark:border-gray-700">
                    <time class="text-lg font-semibold text-gray-900 dark:text-white">{{ item.month }}</time>
                    <ol class="mt-3 divide-y divider-gray-200 dark:divide-gray-700">
                        <li v-for="(item2, index2) in item.articles" :key="index2">
                            <a @click="goArticleDetail(item2.id)"
                                class="cursor-pointer items-center block p-3 sm:flex hover:bg-gray-100 dark:hover:bg-gray-700">
                                <img v-if="item2.titleImage" class="w-24 h-12 mb-3 mr-3 rounded-lg sm:mb-0"
                                    :src="item2.titleImage" alt="文章封面图" />
                                <div class="text-gray-600 dark:text-gray-400">
                                    <div class="text-base font-normal"><span
                                            class="font-medium text-gray-900 dark:text-white">{{ item2.title }}</span>
                                    </div>
                                    <span
                                        class="inline-flex items-center text-xs font-normal text-gray-500 dark:text-gray-400">
                                        <svg class="w-2.5 h-2.5 mr-2 mb-1px" aria-hidden="true"
                                            xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 20 20">
                                            <path stroke="currentColor" stroke-linecap="round" stroke-linejoin="round"
                                                stroke-width="2"
                                                d="M5 1v3m5-3v3m5-3v3M1 7h18M5 11h10M2 3h16a1 1 0 0 1 1 1v14a1 1 0 0 1-1 1H2a1 1 0 0 1-1-1V4a1 1 0 0 1 1-1Z" />
                                        </svg>
                                        {{ item2.createTime }}
                                    </span>
                                </div>
                            </a>
                        </li>
                    </ol>
                </div>
                </template>

                <!-- 分页 -->
                <nav aria-label="Page navigation example" v-if="total > 0">
                    <ul class="flex items-center justify-center mt-10 mb-10 -space-x-px h-10 text-base">
                        <li>
                            <a v-if="current > 1" @click="getArchiveList(current - 1)"
                                class="flex items-center justify-center px-4 h-10 ml-0 leading-tight text-gray-500 bg-white border border-gray-300 rounded-l-lg hover:bg-gray-100 hover:text-gray-700 dark:bg-gray-800 dark:border-gray-700 dark:text-gray-400 dark:hover:bg-gray-700 dark:hover:text-white">
                                <span class="sr-only">Previous</span>
                                <svg class="w-3 h-3" aria-hidden="true" xmlns="http://www.w3.org/2000/svg" fill="none"
                                    viewBox="0 0 6 10">
                                    <path stroke="currentColor" stroke-linecap="round" stroke-linejoin="round"
                                        stroke-width="2" d="M5 1 1 5l4 4" />
                                </svg>
                            </a>
                            <a v-else @click="getArchiveList(current)"
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
                            <a @click="getArchiveList(page)"
                                class="flex items-center border-gray-300 justify-center px-4 h-10 leading-tight bg-white border dark:bg-gray-800 dark:border-gray-700 dark:text-gray-400 dark:hover:bg-gray-700 dark:hover:text-white"
                                :class="[page == current ? 'text-blue-600 bg-blue-50 hover:bg-blue-100 hover:text-blue-700' : 'text-gray-500  hover:bg-gray-100 hover:text-gray-700']">
                                {{ page }}
                            </a>
                        </li>
                        <li>
                            <a v-if="current < pages" @click="getArchiveList(current + 1)"
                                class="flex items-center justify-center px-4 h-10 leading-tight text-gray-500 bg-white border border-gray-300 rounded-r-lg hover:bg-gray-100 hover:text-gray-700 dark:bg-gray-800 dark:border-gray-700 dark:text-gray-400 dark:hover:bg-gray-700 dark:hover:text-white">
                                <span class="sr-only">Next</span>
                                <svg class="w-3 h-3" aria-hidden="true" xmlns="http://www.w3.org/2000/svg" fill="none"
                                    viewBox="0 0 6 10">
                                    <path stroke="currentColor" stroke-linecap="round" stroke-linejoin="round"
                                        stroke-width="2" d="m1 9 4-4-4-4" />
                                </svg>
                            </a>
                            <a v-else="current == pages" @click="getArchiveList(current)"
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
import SkeletonArchive from '@/components/SkeletonArchive.vue'
import SkeletonSidebar from '@/components/SkeletonSidebar.vue'
import { useRouter } from 'vue-router'
import { getArchives } from '@/api/frontend/archive'
import { ref } from 'vue'

const router = useRouter()

const loading = ref(true)

const goArticleDetail = (articleId) => {
    console.log('跳转详情页' + articleId)
    router.push({ path: '/article/detail', query: { articleId: articleId } })
}

const archives = ref([])
// 当前页码
const current = ref(1)
const total = ref(0)
const size = ref(10)
const pages = ref(0)

// 获取分页数据
function getArchiveList(currentPage) {
    console.log('获取分页数据')
    getArchives({ current: currentPage, size: size.value })
        .then((res) => {
            console.log(res)
            if (res.success == true) {
                archives.value = res.data
                current.value = res.current
                total.value = res.total
                size.value = res.size
                pages.value = res.pages
            }
        })
        .finally(() => { loading.value = false })
}
getArchiveList(current.value)

</script>