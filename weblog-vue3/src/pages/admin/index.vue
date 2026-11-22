<script setup>
import { shallowRef } from 'vue'
import { ArrowUpRightIcon, ClipboardDocumentListIcon, DocumentTextIcon, EyeIcon, FolderIcon, TagIcon } from '@heroicons/vue/24/outline'
import ArticlePublishChart from '@/components/ArticlePublishChart.vue'
import PVChart from '@/components/PVChart.vue'
import CategoryArticleChart from '@/components/CategoryArticleChart.vue'
import TagArticleChart from '@/components/TagArticleChart.vue'
import { getDashboardArticleStatisticsInfo } from '@/api/admin/dashboard'

const metrics = [
    { label: '文章总数', icon: DocumentTextIcon, key: 'articleTotalCount', tone: 'accent', to: '/admin/article/list' },
    { label: '分类总数', icon: FolderIcon, key: 'categoryTotalCount', tone: 'cyan', to: '/admin/category/list' },
    { label: '标签总数', icon: TagIcon, key: 'tagTotalCount', tone: 'violet', to: '/admin/tag/list' },
    { label: '累计访问', icon: EyeIcon, key: 'pvTotalCount', tone: 'yellow' },
]
const statistics = shallowRef(null)
const state = shallowRef('loading')
const numberFormat = new Intl.NumberFormat('zh-CN')

async function loadStatistics() {
    state.value = 'loading'
    try {
        const response = await getDashboardArticleStatisticsInfo()
        statistics.value = response.success ? response.data : null
        state.value = statistics.value ? 'ready' : 'failed'
    } catch {
        state.value = 'failed'
    }
}
loadStatistics()
</script>

<template>
    <div class="dashboard">
        <header class="wb-panel dashboard-heading">
            <div>
                <h1>仪表盘</h1>
                <p>内容、浏览量与运维概览</p>
            </div>
            <RouterLink to="/admin/monitor/log" class="wb-action">
                <ClipboardDocumentListIcon aria-hidden="true" /> 实时日志
            </RouterLink>
        </header>

        <dl class="metrics" aria-label="博客统计" :aria-busy="state === 'loading'">
            <div v-for="metric in metrics" :key="metric.key" class="wb-panel metric" :class="'tone-' + metric.tone">
                <dt>
                    <span class="metric-icon"><component :is="metric.icon" aria-hidden="true" /></span>
                    {{ metric.label }}
                    <RouterLink v-if="metric.to" :to="metric.to" :aria-label="'查看' + metric.label" class="metric-link">
                        <ArrowUpRightIcon aria-hidden="true" />
                    </RouterLink>
                </dt>
                <dd v-if="state === 'ready'">{{ numberFormat.format(statistics[metric.key] ?? 0) }}</dd>
                <dd v-else-if="state === 'loading'" class="skeleton metric-placeholder" aria-label="正在加载"></dd>
                <dd v-else aria-label="数据不可用">—</dd>
            </div>
        </dl>

        <div v-if="state === 'failed'" class="stats-error" role="status">
            统计数据未加载。 <button type="button" class="wb-action" @click="loadStatistics">重试</button>
        </div>

        <div class="trend-grid">
            <PVChart />
            <ArticlePublishChart />
        </div>
        <div class="topic-grid">
            <CategoryArticleChart />
            <TagArticleChart />
        </div>
    </div>
</template>

<style scoped>
.dashboard { display: grid; gap: 16px; }
.dashboard-heading {
    display: flex;
    flex-wrap: wrap;
    justify-content: space-between;
    align-items: end;
    gap: 14px 22px;
    padding: clamp(20px, 3.2vw, 34px);
    background:
        linear-gradient(90deg, var(--wb-grid) 1px, transparent 1px) 4px 4px / 28px 28px,
        linear-gradient(var(--wb-grid) 1px, transparent 1px) 4px 4px / 28px 28px,
        linear-gradient(104deg, var(--wb-panel) 40%, var(--wb-cyan-soft) 100%),
        var(--wb-panel);
    border-left: 3px solid var(--wb-accent);
}
.dashboard-heading h1 { font: 750 clamp(1.8rem, 3.5vw, 2.75rem)/1.15 var(--wb-display); }
.dashboard-heading p { margin-top: 6px; color: var(--wb-muted); font-size: 0.9rem; }
.dashboard-heading .wb-action { background: var(--wb-panel); }
.dashboard-heading .wb-action:hover { background: var(--wb-raised); }
.metrics { display: grid; grid-template-columns: repeat(4, minmax(0, 1fr)); gap: clamp(9px, 1.25vw, 16px); }
.metric { padding: 13px 15px 15px; }
.tone-accent { --metric-color: var(--wb-accent); }
.tone-cyan { --metric-color: var(--wb-cyan); }
.tone-violet { --metric-color: var(--wb-violet); }
.tone-yellow { --metric-color: var(--wb-yellow); }
.metric dt { display: flex; align-items: center; gap: 8px; color: var(--wb-muted); font-size: 0.83rem; }
.metric-icon { width: 29px; height: 29px; flex: none; display: grid; place-items: center; color: var(--metric-color); background: color-mix(in srgb, var(--metric-color) 13%, var(--wb-panel)); border-radius: 6px; }
.metric-icon svg { width: 16px; height: 16px; }
.metric-link { display: grid; flex: none; margin-left: auto; width: 25px; height: 26px; place-items: center; color: var(--wb-dim); }
.metric-link:hover { color: var(--wb-accent); }
.metric-link svg { width: 14px; height: 14px; }
.metric dd { font: 650 clamp(1.46rem, 2.7vw, 2.3rem)/1.15 var(--wb-display); font-variant-numeric: tabular-nums; overflow-wrap: anywhere; margin-top: 8px; }
.metric-placeholder { width: 42%; height: 2rem; }
.stats-error { color: var(--wb-muted); font-size: 0.85rem; }
.trend-grid,
.topic-grid { display: grid; grid-template-columns: minmax(0, 1.62fr) minmax(0, 1fr); gap: 16px; }
.topic-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); }

@media (max-width: 1100px) {
    .metrics { grid-template-columns: repeat(2, minmax(0, 1fr)); }
}
@media (max-width: 690px) {
    .trend-grid,
    .topic-grid { grid-template-columns: minmax(0, 1fr); }
    .metric { padding: 10px; }
    .metric dt { gap: 5px; }
}
</style>
