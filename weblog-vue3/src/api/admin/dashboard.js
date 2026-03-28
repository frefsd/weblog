import axios from "@/axios"

// 获取文章统计信息（总数、今日发布等）
export function getDashboardArticleStatisticsInfo() {
    return axios.post("/admin/dashboard/article/statistics")
}

// 获取发布文章统计信息（最近 7 天）
export function getDashboardPublishArticleStatisticsInfo() {
    return axios.post("/admin/dashboard/publishArticle/statistics")
}

// 获取 PV 统计信息（最近 7 天）
export function getDashboardPVStatisticsInfo() {
    return axios.post("/admin/dashboard/pv/statistics")
}

// 获取文章分类统计（扇形图）
export function getCategoryArticleStatistics() {
    return axios.post("/admin/dashboard/category/statistics")
}

// 获取文章标签统计（柱状图）
export function getTagArticleStatistics() {
    return axios.post("/admin/dashboard/tag/statistics")
}
