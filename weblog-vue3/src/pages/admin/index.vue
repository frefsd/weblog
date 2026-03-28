<template>
    <div>
        <el-row :gutter="16">
            <el-col :span="4" :offset="0">
                <el-card shadow="never" class="border-1">
                    <!-- card body -->
                    <div class="flex items-center">
                        <div class="mr-4"><el-icon class="text-gray-500 text-xl rounded-full bg-gray-100 w-10 h-10">
                                <Document />
                            </el-icon></div>
                        <div>
                            <div class="text-gray-500">文章</div>
                            <CountTo :value="articleTotalCount"></CountTo>
                        </div>
                    </div>
                </el-card>
            </el-col>
            <el-col :span="4" :offset="0">
                <el-card shadow="never" :body-style="{ padding: '20px' }" class="border-1">
                    <div class="flex items-center">
                        <div class="mr-4"><el-icon class="text-gray-500 text-xl rounded-full bg-gray-100 w-10 h-10">
                                <Folder />
                            </el-icon></div>
                        <div>
                            <div class="text-gray-500">分类</div>
                            <CountTo :value="categoryTotalCount"></CountTo>
                        </div>
                    </div>
                </el-card>
            </el-col>
            <el-col :span="4" :offset="0">
                <el-card shadow="never" :body-style="{ padding: '20px' }" class="border-1">
                    <div class="flex items-center">
                        <div class="mr-4"><el-icon class="text-gray-500 text-xl rounded-full bg-gray-100 w-10 h-10">
                                <PriceTag />
                            </el-icon></div>
                        <div>
                            <div class="text-gray-500">标签</div>
                            <CountTo :value="tagTotalCount"></CountTo>
                        </div>
                    </div>
                </el-card>
            </el-col>
            <el-col :span="4" :offset="0">
                <el-card shadow="never" :body-style="{ padding: '20px' }" class="border-1">
                    <div class="flex items-center">
                        <div class="mr-4"><el-icon class="text-gray-500 text-xl rounded-full bg-gray-100 w-10 h-10">
                                <View />
                            </el-icon></div>
                        <div>
                            <div class="text-gray-500">总浏览量</div>
                            <CountTo :value="pvTotalCount"></CountTo>
                        </div>
                    </div>
                </el-card>
            </el-col>

        </el-row>

        <el-row :gutter="20" class="mt-5">
            <el-col :span="12" :offset="0">
                <ArticlePublishChart></ArticlePublishChart>
            </el-col>
            <el-col :span="12" :offset="0">
                <PVChart></PVChart>
            </el-col>
        </el-row>

        <el-row :gutter="20" class="mt-5">
            <el-col :span="12" :offset="0">
                <CategoryArticleChart></CategoryArticleChart>
            </el-col>
            <el-col :span="12" :offset="0">
                <TagArticleChart></TagArticleChart>
            </el-col>
        </el-row>

    </div>


</template>

<script setup>
import CountTo from '@/components/CountTo.vue'
import ArticlePublishChart from '@/components/ArticlePublishChart.vue'
import PVChart from '@/components/PVChart.vue'
import CategoryArticleChart from '@/components/CategoryArticleChart.vue'
import TagArticleChart from '@/components/TagArticleChart.vue'
import { ref } from 'vue'
import { getDashboardArticleStatisticsInfo } from '@/api/admin/dashboard'


const articleTotalCount = ref(0)
const categoryTotalCount = ref(0)
const tagTotalCount = ref(0)
const pvTotalCount = ref(0)

getDashboardArticleStatisticsInfo().then((e) => {
    if (e.success) {
        // 使用新的字段名（与VO中的字段名一致）
        articleTotalCount.value = e.data.articleTotalCount || 0
        categoryTotalCount.value = e.data.categoryTotalCount || 0
        tagTotalCount.value = e.data.tagTotalCount || 0
        pvTotalCount.value = e.data.pvTotalCount || 0
    }
})


</script>

<style scope>
.w-10 {
    width: 2.5rem !important;
}

.h-10 {
    height: 2.5rem !important;
}
</style>
