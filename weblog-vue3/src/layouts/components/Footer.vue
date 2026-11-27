<script setup>
import { computed } from 'vue'
import { useStore } from 'vuex'
import { CommandLineIcon } from '@heroicons/vue/24/outline'
import filingIcon from '@/assets/beian.png'

const store = useStore()
const brandName = computed(() => store.state.setting?.blogName || 'WeBlog')
</script>

<template>
    <footer class="site-footer">
        <div class="site-width footer-content">
            <RouterLink class="footer-brand" to="/">
                <CommandLineIcon aria-hidden="true" />
                {{ brandName }}
            </RouterLink>
            <div class="footer-links">
                <a href="https://beian.miit.gov.cn/#/Integrated/index" target="_blank" rel="noopener noreferrer">皖ICP备2026016086号-1</a>
                <a href="https://beian.mps.gov.cn/#/query/webSearch?code=34162102003429" target="_blank" rel="noopener noreferrer">
                    <img :src="filingIcon" alt="" width="14" height="14" loading="lazy" />
                    皖公网安备34162102003429号
                </a>
                <a href="https://github.com/frefsd" target="_blank" rel="noopener noreferrer">© 2026 凡尘</a>
            </div>
        </div>
    </footer>
</template>

<style scoped>
.site-footer {
    margin-top: auto;
    border-top: 1px solid var(--wb-line);
    background: var(--wb-panel);
}
.footer-content,
.footer-links {
    display: flex;
    align-items: center;
    flex-wrap: wrap;
}

.footer-content {
    justify-content: space-between;
    gap: 12px 24px;
    padding-block: 24px;
}

.footer-brand,
.footer-links a {
    display: inline-flex;
    align-items: center;
    gap: 7px;
    overflow-wrap: anywhere;
}

.footer-brand { color: var(--wb-text); font: 700 1rem/1.2 var(--wb-display); }
.footer-brand svg { width: 18px; height: 18px; color: var(--wb-accent); }
.footer-links { color: var(--wb-muted); font-size: 0.78rem; gap: 6px 20px; }
.footer-links a { min-height: 32px; transition: color 150ms ease; }
.footer-links a:hover,
.footer-brand:hover { color: var(--wb-accent); }
</style>
