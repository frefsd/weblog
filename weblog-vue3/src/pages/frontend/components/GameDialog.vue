<template>
    <el-dialog v-model="visible" title="💕 哄女友小游戏" :width="dialogWidth"
        align-center lock-scroll :close-on-click-modal="false" destroy-on-close
        @closed="handleClose" append-to-body :z-index="99999"
        modal-class="game-dialog-modal" class="game-dialog">
        <div class="game-body">
            <!-- 原谅值进度条 -->
            <div class="meter-section">
                <div class="meter-header">
                    <span class="meter-label">女友原谅值</span>
                </div>
                <div class="meter-track">
                    <div class="meter-fill" :style="{ width: forgiveness + '%' }"></div>
                    <span class="meter-text">{{ forgiveness }} / 100</span>
                </div>
            </div>

            <!-- 场景描述 -->
            <div v-if="scenario" class="scenario-box">
                <span class="scenario-icon">💔</span>
                <span>{{ scenario }}</span>
            </div>

            <!-- 对话区域 -->
            <div ref="dialogueRef" class="dialogue-area">
                <div v-for="(msg, i) in messages" :key="i"
                    :class="['bubble', msg.role === 'user' ? 'bubble-user' : 'bubble-ai']">
                    <div class="bubble-content">{{ msg.content }}</div>
                    <div v-if="msg.role === 'assistant' && msg.scoreChange"
                        :class="['score-tag', msg.scoreChange >= 0 ? 'score-up' : 'score-down']">
                        {{ msg.scoreChange >= 0 ? '+' : '' }}{{ msg.scoreChange }}
                    </div>
                </div>
                <div v-if="loading" class="loading-dots">
                    <span class="dot"></span>
                    <span class="dot"></span>
                    <span class="dot"></span>
                </div>
            </div>

            <!-- 结果弹窗 -->
            <div v-if="gameStatus === 'won' || gameStatus === 'lost'" class="result-overlay">
                <div class="result-card">
                    <div class="result-icon">{{ gameStatus === 'won' ? '🎉' : '💔' }}</div>
                    <p class="result-title">{{ gameStatus === 'won' ? '恭喜你通关了，你的女朋友已经原谅你了！' : '游戏结束，你的女朋友已经甩了你' }}</p>
                    <p class="result-score">最终原谅值：{{ forgiveness }} / 100</p>
                    <div class="result-actions">
                        <el-button type="primary" class="restart-btn" @click="restartGame">再来一次</el-button>
                        <el-button @click="visible = false">关闭</el-button>
                    </div>
                </div>
            </div>

            <!-- 输入区域 -->
            <div v-if="gameStatus === 'playing'" class="input-area">
                <el-input v-model="replyText" placeholder="说点好听的哄哄她..."
                    :disabled="loading" @keydown.enter.prevent="handleSend" class="game-input" maxlength="500"
                    show-word-limit>
                    <template #append>
                        <el-button :loading="loading" :disabled="!replyText.trim()" @click="handleSend"
                            class="send-btn">发送</el-button>
                    </template>
                </el-input>
            </div>
        </div>
    </el-dialog>
</template>

<script setup>
import { ref, computed, watch, nextTick } from 'vue'
import { startGame, sendReply, closeGame } from '@/api/frontend/game'

const props = defineProps({
    modelValue: Boolean
})
const emit = defineEmits(['update:modelValue', 'game-status-change'])

const visible = computed({
    get: () => props.modelValue,
    set: (val) => emit('update:modelValue', val)
})

const dialogWidth = ref('min(680px, 94vw)')
const dialogueRef = ref(null)
const replyText = ref('')
const loading = ref(false)

// 游戏状态
const sessionId = ref('')
const forgiveness = ref(20)
const scenario = ref('')
const messages = ref([])
const gameStatus = ref('playing') // playing | won | lost

// 开始游戏
async function initGame() {
    loading.value = true
    try {
        const res = await startGame()
        if (res.success) {
            sessionId.value = res.data.sessionId
            scenario.value = res.data.scenario
            forgiveness.value = res.data.forgiveness
            messages.value = []
            gameStatus.value = 'playing'
        }
    } catch (e) {
        messages.value.push({
            role: 'assistant',
            content: '哎呀，小暖现在不想说话... 等会儿再来吧 😔'
        })
    } finally {
        loading.value = false
    }
}

// 发送回复
async function handleSend() {
    const text = replyText.value.trim()
    if (!text || loading.value || gameStatus.value !== 'playing') return

    replyText.value = ''
    messages.value.push({ role: 'user', content: text })
    loading.value = true

    try {
        const res = await sendReply(sessionId.value, text)
        if (res.success) {
            const data = res.data
            messages.value.push({
                role: 'assistant',
                content: data.reply,
                scoreChange: data.scoreChange
            })
            forgiveness.value = data.forgiveness

            if (data.status === 'won' || data.status === 'lost') {
                gameStatus.value = data.status
                emit('game-status-change', data.status)
            }
            await nextTick()
            scrollToBottom()
        }
    } catch (e) {
        messages.value.push({
            role: 'system',
            content: '发送失败了，再试试吧...',
            isError: true
        })
    } finally {
        loading.value = false
    }
}

// 重新开始
async function restartGame() {
    replyText.value = ''
    messages.value = []
    gameStatus.value = 'playing'
    await initGame()
}

// 监听对话框打开
watch(() => props.modelValue, (val) => {
    if (val) {
        initGame()
    }
})

// 自动滚动
watch(() => messages.value.length, async () => {
    await nextTick()
    scrollToBottom()
})

