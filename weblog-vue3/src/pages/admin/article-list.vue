<template>

    <el-card :body-style="{ padding: '20px' }" class="mb-5 border-1">
        <!-- card body -->
        <el-text class="mx-1 mr-3">文章标题</el-text>
        <el-input v-model="searchTitle" placeholder="请输入（模糊查询）" class="w-50 mr-5" />

        <el-text class="mx-1 mr-3">发布日期</el-text>
        <el-date-picker style="top: 3px" v-model="pickDate" type="daterange" range-separator="至"
            start-placeholder="开始时间" end-placeholder="结束时间" :shortcuts="shortcuts" size="default"
            @change="datepickerChange" />

        <el-button type="primary" class="ml-3" :icon="Search" @click="getTableData">查询</el-button>
        <el-button class="ml-3" :icon="RefreshRight" @click="reset">重置</el-button>
    </el-card>


    <el-card class="border-1">
        <!-- card body -->
        <!-- 新增按钮 -->
        <div>
            <el-button type="primary" @click="showArticlePublishEditor">
                <el-icon class="mr-1">
                    <EditPen />
                </el-icon>
                写文章</el-button>
        </div>

        <div class="table-wrapper">
            <el-table :data="tableData" stripe style="width: 100%" class="mt-4" v-loading="tableLoading">
                <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip>
                    <template #default="scope">
                        <span class="title-cell">
                            <el-tag v-if="scope.row.isTop === 1" size="small" type="warning" style="margin-right:4px; vertical-align: middle;">置顶</el-tag>
                            {{ scope.row.title }}
                        </span>
                    </template>
                </el-table-column>
                <el-table-column label="预览图" width="120">
                    <template #default="scope">
                        <el-image style="width: 50px;" :src="scope.row.titleImage" />
                    </template>
                </el-table-column>
                <el-table-column label="创建时间" width="170">
                    <template #default="{ row }">
                        {{ row.createTime ? moment(row.createTime).format('YYYY-MM-DD HH:mm:ss') : '-' }}
                    </template>
                </el-table-column> <el-table-column label="操作" width="380">
                    <template #default="scope">
                        <el-button size="small" :type="scope.row.isTop === 1 ? 'warning' : 'default'"
                            @click="toggleTop(scope.row)">
                            <el-icon class="mr-1">
                                <Top />
                            </el-icon>
                            {{ scope.row.isTop === 1 ? '已置顶' : '置顶' }}</el-button>
                        <el-button size="small" @click="showArticleUpdateEditorShow(scope.row)">
                            <el-icon class="mr-1">
                                <Edit />
                            </el-icon>
                            编辑</el-button>
                        <el-button size="small" @click="previewArticle(scope.row)">
                            <el-icon class="mr-1">
                                <View />
                            </el-icon>
                            预览</el-button>
                        <el-button type="danger" size="small" :loading="deletingId === scope.row.id"
                            :disabled="deletingId !== null && deletingId !== scope.row.id"
                            @click="deleteArticleSubmit(scope.row)">
                            <el-icon class="mr-1">
                                <Delete />
                            </el-icon>
                            删除
                        </el-button>
                    </template>
                </el-table-column>
            </el-table>
        </div>

        <div class="mt-5 flex item-center justify-center">
            <el-pagination v-model:current-page="current" v-model:page-size="size" :page-sizes="[10, 20, 50]"
                :small="small" :disabled="disabled" background="true" layout="total, sizes, prev, pager, next, jumper"
                :total="total" @size-change="handleSizeChange" @current-change="getTableData" />
        </div>
    </el-card>

    <!-- 写博客 -->
    <el-dialog v-model="isArticlePublishEditorShow" fullscreen="true" :show-close="false" :modal="false">

        <template #header="{ close, titleId, titleClass }">
            <div class="">
                <div class="my-header flex justify-between">
                    <h4 class="font-bold">写文章</h4>
                    <div>
                        <el-button @click="isArticlePublishEditorShow = false">取消</el-button>
                        <el-button type="primary" @click="onSubmit" :loading="publishLoading">
                            <el-icon class="mr-1">
                                <Promotion />
                            </el-icon>
                            {{ publishLoading ? '发布中...' : '发布' }}
                        </el-button>
                    </div>
                </div>
            </div>
        </template>
        <el-form :model="form" ref="publishArticleFormRef" label-position="top" :size="large" :rules="rules">
            <el-form-item label="标题" prop="title">
                <el-input v-model="form.title" autocomplete="off" size="large" maxlength="40" show-word-limit
                    clearable />
            </el-form-item>
            <el-form-item label="内容" prop="content">
                <MdEditor v-model="form.content" @onUploadImg="onUploadImg" editorId="publishArticleEditor" />
            </el-form-item>
            <el-form-item label="封面" prop="titleImage">
                <el-upload class="avatar-uploader" action="#" :on-change="handleTitleImageChange" :auto-upload="false"
                    :show-file-list="false" :on-success="handleAvatarSuccess">
                    <img v-if="form.titleImage" :src="form.titleImage" class="avatar" />
                    <el-icon v-else class="avatar-uploader-icon">
                        <Plus />
                    </el-icon>
                </el-upload>
            </el-form-item>
            <el-form-item label="摘要" prop="description">
                <el-input v-model="form.description" :rows="3" type="textarea" placeholder="请输入文章摘要" />
            </el-form-item>
            <el-form-item label="分类" prop="categoryId">
                <el-select v-model="form.categoryId" clearable placeholder="---请选择---" size="large" :teleported="false"
                    popper-class="dialog-select-popper">
                    <el-option v-for="item in categories" :key="item.value" :label="item.label" :value="item.value" />
                </el-select>
            </el-form-item>
            <el-form-item label="标签" prop="tags">
                <!-- 标签选择 -->
                <el-select v-model="form.tags" multiple filterable remote reserve-keyword placeholder="---请输入---"
                    remote-show-suffix :remote-method="remoteMethod" allow-create default-first-option
                    :loading="tagSelectLoading" size="large" :teleported="false" popper-class="dialog-select-popper">
                    <el-option v-for="item in options" :key="item.value" :label="item.label" :value="item.value" />
                </el-select>
            </el-form-item>

        </el-form>
    </el-dialog>

    <!-- 编辑博客 -->
    <el-dialog v-model="isArticleUpdateEditorShow" fullscreen="true" :show-close="false" :modal="false">
        <template #header="{ close, titleId, titleClass }">
            <div class="my-header flex items-center justify-between">
                <h4 class="font-bold">编辑文章</h4>
                <div>
                    <el-button @click="isArticleUpdateEditorShow = false">取消</el-button>
                    <el-button type="primary" @click="updateSubmit" :loading="updateLoading">
                        <el-icon class="mr-1">
                            <Promotion />
                        </el-icon>
                        {{ updateLoading ? '提交中...' : '提交' }}
                    </el-button>
                </div>
            </div>
        </template>
        <el-form :model="form" ref="updateArticleFormRef" label-position="top" :size="large" :rules="rules">
            <el-form-item label="标题" prop="title">
                <el-input v-model="form.title" autocomplete="off" size="large" maxlength="40" show-word-limit
                    clearable />
            </el-form-item>
            <el-form-item label="内容" prop="content">
                <!-- <MDEditor :content="form.content" @event="handleMd"></MDEditor> -->
                <MdEditor v-model="form.content" @onUploadImg="onUploadImg" editorId="updateArticleEditor" />
            </el-form-item>
            <el-form-item label="封面" prop="titleImage">
                <el-upload class="avatar-uploader" action="#" :on-change="handleTitleImageChange" :auto-upload="false"
                    :show-file-list="false" :on-success="handleAvatarSuccess">
                    <img v-if="form.titleImage" :src="form.titleImage" class="avatar" />
                    <el-icon v-else class="avatar-uploader-icon">
                        <Plus />
                    </el-icon>
                </el-upload>
            </el-form-item>
            <el-form-item label="摘要" prop="description">
                <el-input v-model="form.description" :rows="3" type="textarea" placeholder="请输入文章摘要" />
            </el-form-item>
            <el-form-item label="分类" prop="categoryId">
                <el-select v-model="form.categoryId" clearable placeholder="---请选择---" size="large" :teleported="false"
                    popper-class="dialog-select-popper">
                    <el-option v-for="item in categories" :key="item.value" :label="item.label" :value="item.value" />
                </el-select>
            </el-form-item>
            <el-form-item label="标签" prop="tags">
                <!-- 标签选择 -->
                <el-select v-model="form.tags" multiple filterable remote reserve-keyword placeholder="---请输入---"
                    remote-show-suffix :remote-method="remoteMethod" allow-create default-first-option
                    :loading="tagSelectLoading" size="large" :teleported="false" popper-class="dialog-select-popper">
                    <el-option v-for="item in options" :key="item.value" :label="item.label" :value="item.value" />
                </el-select>
            </el-form-item>
        </el-form>
    </el-dialog>
