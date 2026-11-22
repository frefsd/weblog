<script setup>
import DashboardChart from './DashboardChart.vue'
import { getTagArticleStatistics } from '@/api/admin/dashboard'

async function loadTags() {
    const response = await getTagArticleStatistics()
    if (!response.success) return response
    return { ...response, data: (response.data ?? []).filter(row => row.articleCount > 0) }
}

function makeOption(rows, color) {
    const ordered = [...rows].sort((a, b) => b.articleCount - a.articleCount)
    const maxVisible = 7
    return {
        grid: { top: 8, right: ordered.length > maxVisible ? 24 : 13, bottom: 10, left: 6, containLabel: true },
        tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
        xAxis: {
            type: 'value',
            minInterval: 1,
            axisLabel: { color: color.muted },
            splitLine: { lineStyle: { color: color.line, type: 'dashed' } },
        },
        yAxis: {
            type: 'category',
            inverse: true,
            data: ordered.map(row => row.tagName),
            axisLabel: { color: color.muted, width: 88, overflow: 'truncate' },
            axisTick: { show: false },
            axisLine: { show: false },
        },
        dataZoom: ordered.length > maxVisible ? [
            { type: 'inside', yAxisIndex: 0, zoomOnMouseWheel: false, moveOnMouseWheel: true, startValue: 0, endValue: maxVisible - 1 },
            { type: 'slider', yAxisIndex: 0, startValue: 0, endValue: maxVisible - 1, right: 0, width: 12, showDetail: false, borderColor: color.line, fillerColor: color.violet + '44', handleSize: '95%' },
        ] : [],
        series: [{
            name: '文章数量',
            type: 'bar',
            data: ordered.map(row => row.articleCount),
            barMaxWidth: 14,
            itemStyle: { color: color.violet, borderRadius: [0, 3, 3, 0] },
        }],
    }
}
</script>

<template>
    <DashboardChart title="文章标签" :fetch-data="loadTags" :make-option="makeOption" />
</template>
