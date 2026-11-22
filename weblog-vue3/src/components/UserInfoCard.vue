<script setup>
import { computed } from 'vue'
import { useStore } from 'vuex'
import { ArrowUpRightIcon } from '@heroicons/vue/24/outline'
import defaultAvatar from '@/assets/milk-cat.png'

const store = useStore()
const setting = computed(() => store.state.setting || {})
const profiles = computed(() => [
    { name: 'GitHub', url: setting.value.githubHome },
    { name: 'Gitee', url: setting.value.giteeHome },
].filter(({ url }) => url))
</script>

<template>
    <section class="wb-panel author-card" aria-label="关于作者">
        <div class="author">
            <img :src="setting.avatar || defaultAvatar" alt="" width="52" height="52" loading="lazy" />
            <div>
                <h2>{{ setting.author || '凡尘' }}</h2>
                <p v-if="setting.blogName">{{ setting.blogName }}</p>
            </div>
        </div>
        <p v-if="setting.introduction" class="author-intro">{{ setting.introduction }}</p>
        <div v-if="profiles.length" class="profiles">
            <a v-for="{ name, url } in profiles" :key="name" :href="url" class="wb-chip" target="_blank" rel="noopener noreferrer">
                {{ name }} <ArrowUpRightIcon aria-hidden="true" />
            </a>
        </div>
    </section>
</template>

<style scoped>
.author-card {
    position: relative;
    display: grid;
    gap: 14px;
    padding: 18px;
    background: linear-gradient(140deg, var(--wb-accent-soft), transparent 65%), var(--wb-panel);
}

.author { display: flex; align-items: center; gap: 11px; min-width: 0; }
.author img { flex: none; border: 1px solid var(--wb-line); border-radius: 8px; background: var(--wb-raised); object-fit: cover; }
.author h2 { font: 700 1.1rem/1.35 var(--wb-display); overflow-wrap: anywhere; }
.author p,
.author-intro { color: var(--wb-muted); font-size: 0.82rem; overflow-wrap: anywhere; }
.profiles { display: flex; flex-wrap: wrap; gap: 8px; }
.profiles .wb-chip { gap: 5px; }
.profiles svg { width: 12px; height: 12px; }
</style>
