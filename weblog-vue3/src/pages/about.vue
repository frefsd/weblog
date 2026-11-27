<script setup>
import PublicLayout from '@/layouts/PublicLayout.vue'
import UserInfoCard from '@/components/UserInfoCard.vue'
</script>

<template>
    <PublicLayout>
        <main class="site-width site-page about-page">
            <header class="wb-page-heading"><h1 class="wb-section-title">关于作者</h1></header>
            <UserInfoCard />
        </main>
    </PublicLayout>
</template>

<style scoped>
.about-page { max-width: 740px; }
</style>
