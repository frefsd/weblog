<script setup>
import zhCn from 'element-plus/es/locale/lang/zh-cn'
</script>

<template>
    <el-config-provider :locale="zhCn">
        <router-view v-slot="{ Component, route }">
            <div v-if="Component" :key="route.matched[0]?.path === '/admin' ? '/admin' : route.fullPath" class="app-view">
                <component :is="Component" />
            </div>
        </router-view>
    </el-config-provider>
</template>
