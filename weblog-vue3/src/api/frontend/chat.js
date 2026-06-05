/**
 * 流式 AI 对话（SSE 逐字返回）
 * 使用原生 fetch 绕过 axios 7s 超时限制，并支持 ReadableStream 解析 SSE
 *
 * @param {string|null} sessionId - 会话ID
 * @param {string} question - 用户问题
 * @param {AbortSignal} signal - 用于取消请求
 * @returns {Promise<Response>} fetch Response 对象（body 为 ReadableStream）
 */
export function sendChatMessageStream(sessionId, question, signal) {
    return fetch('/api/ai/chat/stream', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ sessionId, question }),
        signal
    })
}
