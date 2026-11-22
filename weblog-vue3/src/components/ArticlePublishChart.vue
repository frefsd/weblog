<script setup>
import DashboardChart from './DashboardChart.vue'
import { getDashboardPublishArticleStatisticsInfo } from '@/api/admin/dashboard'

function makeOption(rows, color) {
    return {
        grid: { top: 14, right: 8, bottom: 12, left: 6, containLabel: true },
        tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
        xAxis: {
            type: 'category',
            data: rows.map(row => row.date),
            axisTick: { show: false },
            axisLine: { lineStyle: { color: color.line } },
            axisLabel: { color: color.muted, formatter: date => date.slice(-5), hideOverlap: true },
        },
        yAxis: {
            type: 'value',
            minInterval: 1,
            axisLabel: { color: color.muted },
            splitLine: { lineStyle: { color: color.line, type: 'dashed' } },
        },
        series: [{
            name: '发布文章',
            type: 'bar',
            data: rows.map(row => row.count),
            barMaxWidth: 24,
            itemStyle: { color: color.accent, borderRadius: [4, 4, 0, 0] },
        }],
    }
}
</script>

<template>
    <DashboardChart title="发布动态" caption="近 7 天 · 篇" :fetch-data="getDashboardPublishArticleStatisticsInfo" :make-option="makeOption" />
</template>
