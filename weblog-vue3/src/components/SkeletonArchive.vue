<template>
  <div>
    <div v-for="i in 3" :key="i" class="p-5 mb-4 border border-gray-200 rounded-lg bg-white dark:bg-gray-800 dark:border-gray-700">
      <!-- 月份占位 -->
      <div class="skeleton h-5 w-24 mb-4"></div>
      <!-- 文章列表 -->
      <div class="space-y-3">
        <div v-for="j in 3" :key="j" class="flex items-center gap-3 p-2">
          <!-- 缩略图 -->
          <div class="skeleton w-24 h-12 rounded-lg flex-shrink-0"></div>
          <div class="flex-1 min-w-0">
            <div class="skeleton h-4 w-3/4 mb-2"></div>
            <div class="skeleton h-3 w-28"></div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
