<template>
    <el-card class="admin-filter-card">
        <el-form class="admin-filters" inline label-position="top" @submit.prevent="getTableData">
            <el-form-item label="分类名称">
                <el-input v-model="searchCategoryName" placeholder="输入关键词" class="filter-input" clearable />
            </el-form-item>
            <el-form-item label="创建日期">
                <el-date-picker v-model="pickDate" type="daterange" range-separator="至"
                    start-placeholder="开始日期" end-placeholder="结束日期" :shortcuts="shortcuts"
                    class="filter-date" popper-class="weblog-range-popper" @change="datepickerChange" />
            </el-form-item>
            <el-form-item class="filter-actions">
                <el-button type="primary" native-type="submit" :icon="Search">查询</el-button>
                <el-button :icon="RefreshRight" @click="reset">重置</el-button>
            </el-form-item>
        </el-form>
    </el-card>

    <el-card  class="border-1">
        <!-- card body -->
        <!-- 新增按钮 -->
        <div>
            <el-button type="primary" @click="isAddCatagoryDialogShow = true">
                <el-icon class="mr-1">
                    <Plus />
                </el-icon>
                新增</el-button>
        </div>

        <el-table :data="tableData" stripe style="width: 100%" class="mt-4" v-loading="tableLoading">
            <el-table-column prop="name" label="分类名称" width="180">

            </el-table-column>
            <el-table-column label="创建时间" width="180">
                <template #default="{ row }">
                    {{ row.createTime ? moment(row.createTime).format('YYYY-MM-DD HH:mm:ss') : '-' }}
                </template>
            </el-table-column> <el-table-column label="操作">
                <template #default="scope">
                    <el-button type="danger" size="small" @click="deleteCategorySubmit(scope.row)">
                        <el-icon class="mr-1">
                            <Delete />
                        </el-icon>
                        删除
                    </el-button>
                </template>
            </el-table-column>
        </el-table>

        <div class="mt-5 flex item-center justify-center">
            <el-pagination v-model:current-page="current" v-model:page-size="size" :page-sizes="[10, 20, 50]"
                :small="small" :disabled="disabled" background="true" layout="total, sizes, prev, pager, next, jumper"
                :total="total" @size-change="handleSizeChange" @current-change="getTableData" />
        </div>
    </el-card>

    <!-- 新增分类 -->
    <el-dialog v-model="isAddCatagoryDialogShow" title="新增分类" width="480px">
        <el-form :model="form" ref="formRef" label-position="top" size="large" :rules="rules">
            <el-form-item label="分类名称" prop="name">
                <el-input v-model="form.name" autocomplete="off" size="large" maxlength="10" show-word-limit
                    clearable />
            </el-form-item>
        </el-form>
        <template #footer>
            <span class="dialog-footer">
                <el-button @click="isAddCatagoryDialogShow = false">取消</el-button>
                <el-button type="primary" @click="addCategorySubmit">
                    提交
                </el-button>
            </span>
        </template>
    </el-dialog>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { addCategory, getCategoryPageList, deleteCategory } from '@/api/admin/category'
import { showMessage } from '@/composables/util'
import moment from 'moment';
import { Search, RefreshRight } from '@element-plus/icons-vue'

const isAddCatagoryDialogShow = ref(false)

const searchCategoryName = ref('')
const pickDate = ref('')
const startDate = reactive({})
const endDate = reactive({})

const reset = () => {
    pickDate.value = ''
    startDate.value = null
    endDate.value = null
    searchCategoryName.value = ''
}

const datepickerChange = (range) => {
    startDate.value = range?.[0] ? moment(range[0]).format('YYYY-MM-DD HH:mm:ss') : null
    endDate.value = range?.[1] ? moment(range[1]).format('YYYY-MM-DD HH:mm:ss') : null
}

const shortcuts = [
    {
        text: '最近一周',
        value: () => {
            const end = new Date()
            const start = new Date()
            start.setTime(start.getTime() - 3600 * 1000 * 24 * 7)
            return [start, end]
        },
    },
    {
        text: '最近一个月',
        value: () => {
            const end = new Date()
            const start = new Date()
            start.setTime(start.getTime() - 3600 * 1000 * 24 * 30)
            return [start, end]
        },
    },
    {
        text: '最近三个月',
        value: () => {
            const end = new Date()
            const start = new Date()
            start.setTime(start.getTime() - 3600 * 1000 * 24 * 90)
            return [start, end]
        },
    },
]

const form = reactive({
    name: ''
})

const formRef = ref(null)
const rules = {
    name: [
        { required: true, message: '请输入分类名称', trigger: 'blur' },
        { min: 1, max: 10, message: '分类名称字数要求大于1个字符，小于20个字符', trigger: 'blur' },
    ]
}

const addCategorySubmit = () => {
    addCategory(form).then((e) => {
        console.log(e)
        if (e.success == false) {
            var message = e.message
            showMessage(message, 'warning', 'message')
            return
        }

        showMessage('添加成功', 'success', 'message')
        isAddCatagoryDialogShow.value = false
        getTableData()
    })
}

const tableLoading = ref(false)
const tableData = ref([])
// 当前页码
const current = ref(1)
const total = ref(0)
const size = ref(10)

// 获取分页数据
function getTableData() {
    console.log('获取分页数据')
    tableLoading.value = true
    getCategoryPageList({ current: current.value, size: size.value, startDate: startDate.value, endDate: endDate.value, categoryName: searchCategoryName.value })
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
    console.log('选择的页码' + e)
    size.value = e
    getTableData()
}

const deleteCategorySubmit = (row) => {
    ElMessageBox.confirm(
        '是否确认要删除该分类?',
        '提示',
        {
            confirmButtonText: '确认',
            cancelButtonText: '取消',
            type: 'warning',
        }
    )
        .then(() => {
            deleteCategory(row.id).then((e) => {
                if (e.success == true) {
                    showMessage('删除成功', 'success')
                    getTableData()
                } else {
                    let message = e.message
                    showMessage(message, 'warning')
                }
            })

        })
}
</script>

<style></style>