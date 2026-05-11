<template>
  <div class="bg-white border border-gray-200 rounded-lg p-5 dark:bg-gray-800 dark:border-gray-700">
    <div class="skeleton h-5 w-16 mb-4"></div>
    <div class="space-y-3">
      <div v-for="i in 6" :key="i" class="flex items-center gap-2">
        <div class="skeleton h-4 w-4 rounded flex-shrink-0"></div>
        <div class="skeleton h-4 w-32"></div>
      </div>
    </div>
  </div>
</template>
