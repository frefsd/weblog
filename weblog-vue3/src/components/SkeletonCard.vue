<script setup>
defineProps({
    lead: { type: Boolean, default: false },
})
</script>

<template>
    <div class="article-placeholder" :class="{ 'article-placeholder--lead': lead }" aria-hidden="true">
        <div class="skeleton placeholder-cover"></div>
        <div class="placeholder-copy">
            <div class="skeleton placeholder-title"></div>
            <div class="skeleton placeholder-description"></div>
            <div class="skeleton placeholder-detail"></div>
        </div>
    </div>
</template>

<style scoped>
.article-placeholder {
    display: grid;
    grid-template-columns: minmax(0, min(34%, 224px)) minmax(0, 1fr);
    align-items: start;
    gap: clamp(10px, 1.6vw, 24px);
    padding-block: clamp(13px, 1.8vw, 23px);
    border-top: 1px solid var(--wb-line);
}

.article-placeholder--lead {
    grid-template-columns: minmax(0, 1fr);
    gap: 13px;
    padding: 0;
    border-top: 0;
}

.placeholder-cover { aspect-ratio: 1.62; }
.article-placeholder--lead .placeholder-cover { aspect-ratio: var(--lead-cover-ratio, 3.5); }
.placeholder-copy { display: grid; gap: 8px; }
.placeholder-title { width: 72%; height: clamp(20px, 2.4vw, 36px); }
.placeholder-description { width: 94%; height: 16px; }
.placeholder-detail { width: 30%; height: 13px; }

@media (max-width: 640px) {
    .article-placeholder { grid-template-columns: minmax(0, min(32%, 142px)) minmax(0, 1fr); }
    .article-placeholder--lead { grid-template-columns: minmax(0, 1fr); }
}
</style>
