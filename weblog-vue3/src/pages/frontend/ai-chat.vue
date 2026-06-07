<template>
    <Header></Header>

    <main class="flex flex-col" style="height: calc(100vh - 65px);">
        <!-- ========== 欢迎区（无对话时展示） ========== -->
        <div v-if="messages.length === 0" class="flex flex-col items-center justify-center flex-1 px-4 md:px-4 text-center">
            <div
                class="w-16 h-16 md:w-20 md:h-20 rounded-2xl bg-gradient-to-br from-blue-400 to-indigo-600 flex items-center justify-center text-3xl md:text-4xl mb-4 shadow-lg shadow-blue-500/25">
                🤖
            </div>
            <h1 class="text-xl md:text-2xl font-bold text-gray-900 dark:text-white mb-2">你好，我是小智</h1>
            <p class="text-gray-500 dark:text-gray-400 max-w-md text-xs md:text-sm">
                我是 WeBlog 的 AI 助手，基于博客内容回答你的问题。试着问我任何技术问题吧！
            </p>
            <div class="flex flex-wrap justify-center gap-2 mt-6 max-w-lg">
                <button v-for="q in suggestedQuestions" :key="q"
                    class="text-xs px-3 py-2 rounded-full bg-blue-50 dark:bg-blue-900/30 text-blue-700 dark:text-blue-300 hover:bg-blue-100 dark:hover:bg-blue-900/50 transition-colors cursor-pointer"
                    @click="handleSuggested(q)">
                    {{ q }}
                </button>
            </div>
        </div>

        <!-- ========== 消息区（有对话时展示） ========== -->
        <div v-else ref="messageAreaRef" class="flex-1 overflow-y-auto px-3 md:px-4 py-4 md:py-6 space-y-4 max-w-3xl mx-auto w-full message-area">
            <div v-for="(msg, idx) in messages" :key="idx" class="flex items-start gap-3" :class="msg.isUser ? 'justify-end' : ''">
                <!-- AI 头像（左侧） -->
                <div v-if="!msg.isUser"
                    class="w-8 h-8 rounded-full bg-gradient-to-br from-blue-400 to-indigo-600 flex-shrink-0 flex items-center justify-center text-sm">
                    🤖
                </div>

                <!-- 消息气泡 -->
                <div :class="[
                    msg.isUser
                        ? 'bg-white dark:bg-gray-800 border border-gray-200 dark:border-gray-700 text-black dark:text-gray-100 rounded-2xl rounded-tr-sm'
                        : 'bg-white dark:bg-gray-800 border border-gray-200 dark:border-gray-700 rounded-2xl rounded-tl-sm',
                    'px-4 py-3 shadow-sm bubble-max'
                ]">
                    <!-- 错误消息 -->
                    <p v-if="msg.isError" class="text-sm leading-relaxed text-red-500 dark:text-red-400">
                        {{ msg.content }}
                    </p>
                    <!-- AI 消息：流式过程中直接 innerHTML 渲染 Markdown，完成后用 Vue v-html -->
                    <div v-else-if="!msg.isUser" class="text-sm leading-relaxed">
                        <div v-if="msg.isStreaming" :ref="(el) => setStreamingRef(el, msg)" class="markdown-body"></div>
                        <div v-else class="markdown-body" v-html="renderMarkdown(msg.content)"></div>
                    </div>
                    <!-- 用户消息：纯文本 -->
                    <p v-else class="text-sm leading-relaxed whitespace-pre-wrap">{{ msg.content }}</p>

                    <!-- 流式输出光标：动态跳动黑点 -->
                    <div v-if="msg.isStreaming" class="inline-flex items-center gap-1 ml-1">
                        <span class="w-1.5 h-1.5 rounded-full bg-gray-600 dark:bg-gray-300 animate-bounce-sm" style="animation-delay:0ms"></span>
                        <span class="w-1.5 h-1.5 rounded-full bg-gray-600 dark:bg-gray-300 animate-bounce-sm" style="animation-delay:150ms"></span>
                        <span class="w-1.5 h-1.5 rounded-full bg-gray-600 dark:bg-gray-300 animate-bounce-sm" style="animation-delay:300ms"></span>
                    </div>

                    <!-- 引用来源 -->
                    <div v-if="msg.sources && msg.sources.length > 0" class="mt-3 pt-3 border-t border-gray-100 dark:border-gray-700">
                        <div class="text-xs text-gray-400 dark:text-gray-500 mb-1.5">📎 参考来源</div>
                        <div v-for="(src, si) in msg.sources" :key="si"
                            class="mt-1.5 p-2.5 bg-blue-50 dark:bg-blue-900/20 border border-blue-100 dark:border-blue-800 rounded-lg text-xs flex items-center gap-2 cursor-pointer hover:bg-blue-100 dark:hover:bg-blue-900/40 transition-colors"
                            @click="goArticle(src.articleId)">
                            <span class="text-blue-700 dark:text-blue-300 font-medium truncate">{{ src.articleTitle }}</span>
                            <span class="text-gray-400 ml-auto flex-shrink-0">→</span>
                        </div>
                    </div>
                </div>

                <!-- 用户头像（右侧） -->
                <div v-if="msg.isUser"
                    class="w-8 h-8 rounded-full bg-gray-300 dark:bg-gray-600 flex-shrink-0 flex items-center justify-center text-sm text-white order-1">
                    U
                </div>
            </div>
        </div>

        <!-- ========== 底部输入区 ========== -->
        <div class="border-t border-gray-200 dark:border-gray-700 bg-white dark:bg-gray-800 px-3 md:px-4 py-2 md:py-3">
            <div class="max-w-3xl mx-auto">
                <div class="flex items-end gap-2 md:gap-2.5">
                    <textarea ref="textareaRef" v-model="inputText" :disabled="isStreaming" rows="1" maxlength="200"
                        class="flex-1 resize-none border-2 border-gray-300 dark:border-gray-500 rounded-2xl px-4 py-3 text-base md:text-lg bg-white dark:bg-gray-700 text-black dark:text-gray-100 placeholder-gray-400 dark:placeholder-gray-500 shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 scrollbar-hide"
                        :placeholder="isStreaming ? '小智正在思考...' : '输入你的问题...'"
                        style="min-height: 48px; max-height: 140px;"
                        @input="onInput"
                        @keydown="onKeydown"></textarea>
                    <!-- 发送 / 停止按钮 -->
                    <button v-if="!isStreaming" :disabled="!inputText.trim()" @click="sendMessage()"
                        class="flex-shrink-0 w-9 h-9 md:w-10 md:h-10 rounded-full flex items-center justify-center transition-all"
                        :class="inputText.trim()
                            ? 'bg-gray-800 dark:bg-gray-200 text-white dark:text-gray-800 cursor-pointer hover:bg-gray-700 dark:hover:bg-gray-300'
                            : 'bg-gray-200 dark:bg-gray-600 text-gray-400 dark:text-gray-500 cursor-not-allowed'">
                        <svg class="w-3.5 h-3.5 md:w-4 md:h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 19V5M5 12l7-7 7 7" />
                        </svg>
                    </button>
                    <button v-else @click="stopStreaming()"
                        class="flex-shrink-0 w-9 h-9 md:w-10 md:h-10 rounded-full bg-red-500 hover:bg-red-600 text-white flex items-center justify-center transition-colors cursor-pointer">
                        <svg class="w-4 h-4" fill="currentColor" viewBox="0 0 24 24">
                            <rect x="6" y="6" width="12" height="12" rx="1" />
                        </svg>
                    </button>
                </div>
                <span class="text-2xs md:text-xs text-gray-400 mt-1 block text-right">{{ inputText.length }}/200</span>
            </div>
        </div>
    </main>

    <Footer></Footer>
