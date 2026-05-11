<template>
    <footer class="footer">
        Copyright © 2026. All rights reserved. Provided by&nbsp;<a class="fanchen" href="https://github.com/frefsd"
            target="_blank">凡尘</a>
    </footer>
</template>

<style scoped>
.footer {
    @apply text-gray-500 text-sm justify-center flex;
    padding-top: 1.5rem;
    padding-bottom: 1.5rem;
    background: rgba(255, 255, 255, 0.5);
    backdrop-filter: blur(12px);
    -webkit-backdrop-filter: blur(12px);
    margin-top: 5rem;
    border-top: 1px solid rgba(255, 255, 255, 0.3);
    transition: background-color 0.3s ease, border-color 0.3s ease;
}

html.dark .footer {
    background: rgba(30, 30, 50, 0.5);
    border-top: 1px solid rgba(255, 255, 255, 0.06);
}

.quanxiaoha {
    text-decoration: underline;
    color: rgb(52, 152, 219);
}
</style>