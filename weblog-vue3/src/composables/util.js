import nprogress from "nprogress"

export function notification(message, type = 'success', dangerouslyUseHTMLString = false) {
    ElNotification({
        message,
        type,
        dangerouslyUseHTMLString,
        duration: 3000
    })
}

export function showModel(content = '提示内容', type = 'warning', title = '') {
    return ElMessageBox.confirm(
        content,
        title,
        {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type,
        }
    )
}

export function showMessage(message = '提示内容', type = 'success', customClass = '') {
    return ElMessage({
        type: type,
        message,
        customClass,
    })
}

export function showPageLoading() {
    nprogress.start()
}

export function hidePageLoading() {
    nprogress.done()
}

/**
 * HTML 转义
 */
const escapeHtml = (str) => str.replace(/[&<>"']/g, char => ({
    '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;'
}[char]))

/**
 * 高亮文本中匹配的关键词
 * @param {string} text 原文
 * @param {string} keyword 要高亮的关键词
 * @returns {string} 高亮后的 HTML 字符串（用 <mark> 包裹）
 */
export function highlightKeyword(text, keyword) {
    if (!text) return text
    if (!keyword || !keyword.trim()) return escapeHtml(text)
    const escapedText = escapeHtml(text)
    const escapedKeyword = escapeHtml(keyword.trim()).replace(/[.*+?^${}()|[\]\\]/g, '\\$&')
    const regex = new RegExp(`(${escapedKeyword})`, 'gi')
    return escapedText.replace(regex, '<mark class="bg-yellow-200 dark:bg-yellow-600 dark:text-white px-0.5 rounded">$1</mark>')
}