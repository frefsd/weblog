<template>
    <div>
        <el-card shadow="never" class="border-1">
            <template #header>
                <div class="flex justify-between">
                    <span class="text-sm">文章分类统计（扇形图）</span>
                </div>
            </template>
            <!-- card body -->
            <div id="categoryChart" style="width: 100%; height: 300px;">

            </div>
        </el-card>

    </div>
</template>

<script setup>
import { onMounted } from 'vue'
import * as echarts from 'echarts'
import { getCategoryArticleStatistics } from '@/api/admin/dashboard'

var myChart = null

onMounted(() => {
    getCategoryArticleStatistics().then((e) => {
        var chartDom = document.getElementById('categoryChart');
        myChart = echarts.init(chartDom);
        var option;

        if (e.success) {
            let dataList = e.data

            // 过滤掉文章数量为 0 的分类
            let filteredData = dataList.filter(item => item.articleCount > 0)

            // 准备图表数据
            let chartData = filteredData.map(item => {
                return {
                    name: item.categoryName,
                    value: item.articleCount
                }
            })

            option = {
                tooltip: {
                    trigger: 'item',
                    formatter: '{b}: {c}篇 ({d}%)'
                },
                legend: {
                    orient: 'horizontal',
                    bottom: '0',
                    left: 'center'
                },
                series: [
                    {
                        name: '文章数量',
                        type: 'pie',
                        radius: ['40%', '70%'],
                        avoidLabelOverlap: false,
                        itemStyle: {
                            borderRadius: 10,
                            borderColor: '#fff',
                            borderWidth: 2
                        },
                        label: {
                            show: false,
                            position: 'center'
                        },
                        emphasis: {
                            label: {
                                show: true,
                                fontSize: 18,
                                fontWeight: 'bold'
                            }
                        },
                        labelLine: {
                            show: false
                        },
                        data: chartData
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
