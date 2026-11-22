<script setup>
import { shallowRef } from 'vue'
import { ArrowRightIcon, HeartIcon } from '@heroicons/vue/24/outline'
import GameDialog from './GameDialog.vue'

const status = shallowRef('idle')
const dialogVisible = shallowRef(false)

function openGame() {
    dialogVisible.value = true
    status.value = 'playing'
}
</script>

<template>
    <section class="wb-panel game-widget" aria-labelledby="game-heading">
        <div class="game-heading">
            <span class="game-icon"><HeartIcon aria-hidden="true" /></span>
            <h2 id="game-heading">哄女友小游戏</h2>
        </div>
        <p v-if="status === 'won'" class="game-hint">通关了，小暖已经原谅你了。</p>
        <p v-else-if="status === 'lost'" class="game-hint">本轮游戏结束。</p>
        <p v-else class="game-hint">试着用几轮对话哄好小暖。</p>
        <button type="button" class="wb-action game-action" @click="openGame">
            {{ status === 'playing' ? '继续游戏' : status === 'idle' ? '开始游戏' : '再来一次' }} <ArrowRightIcon aria-hidden="true" />
        </button>
        <GameDialog v-model="dialogVisible" @game-status-change="status = $event" />
    </section>
</template>

<style scoped>
.game-widget {
    display: grid;
    gap: 9px;
    padding: 16px 17px;
    background: linear-gradient(135deg, var(--wb-cyan-soft), transparent 70%), var(--wb-panel);
}

.game-heading { display: flex; align-items: center; gap: 8px; font-weight: 700; font-size: 0.95rem; }
.game-icon { display: grid; width: 32px; height: 32px; place-items: center; border-radius: 5px; background: var(--wb-accent-soft); color: var(--wb-accent); }
.game-icon svg { width: 17px; height: 17px; }
.game-hint { color: var(--wb-muted); font-size: 0.82rem; }
.game-action { justify-self: start; min-height: 36px; font-size: 0.8rem; }
</style>
