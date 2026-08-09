<template>
    <el-card :body-style="{ padding: '20px' }" class="mb-5 border-1">
        <el-text class="mx-1 mr-3">日志级别</el-text>
        <el-select v-model="searchLevel" placeholder="全部" clearable class="search-select mr-5">
            <el-option label="ERROR" value="ERROR" />
            <el-option label="WARN" value="WARN" />
            <el-option label="INFO" value="INFO" />
        </el-select>

        <el-text class="mx-1 mr-3">日志类型</el-text>
        <el-select v-model="searchType" placeholder="全部" clearable class="search-select mr-5">
            <el-option label="HTTP请求" value="HTTP_REQUEST" />
            <el-option label="外部API" value="EXTERNAL_API" />
            <el-option label="业务日志" value="BUSINESS" />
        </el-select>

        <el-button type="primary" class="ml-3" :icon="Search" @click="getTableData">查询</el-button>
        <el-button class="ml-3" :icon="RefreshRight" @click="reset">重置</el-button>
        <el-button type="danger" class="ml-3" :icon="Delete" @click="clearLogs">清空</el-button>
    </el-card>

    <el-card class="border-1">
        <el-table :data="tableData" stripe style="width: 100%" v-loading="tableLoading">
            <el-table-column type="expand">
                <template #default="{ row }">
                    <div class="p-4">
                        <div class="mb-2">
                            <span class="text-gray-500 text-sm">错误消息：</span>
                            <span class="text-red-500 text-sm">{{ row.errorMessage || '-' }}</span>
                        </div>
                        <div class="mb-2">
                            <span class="text-gray-500 text-sm">请求耗时：</span>
                            <span class="text-sm">{{ row.duration }}ms</span>
                        </div>
                        <div v-if="row.stackTrace">
                            <span class="text-gray-500 text-sm">堆栈跟踪：</span>
                            <el-input type="textarea" :model-value="row.stackTrace" readonly rows="6" class="mt-1"
                                style="font-family: monospace; font-size: 12px;" />
                        </div>
                    </div>
                </template>
            </el-table-column>
            <el-table-column prop="level" label="级别" width="90">
                <template #default="{ row }">
                    <el-tag :type="row.level === 'ERROR' ? 'danger' : row.level === 'WARN' ? 'warning' : 'info'"
                        size="small">
                        {{ row.level }}
                    </el-tag>
                </template>
            </el-table-column>
            <el-table-column prop="type" label="类型" width="110">
                <template #default="{ row }">
                    <el-tag type="info" size="small">
                        {{ row.type === 'HTTP_REQUEST' ? 'HTTP请求' : row.type === 'EXTERNAL_API' ? '外部API' : '业务日志' }}
                    </el-tag>
                </template>
            </el-table-column>
            <el-table-column prop="method" label="方法" min-width="220" show-overflow-tooltip>
                <template #default="{ row }">
                    <span style="font-family: monospace; font-size: 12px;">{{ row.method }}</span>
                </template>
            </el-table-column>
            <el-table-column prop="uri" label="URI" width="160" show-overflow-tooltip>
                <template #default="{ row }">
                    <span style="font-family: monospace; font-size: 12px;">{{ row.uri }}</span>
                </template>
            </el-table-column>
            <el-table-column prop="duration" label="耗时" width="80">
                <template #default="{ row }">
                    {{ row.duration }}ms
                </template>
            </el-table-column>
            <el-table-column prop="ip" label="IP" width="140" />
            <el-table-column label="时间" width="170">
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
import { getLogPageList, clearAllLogs } from '@/api/admin/monitor'
import { showMessage } from '@/composables/util'
import moment from 'moment'
import { Search, RefreshRight, Delete } from '@element-plus/icons-vue'

const searchLevel = ref('')
const searchType = ref('')

const reset = () => {
    searchLevel.value = ''
    searchType.value = ''
}

const clearLogs = () => {
    ElMessageBox.confirm(
        '清空后将删除所有日志记录，此操作不可恢复，是否确认清空?',
        '警告',
        {
            confirmButtonText: '确认清空',
            cancelButtonText: '取消',
            type: 'warning',
        }
    ).then(() => {
        clearAllLogs().then((e) => {
            if (e.success == true) {
                showMessage('日志已清空', 'success')
                getTableData()
            } else {
                showMessage(e.message, 'warning')
            }
        })
    }).catch(() => { })
}

const tableLoading = ref(false)
const tableData = ref([])
const current = ref(1)
const total = ref(0)
const size = ref(10)

function getTableData() {
    tableLoading.value = true
    getLogPageList({
        current: current.value,
        size: size.value,
        level: searchLevel.value,
        type: searchType.value
    })
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

<style scoped>
.search-select {
    --el-select-width: 140px;
}
</style>
