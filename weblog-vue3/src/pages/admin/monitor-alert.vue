<template>
    <el-card class="border-1">
        <el-table :data="tableData" stripe style="width: 100%" v-loading="tableLoading">
            <el-table-column label="规则名称" min-width="150">
                <template #default="{ row }">
                    <b>{{ row.ruleName || row.ruleId }}</b>
                </template>
            </el-table-column>
            <el-table-column prop="logLevel" label="日志级别" width="100">
                <template #default="{ row }">
                    <el-tag :type="row.logLevel === 'ERROR' ? 'danger' : row.logLevel === 'WARN' ? 'warning' : 'info'"
                        size="small">
                        {{ row.logLevel }}
                    </el-tag>
                </template>
            </el-table-column>
            <el-table-column prop="triggerCount" label="触发数量" width="100">
                <template #default="{ row }">
                    <span style="color: var(--el-color-danger); font-weight: 600;">{{ row.triggerCount }}</span>
                </template>
            </el-table-column>
            <el-table-column prop="threshold" label="阈值" width="70">
                <template #default="{ row }">
                    {{ row.threshold }}
                </template>
            </el-table-column>
            <el-table-column label="通知状态" width="100">
                <template #default="{ row }">
                    <el-tag :type="row.notifyStatus === 1 ? 'success' : 'danger'" size="small">
                        {{ row.notifyStatus === 1 ? '已通知' : '未通知' }}
                    </el-tag>
                </template>
            </el-table-column>
            <el-table-column prop="errorMessage" label="错误消息" min-width="240" show-overflow-tooltip>
                <template #default="{ row }">
                    <span class="text-sm">{{ row.errorMessage || '-' }}</span>
                </template>
            </el-table-column>
            <el-table-column label="触发时间" width="170">
                <template #default="{ row }">
                    {{ row.createTime ? moment(row.createTime).format('YYYY-MM-DD HH:mm:ss') : '-' }}
                </template>
            </el-table-column>
        </el-table>

        <div class="mt-5 flex item-center justify-center">
            <el-pagination v-model:current-page="current" v-model:page-size="size" :page-sizes="[10, 20, 50]"
                :small="small" :disabled="disabled" background="true" layout="total, sizes, prev, pager, next, jumper"
                :total="total" @size-change="handleSizeChange" @current-change="getTableData" />
        </div>
    </el-card>
</template>

<script setup>
import { ref } from 'vue'
import { getAlertPageList } from '@/api/admin/monitor'
import moment from 'moment'

const tableLoading = ref(false)
const tableData = ref([])
const current = ref(1)
const total = ref(0)
const size = ref(10)

function getTableData() {
    tableLoading.value = true
    getAlertPageList({ current: current.value, size: size.value })
        .then((res) => {
            if (res.success == true) {
                tableData.value = res.data.records
                current.value = res.data.current
                total.value = res.data.total
                size.value = res.data.size
            }
        }).finally(() => {
            tableLoading.value = false
        })
}
getTableData()

const handleSizeChange = (e) => {
    size.value = e
    getTableData()
}
</script>
