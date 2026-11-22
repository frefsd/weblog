<template>
    <footer class="admin-footer">
        <span>© 2026 WeBlog</span>
        <a href="https://github.com/frefsd" target="_blank" rel="noopener noreferrer">凡尘</a>
    </footer>
</template>

<style scoped>
.admin-footer {
    display: flex;
    justify-content: center;
    gap: 8px;
    padding: 16px;
    color: var(--wb-dim);
    border-top: 1px solid var(--wb-line);
    font-size: 0.8rem;
}
.admin-footer a:hover { color: var(--wb-accent); }
</style>
