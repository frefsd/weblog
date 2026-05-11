<template>
    <Header></Header>

    <div class="container mx-auto max-w-screen-xl mt-8 px-4">
        <div class="grid grid-cols-1 lg:grid-cols-4 gap-8">
            <!-- 左边栏 -->
            <div class="col-span-1 lg:col-span-3">
                <template v-if="loading">
                    <SkeletonList />
                </template>
                <template v-else>
                <div
                    class="mb-3 w-full font-medium p-5 bg-white border border-gray-200 rounded-lg dark:bg-gray-800 dark:border-gray-700">
                    <h2 class="mb-2 font-bold text-gray-900 uppercase dark:text-white">标签</h2>
                    <div @click="goTagArticleListPage(item.id, item.name)" v-for="(item, index) in tags" :key="index"
                        class="inline-block rounded-full bg-green-100 text-green-800 text-sm font-medium mr-2 mb-2 px-2.5 py-0.5 rounded dark:bg-green-900 dark:text-green-300 hover:bg-green-200 hover:text-green-900">
                        {{ item.name }}
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
import SkeletonList from '@/components/SkeletonList.vue'
import SkeletonSidebar from '@/components/SkeletonSidebar.vue'
import { useRouter } from 'vue-router'
import { getTags } from '@/api/frontend/tag'
import { ref } from 'vue'

const router = useRouter()

const loading = ref(true)

const goTagArticleListPage = (id, name) => {
    router.push({ path: '/tag/list', query: { id: id, name: name } })
}

const tags = ref([])
getTags().then((e) => {
    if (e.success) {
        tags.value = e.data
    }
}).finally(() => { loading.value = false })
</script>