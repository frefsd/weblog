<template>
    <el-card shadow="never" :body-style="{ padding: '20px' }">
        <el-form :model="form" label-width="160px" :rules="rules">
            <el-form-item label="博客名称" prop="blogName">
                <el-input v-model="form.blogName" clearable />
            </el-form-item>

            <el-form-item label="作者名" prop="author">
                <el-input v-model="form.author" clearable />
            </el-form-item>

            <el-form-item label="作者头像" prop="avatar">
                <el-upload class="avatar-uploader-container" action="#" :on-change="handleTitleImageChange"
                    :auto-upload="false" :show-file-list="false" :on-success="handleAvatarSuccess">
                    <img v-if="form.avatar" :src="form.avatar" class="avatar-img" />
                    <el-icon v-else class="avatar-uploader-icon">
                        <Plus />
                    </el-icon>
                </el-upload>
            </el-form-item>

            <el-form-item label="介绍语">
                <el-input v-model="form.introduction" type="textarea" />
            </el-form-item>

            <el-form-item label="开启 GihHub 访问">
                <el-switch v-model="isGithubCheck" inline-prompt :active-icon="Check" :inactive-icon="Close"
                    @change="githubSwitchChange" />
            </el-form-item>
            <el-form-item label="GitHub 主页访问地址" v-if="isGithubCheck">
                <el-input v-model="form.githubHome" clearable placeholder="请输入 GitHub 主页访问的 URL" />
            </el-form-item>

            <!-- <el-form-item label="开启 CSDN 访问">
                <el-switch v-model="isCSDNCheck" inline-prompt :active-icon="Check" :inactive-icon="Close"
                    @change="csdnSwitchChange" />
            </el-form-item>
            <el-form-item label="CSDN 主页访问地址" v-if="isCSDNCheck">
                <el-input v-model="form.csdnHome" clearable placeholder="请输入 CSDN 主页访问的 URL" />
            </el-form-item> -->

            <el-form-item label="开启 Gitee 访问">
                <el-switch v-model="isGiteeCheck" inline-prompt :active-icon="Check" :inactive-icon="Close"
                    @change="giteeSwitchChange" />
            </el-form-item>
            <el-form-item label="Gitee 主页访问地址" v-if="isGiteeCheck">
                <el-input v-model="form.giteeHome" clearable placeholder="请输入 Gitee 主页访问的 URL" />
            </el-form-item>

            <!-- <el-form-item label="开启知乎访问">
                <el-switch v-model="isZhihuCheck" inline-prompt :active-icon="Check" :inactive-icon="Close"
                    @change="zhihuSwitchChange" />
            </el-form-item>
            <el-form-item label="知乎主页访问地址" v-if="isZhihuCheck">
                <el-input v-model="form.zhihuHome" clearable placeholder="请输入知乎主页访问的 URL" />
            </el-form-item> -->
            <el-form-item>
                <el-button type="primary" @click="onSubmit">保存</el-button>
            </el-form-item>
        </el-form>
    </el-card>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { Check, Close, Plus } from '@element-plus/icons-vue' // 确保引入了 Plus 图标
import { uploadFile } from '@/api/admin/file'
import { showMessage } from '@/composables/util'
import { getBlogSettingDetail, updateBlogSetting } from '@/api/admin/blogsetting'

const isGithubCheck = ref(false)
const isCSDNCheck = ref(false)
const isGiteeCheck = ref(false)
const isZhihuCheck = ref(false)

const form = reactive({
    blogName: '',
    author: '',
    avatar: '',
    introduction: '',
    githubHome: '',
    giteeHome: '',
    csdnHome: '',
    zhihuHome: '',
})

const rules = {
    blogName: [{ required: true, message: '请输入博客名称', trigger: 'blur' }],
    author: [{ required: true, message: '请输入作者名称', trigger: 'blur' }],
    avatar: [{ required: true, message: '请选择作者头像', trigger: 'change' }], // 触发条件改为 change 更合适
}

const githubSwitchChange = (e) => {
    if (e == false) form.githubHome = ''
}
const csdnSwitchChange = (e) => {
    if (e == false) form.csdnHome = ''
}
const giteeSwitchChange = (e) => {
    if (e == false) form.giteeHome = ''
}
const zhihuSwitchChange = (e) => {
    if (e == false) form.zhihuHome = ''
}

const handleTitleImageChange = (file) => {
    console.log('开始上传文件', file)
    let formData = new FormData()
    formData.append("file", file.raw);

    uploadFile(formData).then((e) => {
        if (e.success == false) {
            showMessage(e.message || '上传失败', 'error', 'message')
            return
        }
        form.avatar = e.data.url
        showMessage('头像上传成功', 'success', 'message')
    }).catch(err => {
        console.error(err)
        showMessage('网络错误，上传失败', 'error', 'message')
    })
}

function initBlogSetting() {
    getBlogSettingDetail().then((e) => {
        if (e.success == true) {
            const data = e.data;
            form.blogName = data.blogName
            form.author = data.author
            form.avatar = data.avatar
            form.introduction = data.introduction

            // 简化逻辑判断
            isGithubCheck.value = !!data.githubHome
            form.githubHome = data.githubHome || ''

            isGiteeCheck.value = !!data.giteeHome
            form.giteeHome = data.giteeHome || ''

            isCSDNCheck.value = !!data.csdnHome
            form.csdnHome = data.csdnHome || ''

            isZhihuCheck.value = !!data.zhihuHome
            form.zhihuHome = data.zhihuHome || ''
        }
    })
}

initBlogSetting()

const onSubmit = () => {
    // 这里有个小 bug，form 里没有 content 字段，应该是打印 form 整体
    console.log('提交内容', form)

    updateBlogSetting(form).then((e) => {
        if (e.success == false) {
            showMessage(e.message || '更新失败', 'warning', 'message')
            return
        }
        showMessage('更新成功', 'success', 'message')
        initBlogSetting()
    })
}
</script>

<style scoped>
/* 
   核心修改区域 
   定义一个固定的容器大小，这里设置为 150x150，你可以根据需要调整 
*/
.avatar-uploader-container {
    width: 150px;
    height: 150px;
    border: 1px dashed #d9d9d9;
    border-radius: 6px;
    cursor: pointer;
    position: relative;
    overflow: hidden;
    transition: border-color 0.3s;
    display: flex;
    /* 使用 flex 布局让内容居中 */
    justify-content: center;
    align-items: center;
    background-color: #fbfbfb;
}

.avatar-uploader-container:hover {
    border-color: #409EFF;
}

/* 
   核心修改区域 
   控制图片样式：强制宽高 100% 填满容器，并使用 object-fit: cover 裁剪图片以防变形 
*/
.avatar-img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    /* 关键属性：保持比例填充，多余部分裁剪，不变形 */
    display: block;
}

/* 上传图标的大小也要和容器匹配 */
.avatar-uploader-icon {
    font-size: 28px;
    color: #8c939d;
    width: 100%;
    height: 100%;
    text-align: center;
    line-height: 150px;
    /* 如果不用 flex，这行负责垂直居中 */
}

/* 提示文字样式 */
.upload-tip {
    font-size: 12px;
    color: #909399;
    margin-top: 8px;
    line-height: 1.5;
}
</style>