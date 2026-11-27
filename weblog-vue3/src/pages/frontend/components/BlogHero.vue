<script setup>
import { computed, ref, shallowRef } from 'vue'
import { useDocumentVisibility, useIntersectionObserver } from '@vueuse/core'
import { ArrowDownIcon, MapPinIcon, PauseIcon, PlayIcon, SparklesIcon } from '@heroicons/vue/24/outline'
import cityscape from '@/assets/shanghai-night.webp'

defineProps({
    introduction: { type: String, default: '' },
})

const heroElement = ref(null)
const heroVisible = shallowRef(true)
const motionPaused = shallowRef(false)
const documentVisibility = useDocumentVisibility()
const shouldPauseMotion = computed(() => motionPaused.value || !heroVisible.value || documentVisibility.value !== 'visible')
const motionLabel = computed(() => motionPaused.value ? '播放背景动画' : '暂停背景动画')

useIntersectionObserver(heroElement, ([entry]) => {
    heroVisible.value = entry.isIntersecting
}, { rootMargin: '-60px 0px 0px 0px', threshold: 0 })

function showArticles() {
    const articles = document.getElementById('latest-articles')
    articles?.focus({ preventScroll: true })
    articles?.scrollIntoView({ behavior: window.matchMedia('(prefers-reduced-motion: reduce)').matches ? 'auto' : 'smooth' })
}
</script>

<template>
    <section ref="heroElement" class="blog-hero" :class="{ 'is-motion-paused': shouldPauseMotion }" aria-labelledby="blog-headline">
        <img class="hero-image" :src="cityscape" alt="" width="1672" height="941" fetchpriority="high" />
        <div class="hero-river" aria-hidden="true"></div>
        <div class="site-width hero-content">
            <h1 id="blog-headline">开发笔记<span>与技术实践</span></h1>
            <p v-if="introduction">{{ introduction }}</p>
            <div class="hero-actions">
                <a href="#latest-articles" class="hero-action hero-action--primary" @click.prevent="showArticles">
                    浏览文章 <ArrowDownIcon aria-hidden="true" />
                </a>
                <RouterLink to="/ai" class="hero-action">
                    问小智 <SparklesIcon aria-hidden="true" />
                </RouterLink>
            </div>
        </div>
        <div class="hero-corner">
            <span class="hero-location"><MapPinIcon aria-hidden="true" /> 上海 · 外滩</span>
            <button
                type="button"
                class="hero-motion-toggle"
                :aria-label="motionLabel"
                :title="motionLabel"
                @click="motionPaused = !motionPaused"
            >
                <PlayIcon v-if="motionPaused" aria-hidden="true" />
                <PauseIcon v-else aria-hidden="true" />
            </button>
        </div>
    </section>
</template>

<style scoped>
.blog-hero {
    --hero-height: clamp(350px, 29vw, 450px);
    position: relative;
    isolation: isolate;
    display: grid;
    align-items: center;
    overflow: hidden;
    min-height: var(--hero-height);
    background: #102733;
    color: var(--wb-nav-text);
}

