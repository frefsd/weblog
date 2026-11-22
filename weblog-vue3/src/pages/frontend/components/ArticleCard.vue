<script setup>
import { computed, shallowRef, watch } from 'vue'
import { ArrowUpRightIcon, CodeBracketIcon, FolderIcon } from '@heroicons/vue/24/outline'

import { highlightKeyword } from '@/composables/util'

const props = defineProps({
    keyword: { type: String, default: '' },
    article: { type: Object, required: true },
})

const coverFailed = shallowRef(false)
const detail = computed(() => ({ path: '/article/detail', query: { articleId: props.article.id } }))
const date = computed(() => String(props.article.createTime ?? '').slice(0, 10))
watch(() => props.article.titleImage, () => { coverFailed.value = false })
</script>

<template>
    <article class="wb-panel article-card">
        <RouterLink :to="detail" class="article-cover" :aria-label="article.title">
            <img v-if="article.titleImage && !coverFailed" :src="article.titleImage" alt="" loading="lazy" decoding="async" @error="coverFailed = true" />
            <CodeBracketIcon v-else class="cover-icon" aria-hidden="true" />
            <span v-if="article.isTop === 1" class="pinned">置顶</span>
            <span class="cover-arrow"><ArrowUpRightIcon aria-hidden="true" /></span>
        </RouterLink>

        <div class="article-copy">
            <div v-if="article.category || date" class="article-meta">
                <RouterLink v-if="article.category" :to="{ path: '/category/list', query: { id: article.category.id, name: article.category.name } }" class="article-category">
                    <FolderIcon aria-hidden="true" /> {{ article.category.name }}
                </RouterLink>
                <time v-if="date" :datetime="date">{{ date }}</time>
            </div>
            <h3>
                <RouterLink :to="detail">
                    <span v-if="keyword" v-html="highlightKeyword(article.title, keyword)"></span>
                    <template v-else>{{ article.title }}</template>
                </RouterLink>
            </h3>
            <p v-if="article.description">
                <span v-if="keyword" v-html="highlightKeyword(article.description, keyword)"></span>
                <template v-else>{{ article.description }}</template>
            </p>
            <div v-if="article.tags?.length" class="article-tags">
                <RouterLink v-for="tag in article.tags" :key="tag.id" :to="{ path: '/tag/list', query: { id: tag.id, name: tag.name } }" class="wb-chip">
                    # {{ tag.name }}
                </RouterLink>
            </div>
        </div>
    </article>
</template>

<style scoped>
.article-card {
    display: flex;
    flex-direction: column;
    overflow: hidden;
    transition: border-color 180ms ease, box-shadow 180ms ease;
}
.article-card:hover,
.article-card:focus-within {
    border-color: color-mix(in srgb, var(--wb-accent) 48%, var(--wb-line));
    box-shadow: 0 12px 36px #0002;
}

.article-cover {
    position: relative;
    display: grid;
    place-items: center;
    overflow: hidden;
    aspect-ratio: 2.3 / 1;
    background:
        linear-gradient(90deg, var(--wb-grid) 1px, transparent 1px) 0 0 / 24px 24px,
        linear-gradient(var(--wb-grid) 1px, transparent 1px) 0 0 / 24px 24px,
        linear-gradient(125deg, var(--wb-raised), var(--wb-cyan-soft), var(--wb-accent-soft));
    border-bottom: 1px solid var(--wb-line);
}

.article-cover:focus-visible { outline-offset: -4px; }
.article-cover img { width: 100%; height: 100%; position: absolute; inset: 0; object-fit: cover; transition: transform 320ms ease; }
.article-cover:hover img { transform: scale(1.035); }
.cover-icon { width: 42px; height: 42px; color: var(--wb-cyan); opacity: 0.78; }
.cover-arrow,
.pinned {
    position: absolute;
    display: inline-grid;
    place-items: center;
    min-height: 32px;
    background: #0b1422df;
    color: #fff;
    border: 1px solid #cfddfa43;
    border-radius: 5px;
}
.pinned { left: 10px; top: 10px; padding-inline: 8px; color: #ffac94; font-size: 0.75rem; font-weight: 700; }
.cover-arrow { width: 32px; right: 10px; bottom: 10px; }
.cover-arrow svg { width: 16px; height: 16px; }
.article-copy { display: flex; flex: 1; flex-direction: column; gap: 9px; padding: 18px; }
.article-meta { display: flex; flex-wrap: wrap; justify-content: space-between; gap: 4px 12px; color: var(--wb-dim); font: 0.76rem/1.5 var(--wb-mono); }
.article-category { display: inline-flex; gap: 5px; align-items: center; color: var(--wb-cyan); }
.article-category svg { width: 15px; height: 15px; flex: none; }
.article-category:hover { text-decoration: underline; }
.article-copy h3 { font: 700 clamp(1.1rem, 1.8vw, 1.37rem)/1.42 var(--wb-display); overflow-wrap: anywhere; }
.article-copy h3 a:hover { color: var(--wb-accent); }
.article-copy p {
    display: -webkit-box;
    overflow: hidden;
    color: var(--wb-muted);
    font-size: 0.89rem;
    overflow-wrap: anywhere;
    -webkit-box-orient: vertical;
    -webkit-line-clamp: 2;
}
.article-copy :deep(mark) { background: var(--wb-accent-soft); color: var(--wb-accent); }
.article-tags { display: flex; flex-wrap: wrap; gap: 6px; margin-top: auto; padding-top: 4px; }
</style>
