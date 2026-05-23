import axios from '@/axios'

export function startGame() {
    return axios.post('/game/start')
}

export function sendReply(sessionId, content) {
    return axios.post('/game/reply', { sessionId, content })
}

export function closeGame(sessionId) {
    return axios.post('/game/close', { sessionId })
}
