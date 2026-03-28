<template>
    <div>
        <el-card shadow="never" class="border-1">
            <template #header>
                <div class="flex justify-between">
                    <span class="text-sm">文章发布热点图</span>
                </div>
            </template>
            <!-- card body -->
            <div id="publishArticleChart" style="width: 100%; height: 300px;">

            </div>
        </el-card>

    </div>
</template>

<script setup>
import { onMounted } from 'vue';
import * as echarts from 'echarts';
import { getDashboardPublishArticleStatisticsInfo } from '@/api/admin/dashboard'

var myChart = null

onMounted(() => {
    getDashboardPublishArticleStatisticsInfo().then((e) => {
        if (e.success) {
            let dataList = e.data
            let myData = []
            dataList.forEach(item => {
                myData.push([
                    item.date,
                    item.count
                ]);
            })

            var chartDom = document.getElementById('publishArticleChart');
            myChart = echarts.init(chartDom);

            var option;

            option = {
                visualMap: {
                    show: false,
                    min: 0,
                    max: 10
                },
                calendar: {
                    top: 30,
                    left: 30,
                    right: 30,
                    bottom: 30,
                    itemSize: 18,
                    itemStyle: {
                        borderColor: '#fff',
                        borderWidth: 2,
                        shadowBlur: 10,
                        shadowColor: 'rgba(0, 0, 0, 0.2)'
                    },
                    monthLabel: {
                        nameMap: 'cn'
                    },
                    dayLabel: {
                        nameMap: 'cn'
                    },
                    range: [new Date().getTime() - 3600 * 24 * 1000 * 6, new Date().getTime()]
                },
                series: {
                    type: 'heatmap',
                    coordinateSystem: 'calendar',
                    data: myData,
                    label: {
                        show: true,
                        formatter: '{c}',
                        fontSize: 10,
                        color: '#333'
                    }
                }
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
