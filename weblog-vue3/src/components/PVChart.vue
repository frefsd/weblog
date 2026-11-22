<script setup>
import DashboardChart from './DashboardChart.vue'
import { getDashboardPVStatisticsInfo } from '@/api/admin/dashboard'

function makeOption(rows, color) {
    return {
        grid: { top: 14, right: 26, bottom: 12, left: 7, containLabel: true },
        tooltip: { trigger: 'axis' },
        xAxis: {
            type: 'category',
            boundaryGap: false,
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
            name: '浏览量',
            type: 'line',
            data: rows.map(row => row.pv),
            smooth: true,
            symbol: 'circle',
            symbolSize: 6,
            itemStyle: { color: color.cyan },
            lineStyle: { width: 2.5 },
            areaStyle: {
                color: {
                    type: 'linear', x: 0, y: 0, x2: 0, y2: 1,
                    colorStops: [{ offset: 0, color: color.cyan + '48' }, { offset: 1, color: color.cyan + '04' }],
                },
            },
        }],
    }
}
</script>

<template>
    <DashboardChart title="访问趋势" caption="近 7 天 · PV" :fetch-data="getDashboardPVStatisticsInfo" :make-option="makeOption" />
</template>
