<script setup>
import Header from './components/Header.vue'
import Footer from './components/Footer.vue'
</script>

<template>
    <div class="public-site">
        <Header />
        <slot />
        <Footer />
    </div>
</template>

<style src="@/assets/public.css"></style>
