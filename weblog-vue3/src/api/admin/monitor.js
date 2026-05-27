import axios from "@/axios"

export function getLogPageList(data) {
    return axios.post("/monitor/log/page", data)
}

export function clearAllLogs() {
    return axios.post("/monitor/log/clear")
}

export function getRuleList() {
    return axios.post("/monitor/rule/list")
}

export function saveRule(data) {
    return axios.post("/monitor/rule/save", data)
}

export function deleteRule(id) {
    return axios.post(`/monitor/rule/delete?id=${id}`)
}

export function getAlertPageList(data) {
    return axios.post("/monitor/alert/page", data)
}