</template>

<script setup>
import { ref, reactive } from 'vue';
import { publishArticle, getArticlePageList, deleteArticle, getArticleDetail, updateArticle, toggleArticleTop } from '@/api/admin/article'
import { uploadFile } from '@/api/admin/file'
import MdEditor from 'md-editor-v3'
import 'md-editor-v3/lib/style.css'
import { showMessage } from '@/composables/util'
import { useRouter } from 'vue-router'
import { getCategorySelect } from '@/api/admin/category'
import { selectTags, getTagSelect } from '@/api/admin/tag'
import moment from 'moment';
import { Search, RefreshRight, Top } from '@element-plus/icons-vue'

const router = useRouter()

const isArticlePublishEditorShow = ref(false)
const isArticleUpdateEditorShow = ref(false)
const tableLoading = ref(false)
const publishLoading = ref(false)
const updateLoading = ref(false)
const deletingId = ref(null)

const searchTitle = ref('')
const pickDate = ref('')
const startDate = reactive({})
const endDate = reactive({})

const reset = () => {
    pickDate.value = ''
    startDate.value = null
    endDate.value = null
    searchTitle.value = ''
}

const datepickerChange = (e) => {
    startDate.value = moment(e[0]).format('YYYY-MM-DD HH:mm:ss')
    endDate.value = moment(e[1]).format('YYYY-MM-DD HH:mm:ss')
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

const handleTitleImageChange = (file) => {
    console.log('开始上传文件')
    console.log(file)
    let formData = new FormData()
    formData.append("file", file.raw);
    uploadFile(formData).then((e) => {
        if (e.success == false) {
            let message = e.message
            showMessage(message, 'warning', 'message')
            return
        }
        form.titleImage = e.data.url
        showMessage('文章题图上传成功', 'success', 'message')
    })
}

const showArticleUpdateEditorShow = (row) => {
    resetArticleForm()
    isArticleUpdateEditorShow.value = true
    let articleId = row.id
    getArticleDetail(articleId).then((e) => {
        if (e.success == true) {
            form.id = e.data.id
            form.title = e.data.title
            form.content = e.data.content
            form.titleImage = e.data.titleImage
            form.categoryId = e.data.categoryId
            form.tags = e.data.tagIds
            form.description = e.data.description
        } else {
            showMessage(e.message || '获取文章详情失败', 'warning', 'message')
        }
    })
}

const onUploadImg = async (files, callback) => {
    const res = await Promise.all(
        files.map((file) => {
            return new Promise((rev, rej) => {
                console.log('==> 开始上传文件...')
                let formData = new FormData()
                formData.append("file", file);
                uploadFile(formData).then((res) => {
                    console.log(res)
                    console.log('访问路径：' + res.data.url)
                    callback([res.data.url]);
                })
            });
        })
    );
}

const previewArticle = (row) => {
    // 打开一个新页面
    let routeData = router.resolve({ path: '/article/detail', query: { articleId: row.id } });
    window.open(routeData.href, '_blank');
}

const form = reactive({
    id: null,
    title: '',
    content: '',
    titleImage: '',
    categoryId: null,
    tags: [],
    description: ""
})

// 重置表单（打开发布/编辑弹窗前、提交成功后调用），覆盖全部 7 个字段 + 清除校验错误态
const resetArticleForm = () => {
    form.id = null
    form.title = ''
    form.content = ''
    form.titleImage = ''
    form.categoryId = null
    form.tags = []
    form.description = ''
    publishArticleFormRef.value?.clearValidate()
    updateArticleFormRef.value?.clearValidate()
}

// 打开发布弹窗：先重置表单再打开，兜底覆盖所有"未重置关闭"路径（取消/ESC）
const showArticlePublishEditor = () => {
    resetArticleForm()
    isArticlePublishEditorShow.value = true
}


const publishArticleFormRef = ref(null)
const updateArticleFormRef = ref(null)
const rules = {
    title: [
        { required: true, message: '请输入文章标题', trigger: 'blur' },
        { min: 1, max: 40, message: '文章标题要求大于1个字符，小于40个字符', trigger: 'blur' },
    ],
    content: [{ required: true }],
    titleImage: [{ required: true }],
    categoryId: [{ required: true, message: '请选择文章分类', trigger: 'blur' }],
    tags: [{ required: true, message: '请选择文章标签', trigger: 'blur' }],
    description: [{ required: true, message: '请输入文章摘要', trigger: 'blur' }],
}

const tableData = ref([])
// 当前页码
const current = ref(1)
const total = ref(0)
const size = ref(10)

// 获取分页数据
function getTableData() {
    console.log('获取分页数据')
    tableLoading.value = true
    getArticlePageList({ current: current.value, size: size.value, startDate: startDate.value, endDate: endDate.value, searchTitle: searchTitle.value })
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


const onSubmit = () => {
    if (publishLoading.value) return
    publishArticleFormRef.value.validate((valid) => {
        if (!valid) {
            return false
        }
        publishLoading.value = true
        publishArticle(form).then((e) => {
            if (e.success == false) {
                showMessage(e.message, 'warning', 'message')
                return
            }
            resetArticleForm()
            isArticlePublishEditorShow.value = false
            showMessage('发布成功', 'success', 'message')
            getTableData()
        }).finally(() => {
            publishLoading.value = false
        })
    })
}

const updateSubmit = () => {
    if (updateLoading.value) return
    updateArticleFormRef.value.validate((valid) => {
        if (!valid) {
            return false
        }
        updateLoading.value = true
        updateArticle(form).then((e) => {
            if (e.success == false) {
                showMessage(e.message, 'warning', 'message')
                return
            }
            resetArticleForm()
            isArticleUpdateEditorShow.value = false
            showMessage('修改成功', 'success', 'message')
            getTableData()
        }).finally(() => {
            updateLoading.value = false
        })
    })
}

const deleteArticleSubmit = (row) => {
    ElMessageBox.confirm(
        '是否确认要删除该文章?',
        '提示',
        {
            confirmButtonText: '确认',
            cancelButtonText: '取消',
            type: 'warning',
        }
    )
        .then(() => {
            deletingId.value = row.id
            deleteArticle(row.id).then((e) => {
                if (e.success == true) {
                    showMessage('删除成功', 'success')
                    getTableData()
                } else {
                    let message = e.message
                    showMessage(message, 'warning')
                }
            }).finally(() => {
                deletingId.value = null
            })
        })
        .catch(() => { })
}

const toggleTop = (row) => {
    toggleArticleTop(row.id).then((e) => {
        if (e.success == true) {
            showMessage(row.isTop === 1 ? '已取消置顶' : '已置顶', 'success')
            getTableData()
        } else {
            showMessage(e.message || '操作失败', 'warning')
        }
    })
}

// 文章分类
const categories = ref([])
getCategorySelect().then((e) => {
    console.log('获取分类数据')
    console.log(e)
    categories.value = e.data
})

// 文章标签
const tagSelectLoading = ref(false)
const options = ref([])
getTagSelect().then((e) => {
    console.log('获取标签数据')
    console.log(e)
    options.value = e.data
})

const remoteMethod = (query) => {
    console.log('远程搜索')
    console.log(options.value)
    if (query) {
        tagSelectLoading.value = true
        setTimeout(() => {
            tagSelectLoading.value = false
            selectTags(query).then((e) => {
                if (e.success) {
                    options.value = e.data
                }
            })
        }, 200)
    }
}
</script>


<style scoped>
.avatar-uploader .avatar {
    width: 278px;
    display: block;
}

.message {
    z-index: 9999 !important;
}

.title-cell {
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    display: block;
}

.table-wrapper {
    overflow-x: auto;
}
</style>

<style>
.w-50 {
    width: 12.5rem !important;
}

.mr-3 {
    margin-right: 0.75rem !important;
}

.avatar-uploader .el-upload {
    border: 1px dashed var(--el-border-color);
    border-radius: 6px;
    cursor: pointer;
    position: relative;
    overflow: hidden;
    transition: var(--el-transition-duration-fast);
}

.avatar-uploader .el-upload:hover {
    border-color: var(--el-color-primary);
}

.el-icon.avatar-uploader-icon {
    font-size: 28px;
    color: #8c939d;
    width: 178px;
    height: 178px;
    text-align: center;
}

.el-select--large {
    width: 600px;
}

.dialog-select-popper {
    z-index: 4000 !important;
}
</style>