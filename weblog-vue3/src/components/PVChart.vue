<template>
    <div>
        <el-card shadow="never" class="border-1">
            <template #header>
                <div class="flex justify-between">
                    <span class="text-sm">PV 访问量统计</span>
                </div>
            </template>
            <!-- card body -->
            <div id="pvChart" style="width: 100%; height: 300px;">

            </div>
        </el-card>

    </div>
</template>

<script setup>
import { onMounted } from 'vue'
import * as echarts from 'echarts'
import { getDashboardPVStatisticsInfo } from '@/api/admin/dashboard'

var myChart = null

onMounted(() => {
    getDashboardPVStatisticsInfo().then((e) => {
        var chartDom = document.getElementById('pvChart');
        myChart = echarts.init(chartDom);
        var option;


        if (e.success) {
            let dataList = e.data
            let dates = dataList.map(item => item.date)
            let pvs = dataList.map(item => item.pv)

            option = {
                tooltip: {
                    trigger: 'axis'
                },
                xAxis: {
                    type: 'category',
                    data: dates,
                    axisLabel: {
                        rotate: 45
                    }
                },
                yAxis: {
                    type: 'value'
                },
                series: [
                    {
                        name: 'PV',
                        data: pvs,
                        type: 'line',
                        smooth: true,
                        areaStyle: {
                            opacity: 0.3
                        },
                        itemStyle: {
                            color: '#409EFF'
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
