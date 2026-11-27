<script setup>
import { shallowRef, watch } from 'vue'

const props = defineProps({
    target: { type: Object, default: null },
})
const progress = shallowRef(0)

watch(() => props.target, (target, _, onCleanup) => {
    progress.value = 0
    if (!target) return

    let frame = 0
    function update() {
        frame = 0
        const { top, height } = target.getBoundingClientRect()
        const distance = height - window.innerHeight
        progress.value = distance > 0 ? Math.min(1, Math.max(0, -top / distance)) : 0
    }

    function measure() {
        if (!frame) frame = requestAnimationFrame(update)
    }

    const observer = new ResizeObserver(measure)
    observer.observe(target)
    window.addEventListener('scroll', measure, { passive: true })
    window.addEventListener('resize', measure)
    measure()

    onCleanup(() => {
        observer.disconnect()
        window.removeEventListener('scroll', measure)
        window.removeEventListener('resize', measure)
        cancelAnimationFrame(frame)
    })
}, { immediate: true, flush: 'post' })
</script>

<template>
    <div class="reading-progress" aria-hidden="true">
        <div class="reading-progress-bar" :style="{ '--reading-progress': progress }"></div>
    </div>
</template>

<style scoped>
.reading-progress {
    position: sticky;
    z-index: 49;
    top: var(--wb-header-height);
    height: 3px;
    margin-bottom: -3px;
    pointer-events: none;
}

.reading-progress-bar {
    height: 100%;
    transform: scaleX(var(--reading-progress));
    transform-origin: left;
    background: var(--wb-nav-accent);
}
</style>
