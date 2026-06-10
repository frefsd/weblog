import axios from '@/axios'

/**
 * 纯文本流式 AI 对话
 * 后端用 StreamingResponseBody 返回纯文本流，前端通过 ReadableStream 逐字读取
 */
export function sendChatMessageStreamText(sessionId, question, signal) {
    return fetch('/api/ai/chat/stream/text', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ sessionId, question }),
        signal
    })
}

/**
 * 校验会话是否有效
 * @param {string|null} sessionId
 * @returns {Promise<{success: boolean, data: {valid: boolean, sessionId: string|null}}>}
 */
export function validateSession(sessionId) {
    return axios.get('/ai/session/validate', {
        params: { sessionId }
    })
}

/**
 * 获取会话的历史聊天记录
 * @param {string} sessionId
 * @returns {Promise<{success: boolean, data: Array}>}
 */
export function getChatHistory(sessionId) {
    return axios.get('/ai/session/history', {
        params: { sessionId }
    })
}
