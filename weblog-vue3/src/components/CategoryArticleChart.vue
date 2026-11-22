<script setup>
import DashboardChart from './DashboardChart.vue'
import { getCategoryArticleStatistics } from '@/api/admin/dashboard'

async function loadCategories() {
    const response = await getCategoryArticleStatistics()
    if (!response.success) return response
    return { ...response, data: (response.data ?? []).filter(row => row.articleCount > 0) }
}

function makeOption(rows, color) {
    const total = rows.reduce((sum, row) => sum + row.articleCount, 0)
    return {
        color: [color.cyan, color.accent, color.violet, color.yellow],
        tooltip: { trigger: 'item', formatter: '{b} · {c} 篇 ({d}%)' },
        title: {
            text: String(total),
            subtext: '篇文章',
            left: 'center',
            top: '35%',
            textStyle: { color: color.text, fontSize: 26, fontWeight: 700 },
            subtextStyle: { color: color.muted },
        },
        legend: {
            type: 'scroll', left: 4, right: 4, bottom: 8,
            textStyle: { color: color.muted },
            pageTextStyle: { color: color.muted },
            pageIconColor: color.muted,
        },
        series: [{
            name: '文章数量',
            type: 'pie',
            radius: ['46%', '70%'],
            center: ['50%', '45%'],
            label: { show: false },
            itemStyle: { borderColor: color.panel, borderWidth: 2, borderRadius: 4 },
            data: rows.map(row => ({ name: row.categoryName, value: row.articleCount })),
        }],
    }
}
</script>

<template>
    <DashboardChart title="文章分类" :fetch-data="loadCategories" :make-option="makeOption" />
</template>
