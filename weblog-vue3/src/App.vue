<template>
  <el-config-provider :locale="locale">
    <FallingPetals v-if="showPetals" />
    <router-view v-slot="{ Component, route }">
      <Transition name="page">
        <component :is="Component" :key="route.fullPath" />
      </Transition>
    </router-view>
  </el-config-provider>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import FallingPetals from '@/components/FallingPetals.vue'

import zhCn from 'element-plus/lib/locale/lang/zh-cn'
let locale = zhCn

const route = useRoute()
const showPetals = computed(() => {
  return !route.path.startsWith('/admin') && !route.path.startsWith('/login')
})
</script>

<style>
body {
  font-family: -apple-system-font, BlinkMacSystemFont, "Helvetica Neue", "PingFang SC", "Hiragino Sans GB", "Microsoft YaHei UI", "Microsoft YaHei", Arial, sans-serif;
  color: #4c4e4d;
  font-size: 16px;
  background: linear-gradient(135deg, #e0e7ff 0%, #f0e6ff 50%, #e6f0ff 100%);
  line-height: 1.6;
}

html.dark body {
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 50%, #0f3460 100%);
}

#nprogress .bar {
   background: #409eff!important;
}

/* 骨架屏 shimmer 动画 */
@keyframes shimmer {
  0% { background-position: -200% 0; }
  100% { background-position: 200% 0; }
}

.skeleton {
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s ease-in-out infinite;
  border-radius: 4px;
}

html.dark .skeleton {
  background: linear-gradient(90deg, #2d2d2d 25%, #3d3d3d 50%, #2d2d2d 75%);
  background-size: 200% 100%;
}

/* 页面过渡动画 - 淡入淡出 */
.page-enter-active,
.page-leave-active {
  transition: opacity 0.25s ease;
}

.page-enter-from,
.page-leave-to {
  opacity: 0;
}
</style>
