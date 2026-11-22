<script setup>
import { ArrowDownIcon, ArrowUpRightIcon, SparklesIcon } from '@heroicons/vue/24/outline'
import cityscape from '@/assets/chongqing-night.webp'

defineProps({
    introduction: { type: String, default: '' },
})

function showArticles() {
    const articles = document.getElementById('latest-articles')
    articles?.focus({ preventScroll: true })
    articles?.scrollIntoView({ behavior: window.matchMedia('(prefers-reduced-motion: reduce)').matches ? 'auto' : 'smooth' })
}
</script>

<template>
    <section class="blog-hero" aria-labelledby="blog-headline">
        <img class="hero-image" :src="cityscape" alt="" width="1920" height="1080" fetchpriority="high" />
        <div class="hero-shade" aria-hidden="true"></div>
        <div class="hero-grid" aria-hidden="true"></div>
        <div class="hero-frame" aria-hidden="true"></div>

        <div class="hero-content">
            <h1 id="blog-headline">开发笔记<span>与技术实践</span></h1>
            <p v-if="introduction">{{ introduction }}</p>
            <div class="hero-actions">
                <a href="#latest-articles" class="wb-action wb-action--primary" @click.prevent="showArticles">
                    浏览文章 <ArrowDownIcon aria-hidden="true" />
                </a>
                <RouterLink to="/ai" class="wb-action">
                    <SparklesIcon aria-hidden="true" /> 问小智
                </RouterLink>
            </div>
        </div>
        <a
            class="photo-credit"
            href="https://unsplash.com/photos/a-view-of-a-city-at-night-from-a-bridge-vMM4_VA8ogw"
            target="_blank"
            rel="noopener noreferrer"
            aria-label="重庆夜景摄影，Albert Canite，打开 Unsplash"
        >重庆夜景 <ArrowUpRightIcon aria-hidden="true" /></a>
    </section>
</template>

<style scoped>
.blog-hero {
    --wb-accent: #ff977c;
    --wb-accent-ink: #21100b;
    --wb-text: #ffffff;
    --wb-muted: #e6edf7;
    --wb-line: #c1d3ee69;
    --wb-dim: #d9e7fb;
    --wb-raised: #1d2e47;
    --wb-cyan: #8aebe6;
    position: relative;
    isolation: isolate;
    display: flex;
    min-height: clamp(400px, 34vw, 482px);
    align-items: center;
    overflow: hidden;
    padding: clamp(32px, 5.1vw, 77px);
    border: 1px solid #7283a74d;
    border-radius: 12px;
    background: #101d31;
    box-shadow: var(--wb-shadow);
}

.hero-image,
.hero-shade,
.hero-grid,
.hero-frame {
    position: absolute;
    inset: 0;
    width: 100%;
    height: 100%;
    pointer-events: none;
}

.hero-image {
    z-index: -3;
    object-fit: cover;
    object-position: 60% 55%;
}

.hero-shade {
    z-index: -2;
    background:
        linear-gradient(90deg, #080b18f7 0%, #0a1020e8 23%, #10192fb8 40%, #192e512b 72%, #0c152c1a 100%),
        linear-gradient(0deg, #090d19c9 0%, transparent 38%, #19234931 100%),
        radial-gradient(ellipse at 82% 22%, #f8523b65, transparent 52%);
}

.hero-grid {
    z-index: -1;
    background-image: linear-gradient(#cef0ff12 1px, transparent 1px), linear-gradient(90deg, #cef0ff12 1px, transparent 1px);
    background-size: 54px 54px;
    mask-image: linear-gradient(90deg, transparent 10%, #000 100%);
}

.hero-frame { inset: 16px; height: auto; width: auto; }

.hero-frame::before,
.hero-frame::after {
    position: absolute;
    width: 27px;
    height: 27px;
    content: "";
}

.hero-frame::before {
    top: 0;
    left: 0;
    border-top: 2px solid var(--wb-accent);
    border-left: 2px solid var(--wb-accent);
    filter: drop-shadow(0 0 7px #ff886a88);
}

.hero-frame::after {
    right: 0;
    bottom: 0;
    border-right: 2px solid #a2f5eea8;
    border-bottom: 2px solid #a2f5eea8;
}

.hero-content {
    width: min(100%, 500px);
    padding-block: 8px;
}

.hero-content h1 {
    color: var(--wb-text);
    font: 820 clamp(2.65rem, 6vw, 5.25rem)/1.12 var(--wb-display);
    letter-spacing: -0.065em;
    text-wrap: balance;
    text-shadow: 0 4px 26px #0008;
}

.hero-content h1 span {
    display: block;
    margin-top: 4px;
    color: var(--wb-accent);
}

.hero-content p {
    margin-top: 22px;
    color: var(--wb-muted);
    max-width: 36ch;
    font-size: clamp(0.95rem, 1.45vw, 1.125rem);
    overflow-wrap: anywhere;
    text-shadow: 0 2px 8px #000a;
}

.hero-actions {
    display: flex;
    flex-wrap: wrap;
    gap: 10px;
    margin-top: 28px;
}

.hero-actions .wb-action { padding-inline: 16px; min-height: 46px; }
.hero-actions .wb-action:not(.wb-action--primary) { background: #0e17279c; }
.hero-actions .wb-action:not(.wb-action--primary):hover { background: var(--wb-raised); }

.photo-credit {
    position: absolute;
    bottom: 27px;
    right: 29px;
    display: inline-flex;
    align-items: center;
    gap: 6px;
    padding: 4px 8px;
    border-radius: 4px;
    background: #101a2abc;
    color: var(--wb-muted);
    font-size: 0.75rem;
}

.photo-credit svg { width: 13px; height: 13px; }
.photo-credit:hover { color: #fff; text-decoration: underline; }

@media (max-width: 620px) {
    .blog-hero { min-height: 420px; padding: 30px 22px 50px; align-items: end; }
    .hero-image { object-position: 72% 55%; }
    .hero-shade {
        background:
            linear-gradient(0deg, #090d1bf7 0%, #10182be9 37%, #1824434a 78%, #34202833),
            linear-gradient(90deg, #090d1b7d, transparent);
    }
    .hero-frame { inset: 10px; }
    .hero-frame::before,
    .hero-frame::after { width: 18px; height: 18px; }
    .hero-content h1 { letter-spacing: -0.035em; }
    .hero-content p { margin-top: 14px; }
    .hero-actions { margin-top: 20px; }
    .photo-credit { bottom: 17px; right: 19px; }
}
</style>
