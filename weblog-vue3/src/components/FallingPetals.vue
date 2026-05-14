<template>
  <canvas
    ref="canvasRef"
    class="falling-petals"
  />
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'

const canvasRef = ref(null)
let animationId = null
let petals = []
let canvasWidth = 0
let canvasHeight = 0

// 花瓣颜色配置
const PETAL_COLORS = [
  { r: 255, g: 183, b: 197, a: 0.6 },  // 浅粉
  { r: 255, g: 158, b: 181, a: 0.5 },  // 粉红
  { r: 252, g: 142, b: 172, a: 0.4 },  // 珊瑚粉
  { r: 255, g: 138, b: 158, a: 0.45 }, // 玫瑰粉
  { r: 248, g: 168, b: 188, a: 0.55 }, // 樱花粉
  { r: 255, g: 192, b: 203, a: 0.35 }, // 淡粉
]

function createPetal(canvasW, canvasH) {
  const color = PETAL_COLORS[Math.floor(Math.random() * PETAL_COLORS.length)]
  return {
    x: Math.random() * (canvasW + 200) - 100,
    y: Math.random() * canvasH - canvasH,
    size: 10 + Math.random() * 16,
    rotation: Math.random() * Math.PI * 2,
    rotationSpeed: (Math.random() - 0.5) * 0.015,
    fallSpeed: 0.5 + Math.random() * 0.8,
    driftAmplitude: 20 + Math.random() * 40,
    driftSpeed: 0.002 + Math.random() * 0.004,
    offsetX: Math.random() * 1000,
    color,
    // 花瓣形态: 0=标准, 1=细长, 2=圆润
    petalType: Math.floor(Math.random() * 3),
    swayOffset: Math.random() * Math.PI * 2,
    opacity: 0.35 + Math.random() * 0.4,
  }
}

function drawPetal(ctx, p) {
  ctx.save()
  ctx.translate(p.x, p.y)
  ctx.rotate(p.rotation)

  const baseSize = p.size
  // 根据形态调整宽高比
  let w, h
  if (p.petalType === 0) {
    w = baseSize * 0.5
    h = baseSize * 0.55
  } else if (p.petalType === 1) {
    w = baseSize * 0.4
    h = baseSize * 0.6
  } else {
    w = baseSize * 0.55
    h = baseSize * 0.5
  }

  const { r, g, b } = p.color

  // 绘制樱花花瓣形状（底部窄 → 中部宽 → 尖端凹陷）
  ctx.beginPath()
  // 从花瓣底部（花托连接处）开始
  ctx.moveTo(0, h)
  // 右侧曲线：从底部向外扩展再向上收拢至尖端右侧
  ctx.bezierCurveTo(w * 0.5, h * 0.5, w * 0.85, h * 0.1, w * 0.3, -h * 0.65)
  // 尖端右侧→凹陷
  ctx.quadraticCurveTo(w * 0.12, -h * 0.8, 0, -h * 0.7)
  // 凹陷→尖端左侧
  ctx.quadraticCurveTo(-w * 0.12, -h * 0.8, -w * 0.3, -h * 0.65)
  // 左侧曲线：从尖端收拢回到底部
  ctx.bezierCurveTo(-w * 0.85, h * 0.1, -w * 0.5, h * 0.5, 0, h)
  ctx.closePath()

  // 径向渐变填充（中心浅、边缘深，更有立体感）
  const grad = ctx.createRadialGradient(0, h * 0.1, 0, 0, h * 0.15, w * 0.9)
  grad.addColorStop(0, `rgba(${Math.min(r + 30, 255)}, ${Math.min(g + 20, 255)}, ${Math.min(b + 20, 255)}, ${p.opacity + 0.1})`)
  grad.addColorStop(0.6, `rgba(${r}, ${g}, ${b}, ${p.opacity})`)
  grad.addColorStop(1, `rgba(${Math.max(r - 30, 0)}, ${Math.max(g - 20, 0)}, ${Math.max(b - 15, 0)}, ${p.opacity - 0.1})`)
  ctx.fillStyle = grad
  ctx.fill()

  // 花瓣脉络：从底部放射状延伸
  ctx.strokeStyle = `rgba(${Math.min(r + 15, 255)}, ${Math.min(g + 10, 255)}, ${Math.min(b + 10, 255)}, ${p.opacity - 0.05})`
  ctx.lineWidth = 0.4

  // 中央主脉
  ctx.beginPath()
  ctx.moveTo(0, h * 0.8)
  ctx.lineTo(0, -h * 0.4)
  ctx.stroke()

  // 左侧副脉
  ctx.beginPath()
  ctx.moveTo(0, h * 0.7)
  ctx.quadraticCurveTo(-w * 0.4, h * 0.1, -w * 0.5, -h * 0.2)
  ctx.stroke()

  // 右侧副脉
  ctx.beginPath()
  ctx.moveTo(0, h * 0.7)
  ctx.quadraticCurveTo(w * 0.4, h * 0.1, w * 0.5, -h * 0.2)
  ctx.stroke()

  // 第二对侧脉（靠近尖端）
  ctx.beginPath()
  ctx.moveTo(0, h * 0.3)
  ctx.quadraticCurveTo(-w * 0.3, -h * 0.2, -w * 0.25, -h * 0.45)
  ctx.stroke()

  ctx.beginPath()
  ctx.moveTo(0, h * 0.3)
  ctx.quadraticCurveTo(w * 0.3, -h * 0.2, w * 0.25, -h * 0.45)
  ctx.stroke()

  ctx.restore()
}

