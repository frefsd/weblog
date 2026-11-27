<script setup>
import { computed } from 'vue'
import { useStore } from 'vuex'
import PublicLayout from '@/layouts/PublicLayout.vue'
import BlogHero from './components/BlogHero.vue'
import LatestArticles from './components/LatestArticles.vue'
import HomeExtras from './components/HomeExtras.vue'

const store = useStore()
const introduction = computed(() => store.state.setting?.introduction || '')
</script>

<template>
    <PublicLayout>
        <main class="home-page">
            <BlogHero :introduction="introduction" />
            <div class="site-width site-page home-content">
                <LatestArticles />
                <HomeExtras />
            </div>
        </main>
    </PublicLayout>
</template>

<style scoped>
.home-page { flex: 1; min-width: 0; }
.home-content { padding-top: clamp(20px, 2.4vw, 35px); }
</style>
