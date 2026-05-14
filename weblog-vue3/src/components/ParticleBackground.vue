<template>
  <canvas ref="canvasRef" class="particle-canvas"></canvas>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'

const canvasRef = ref(null)
let animationId = null
let particles = []
let mouse = { x: null, y: null }
let ctx = null
let width = 0
let height = 0

const COUNT = 90
const CONNECT_DIST = 150
const MOUSE_RADIUS = 200

class Particle {
  constructor() {
    this.reset()
  }
  reset() {
    this.x = Math.random() * width
    this.y = Math.random() * height
    this.vx = (Math.random() - 0.5) * 0.6
    this.vy = (Math.random() - 0.5) * 0.6
    this.r = Math.random() * 1.5 + 0.8
  }
  update() {
    this.x += this.vx
    this.y += this.vy
    if (this.x < 0 || this.x > width) this.vx *= -1
    if (this.y < 0 || this.y > height) this.vy *= -1
    if (mouse.x !== null && mouse.y !== null) {
      const dx = this.x - mouse.x
      const dy = this.y - mouse.y
      const dist = Math.sqrt(dx * dx + dy * dy)
      if (dist < MOUSE_RADIUS && dist > 0) {
        const force = (MOUSE_RADIUS - dist) / MOUSE_RADIUS
        this.x += (dx / dist) * force * 2.5
        this.y += (dy / dist) * force * 2.5
      }
    }
  }
  draw() {
    ctx.beginPath()
    ctx.arc(this.x, this.y, this.r, 0, Math.PI * 2)
    ctx.fillStyle = 'rgba(0, 230, 200, 0.7)'
    ctx.fill()
  }
}

function init() {
  const canvas = canvasRef.value
  ctx = canvas.getContext('2d')
  resize()
  particles = Array.from({ length: COUNT }, () => new Particle())
  animate()
}

function resize() {
  const canvas = canvasRef.value
  width = window.innerWidth
  height = window.innerHeight
  canvas.width = width
  canvas.height = height
}

function animate() {
  ctx.clearRect(0, 0, width, height)
  for (const p of particles) {
    p.update()
    p.draw()
  }
  for (let i = 0; i < particles.length; i++) {
    for (let j = i + 1; j < particles.length; j++) {
      const dx = particles[i].x - particles[j].x
      const dy = particles[i].y - particles[j].y
      const dist = Math.sqrt(dx * dx + dy * dy)
      if (dist < CONNECT_DIST) {
        const opacity = (1 - dist / CONNECT_DIST) * 0.4
        ctx.beginPath()
        ctx.moveTo(particles[i].x, particles[i].y)
        ctx.lineTo(particles[j].x, particles[j].y)
        ctx.strokeStyle = `rgba(0, 230, 200, ${opacity})`
        ctx.lineWidth = 0.5
        ctx.stroke()
      }
    }
  }
  animationId = requestAnimationFrame(animate)
}

function onMouseMove(e) { mouse.x = e.clientX; mouse.y = e.clientY }
function onMouseLeave() { mouse.x = null; mouse.y = null }

onMounted(() => {
  init()
  window.addEventListener('resize', resize)
  window.addEventListener('mousemove', onMouseMove)
  window.addEventListener('mouseleave', onMouseLeave)
})

onBeforeUnmount(() => {
  cancelAnimationFrame(animationId)
  window.removeEventListener('resize', resize)
  window.removeEventListener('mousemove', onMouseMove)
  window.removeEventListener('mouseleave', onMouseLeave)
})
</script>

<style scoped>
.particle-canvas {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 0;
}
</style>
