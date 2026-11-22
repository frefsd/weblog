<script setup>
import { onBeforeUnmount, onMounted, ref, shallowRef, watch } from 'vue'
import * as echarts from 'echarts'
import { useTheme } from '@/composables/theme'

const props = defineProps({
    title: { type: String, required: true },
    fetchData: { type: Function, required: true },
    makeOption: { type: Function, required: true },
    caption: { type: String, default: '' },
    height: { type: Number, default: 226 },
})
const { isDark } = useTheme()
const plot = ref(null)
const data = shallowRef([])
const state = shallowRef('loading')
let chart
let observer

function render() {
    const el = plot.value
    if (!el?.clientWidth || !data.value.length) return

    const styles = getComputedStyle(el)
    const color = (name) => styles.getPropertyValue('--wb-' + name).trim()
    const palette = {
        text: color('text'),
        muted: color('muted'),
        line: color('line'),
        panel: color('panel'),
        raised: color('raised'),
        accent: color('accent'),
        cyan: color('cyan'),
        violet: color('violet'),
        yellow: color('yellow'),
    }
    chart ??= echarts.init(el)
    const option = props.makeOption(data.value, palette)
    chart.setOption({
        backgroundColor: 'transparent',
        animation: !window.matchMedia('(prefers-reduced-motion: reduce)').matches,
        animationDuration: 450,
        textStyle: { color: palette.muted, fontFamily: color('font') },
        ...option,
        tooltip: {
            confine: true,
            renderMode: 'richText',
            backgroundColor: palette.raised,
            borderColor: palette.line,
            textStyle: { color: palette.text },
            ...option.tooltip,
        },
    }, true)
}

async function load() {
    state.value = 'loading'
    try {
        const response = await props.fetchData()
        if (!response.success) {
            state.value = 'failed'
            return
        }
        data.value = response.data ?? []
        state.value = data.value.length ? 'ready' : 'empty'
    } catch {
        state.value = 'failed'
    }
}

watch([data, isDark], render, { flush: 'post' })

onMounted(() => {
    observer = new ResizeObserver(([entry]) => {
        if (!entry.contentRect.width) return
        if (chart) chart.resize()
        else render()
    })
    observer.observe(plot.value)
    load()
})

onBeforeUnmount(() => {
    observer?.disconnect()
    chart?.dispose()
})
</script>

<template>
    <section class="wb-panel chart-panel" :aria-label="title" :aria-busy="state === 'loading'">
        <header class="chart-header">
            <h2>{{ title }}</h2>
            <span v-if="caption">{{ caption }}</span>
        </header>
        <div class="chart-body" :style="{ minHeight: height + 'px' }">
            <div ref="plot" class="chart-plot" :class="{ 'is-ready': state === 'ready' }" role="img" :aria-label="title" :aria-hidden="state !== 'ready'"></div>
            <div v-if="state !== 'ready'" class="wb-state chart-state">
                <span v-if="state === 'loading'" role="status">正在加载…</span>
                <span v-else-if="state === 'empty'">暂无数据</span>
                <template v-else>
                    <span role="status">数据加载失败</span>
                    <button type="button" class="wb-action" @click="load">重试</button>
                </template>
            </div>
        </div>
    </section>
</template>

<style scoped>
.chart-panel { padding: 16px 16px 4px; }
.chart-header { display: flex; align-items: center; justify-content: space-between; gap: 10px; margin-bottom: 7px; }
.chart-header h2 { margin: 0; font: 700 1rem/1.4 var(--wb-display); }
.chart-header span { color: var(--wb-dim); font-size: 0.76rem; white-space: nowrap; }
.chart-body { position: relative; min-width: 0; }
.chart-plot,
.chart-state { position: absolute; inset: 0; }
.chart-plot { visibility: hidden; }
.chart-plot.is-ready { visibility: visible; }
</style>
