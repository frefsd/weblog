<script setup>
import { ArrowLeftIcon, MagnifyingGlassIcon } from '@heroicons/vue/24/outline'
import PublicLayout from '@/layouts/PublicLayout.vue'
</script>

<template>
    <PublicLayout>
        <main class="site-width site-page missing-page">
            <div class="wb-panel missing-panel">
                <p class="status-code">404</p>
                <h1>页面未找到</h1>
                <p class="missing-help">这个页面可能已移动或删除。</p>
                <div class="missing-actions">
                    <RouterLink to="/" class="wb-action wb-action--primary"><ArrowLeftIcon aria-hidden="true" /> 返回首页</RouterLink>
                    <RouterLink to="/search" class="wb-action"><MagnifyingGlassIcon aria-hidden="true" /> 搜索文章</RouterLink>
                </div>
            </div>
        </main>
    </PublicLayout>
</template>

<style scoped>
.missing-page { display: grid; align-items: center; }
.missing-panel {
    display: grid;
    justify-items: center;
    text-align: center;
    padding: clamp(32px, 7vw, 90px) 16px;
    background:
        linear-gradient(var(--wb-grid) 1px, transparent 1px) 0 0 / 32px 32px,
        linear-gradient(90deg, var(--wb-grid) 1px, transparent 1px) 0 0 / 32px 32px,
        radial-gradient(ellipse at 50% 25%, var(--wb-accent-soft), transparent 62%),
        var(--wb-panel);
}
.status-code { color: var(--wb-accent); font: 770 clamp(4rem, 15vw, 9rem)/1 var(--wb-display); letter-spacing: 0.04em; }
.missing-panel h1 { margin-top: 8px; font: 700 clamp(1.5rem, 4vw, 2.4rem)/1.3 var(--wb-display); }
.missing-help { margin-top: 8px; color: var(--wb-muted); }
.missing-actions { display: flex; flex-wrap: wrap; justify-content: center; gap: 9px; margin-top: 24px; }
</style>
