<template>
    <div
        class="game-widget w-full font-medium p-5 bg-white border border-gray-200 rounded-lg dark:bg-gray-800 dark:border-gray-700">
        <!-- 标题 -->
        <h2 class="mb-3 font-bold text-gray-900 uppercase dark:text-white flex items-center gap-1">
            <span>💕</span> 哄女友小游戏
        </h2>

        <!-- 空闲状态 -->
        <template v-if="status === 'idle'">
            <div class="flex flex-col items-center py-2">
                <div
                    class="w-16 h-16 rounded-full bg-gradient-to-br from-pink-300 to-purple-400 flex items-center justify-center text-2xl mb-3">
                    😊
                </div>
                <span class="text-sm text-gray-500 dark:text-gray-400 mb-3">来哄哄小暖吧~</span>
                <el-button class="start-btn" size="small" @click="startGame">❤ 开始游戏</el-button>
            </div>
        </template>

        <!-- 游戏中状态 -->
        <template v-else-if="status === 'playing'">
            <div class="flex flex-col items-center py-1">
                <div
                    class="w-14 h-14 rounded-full bg-gradient-to-br from-pink-300 to-purple-400 flex items-center justify-center text-xl mb-2">
                    😊</div>
                <div class="w-full meter-mini">
                    <div class="meter-mini-track">
                        <div class="meter-mini-fill" :style="{ width: forgiveness + '%' }"></div>
                    </div>
                    <span class="meter-mini-text">{{ forgiveness }}/100</span>
                </div>
                <el-button class="continue-btn" size="small" @click="openDialog">💕 继续哄她</el-button>
            </div>
        </template>

        <!-- 结束状态 -->
        <template v-else>
            <div class="flex flex-col items-center py-2">
                <div class="text-2xl mb-2">{{ status === 'won' ? '🎉' : '💔' }}</div>
                <span class="text-sm text-gray-500 dark:text-gray-400 mb-3">
                    {{ status === 'won' ? '恭喜你通关了，你的女朋友已经原谅你了！' : '游戏结束，你的女朋友已经甩了你' }}
                </span>
                <el-button class="start-btn" size="small" @click="startGame">🔄 再来一次</el-button>
            </div>
        </template>

        <!-- 游戏弹窗 -->
        <GameDialog v-model="dialogVisible" @game-status-change="onGameStatusChange" />
    </div>
</template>

<script setup>
import { ref } from 'vue'
import GameDialog from './GameDialog.vue'

const status = ref('idle') // idle | playing | won | lost
const forgiveness = ref(20)
const dialogVisible = ref(false)

function startGame() {
    dialogVisible.value = true
    status.value = 'playing'
    forgiveness.value = 20
}

function openDialog() {
    dialogVisible.value = true
}

function onGameStatusChange(newStatus) {
    if (newStatus === 'idle') {
        status.value = 'idle'
        forgiveness.value = 20
    } else if (newStatus === 'won') {
        status.value = 'won'
        forgiveness.value = 100
    } else if (newStatus === 'lost') {
        status.value = 'lost'
        forgiveness.value = 0
    }
}
</script>

<style scoped>
.game-widget {
    transition: all 0.3s ease;
}

.meter-mini {
    width: 100%;
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 10px;
}

.meter-mini-track {
    flex: 1;
    height: 8px;
    background: #f3f4f6;
    border-radius: 4px;
    overflow: hidden;
}

.meter-mini-fill {
    height: 100%;
    border-radius: 4px;
    background: linear-gradient(90deg, #ec4899, #a855f7);
    transition: width 0.5s ease;
}

.meter-mini-text {
    font-size: 11px;
    font-weight: 700;
    color: #9ca3af;
    white-space: nowrap;
}

.start-btn {
    background: linear-gradient(135deg, #ec4899, #a855f7) !important;
    border: none !important;
    color: white !important;
    border-radius: 20px !important;
    padding: 8px 20px !important;
}

.continue-btn {
    background: linear-gradient(135deg, #ec4899, #a855f7) !important;
    border: none !important;
    color: white !important;
    border-radius: 20px !important;
    padding: 6px 18px !important;
}

/* 暗黑模式 */
html.dark .meter-mini-track {
    background: #374151;
}

html.dark .meter-mini-text {
    color: #6b7280;
}
</style>
