<template>
    <div class="reading-placeholder" aria-hidden="true">
        <div class="placeholder-heading">
            <div class="skeleton placeholder-category"></div>
            <div class="skeleton placeholder-title"></div>
            <div class="skeleton placeholder-date"></div>
        </div>
        <div class="placeholder-body">
            <div v-for="line in 7" :key="line" class="skeleton placeholder-line"></div>
        </div>
    </div>
</template>

<style scoped>
.placeholder-heading,
.placeholder-body { display: grid; gap: 12px; }
.placeholder-heading { padding-block: 9px 23px; border-bottom: 1px solid var(--wb-line); }
.placeholder-category { width: 110px; height: 19px; }
.placeholder-title { width: 82%; height: clamp(30px, 5vw, 57px); }
.placeholder-date { width: 50%; height: 17px; }
.placeholder-body { margin-block: 24px; }
.placeholder-line { height: 16px; }
.placeholder-line:nth-child(3n) { width: 68%; }
</style>
