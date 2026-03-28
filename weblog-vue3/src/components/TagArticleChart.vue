<template>
    <div>
        <el-card shadow="never" class="border-1">
            <template #header>
                <div class="flex justify-between">
                    <span class="text-sm">文章标签统计（柱状图）</span>
                </div>
            </template>
            <!-- card body -->
            <div id="tagChart" style="width: 100%; height: 300px;">

            </div>
        </el-card>

    </div>
</template>

<script setup>
import { onMounted } from 'vue'
import * as echarts from 'echarts'
import { getTagArticleStatistics } from '@/api/admin/dashboard'

var myChart = null

onMounted(() => {
    getTagArticleStatistics().then((e) => {
        var chartDom = document.getElementById('tagChart');
        myChart = echarts.init(chartDom);
        var option;

        if (e.success) {
            let dataList = e.data

            // 过滤掉文章数量为 0 的标签
            let filteredData = dataList.filter(item => item.articleCount > 0)

            // 准备图表数据
            let tagNames = filteredData.map(item => item.tagName)
            let articleCounts = filteredData.map(item => item.articleCount)

            option = {
                tooltip: {
                    trigger: 'axis',
                    axisPointer: {
                        type: 'shadow'
                    }
                },
                grid: {
                    left: '3%',
                    right: '4%',
                    bottom: '15%',
                    top: '3%',
                    containLabel: true
                },
                xAxis: {
                    type: 'category',
                    data: tagNames,
                    axisLabel: {
                        rotate: 45,
                        interval: 0
                    }
                },
                yAxis: {
                    type: 'value'
                },
                series: [
                    {
                        name: '文章数量',
                        type: 'bar',
                        barWidth: '50%',
                        data: articleCounts,
                        itemStyle: {
                            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                                { offset: 0, color: '#83bff6' },
                                { offset: 0.5, color: '#188df0' },
                                { offset: 1, color: '#188df0' }
                            ])
                        }
                    }
                ]
            };

            option && myChart.setOption(option);
        }
    })

    // 窗口大小变化时重新渲染
    window.addEventListener('resize', () => {
        myChart && myChart.resize();
    });
})
</script>
