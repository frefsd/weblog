<template>
    <div class="meun shadow-md fixed bg-light-50 transition-all duration-300"
        :style="{ width: $store.state.menuWidth }">
        <div class="flex items-center justify-center h-[64px]">
            <img v-if="$store.state.menuWidth == '250px'" src="@/assets/milk-cat.png" class="h-[60px]">
            <img v-else src="@/assets/milk-cat.png" class="h-[60px]">
        </div>

        <el-menu :collapse="isCollapse" class="border-0 admin-el-menu" :default-active="defaultActive"
            :collapse-transition="false" unique-opened @select="handleSelect">
            <template v-for="(item, index) in menus" :index="index">
                <!-- 有子菜单 -->
                <el-sub-menu v-if="item.child && item.child.length > 0" :index="index.toString()" class="admin-el-sub-menu">
                    <template #title>
                        <el-icon>
                            <component :is="item.icon"></component>
                        </el-icon>
                        <span>{{ item.name }}</span>
                    </template>
                    <el-menu-item v-for="(child, childIndex) in item.child" :key="childIndex" :index="child.path"
                        class="admin-el-menu-item">
                        <el-icon>
                            <component :is="child.icon"></component>
                        </el-icon>
                        <span>{{ child.name }}</span>
                    </el-menu-item>
                </el-sub-menu>
                <!-- 无子菜单 -->
                <el-menu-item v-else :index="item.path" class="admin-el-menu-item">
                    <el-icon>
                        <component :is="item.icon"></component>
                    </el-icon>
                    <span>{{ item.name }}</span>
                </el-menu-item>
            </template>

        </el-menu>
    </div>
</template>

<script setup>
import { useRouter, useRoute } from 'vue-router';
import { computed, ref } from 'vue';
import { useStore } from 'vuex';

const router = useRouter()
const route = useRoute()
const store = useStore()

const defaultActive = ref(route.path)

// 是否折叠
const isCollapse = computed(() => !(store.state.menuWidth == '250px'))

const menus = [{
    'name': '仪表盘',
    'icon': 'Monitor',
    'path': '/admin',
    'child': []
},
{
    'name': '文章管理',
    'icon': 'Document',
    'path': '/admin/article/list',
    'child': []
},
{
    'name': '分类管理',
    'icon': 'FolderOpened',
    'path': '/admin/category/list',
    'child': []
},
{
    'name': '标签管理',
    'icon': 'PriceTag',
    'path': '/admin/tag/list',
    'child': []
},
{
    'name': '博客设置',
    'icon': 'Setting',
    'path': '/admin/blog/setting',
    'child': []
},
{
    'name': '监控中心',
    'icon': 'Monitor',
    'path': '/admin/monitor/log',
    'child': [
        {
            'name': '实时日志',
            'icon': 'Tickets',
            'path': '/admin/monitor/log',
        },
        {
            'name': '告警规则',
            'icon': 'Bell',
            'path': '/admin/monitor/rule',
        },
        {
            'name': '告警记录',
            'icon': 'Clock',
            'path': '/admin/monitor/alert',
        }
    ]
}
]

const handleSelect = (e) => {
    console.log(defaultActive)
    console.log(route.path)

    router.push(e)
}
</script>

<style>
.meun {
    transition: all 0.3s;
    width: 250px;
    top: 0;
    bottom: 0;
    left: 0;
    overflow-y: auto;
    overflow-x: hidden;
    background-color: #001428 !important;
}

.admin-el-menu {
    background-color: #001428 !important;
    border-right: 0;
}

.admin-el-menu-item {
    color: #c0c4cc !important;
}

.el-menu-item.is-active {
    background-color: #ffffff10 !important;
}

.el-menu-item.is-active:before {
    content: "";
    position: absolute;
    top: 0;
    left: 0;
    width: 2px;
    height: 100%;
    background-color: var(--el-color-primary);
}

.el-menu-item:hover {
    background-color: #ffffff10;
}

.admin-el-sub-menu .el-sub-menu__title {
    color: #c0c4cc !important;
    background-color: #001428 !important;
}

.admin-el-sub-menu .el-sub-menu__title:hover {
    background-color: #ffffff10 !important;
}

.admin-el-sub-menu .el-menu {
    background-color: #001428 !important;
}

.admin-el-sub-menu .el-menu .el-menu-item {
    background-color: #001428 !important;
    color: #c0c4cc !important;
}

.admin-el-sub-menu .el-menu .el-menu-item:hover {
    background-color: #ffffff10 !important;
}

.meun::-webkit-scrollbar {
    width: 0;
}

.logo {
    height: 64px;
    background-color: #001428;
    color: #fff;
    @apply flex justify-center items-center text-xl font-thin;
}
</style>