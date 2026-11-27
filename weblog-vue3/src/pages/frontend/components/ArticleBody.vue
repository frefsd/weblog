<script setup>
import { computed } from 'vue'
import MarkdownIt from 'markdown-it'
import hljs from 'highlight.js'
import 'highlight.js/styles/tokyo-night-dark.css'
import DOMPurify from 'dompurify'

const props = defineProps({
    content: { type: String, required: true },
})

const markdown = new MarkdownIt({ html: true, linkify: true, typographer: true, breaks: true })
const renderedContent = computed(() => DOMPurify.sanitize(markdown.render(props.content), {
    USE_PROFILES: { html: true },
}))
const vHighlight = {
    mounted(element) {
        for (const block of element.querySelectorAll('pre code')) {
            hljs.highlightElement(block)
        }
    },
}
</script>

<template>
    <div v-viewer v-highlight class="article-content" v-html="renderedContent"></div>
</template>

<style scoped>
.article-content { max-width: 74ch; margin: clamp(22px, 4vw, 36px) auto; font-size: clamp(1rem, 1.6vw, 1.11rem); line-height: 1.9; overflow-wrap: anywhere; }
.article-content :deep(p),
.article-content :deep(ul),
.article-content :deep(ol) { margin-block: 0 1.08em; }
.article-content :deep(:is(h1, h2, h3, h4, h5, h6)) {
    margin-block: 1.45em 0.58em;
    color: var(--wb-text);
    font-family: var(--wb-display);
    font-weight: 700;
    line-height: 1.36;
    scroll-margin-top: calc(var(--wb-header-height) + 16px);
}

.article-content :deep(h1) { font-size: 1.75em; }
.article-content :deep(h2) { padding-bottom: 0.28em; border-bottom: 1px solid var(--wb-line); font-size: 1.48em; }
.article-content :deep(h3) { font-size: 1.23em; }
.article-content :deep(ul),
.article-content :deep(ol) { padding-inline-start: 1.5em; }
.article-content :deep(ul) { list-style: disc; }
.article-content :deep(ol) { list-style: decimal; }
.article-content :deep(a) { color: var(--wb-cyan); text-decoration: underline; text-underline-offset: 3px; }
.article-content :deep(blockquote) { margin: 1.2em 0; padding: 0.76em 1em; border-inline-start: 3px solid var(--wb-accent); background: var(--wb-raised); color: var(--wb-muted); }
.article-content :deep(blockquote p:last-child) { margin: 0; }
.article-content :deep(code) { font-family: var(--wb-mono); font-size: 0.9em; }
.article-content :deep(:not(pre) > code) { color: var(--wb-accent); background: var(--wb-accent-soft); border-radius: 3px; padding: 0.1em 0.3em; }
.article-content :deep(pre) {
    max-width: 100%;
    margin: 1.25em 0;
    overflow-x: auto;
    padding: clamp(0.65em, 2.6vw, 1em);
    border: 1px solid #314754;
    border-radius: 6px;
    background: #10212a;
    color: #e4eafa;
    line-height: 1.65;
    overflow-wrap: normal;
}

.article-content :deep(pre code) { padding: 0; background: transparent; }
.article-content :deep(table) { display: block; width: max-content; max-width: 100%; overflow-x: auto; margin: 1.2em 0; border-collapse: collapse; }
.article-content :deep(th),
.article-content :deep(td) { padding: 0.4em 0.7em; border: 1px solid var(--wb-line); }
.article-content :deep(th) { background: var(--wb-raised); }
.article-content :deep(img) { display: block; height: auto; max-width: 100%; border-radius: 6px; margin: 1.2em auto; cursor: zoom-in; }
.article-content :deep(hr) { margin: 1.6em 0; border-color: var(--wb-line); }
</style>
