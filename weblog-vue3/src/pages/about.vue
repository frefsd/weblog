<script setup>
import Header from '@/layouts/components/Header.vue'
import Footer from '@/layouts/components/Footer.vue'
import UserInfoCard from '@/components/UserInfoCard.vue'
</script>

<template>
    <Header />
    <main class="site-width site-page about-page">
        <header class="wb-page-heading"><h1 class="wb-section-title">关于作者</h1></header>
        <UserInfoCard />
    </main>
    <Footer />
</template>

<style scoped>
.about-page { max-width: 740px; }
</style>
