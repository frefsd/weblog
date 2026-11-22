import { readonly, shallowRef } from 'vue'

const isDark = shallowRef(localStorage.getItem('theme') !== 'light')

function applyTheme() {
    document.documentElement.classList.toggle('dark', isDark.value)
}

applyTheme()

export function useTheme() {
    function toggleTheme() {
        isDark.value = !isDark.value
        applyTheme()
        localStorage.setItem('theme', isDark.value ? 'dark' : 'light')
    }

    return { isDark: readonly(isDark), toggleTheme }
}