function initPetals(count, canvasW, canvasH) {
  petals = []
  for (let i = 0; i < count; i++) {
    petals.push(createPetal(canvasW, canvasH))
  }
}

function updatePetals() {
  for (const p of petals) {
    // 垂直下落
    p.y += p.fallSpeed

    // 水平摇摆（模拟风吹）
    p.offsetX += p.driftSpeed
    p.x += Math.sin(p.offsetX + p.swayOffset) * 0.3

    // 旋转
    p.rotation += p.rotationSpeed

    // 超出底部后重置到顶部
    if (p.y > canvasHeight + p.size) {
      Object.assign(p, createPetal(canvasWidth, canvasHeight))
      p.y = -p.size * 2
    }

    // 超出左右边界后从另一侧出现
    if (p.x < -p.size * 3) p.x = canvasWidth + p.size
    if (p.x > canvasWidth + p.size * 3) p.x = -p.size
  }
}

function animate(ctx) {
  ctx.clearRect(0, 0, canvasWidth, canvasHeight)
  updatePetals()
  for (const p of petals) {
    drawPetal(ctx, p)
  }
  animationId = requestAnimationFrame(() => animate(ctx))
}

function getPetalCount() {
  const w = window.innerWidth
  if (w < 640) return 10
  if (w < 1024) return 15
  return 22
}

function initCanvas() {
  const canvas = canvasRef.value
  if (!canvas) return

  const ctx = canvas.getContext('2d')
  canvasWidth = window.innerWidth
  canvasHeight = window.innerHeight

  const dpr = window.devicePixelRatio || 1
  canvas.width = canvasWidth * dpr
  canvas.height = canvasHeight * dpr
  canvas.style.width = canvasWidth + 'px'
  canvas.style.height = canvasHeight + 'px'
  ctx.scale(dpr, dpr)

  initPetals(getPetalCount(), canvasWidth, canvasHeight)
  animate(ctx)
}

function handleResize() {
  if (animationId) cancelAnimationFrame(animationId)

  const canvas = canvasRef.value
  if (!canvas) return

  const ctx = canvas.getContext('2d')
  canvasWidth = window.innerWidth
  canvasHeight = window.innerHeight

  const dpr = window.devicePixelRatio || 1
  canvas.width = canvasWidth * dpr
  canvas.height = canvasHeight * dpr
  canvas.style.width = canvasWidth + 'px'
  canvas.style.height = canvasHeight + 'px'
  ctx.scale(dpr, dpr)

  const count = getPetalCount()
  if (petals.length < count) {
    while (petals.length < count) {
      petals.push(createPetal(canvasWidth, canvasHeight))
    }
  } else if (petals.length > count) {
    petals.length = count
  }

  animate(ctx)
}

// 页面不可见时暂停动画
function handleVisibilityChange() {
  if (document.hidden) {
    if (animationId) {
      cancelAnimationFrame(animationId)
      animationId = null
    }
  } else {
    const canvas = canvasRef.value
    if (canvas) {
      const ctx = canvas.getContext('2d')
      animate(ctx)
    }
  }
}

onMounted(() => {
  initCanvas()
  window.addEventListener('resize', handleResize)
  document.addEventListener('visibilitychange', handleVisibilityChange)
})

onUnmounted(() => {
  if (animationId) cancelAnimationFrame(animationId)
  window.removeEventListener('resize', handleResize)
  document.removeEventListener('visibilitychange', handleVisibilityChange)
})
</script>

<style scoped>
.falling-petals {
  position: fixed;
  top: 0;
  left: 0;
  z-index: -1;
  pointer-events: none;
}
</style>
