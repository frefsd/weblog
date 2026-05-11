<template>
  <div class="space-y-6">
    <!-- UserInfoCard 占位 -->
    <div class="bg-white/70 backdrop-blur-sm border border-white/30 rounded-xl p-6 dark:bg-gray-800/75 dark:border-gray-600/30">
      <div class="flex flex-col items-center">
        <!-- 头像 -->
        <div class="skeleton w-20 h-20 rounded-full mb-3"></div>
        <!-- 名称 -->
        <div class="skeleton h-5 w-24 mb-1"></div>
        <div class="skeleton h-4 w-32 mb-4"></div>
        <!-- 统计数字 -->
        <div class="flex justify-center gap-6 w-full">
          <div class="text-center">
            <div class="skeleton h-6 w-10 mb-1 mx-auto"></div>
            <div class="skeleton h-3 w-8 mx-auto"></div>
          </div>
          <div class="text-center">
            <div class="skeleton h-6 w-10 mb-1 mx-auto"></div>
            <div class="skeleton h-3 w-8 mx-auto"></div>
          </div>
          <div class="text-center">
            <div class="skeleton h-6 w-10 mb-1 mx-auto"></div>
            <div class="skeleton h-3 w-8 mx-auto"></div>
          </div>
        </div>
      </div>
    </div>

    <!-- 分类列表占位 -->
    <div class="bg-white/70 backdrop-blur-sm border border-white/30 rounded-xl p-5 dark:bg-gray-800/75 dark:border-gray-600/30">
      <div class="skeleton h-5 w-16 mb-4"></div>
      <div class="space-y-3">
        <div v-for="i in 4" :key="i" class="flex items-center gap-2">
          <div class="skeleton h-4 w-4 rounded"></div>
          <div class="skeleton h-4 w-24"></div>
        </div>
      </div>
    </div>

    <!-- 标签列表占位 -->
    <div class="bg-white/70 backdrop-blur-sm border border-white/30 rounded-xl p-5 dark:bg-gray-800/75 dark:border-gray-600/30">
      <div class="skeleton h-5 w-12 mb-4"></div>
      <div class="flex flex-wrap gap-2">
        <div v-for="i in 6" :key="i" class="skeleton h-6 w-16 rounded-full"></div>
      </div>
    </div>
  </div>
</template>
