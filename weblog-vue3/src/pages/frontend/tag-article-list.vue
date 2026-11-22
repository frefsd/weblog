<script setup>
import Header from '@/layouts/components/Header.vue'
import Footer from '@/layouts/components/Footer.vue'
import TopicArticles from './components/TopicArticles.vue'
</script>

<template>
    <Header />
    <TopicArticles kind="tag" />
    <Footer />
</template>