.hero-image {
    position: absolute;
    z-index: -3;
    inset: 0 0 0 auto;
    width: min(80%, calc(var(--hero-height) * 2.18));
    height: 100%;
    object-fit: cover;
    object-position: 72% 43%;
    mask-image: linear-gradient(90deg, transparent, #000 30%);
    transform-origin: 70% 62%;
}

.hero-river {
    position: absolute;
    z-index: -2;
    inset: auto 0 0 28%;
    height: 20%;
    overflow: hidden;
    mask-image: linear-gradient(180deg, transparent, #000 37%, #000 84%, #0009);
    mix-blend-mode: screen;
    pointer-events: none;
}

.hero-river::before,
.hero-river::after {
    position: absolute;
    inset: -24% -12%;
    content: "";
}

.hero-river::before {
    background:
        radial-gradient(ellipse 31% 17% at 69% 55%, #ffab5170, transparent 84%),
        radial-gradient(ellipse 23% 10% at 47% 68%, #ffbf5063, transparent 87%),
        radial-gradient(ellipse 30% 8% at 88% 76%, #ffd09342, transparent 85%);
    opacity: 0.58;
}

.hero-river::after {
    background:
        radial-gradient(ellipse 25% 15% at 85% 46%, #7fb8ee69, transparent 89%),
        radial-gradient(ellipse 27% 9% at 66% 71%, #89aafa56, transparent 86%),
        radial-gradient(ellipse 15% 6% at 90% 68%, #92dbff78, transparent 89%);
    opacity: 0.46;
}

.blog-hero::after {
    content: "";
    position: absolute;
    z-index: -1;
    inset: 0;
    background:
        linear-gradient(90deg, #102733 0%, #102733 15%, #0c2330f0 27%, #0b1e2aaf 37%, #112b3a38 55%, transparent 76%),
        linear-gradient(180deg, #08192333, transparent 29%, transparent 72%, #08192353);
    pointer-events: none;
}

.hero-content { padding-block: clamp(36px, 5vw, 68px); }
.hero-content h1 {
    max-width: 9em;
    padding-inline-start: 0.32em;
    border-inline-start: clamp(4px, 0.52vw, 7px) solid #e58642;
    font: 700 clamp(3.15rem, 5.1vw, 5rem)/1.09 var(--wb-display);
    letter-spacing: 0.015em;
    overflow-wrap: anywhere;
    text-shadow: 0 3px 25px #07172080;
}

.hero-content h1 span { display: block; }
.hero-content p {
    max-width: min(33ch, 57%);
    margin-top: 18px;
    color: #f9ead7;
    font: 500 clamp(1rem, 1.7vw, 1.4rem)/1.65 var(--wb-display);
    overflow-wrap: anywhere;
    text-shadow: 0 2px 14px #05131f;
}

.hero-actions { display: flex; flex-wrap: wrap; gap: 12px; margin-top: clamp(20px, 2.3vw, 32px); }
.hero-action {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
    min-width: clamp(120px, 11vw, 166px);
    min-height: 46px;
    padding: 5px 13px;
    border: 1px solid #fff0dbb5;
    border-radius: 5px;
    background: #0d232ecf;
    color: var(--wb-nav-text);
    font-size: clamp(0.9rem, 1.2vw, 1rem);
    font-weight: 650;
    transition: background-color 180ms ease, border-color 180ms ease, color 180ms ease;
}

.hero-action svg { width: 17px; height: 17px; }
.hero-action:hover { border-color: var(--wb-nav-accent); color: var(--wb-nav-accent); }
.hero-action--primary { background: #a44d1c; border-color: #d87937; color: #fff7e9; }
.hero-action--primary:hover { background: #b15723; color: #fff7e9; }
.hero-action:focus-visible,
.hero-motion-toggle:focus-visible { outline-color: var(--wb-nav-accent); }

.hero-corner {
    position: absolute;
    right: var(--wb-gutter);
    bottom: 9px;
    display: flex;
    align-items: center;
    gap: 6px;
}

.hero-location {
    display: inline-flex;
    align-items: center;
    gap: 4px;
    min-height: 28px;
    padding-inline: 7px;
    border-radius: 4px;
    background: #0b1a24d9;
    color: #f9ead7;
    font-size: 0.71rem;
    white-space: nowrap;
}

.hero-location svg { width: 13px; height: 13px; }

.hero-motion-toggle {
    display: none;
    width: 44px;
    height: 44px;
    place-items: center;
    border: 1px solid #fff0db6b;
    border-radius: 5px;
    background: #0b1a24df;
    color: var(--wb-nav-text);
    cursor: pointer;
    transition: background-color 150ms ease, color 150ms ease;
}

.hero-motion-toggle:hover { background: #102733; color: var(--wb-nav-accent); }
.hero-motion-toggle svg { width: 18px; height: 18px; }

@media (prefers-reduced-motion: no-preference) {
    .hero-motion-toggle { display: grid; }
    .hero-river::before { animation: river-warm 8s ease-in-out -1.5s infinite alternate; }
    .hero-river::after { animation: river-cool 11s ease-in-out -3.5s infinite alternate; }
}

@media (min-width: 761px) and (hover: hover) and (pointer: fine) and (prefers-reduced-motion: no-preference) {
    .hero-image { animation: skyline-drift 15s ease-in-out -2s infinite alternate; }
}

.is-motion-paused .hero-image,
.is-motion-paused .hero-river::before,
.is-motion-paused .hero-river::after { animation-play-state: paused; }

@keyframes skyline-drift {
    from { transform: translate3d(0.2%, 0.15%, 0) scale(1.018); }
    to { transform: translate3d(-1.2%, -0.55%, 0) scale(1.055); }
}

@keyframes river-warm {
    from { transform: translate3d(-6%, 2%, 0) scaleX(0.95); opacity: 0.36; }
    to { transform: translate3d(6%, -1%, 0) scaleX(1.07); opacity: 0.81; }
}

@keyframes river-cool {
    from { transform: translate3d(5%, -2%, 0) scaleX(1.12); opacity: 0.30; }
    to { transform: translate3d(-7%, 1.5%, 0) scaleX(0.94); opacity: 0.72; }
}

@media (max-width: 760px) {
    .blog-hero { --hero-height: clamp(321px, 61vw, 385px); }
    .hero-image { width: 100%; mask-image: none; }
    .hero-river { inset-inline-start: 36%; height: 24%; opacity: 0.65; }
    .blog-hero::after {
        background:
            linear-gradient(90deg, #0d202de8 0%, #0d202dc4 36%, #0d202d38 100%),
            linear-gradient(0deg, #071a25a8, transparent 62%);
    }
    .hero-content { padding-block: 35px 59px; }
    .hero-content h1 { font-size: clamp(2.52rem, 10.5vw, 4rem); }
    .hero-content p { max-width: 23em; margin-top: 14px; }
    .hero-actions { gap: 8px; margin-top: 18px; }
}

@media (max-width: 480px) {
    .hero-image { object-position: 39% 43%; }
}

@media (max-width: 360px) {
    .hero-image { object-position: 28% 43%; }
}

@media (pointer: coarse) {
    .hero-corner { bottom: 5px; }
}
</style>
