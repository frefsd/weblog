<script setup>
import Header from '@/layouts/components/Header.vue'
import Footer from '@/layouts/components/Footer.vue'
import TopicDirectory from './components/TopicDirectory.vue'
</script>

<template>
    <Header />
    <TopicDirectory kind="tag" />
    <Footer />
</template>
