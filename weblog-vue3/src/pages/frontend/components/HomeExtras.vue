<script setup>
import { shallowRef } from 'vue'
import { getCategories } from '@/api/frontend/category'
import { getTags } from '@/api/frontend/tag'
import UserInfoCard from '@/components/UserInfoCard.vue'
import GameWidget from './GameWidget.vue'
import TopicLinks from './TopicLinks.vue'

const categories = shallowRef([])
const tags = shallowRef([])

getCategories().then(result => { if (result.success) categories.value = result.data ?? [] }).catch(() => {})
getTags().then(result => { if (result.success) tags.value = result.data ?? [] }).catch(() => {})
</script>

<template>
    <aside class="home-extras" aria-label="作者、主题与小游戏">
        <UserInfoCard />
        <TopicLinks title="分类" path="/category" :entries="categories" />
        <TopicLinks title="标签" path="/tag" :entries="tags" />
        <GameWidget />
    </aside>
</template>

<style scoped>
.home-extras {
    display: grid;
    grid-template-columns: repeat(4, minmax(0, 1fr));
    align-items: start;
    gap: clamp(12px, 2vw, 22px);
    margin-top: clamp(28px, 4.5vw, 58px);
    padding-top: clamp(18px, 3vw, 33px);
    border-top: 1px solid var(--wb-line);
}

@media (max-width: 1040px) { .home-extras { grid-template-columns: repeat(2, minmax(0, 1fr)); } }
@media (max-width: 540px) { .home-extras { grid-template-columns: minmax(0, 1fr); } }
</style>