function scrollToBottom() {
    if (dialogueRef.value) {
        dialogueRef.value.scrollTop = dialogueRef.value.scrollHeight
    }
}

function handleClose() {
    if (sessionId.value) {
        closeGame(sessionId.value)
    }
    emit('game-status-change', 'idle')
}
</script>

<style scoped>
.game-body {
    position: relative;
    height: calc(100vh - 160px);
    display: flex;
    flex-direction: column;
    padding: 0 8px;
}

@media (max-width: 640px) {
    .game-body {
        height: calc(100vh - 140px);
    }
}

/* 原谅值进度条 */
.meter-section {
    padding: 4px 0 24px;
    flex-shrink: 0;
}

.meter-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 10px;
}

.meter-label {
    font-size: 16px;
    font-weight: 600;
    color: #374151;
}

.meter-track {
    height: 24px;
    background: #f3f4f6;
    border-radius: 12px;
    overflow: hidden;
    position: relative;
}

.meter-fill {
    height: 100%;
    border-radius: 12px;
    background: linear-gradient(90deg, #ec4899, #a855f7);
    transition: width 0.5s ease;
}

.meter-text {
    position: absolute;
    right: 10px;
    top: 50%;
    transform: translateY(-50%);
    font-size: 12px;
    font-weight: 700;
    color: #6b7280;
}

/* 场景描述 */
.scenario-box {
    padding: 16px 20px;
    background: #fdf2f8;
    border-radius: 12px;
    font-size: 15px;
    color: #6b7280;
    line-height: 1.8;
    margin-bottom: 20px;
    flex-shrink: 0;
}

.scenario-icon {
    margin-right: 6px;
}

/* 对话区域 */
.dialogue-area {
    flex: 1;
    min-height: 0;
    overflow-y: auto;
    padding: 12px 6px;
    scroll-behavior: smooth;
}

.dialogue-area::-webkit-scrollbar {
    width: 4px;
}

.dialogue-area::-webkit-scrollbar-thumb {
    background: #e5e7eb;
    border-radius: 2px;
}

.bubble {
    margin-bottom: 14px;
    max-width: 85%;
    position: relative;
}

.bubble-ai {
    margin-right: auto;
}

.bubble-user {
    margin-left: auto;
}

.bubble-content {
    padding: 12px 16px;
    font-size: 15px;
    line-height: 1.7;
    word-break: break-word;
    white-space: pre-wrap;
}

.bubble-ai .bubble-content {
    background: #fff;
    border: 1px solid #f3e8ff;
    border-radius: 12px 12px 12px 4px;
    color: #374151;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06);
}

.bubble-user .bubble-content {
    background: linear-gradient(135deg, #ec4899, #a855f7);
    color: white;
    border-radius: 12px 12px 4px 12px;
}

.score-tag {
    position: absolute;
    bottom: -8px;
    right: 6px;
    font-size: 12px;
    font-weight: 700;
    padding: 2px 8px;
    border-radius: 10px;
}

.score-up {
    color: #22c55e;
    background: #dcfce7;
}

.score-down {
    color: #ef4444;
    background: #fce7e7;
}

/* 加载动画 */
.loading-dots {
    display: flex;
    gap: 4px;
    padding: 12px 4px;
}

.dot {
    width: 8px;
    height: 8px;
    background: #ec4899;
    border-radius: 50%;
    animation: pulse 1.2s ease-in-out infinite;
}

.dot:nth-child(2) {
    animation-delay: 0.2s;
}

.dot:nth-child(3) {
    animation-delay: 0.4s;
}

@keyframes pulse {
    0%, 100% {
        opacity: 0.3;
        transform: scale(0.8);
    }
    50% {
        opacity: 1;
        transform: scale(1.2);
    }
}

/* 结果覆盖层 */
.result-overlay {
    position: absolute;
    inset: 0;
    background: rgba(255, 255, 255, 0.9);
    backdrop-filter: blur(8px);
    display: flex;
    align-items: center;
    justify-content: center;
    z-index: 10;
    border-radius: 8px;
}

.result-card {
    text-align: center;
    padding: 24px;
}

.result-icon {
    font-size: 56px;
    margin-bottom: 16px;
}

.result-title {
    font-size: 18px;
    font-weight: 600;
    color: #374151;
    margin-bottom: 8px;
}

.result-score {
    font-size: 14px;
    color: #9ca3af;
    margin-bottom: 20px;
}

.restart-btn {
    background: linear-gradient(135deg, #ec4899, #a855f7) !important;
    border: none !important;
    color: white !important;
}

/* 输入区域 */
.input-area {
    flex-shrink: 0;
    padding-top: 20px;
    padding-bottom: 4px;
    border-top: 1px solid #f3e8ff;
    margin-top: 12px;
}

.send-btn {
    background: linear-gradient(135deg, #ec4899, #a855f7) !important;
    border: none !important;
    color: white !important;
}

.game-input :deep(.el-input-group__append) {
    background: transparent;
    border: none;
}

/* 暗黑模式适配 */
html.dark .meter-label {
    color: #e5e7eb;
}

html.dark .meter-track {
    background: #374151;
}

html.dark .scenario-box {
    background: rgba(236, 72, 153, 0.1);
    color: #d1d5db;
}

html.dark .bubble-ai .bubble-content {
    background: #1f2937;
    border-color: #374151;
    color: #e5e7eb;
}

html.dark .result-overlay {
    background: rgba(17, 24, 39, 0.9);
}

html.dark .result-title {
    color: #e5e7eb;
}

html.dark .input-area {
    border-color: #374151;
}
</style>
