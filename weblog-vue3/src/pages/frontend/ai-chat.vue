<script setup>
import { ArrowUpIcon, ArrowUpRightIcon, CpuChipIcon, SparklesIcon, StopIcon, UserCircleIcon } from '@heroicons/vue/24/outline'
import { ref, nextTick, watch, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import Header from '@/layouts/components/Header.vue'
import Footer from '@/layouts/components/Footer.vue'
import { sendChatMessageStreamText, validateSession, getChatHistory } from '@/api/frontend/chat'
import { marked } from 'marked'
import DOMPurify from 'dompurify'

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

// 组件卸载时清理
onUnmounted(() => {
    stopCompletionPolling()
    // 中止未完成的流式请求：否则离开页面后 fetch 仍持续接收 token，
    // 并不断写入已卸载组件的 DOM 引用
    if (abortController) {
        abortController.abort()
        abortController = null
    }
})

// 挂载时从后端校验 session 并加载聊天记录
onMounted(async () => {
    const sid = getSessionId()
    //console.log('[AI Chat] onMounted: localStorage sessionId =', sid)
    if (!sid) {
        // 无 sessionId → 显示欢迎页
        scrollToBottom()
        return
    }
    try {
        const res = await validateSession(sid)
        if (res.success && res.data) {
            if (res.data.valid) {
                // 会话有效 → 从历史接口加载聊天记录
                const hist = await getChatHistory(sid)
                // console.log('[AI Chat] getChatHistory 返回:', JSON.stringify(hist))
                if (hist.success && Array.isArray(hist.data)) {
                    messages.value = hist.data.map(m => ({ ...m, isStreaming: false }))
                } else {
                }
            } else {
                // 会话无效/过期 → 清除本地 sessionId，回到欢迎页
                localStorage.removeItem(SESSION_KEY)
            }
        } else {
        }
    } catch (err) {
    }
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
    if (e.key === 'Enter' && !e.shiftKey && !e.isComposing) {
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
    // AI 回复里可能带出 RAG 检索到的文章片段，必须消毒后再渲染
    return DOMPurify.sanitize(marked.parse(text))
}

// ---- 后台流完成轮询检测（保留但简化：不再依赖 localStorage） ----
let completionCheckTimer = null

function hasIncompleteStreamingMessage() {
    return messages.value.some(m => !m.isUser && m.isStreaming)
}

function startCompletionPolling() {
    if (completionCheckTimer) return
    const CHECK_INTERVAL = 3000
    completionCheckTimer = setInterval(async () => {
        const sid = getSessionId()
        if (!sid) { stopCompletionPolling(); return }
        try {
            const hist = await getChatHistory(sid)
            if (hist.success && Array.isArray(hist.data)) {
                const lastAi = [...hist.data].reverse().find(m => !m.isUser)
                if (lastAi && !lastAi.isStreaming) {
                    messages.value = hist.data.map(m => ({ ...m, isStreaming: false }))
                    stopCompletionPolling()
                }
            }
        } catch (_) { }
    }, CHECK_INTERVAL)
}

function stopCompletionPolling() {
    if (completionCheckTimer) {
        clearInterval(completionCheckTimer)
        completionCheckTimer = null
    }
}

/**
 * 逐字渲染 Markdown，如果待渲染字符超过阈值则跳过动画直接批量渲染。
 * @param {HTMLElement} el 目标 DOM
 * @param {string} fullText 完整文本
 * @param {number} startLen 当前已渲染的长度
 * @param {number} delay 每字延迟（ms），仅在小量时生效
 */
async function renderStreamChunk(el, fullText, startLen, delay = 3) {
    const newChars = fullText.length - startLen
    const BULK_THRESHOLD = 20
    if (newChars > BULK_THRESHOLD) {
        // 累积大量新字符 → 批量渲染，跳过逐字动画
        el.innerHTML = renderMarkdown(fullText)
    } else {
        // 少量新字符 → 逐字动画（正常流式体验）
        for (let i = startLen; i < fullText.length; i++) {
            el.innerHTML = renderMarkdown(fullText.substring(0, i + 1))
            await new Promise(r => setTimeout(r, delay))
        }
    }
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
    console.log('[AI Chat] 发送消息: sessionId=', sessionId, 'question=', text)

    try {
        const response = await sendChatMessageStreamText(sessionId, text, abortController.signal)

        if (!response.ok) {
            throw new Error(`HTTP ${response.status}`)
        }

        const reader = response.body.getReader()
        const decoder = new TextDecoder('utf-8')
        let fullText = ''

        while (true) {
            const { done, value } = await reader.read()
            if (done) break

            fullText += decoder.decode(value, { stream: true })

            // 流式过程中：如果还未收到元数据标记，实时渲染文本内容
            if (!fullText.includes('___META___')) {
                const el2 = aiMsg._el
                if (el2) {
                    await renderStreamChunk(el2, fullText, aiMsg.content.length)
                }
                aiMsg.content = fullText
            }
            // 如果已包含 ___META___，不再继续渲染文本（等流结束后统一解析）
        }

        // 流结束后，从完整响应中提取正文并解析元数据
        if (fullText.includes('___META___')) {
            const cleanEnd = fullText.indexOf('___META___')
            const textPart = fullText.substring(0, cleanEnd)
            aiMsg.content = textPart

            const metaStr = fullText.substring(fullText.lastIndexOf('___META___') + 11).trim()
            try {
                const meta = JSON.parse(metaStr)
                aiMsg.sources = meta.sources || []
                if (meta.sessionId) {
                    //console.log('[AI Chat] 流完成: 设置 sessionId=', meta.sessionId)
                    setSessionId(meta.sessionId)
                }
            } catch (e) {
                console.error('元数据JSON解析失败:', metaStr, e)
            }
        } else {
            aiMsg.content = fullText
        }
        // 标记结束，触发 Vue 切换到 markdown 视图
        aiMsg.isStreaming = false
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

<template>
    <Header />
    <main class="site-width site-page chat-page">
        <section class="wb-panel chat-shell" aria-labelledby="chat-title">
            <header class="chat-header">
                <span class="chat-icon"><CpuChipIcon aria-hidden="true" /></span>
                <div>
                    <h1 id="chat-title">小智</h1>
                    <p>基于博客内容的 AI 助手</p>
                </div>
            </header>
            <div v-if="!messages.length" class="chat-welcome">
                <span class="welcome-icon" aria-hidden="true"><SparklesIcon /></span>
                <h2>今天想了解什么？</h2>
                <div class="chat-suggestions" aria-label="推荐问题">
                    <button v-for="question in suggestedQuestions" :key="question" type="button" class="wb-chip" @click="handleSuggested(question)">
                        {{ question }} <ArrowUpRightIcon aria-hidden="true" />
                    </button>
                </div>
            </div>

            <div v-else ref="messageAreaRef" class="chat-messages" aria-label="对话记录">
                <div v-for="(msg, index) in messages" :key="index" class="chat-row" :class="{ 'is-user': msg.isUser }">
                    <span class="message-icon" aria-hidden="true">
                        <UserCircleIcon v-if="msg.isUser" />
                        <CpuChipIcon v-else />
                    </span>
                    <div class="chat-bubble" :class="{ 'is-error': msg.isError }">
                        <p v-if="msg.isError || msg.isUser" class="plain-message">{{ msg.content }}</p>
                        <div v-else-if="msg.isStreaming" :ref="el => setStreamingRef(el, msg)" class="markdown-body"></div>
                        <div v-else class="markdown-body" v-html="renderMarkdown(msg.content)"></div>

                        <div v-if="msg.isStreaming" class="typing-indicator" role="status" aria-label="小智正在回答">
                            <i></i><i></i><i></i>
                        </div>

                        <div v-if="msg.sources?.length" class="message-sources">
                            <span>参考文章</span>
                            <button v-for="(source, sourceIndex) in msg.sources" :key="sourceIndex" type="button" class="wb-chip" @click="goArticle(source.articleId)">
                                {{ source.articleTitle }} <ArrowUpRightIcon aria-hidden="true" />
                            </button>
                        </div>
                    </div>
                </div>
            </div>

            <form class="chat-composer" @submit.prevent="sendMessage">
                <div class="composer-row">
                    <textarea
                        ref="textareaRef"
                        v-model="inputText"
                        aria-label="输入你的问题"
                        :placeholder="isStreaming ? '小智正在回答…' : '输入你的问题…'"
                        :disabled="isStreaming"
                        rows="1"
                        maxlength="200"
                        @input="onInput"
                        @keydown="onKeydown"
                    ></textarea>
                    <button v-if="!isStreaming" type="submit" class="wb-action wb-action--primary send-message" aria-label="发送问题" :disabled="!inputText.trim()">
                        <ArrowUpIcon aria-hidden="true" />
                    </button>
                    <button v-else type="button" class="wb-icon-button send-message" aria-label="停止生成" @click="stopStreaming">
                        <StopIcon aria-hidden="true" />
                    </button>
                </div>
                <div class="composer-details">
                    <span class="keyboard-hint">Enter 发送 · Shift + Enter 换行</span>
                    <span class="character-count">{{ inputText.length }}/200</span>
                </div>
            </form>
        </section>
    </main>
    <Footer />
</template>

<style scoped>
.chat-page { max-width: 1060px; padding-block: clamp(14px, 3vw, 34px); }
.chat-shell {
    display: flex;
    flex-direction: column;
    height: clamp(550px, calc(100dvh - 148px), 870px);
    overflow: hidden;
    box-shadow: var(--wb-shadow);
}
.chat-header {
    display: flex;
    flex: none;
    align-items: center;
    gap: 10px;
    padding: 14px clamp(13px, 2.6vw, 27px);
    border-bottom: 1px solid var(--wb-line);
}
.chat-header h1 { font: 720 1.1rem/1.25 var(--wb-display); }
.chat-header p { color: var(--wb-muted); font-size: 0.78rem; }
.chat-icon,
.welcome-icon,
.message-icon {
    display: grid;
    flex: none;
    place-items: center;
    color: var(--wb-cyan);
    background: var(--wb-cyan-soft);
    border: 1px solid color-mix(in srgb, var(--wb-cyan) 35%, var(--wb-line));
    border-radius: 7px;
}
.chat-icon { width: 42px; height: 42px; }
.chat-icon svg { width: 22px; height: 22px; }

.chat-welcome,
.chat-messages {
    flex: 1;
    min-height: 0;
    padding: clamp(12px, 3vw, 26px);
    background:
        linear-gradient(var(--wb-grid) 1px, transparent 1px) 0 0 / 34px 34px,
        linear-gradient(90deg, var(--wb-grid) 1px, transparent 1px) 0 0 / 34px 34px,
        radial-gradient(ellipse at 45% 10%, var(--wb-cyan-soft), transparent 55%),
        var(--wb-bg);
}
.chat-welcome { display: flex; flex-direction: column; align-items: center; justify-content: center; text-align: center; gap: 15px; overflow-y: auto; }
.welcome-icon { width: 54px; height: 54px; }
.welcome-icon svg { width: 27px; height: 27px; }
.chat-welcome h2 { font: 720 clamp(1.45rem, 4vw, 2.25rem)/1.35 var(--wb-display); }
.chat-suggestions { display: flex; flex-wrap: wrap; justify-content: center; gap: 7px; max-width: 536px; }
.chat-suggestions button,
.message-sources button { gap: 6px; text-align: left; }
.chat-suggestions svg,
.message-sources svg { width: 13px; height: 13px; flex: none; color: var(--wb-cyan); }

.chat-messages { overflow-y: auto; overscroll-behavior: contain; }
.chat-row { display: flex; gap: 8px; align-items: start; margin-bottom: 12px; }
.chat-row.is-user { flex-direction: row-reverse; }
.message-icon { width: 31px; height: 31px; }
.message-icon svg { width: 16px; height: 16px; }
.is-user .message-icon { background: var(--wb-accent-soft); color: var(--wb-accent); border-color: var(--wb-line); }
.chat-bubble { max-width: min(82%, 680px); min-width: 0; padding: 10px 13px; border: 1px solid var(--wb-line); border-radius: 0 8px 8px; background: var(--wb-panel); overflow-wrap: anywhere; font-size: 0.93rem; }
.is-user .chat-bubble { border-radius: 8px 0 8px 8px; background: var(--wb-accent-soft); }
.chat-bubble.is-error { border-color: var(--el-color-danger); }
.plain-message { white-space: pre-wrap; }

.typing-indicator { display: inline-flex; gap: 4px; align-items: center; min-height: 16px; }
.typing-indicator i { width: 4px; height: 4px; background: var(--wb-cyan); border-radius: 50%; animation: chat-pulse 820ms ease-in-out infinite alternate; }
.typing-indicator i:nth-child(2) { animation-delay: 120ms; }
.typing-indicator i:nth-child(3) { animation-delay: 240ms; }
@keyframes chat-pulse { from { opacity: 0.4; } to { opacity: 1; } }

.message-sources { display: flex; flex-wrap: wrap; align-items: center; gap: 6px; margin-top: 10px; padding-top: 8px; border-top: 1px solid var(--wb-line); }
.message-sources > span { color: var(--wb-muted); font-size: 0.78rem; }
.chat-composer { flex: none; padding: 12px clamp(12px, 2.7vw, 25px) 8px; border-top: 1px solid var(--wb-line); }
.composer-row { display: grid; grid-template-columns: minmax(0, 1fr) auto; align-items: end; gap: 8px; }
.composer-row textarea { width: 100%; resize: none; min-height: 46px; max-height: 120px; padding: 9px 11px; border: 1px solid var(--wb-line); border-radius: 6px; color: var(--wb-text); background: var(--wb-bg); }
.composer-row textarea::placeholder { color: var(--wb-dim); }
.composer-row textarea:focus-visible { outline-offset: 1px; }
.send-message { width: 46px; min-height: 46px; padding: 0; }
.send-message:disabled { background: var(--wb-raised); border-color: var(--wb-line); color: var(--wb-dim); cursor: not-allowed; }
.composer-details { display: flex; justify-content: flex-end; gap: 10px; color: var(--wb-dim); padding-top: 5px; font-size: 0.75rem; }
.character-count { font-variant-numeric: tabular-nums; }
.keyboard-hint { margin-right: auto; }

.chat-bubble :deep(.markdown-body p),
.chat-bubble :deep(.markdown-body ul),
.chat-bubble :deep(.markdown-body ol) { margin-bottom: 0.65em; }
.chat-bubble :deep(.markdown-body :last-child) { margin-bottom: 0; }
.chat-bubble :deep(.markdown-body ul),
.chat-bubble :deep(.markdown-body ol) { list-style: revert; padding-left: 1.5em; }
.chat-bubble :deep(.markdown-body a) { color: var(--wb-cyan); text-decoration: underline; }
.chat-bubble :deep(.markdown-body pre) { overflow: auto; margin: 0.6em 0; padding: 0.65em; background: var(--wb-bg); border: 1px solid var(--wb-line); border-radius: 5px; }
.chat-bubble :deep(.markdown-body code) { font: 0.88em/1.55 var(--wb-mono); }
.chat-bubble :deep(.markdown-body :not(pre) > code) { color: var(--wb-accent); background: var(--wb-accent-soft); padding: 0.1em 0.25em; }
.chat-bubble :deep(.markdown-body blockquote) { border-left: 2px solid var(--wb-accent); padding-left: 0.65em; color: var(--wb-muted); }
.chat-bubble :deep(.markdown-body :is(h1, h2, h3, h4)) { font-weight: 700; margin: 0.8em 0 0.35em; }
.chat-bubble :deep(.markdown-body table) { display: block; overflow-x: auto; }
.chat-bubble :deep(.markdown-body th),
.chat-bubble :deep(.markdown-body td) { padding: 0.28em 0.45em; border: 1px solid var(--wb-line); }

@media (max-width: 560px) {
    .chat-page { width: calc(100% - 20px); }
    .chat-shell { height: max(510px, calc(100dvh - 99px)); }
    .chat-bubble { max-width: calc(100% - 39px); }
}
@media (pointer: coarse) { .keyboard-hint { display: none; } }
</style>
