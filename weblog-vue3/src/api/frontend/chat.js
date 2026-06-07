/**
 * 是否直接连接后端（绕过 Vite proxy 缓冲，确保流式实时推送）
 * 生产环境改为 false 使用 /api 代理
 */
const DIRECT_BACKEND = true
const BACKEND_URL = DIRECT_BACKEND ? 'http://localhost:8081' : '/api'

/**
 * 纯文本流式 AI 对话
 * 后端用 StreamingResponseBody 返回纯文本流，前端通过 ReadableStream 逐字读取
 */
export function sendChatMessageStreamText(sessionId, question, signal) {
    return fetch(BACKEND_URL + '/ai/chat/stream/text', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ sessionId, question }),
        signal
    })
}
