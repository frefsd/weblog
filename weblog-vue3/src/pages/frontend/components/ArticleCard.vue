<script setup>
import { computed, shallowRef, watch } from 'vue'
import { CodeBracketIcon } from '@heroicons/vue/24/outline'
import { highlightKeyword } from '@/composables/util'

const props = defineProps({
    article: { type: Object, required: true },
    headingTag: { type: String, default: 'h2' },
    keyword: { type: String, default: '' },
    lead: { type: Boolean, default: false },
})

const coverFailed = shallowRef(false)
const detail = computed(() => ({ name: 'ArticleDetail', params: { id: props.article.id } }))
const date = computed(() => String(props.article.createTime ?? '').slice(0, 10))

watch(() => props.article.titleImage, () => { coverFailed.value = false })
</script>

<template>
    <article class="article-card" :class="{ 'article-card--lead': lead }">
        <RouterLink :to="detail" class="article-cover" :aria-label="article.title">
            <img
                v-if="article.titleImage && !coverFailed"
                :src="article.titleImage"
                alt=""
                :loading="lead ? 'eager' : 'lazy'"
                decoding="async"
                @error="coverFailed = true"
            />
            <CodeBracketIcon v-else class="cover-icon" aria-hidden="true" />
        </RouterLink>

        <div class="article-copy">
            <component :is="headingTag" class="article-title">
                <RouterLink :to="detail">
                    <span v-if="keyword" v-html="highlightKeyword(article.title, keyword)"></span>
                    <template v-else>{{ article.title }}</template>
                </RouterLink>
            </component>
            <p v-if="article.description" class="article-description">
                <span v-if="keyword" v-html="highlightKeyword(article.description, keyword)"></span>
                <template v-else>{{ article.description }}</template>
            </p>
            <div v-if="article.isTop === 1 || article.category || date" class="article-meta">
                <span v-if="article.isTop === 1" class="pinned">置顶</span>
                <RouterLink v-if="article.category" :to="{ path: '/category/list', query: { id: article.category.id, name: article.category.name } }" class="article-category">
                    {{ article.category.name }}
                </RouterLink>
                <time v-if="date" :datetime="date">{{ date }}</time>
            </div>
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
    display: grid;
    grid-template-columns: minmax(0, min(34%, 224px)) minmax(0, 1fr);
    align-items: start;
    gap: clamp(10px, 1.6vw, 24px);
    min-width: 0;
    padding-block: clamp(13px, 1.8vw, 23px);
    border-top: 1px solid var(--wb-line);
}

.article-card--lead {
    grid-template-columns: minmax(0, 1fr);
    gap: 13px;
    padding: 0;
    border-top: 0;
}

.article-cover {
    position: relative;
    display: grid;
    place-items: center;
    overflow: hidden;
    aspect-ratio: 1.62;
    border-radius: 6px;
    background:
        repeating-linear-gradient(118deg, transparent 0 29px, #f8d09b15 30px, transparent 31px 60px),
        linear-gradient(145deg, #102733, #264552 64%, #6a4536);
    color: #f1c598;
}

.article-card--lead .article-cover { aspect-ratio: var(--lead-cover-ratio, 3.5); }
.article-cover:focus-visible { outline-offset: 3px; }
.article-cover img {
    position: absolute;
    inset: 0;
    width: 100%;
    height: 100%;
    object-fit: cover;
    transition: transform 240ms ease;
}

.cover-icon { width: clamp(20px, 5vw, 42px); height: clamp(20px, 5vw, 42px); }
.article-copy { display: grid; min-width: 0; align-content: start; gap: 6px; }
.article-title {
    margin: 0;
    font: 700 clamp(1.25rem, 2.1vw, 2rem)/1.22 var(--wb-display);
    overflow-wrap: anywhere;
}
.article-card--lead .article-title { font-size: clamp(1.9rem, 3.35vw, 3.05rem); }
.article-title a,
.article-category { transition: color 150ms ease; }
.article-title a:hover,
.article-title a:focus-visible,
.article-category { color: var(--wb-accent); }
.article-category:hover { text-decoration: underline; text-underline-offset: 3px; }
.article-description {
    display: -webkit-box;
    margin: 0;
    overflow: hidden;
    color: var(--wb-muted);
    font-size: clamp(0.88rem, 1.2vw, 1.02rem);
    line-height: 1.55;
    overflow-wrap: anywhere;
    -webkit-box-orient: vertical;
    -webkit-line-clamp: 2;
}

.article-meta {
    display: flex;
    min-width: 0;
    flex-wrap: wrap;
    align-items: baseline;
    gap: 4px 12px;
    color: var(--wb-dim);
    font-size: 0.78rem;
    font-variant-numeric: tabular-nums;
    overflow-wrap: anywhere;
}

.pinned { color: var(--wb-accent); font-weight: 700; }
.article-copy :deep(mark) { border-radius: 2px; background: var(--wb-accent-soft); color: var(--wb-accent); }
.article-tags { display: flex; flex-wrap: wrap; gap: 4px 6px; margin-top: 2px; }
.article-tags .wb-chip { background: transparent; }

@media (hover: hover) and (pointer: fine) and (prefers-reduced-motion: no-preference) {
    .article-cover:hover img { transform: scale(1.035); }
}

@media (max-width: 640px) {
    .article-card { grid-template-columns: minmax(0, min(32%, 142px)) minmax(0, 1fr); }
    .article-card--lead { grid-template-columns: minmax(0, 1fr); }
}
</style>
