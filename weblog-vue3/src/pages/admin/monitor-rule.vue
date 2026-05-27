<template>
    <el-card class="border-1">
        <div>
            <el-button type="primary" @click="openRuleDialog()">
                <el-icon class="mr-1">
                    <Plus />
                </el-icon>
                新增规则
            </el-button>
        </div>

        <el-table :data="tableData" stripe style="width: 100%" class="mt-4" v-loading="tableLoading">
            <el-table-column prop="name" label="规则名称" min-width="160" />
            <el-table-column prop="logLevel" label="监控级别" width="100">
                <template #default="{ row }">
                    <el-tag :type="row.logLevel === 'ERROR' ? 'danger' : row.logLevel === 'WARN' ? 'warning' : 'info'"
                        size="small">
                        {{ row.logLevel }}
                    </el-tag>
                </template>
            </el-table-column>
            <el-table-column prop="timeWindow" label="时间窗口" width="120">
                <template #default="{ row }">
                    {{ row.timeWindow }} 分钟
                </template>
            </el-table-column>
            <el-table-column prop="threshold" label="阈值" width="80">
                <template #default="{ row }">
                    <b>{{ row.threshold }}</b> 条
                </template>
            </el-table-column>
            <el-table-column label="状态" width="80">
                <template #default="{ row }">
                    <el-switch v-model="row.status" :active-value="1" :inactive-value="0"
                        @change="(val) => handleStatusChange(row, val)" />
                </template>
            </el-table-column>
            <el-table-column label="更新时间" width="170">
                <template #default="{ row }">
                    {{ row.updateTime ? moment(row.updateTime).format('YYYY-MM-DD HH:mm:ss') : '-' }}
                </template>
            </el-table-column>
            <el-table-column label="操作" width="180">
                <template #default="scope">
                    <div class="flex gap-1">
                    <el-button type="primary" size="small" @click="openRuleDialog(scope.row)">
                        <el-icon class="mr-1">
                            <Edit />
                        </el-icon>
                        编辑
                    </el-button>
                    <el-button type="danger" size="small" @click="deleteRuleSubmit(scope.row)">
                        <el-icon class="mr-1">
                            <Delete />
                        </el-icon>
                        删除
                    </el-button>
                    </div>
                </template>
            </el-table-column>
        </el-table>
    </el-card>

    <!-- 新增/编辑规则弹窗 -->
    <el-dialog v-model="isRuleDialogShow" :title="editingRule ? '编辑告警规则' : '新增告警规则'" width="35%"
        :show-close="false" draggable>
        <el-form :model="form" ref="formRef" label-position="top" :size="large" :rules="rules">
            <el-form-item label="规则名称" prop="name">
                <el-input v-model="form.name" placeholder="例如：错误过多告警" maxlength="20" show-word-limit clearable />
            </el-form-item>
            <el-row :gutter="16">
                <el-col :span="12">
                    <el-form-item label="监控级别" prop="logLevel">
                        <el-select v-model="form.logLevel" class="w-full">
                            <el-option label="ERROR" value="ERROR" />
                            <el-option label="WARN" value="WARN" />
                            <el-option label="INFO" value="INFO" />
                        </el-select>
                    </el-form-item>
                </el-col>
                <el-col :span="12">
                    <el-form-item label="时间窗口（分钟）" prop="timeWindow">
                        <el-input-number v-model="form.timeWindow" :min="1" :max="1440" class="w-full" />
                    </el-form-item>
                </el-col>
            </el-row>
            <el-row :gutter="16">
                <el-col :span="12">
                    <el-form-item label="触发阈值" prop="threshold">
                        <el-input-number v-model="form.threshold" :min="1" :max="99999" class="w-full" />
                    </el-form-item>
                </el-col>
                <el-col :span="12">
                    <el-form-item label="启用状态">
                        <div class="flex items-center h-full pt-2">
                            <el-switch v-model="form.status" :active-value="1" :inactive-value="0" />
                            <span class="ml-2 text-sm text-gray-500">{{ form.status === 1 ? '已启用' : '已禁用' }}</span>
                        </div>
                    </el-form-item>
                </el-col>
            </el-row>
        </el-form>
        <template #footer>
            <span class="dialog-footer">
                <el-button @click="isRuleDialogShow = false">取消</el-button>
                <el-button type="primary" @click="saveRuleSubmit">保存</el-button>
            </span>
        </template>
    </el-dialog>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { getRuleList, saveRule, deleteRule } from '@/api/admin/monitor'
import { showMessage } from '@/composables/util'
import moment from 'moment'
import { Plus, Edit, Delete } from '@element-plus/icons-vue'

const isRuleDialogShow = ref(false)
const editingRule = ref(null)
const formRef = ref(null)

const form = reactive({
    name: '',
    logLevel: 'ERROR',
    timeWindow: 5,
    threshold: 5,
    status: 1
})

const rules = {
    name: [
        { required: true, message: '请输入规则名称', trigger: 'blur' },
        { min: 1, max: 20, message: '规则名称长度在 1 到 20 个字符', trigger: 'blur' },
    ],
    logLevel: [
        { required: true, message: '请选择监控级别', trigger: 'change' },
    ],
    timeWindow: [
        { required: true, message: '请输入时间窗口', trigger: 'blur' },
    ],
    threshold: [
        { required: true, message: '请输入触发阈值', trigger: 'blur' },
    ],
}

function openRuleDialog(row) {
    if (row) {
        editingRule.value = row
        form.name = row.name
        form.logLevel = row.logLevel
        form.timeWindow = row.timeWindow
        form.threshold = row.threshold
        form.status = row.status
    } else {
        editingRule.value = null
        form.name = ''
        form.logLevel = 'ERROR'
        form.timeWindow = 5
        form.threshold = 5
        form.status = 1
    }
    isRuleDialogShow.value = true
}

function saveRuleSubmit() {
    formRef.value.validate((valid) => {
        if (!valid) return

        const data = {
            name: form.name,
            logLevel: form.logLevel,
            timeWindow: form.timeWindow,
            threshold: form.threshold,
            status: form.status,
        }
        if (editingRule.value) {
            data.id = editingRule.value.id
        }

        saveRule(data).then((e) => {
            if (e.success == false) {
                showMessage(e.message, 'warning', 'message')
                return
            }
            showMessage(editingRule.value ? '更新成功' : '添加成功', 'success', 'message')
            isRuleDialogShow.value = false
            getTableData()
        })
    })
}

function handleStatusChange(row, val) {
    saveRule({
        id: row.id,
        name: row.name,
        logLevel: row.logLevel,
        timeWindow: row.timeWindow,
        threshold: row.threshold,
        status: val,
    }).then((e) => {
        if (e.success == false) {
            row.status = val === 1 ? 0 : 1
            showMessage(e.message, 'warning')
        }
    })
}

const tableLoading = ref(false)
const tableData = ref([])

function getTableData() {
    tableLoading.value = true
    getRuleList()
        .then((res) => {
            if (res.success == true) {
                tableData.value = res.data
            }
        }).finally(() => {
            tableLoading.value = false
        })
}
getTableData()

function deleteRuleSubmit(row) {
    ElMessageBox.confirm(
        '是否确认要删除该规则?',
        '提示',
        {
            confirmButtonText: '确认',
            cancelButtonText: '取消',
            type: 'warning',
        }
    )
        .then(() => {
            deleteRule(row.id).then((e) => {
                if (e.success == true) {
                    showMessage('删除成功', 'success')
                    getTableData()
                } else {
                    showMessage(e.message, 'warning')
                }
            })
        })
}
</script>

<style scoped>
.w-full {
    width: 100%;
}
</style>
