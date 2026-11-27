<script setup>
import { computed, ref, shallowRef, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowRightIcon, MagnifyingGlassIcon } from '@heroicons/vue/24/outline'
import { searchArticles } from '@/api/frontend/article'

const props = defineProps({
    modelValue: { type: Boolean, default: false },
})
const emit = defineEmits(['update:modelValue'])
const router = useRouter()
const input = ref(null)
const query = shallowRef('')
const results = shallowRef([])
const total = shallowRef(0)
const loading = shallowRef(false)
const failed = shallowRef(false)
const keyword = computed(() => query.value.trim())

function close() {
    emit('update:modelValue', false)
}

function viewResults() {
    if (!keyword.value) return
    router.push({ path: '/search', query: { q: keyword.value } })
    close()
}

watch(() => props.modelValue, (open) => {
    if (!open) query.value = ''
})

watch(keyword, (value, _, onCleanup) => {
    results.value = []
    total.value = 0
    failed.value = false
    loading.value = Boolean(value)
    if (!value) return

    const controller = new AbortController()
    const timer = setTimeout(async () => {
        try {
            const response = await searchArticles(value, 1, 5, { signal: controller.signal })
            if (controller.signal.aborted) return
            if (response.success) {
                results.value = response.data.records ?? []
                total.value = response.data.total ?? 0
            } else {
                failed.value = true
            }
        } catch {
            if (!controller.signal.aborted) failed.value = true
        } finally {
            if (!controller.signal.aborted) loading.value = false
        }
    }, 250)

    onCleanup(() => {
        clearTimeout(timer)
        controller.abort()
    })
})
</script>

<template>
    <el-dialog
        :model-value="modelValue"
        class="public-search-dialog"
        title="搜索文章"
        width="560px"
        top="min(13vh, 100px)"
        append-to-body
        @update:model-value="emit('update:modelValue', $event)"
        @opened="input?.focus()"
    >
        <form class="search-form" role="search" @submit.prevent="viewResults">
            <el-input ref="input" v-model="query" :prefix-icon="MagnifyingGlassIcon" placeholder="输入标题或摘要关键词" aria-label="搜索关键词" maxlength="120" size="large" clearable />
            <el-button type="primary" native-type="submit" size="large" :disabled="!keyword">搜索</el-button>
        </form>

        <div v-if="keyword" class="quick-results" :aria-busy="loading">
            <p v-if="loading" class="search-hint" role="status">正在搜索…</p>
            <p v-else-if="failed" class="search-hint" role="status">搜索暂不可用，请稍后重试。</p>
            <p v-else-if="!results.length" class="search-hint" role="status">没有找到匹配的文章。</p>
            <template v-else>
                <RouterLink
                    v-for="article in results"
                    :key="article.id"
                    :to="{ name: 'ArticleDetail', params: { id: article.id } }"
                    class="quick-result"
                    @click="close"
                >
                    <span class="result-text">
                        <strong>{{ article.title }}</strong>
                        <small v-if="article.description">{{ article.description }}</small>
                    </span>
                    <ArrowRightIcon aria-hidden="true" />
                </RouterLink>
                <button type="button" class="view-results" @click="viewResults">查看 {{ total }} 条结果</button>
            </template>
        </div>
    </el-dialog>
</template>

<style scoped>
.search-form {
    display: grid;
    grid-template-columns: minmax(0, 1fr) auto;
    gap: 8px;
}

.quick-results {
    margin-top: 12px;
    max-height: min(380px, 54dvh);
    overflow-y: auto;
}

.quick-result {
    display: flex;
    color: var(--wb-text);
    gap: 14px;
    align-items: center;
    justify-content: space-between;
    padding: 10px;
    border-radius: 6px;
    transition: background-color 150ms ease;
}

.quick-result:hover,
.quick-result:focus-visible { background: var(--wb-raised); }
.quick-result svg { width: 16px; height: 16px; flex: none; color: var(--wb-accent); }
.result-text { display: grid; min-width: 0; }
.result-text strong,
.result-text small { overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.result-text small,
.search-hint { color: var(--wb-muted); }
.search-hint { padding: 10px 0; }
.view-results { margin-top: 6px; min-height: 40px; padding: 0 10px; color: var(--wb-accent); font-weight: 650; }
</style>