</template>

<script setup>
import { ref, nextTick, watch, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import Header from '@/layouts/components/Header.vue'
import Footer from '@/layouts/components/Footer.vue'
import { sendChatMessageStreamText } from '@/api/frontend/chat'
import axios from '@/axios'
import { marked } from 'marked'

// 配置 marked Markdown 渲染器
marked.setOptions({
    breaks: true,   // 换行转为 <br>
    gfm: true       // GitHub 风格 Markdown
})

const router = useRouter()

// ---- 推荐问题 ----
const suggestedQuestions = [
    '什么是前端开发？',
    'Java 如何入门？',
    'Vue3 有什么新特性？',
    'Spring Boot 怎么配置数据库？'
]

// ---- 数据 ----
const messages = ref([])        
const inputText = ref('')
const isStreaming = ref(false)
const textareaRef = ref(null)
const messageAreaRef = ref(null)

// SSE 中断控制器
let abortController = null

// ---- 会话 ID（localStorage） ----
const SESSION_KEY = 'ai_session_id'

function getSessionId() {
    return localStorage.getItem(SESSION_KEY) || null
}

function setSessionId(id) {
    if (id) {
        localStorage.setItem(SESSION_KEY, id)
    }
}

// ---- 消息持久化（localStorage） ----
const MESSAGES_KEY = 'ai_messages'
const MAX_STORED_MESSAGES = 50

function saveMessages() {
    try {
        const arr = messages.value.slice(-MAX_STORED_MESSAGES)
        localStorage.setItem(MESSAGES_KEY, JSON.stringify(arr))
    } catch (_) { /* localStorage 满时忽略 */ }
}

function loadMessages() {
    try {
        const raw = localStorage.getItem(MESSAGES_KEY)
        if (raw) {
            const arr = JSON.parse(raw)
            if (Array.isArray(arr)) {
                messages.value = arr.map(m => ({ ...m, isStreaming: false }))
            }
        }
    } catch (_) { /* 数据损坏时忽略 */ }
}

// 监听消息数量变化 → 自动持久化（只在增删消息时保存，避免流式中间状态覆盖完整内容）
watch(() => messages.value.length, () => saveMessages())

// 挂载时校验 session 有效性，过期则清空聊天记录并重新开始
onMounted(async () => {
    const sid = getSessionId()
    if (sid) {
        try {
            const res = await axios.get('/ai/session/validate', {
                params: { sessionId: sid }
            })
            // axios 拦截器已处理 data，res 就是 { success, data, ... }
            if (res.success && res.data && !res.data.valid) {
                // 会话已过期 → 清空 localStorage 中的消息
                localStorage.removeItem(MESSAGES_KEY)
                // 用后端返回的新 sessionId 替换旧的
                if (res.data.sessionId) {
                    setSessionId(res.data.sessionId)
                }
                // 不加载旧消息，直接显示欢迎界面
                scrollToBottom()
                return
            }
        } catch (_) {
            // 校验接口失败时降级处理：仍然尝试加载旧消息
        }
    }
    loadMessages()
    scrollToBottom()
})

// ---- textarea 自动撑高 ----
function onInput() {
    const el = textareaRef.value
    if (el) {
        el.style.height = 'auto'
        el.style.height = Math.min(el.scrollHeight, 100) + 'px'
    }
}

// ---- 键盘事件：Enter 发送，Shift+Enter 换行 ----
function onKeydown(e) {
    if (e.key === 'Enter' && !e.shiftKey) {
        e.preventDefault()
        if (!isStreaming.value && inputText.value.trim()) {
            sendMessage()
        }
    }
}

// ---- 点击推荐问题 ----
function handleSuggested(q) {
    inputText.value = q
    sendMessage()
}

// ---- 流式显示的 DOM ref：直接存到消息对象上 ----
// 注意：不能用 Map<msg, el> 因为模板中的 msg 是 Vue 响应式 Proxy，
// 而流式循环里的 aiMsg 是原始对象，Proxy !== 原始对象，Map 查不到。
function setStreamingRef(el, msg) {
    msg._el = el || null
}
function goArticle(articleId) {
    if (articleId) {
        router.push({ path: '/article/detail', query: { articleId } })
    }
}

// ---- 滚动到底部 ----
function scrollToBottom() {
    nextTick(() => {
        const el = messageAreaRef.value
        if (el) {
            el.scrollTop = el.scrollHeight
        }
    })
}

// ---- Markdown 渲染 ----
function renderMarkdown(text) {
    if (!text) return ''
    return marked.parse(text)
}

// ---- 发送消息（SSE 流式） ----
async function sendMessage() {
    const text = inputText.value.trim()
    if (!text || isStreaming.value) return

    // 清空输入
    inputText.value = ''
    nextTick(() => { if (textareaRef.value) textareaRef.value.style.height = 'auto' })

    // 添加用户消息 + AI 占位消息
    messages.value.push({ isUser: true, content: text })
    const aiMsg = { isUser: false, isStreaming: true, content: '', sources: [] }
    messages.value.push(aiMsg)
    scrollToBottom()

    isStreaming.value = true
    abortController = new AbortController()

    const sessionId = getSessionId()

    try {
        const response = await sendChatMessageStreamText(sessionId, text, abortController.signal)

        if (!response.ok) {
            throw new Error(`HTTP ${response.status}`)
        }

        const reader = response.body.getReader()
        const decoder = new TextDecoder('utf-8')
        let fullText = ''
        let metaResolved = false

        while (true) {
            const { done, value } = await reader.read()
            if (done) break

            fullText += decoder.decode(value, { stream: true })

            // 检测是否包含元数据分隔符
            const metaIdx = fullText.lastIndexOf('___META___')
            if (metaIdx >= 0 && !metaResolved) {
                metaResolved = true
                const cleanEnd = fullText.indexOf('___META___')
                const textPart = fullText.substring(0, cleanEnd)
                // 逐字显示：直接 innerHTML 渲染 Markdown
                const el2 = aiMsg._el
                if (el2) {
                    for (let i = aiMsg.content.length; i < textPart.length; i++) {
                        el2.innerHTML = renderMarkdown(textPart.substring(0, i + 1))
                        await new Promise(r => setTimeout(r, 30))
                    }
                }
                // 同步到 Vue reactive 状态
                aiMsg.content = textPart

                // 解析元数据
                const metaStr = fullText.substring(metaIdx + 11).trim()
                try {
                    const meta = JSON.parse(metaStr)
                    aiMsg.sources = meta.sources || []
                    if (meta.sessionId) {
                        setSessionId(meta.sessionId)
                    }
                } catch (e) {
                    console.error('元数据解析失败:', metaStr, e)
                }
            } else if (!metaResolved) {
                // 逐字显示：直接 innerHTML 渲染 Markdown
                const el2 = aiMsg._el
                if (el2) {
                    for (let i = aiMsg.content.length; i < fullText.length; i++) {
                        el2.innerHTML = renderMarkdown(fullText.substring(0, i + 1))
                        await new Promise(r => setTimeout(r, 30))
                    }
                }
                // 同步到 Vue reactive 状态
                aiMsg.content = fullText
            }
        }

        // 流结束
        if (!metaResolved) {
            aiMsg.content = fullText
        }
        // 同步完成后标记结束，触发 Vue 切换到 markdown 视图
        aiMsg.isStreaming = false
        // 保存到 localStorage
        saveMessages()
    } catch (err) {
        // 用户主动中断
        if (err.name === 'AbortError') {
            if (aiMsg.content) {
                aiMsg.isStreaming = false
            } else {
                // 还没收到任何 token 就中断 → 移除 AI 消息并提示
                const idx = messages.value.indexOf(aiMsg)
                if (idx >= 0) messages.value.splice(idx, 1)
                messages.value.push({ isUser: false, content: '已取消生成。', isError: true })
            }
        } else {
            aiMsg.isStreaming = false
            if (!aiMsg.content) {
                aiMsg.content = '抱歉，AI 服务暂时不可用，请稍后再试。'
            }
            aiMsg.isError = true
        }
    } finally {
        isStreaming.value = false
        abortController = null
    }
}

// ---- 停止流式输出 ----
function stopStreaming() {
    if (abortController) {
        abortController.abort()
        abortController = null
    }
}
</script>

<style scoped>
/* 隐藏消息区滚动条，但保持可滚动 */
.message-area::-webkit-scrollbar {
    display: none;
}
.message-area {
    scrollbar-width: none;
}

/* 隐藏输入框滚动条 */
.scrollbar-hide::-webkit-scrollbar {
    display: none;
}
.scrollbar-hide {
    -ms-overflow-style: none;
    scrollbar-width: none;
}

/* 小幅度弹跳动画（原 animate-bounce 幅度太大） */
@keyframes bounce-sm {
    0%, 100% {
        transform: translateY(0);
    }
    50% {
        transform: translateY(-4px);
    }
}
.animate-bounce-sm {
    animation: bounce-sm 0.6s ease-in-out infinite;
}

/* 移动端气泡宽度 */
@media (max-width: 640px) {
    .bubble-max {
        max-width: 88%;
    }
}
@media (min-width: 641px) {
    .bubble-max {
        max-width: 75%;
    }
}
</style>

<!-- Markdown 正文样式（非 scoped，因为 v-html 插入的内容需要全局样式） -->
<style>
.markdown-body {
    font-size: 0.875rem;
    line-height: 1.7;
    color: inherit;
    word-break: break-word;
}
.markdown-body p {
    margin: 0 0 0.5em 0;
}
.markdown-body p:last-child {
    margin-bottom: 0;
}
.markdown-body strong {
    font-weight: 700;
    color: inherit;
}
.markdown-body em {
    font-style: italic;
}
.markdown-body ol {
    list-style: decimal;
    padding-left: 1.5em;
    margin: 0.25em 0 0.5em 0;
}
.markdown-body ul {
    list-style: disc;
    padding-left: 1.5em;
    margin: 0.25em 0 0.5em 0;
}
.markdown-body li {
    margin-bottom: 0.25em;
}
.markdown-body li:last-child {
    margin-bottom: 0;
}
.markdown-body code {
    font-family: 'SFMono-Regular', Consolas, 'Liberation Mono', Menlo, monospace;
    font-size: 0.8em;
    padding: 0.15em 0.35em;
    background-color: #f3f4f6;
    border-radius: 4px;
    color: #e11d48;
}
.dark .markdown-body code {
    background-color: #374151;
    color: #fca5a5;
}
.markdown-body pre {
    margin: 0.5em 0;
    padding: 0.75em 1em;
    background-color: #f3f4f6;
    border-radius: 8px;
    overflow-x: auto;
    font-size: 0.8em;
    line-height: 1.5;
}
.dark .markdown-body pre {
    background-color: #1f2937;
}
.markdown-body pre code {
    background: none;
    padding: 0;
    color: inherit;
    font-size: inherit;
}
.markdown-body blockquote {
    border-left: 3px solid #d1d5db;
    padding-left: 0.75em;
    margin: 0.5em 0;
    color: #6b7280;
}
.dark .markdown-body blockquote {
    border-left-color: #4b5563;
    color: #9ca3af;
}
.markdown-body h1,
.markdown-body h2,
.markdown-body h3,
.markdown-body h4 {
    font-weight: 700;
    margin: 0.75em 0 0.25em 0;
    line-height: 1.4;
}
.markdown-body h1 { font-size: 1.2em; }
.markdown-body h2 { font-size: 1.1em; }
.markdown-body h3 { font-size: 1em; }
.markdown-body a {
    color: #2563eb;
    text-decoration: underline;
}
.dark .markdown-body a {
    color: #60a5fa;
}
.markdown-body hr {
    border: none;
    border-top: 1px solid #e5e7eb;
    margin: 0.75em 0;
}
.dark .markdown-body hr {
    border-top-color: #374151;
}
</style>
