/**
 * 构建期预渲染（Prerender）。
 *
 * 背景：本站是 Vue SPA，构建产物 index.html 只有一个空的 <div id="app">。
 * 百度蜘蛛不执行 JavaScript，抓到的就是空壳，因此不会有任何文章被收录。
 *
 * 做法：构建完成后，用无头 Chrome 逐个打开站点路由，等页面数据加载完，
 * 把渲染后的完整 DOM 写回 dist/<route>/index.html。
 * 爬虫从此能直接读到标题、摘要与正文；真实用户访问时 JS 仍会接管（Vue 重新挂载）。
 *
 * 前置条件：
 *   1. 必须先 `vite build`（dist 是干净的壳）
 *   2. 后端已启动（默认 http://127.0.0.1:8081），用于拉 sitemap 和页面数据
 *   3. 本机有 Chrome / Edge（或用 PUPPETEER_EXECUTABLE_PATH 指定）
 *
 * 用法：
 *   npm run build:seo        # 构建 + 预渲染（生产构建用这个）
 *   npm run prerender        # 只预渲染（dist 必须刚构建过）
 *
 * 环境变量：
 *   PRERENDER_API      后端地址，默认 http://127.0.0.1:8081
 *   PRERENDER_PORT     本地静态服务端口，默认 4173
 *   VITE_SITE_URL      站点根地址，默认 https://www.fanchen.tech
 */

import http from 'node:http'
import { createReadStream, existsSync } from 'node:fs'
import { mkdir, readFile, stat, writeFile } from 'node:fs/promises'
import path from 'node:path'
import { fileURLToPath } from 'node:url'
import puppeteer from 'puppeteer-core'

const ROOT = path.resolve(fileURLToPath(new URL('..', import.meta.url)))
const DIST = path.join(ROOT, 'dist')
const PORT = Number(process.env.PRERENDER_PORT || 4173)
const ORIGIN = `http://127.0.0.1:${PORT}`
const API_TARGET = process.env.PRERENDER_API || 'http://127.0.0.1:8081'
const SITE_URL = (process.env.VITE_SITE_URL || 'https://www.fanchen.tech').replace(/\/+$/, '')

/** 页面实质内容少于该字符数视为渲染失败，不写盘（目录页本身文字就少，阈值不能太高） */
const MIN_CONTENT_LENGTH = 20

const CHROME_CANDIDATES = [
    process.env.PUPPETEER_EXECUTABLE_PATH,
    'C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe',
    'C:\\Program Files (x86)\\Google\\Chrome\\Application\\chrome.exe',
    'C:\\Program Files (x86)\\Microsoft\\Edge\\Application\\msedge.exe',
    'C:\\Program Files\\Microsoft\\Edge\\Application\\msedge.exe',
    '/usr/bin/google-chrome',
    '/usr/bin/google-chrome-stable',
    '/usr/bin/chromium',
    '/usr/bin/chromium-browser',
    '/opt/google/chrome/chrome',
    '/usr/bin/microsoft-edge',
    '/Applications/Google Chrome.app/Contents/MacOS/Google Chrome',
].filter(Boolean)

const MIME = {
    '.html': 'text/html; charset=utf-8',
    '.js': 'text/javascript; charset=utf-8',
    '.mjs': 'text/javascript; charset=utf-8',
    '.css': 'text/css; charset=utf-8',
    '.json': 'application/json; charset=utf-8',
    '.svg': 'image/svg+xml',
    '.png': 'image/png',
    '.jpg': 'image/jpeg',
    '.jpeg': 'image/jpeg',
    '.gif': 'image/gif',
    '.webp': 'image/webp',
    '.avif': 'image/avif',
    '.ico': 'image/x-icon',
    '.woff': 'font/woff',
    '.woff2': 'font/woff2',
    '.ttf': 'font/ttf',
    '.map': 'application/json; charset=utf-8',
    '.txt': 'text/plain; charset=utf-8',
    '.xml': 'application/xml; charset=utf-8',
}

/** 构建出的原始 SPA 壳，仅用于路由 fallback */
let SHELL = ''

function findChrome() {
    for (const candidate of CHROME_CANDIDATES) {
        if (candidate && existsSync(candidate)) return candidate
    }
    throw new Error('未找到 Chrome/Edge，请设置 PUPPETEER_EXECUTABLE_PATH 指向浏览器可执行文件')
}

/** 目录穿越防护：用相对路径判定，而不是字符串前缀（防 dist-x 这类兄弟目录绕过） */
function isUnderDist(target) {
    const rel = path.relative(DIST, target)
    return rel === '' || (!rel.startsWith('..') && !path.isAbsolute(rel))
}

function sendFile(filePath, res) {
    const ext = path.extname(filePath).toLowerCase()
    res.writeHead(200, {
        'Content-Type': MIME[ext] || 'application/octet-stream',
        'Cache-Control': 'no-store',
    })
    createReadStream(filePath).pipe(res)
}

function sendShell(res) {
    res.writeHead(200, { 'Content-Type': MIME['.html'], 'Cache-Control': 'no-store' })
    res.end(SHELL)
}

/** /api/** 转发到后端（剥掉 /api 前缀，与生产 Nginx 行为保持一致） */
function proxyApi(req, res) {
    const suffix = req.url.startsWith('/api/') ? req.url.slice(4) : '/'
    const target = new URL(suffix, API_TARGET)
    const proxyReq = http.request(
        {
            hostname: target.hostname,
            port: target.port,
            path: target.pathname + target.search,
            method: req.method,
            headers: { ...req.headers, host: target.host },
        },
        (proxyRes) => {
            res.writeHead(proxyRes.statusCode || 502, proxyRes.headers)
            proxyRes.pipe(res)
        },
    )
    proxyReq.setTimeout(20000, () => proxyReq.destroy(new Error('后端响应超时')))
    proxyReq.on('error', (err) => {
        if (res.headersSent) return
        res.writeHead(502, { 'Content-Type': 'text/plain; charset=utf-8' })
        res.end(`代理后端失败: ${err.message}`)
    })
    req.pipe(proxyReq)
}

async function serveStatic(req, res) {
    const rawUrl = req.url || '/'

    if (rawUrl === '/api' || rawUrl.startsWith('/api/')) {
        proxyApi(req, res)
        return
    }

    let urlPath
    try {
        urlPath = decodeURIComponent(rawUrl.split('?')[0])
    } catch {
        res.writeHead(400)
        res.end('bad url')
        return
    }

    const resolved = path.join(DIST, urlPath)
    if (!isUnderDist(resolved)) {
        res.writeHead(403)
        res.end()
        return
    }

    try {
        const info = await stat(resolved)
        if (info.isFile()) {
            sendFile(resolved, res)
            return
        }
        if (info.isDirectory()) {
            const indexFile = path.join(resolved, 'index.html')
            if (existsSync(indexFile)) {
                sendFile(indexFile, res)
                return
            }
        }
    } catch {
        // 落到下面的分支
    }

    // 带扩展名的静态资源缺失 → 真 404（否则浏览器会因 text/html 报 MIME 错误，掩盖问题）
    if (path.extname(urlPath)) {
        res.writeHead(404, { 'Content-Type': 'text/plain; charset=utf-8' })
        res.end('404 Not Found')
        return
    }

    // 无扩展名的前端路由 → 返回构建出的原始壳
    sendShell(res)
}

/** 从后端 sitemap 读取待渲染的 URL 列表 */
function fetchSitemapUrls() {
    return new Promise((resolve, reject) => {
        const target = new URL('/sitemap.xml', API_TARGET)
        const req = http.get(target, (res) => {
            if (res.statusCode !== 200) {
                reject(new Error(`拉取 sitemap 失败: HTTP ${res.statusCode}（后端是否已启动？）`))
                res.resume()
                return
            }
            let body = ''
            res.setEncoding('utf8')
            res.on('data', (chunk) => { body += chunk })
            res.on('end', () => {
                const urls = [...body.matchAll(/<loc>(.*?)<\/loc>/g)].map((m) => m[1])
                resolve(urls)
            })
        })
        req.setTimeout(15000, () => req.destroy(new Error('拉取 sitemap 超时')))
        req.on('error', reject)
    })
}

/**
 * 去掉 <html class="dark">：主题由 theme.js 在浏览器里按 localStorage 决定，
 * 若把预渲染时的深色类固化进 HTML，浅色用户首屏会闪一下暗色。
 */
function stripThemeClass(html) {
    return html.replace(/<html([^>]*)>/, (match, attrs) => {
        const cleaned = attrs.replace(/\s*class="([^"]*)"/, (m, cls) => {
            const rest = cls.split(/\s+/).filter((c) => c && c !== 'dark').join(' ')
            return rest ? ` class="${rest}"` : ''
        })
        return `<html${cleaned}>`
    })
}

async function renderPage(browser, route) {
    const page = await browser.newPage()
    try {
        await page.setViewport({ width: 1280, height: 1000 })
        // 标记预渲染请求：后端据此跳过阅读量自增与访客记录，避免污染真实数据
        await page.setExtraHTTPHeaders({ 'X-Prerender': '1' })

        await page.goto(ORIGIN + route, { waitUntil: 'networkidle0', timeout: 45000 })

        // 等真实内容：不能只看 #app 有子节点——骨架屏首帧就有子节点。
        // 判据 = 骨架屏已消失 + 有实质文本 + 不处于加载/错误态。
        // 用骨架屏而不是字数，是因为 /category、/about 这类目录页本身文字就少。
        await page.waitForFunction(
            () => {
                const app = document.querySelector('#app')
                if (!app) return false
                if (document.querySelector('.skeleton')) return false
                const text = (app.innerText || '').replace(/\s+/g, '')
                if (text.length < 20) return false
                return !/正在加载|加载中|加载失败|数据加载失败/.test(text)
            },
            { timeout: 20000 },
        )

        // 给 ECharts / 动画留一点收尾时间
        await new Promise((resolve) => setTimeout(resolve, 300))

        const html = await page.content()
        const contentLength = await page.evaluate(
            () => (document.querySelector('#app')?.innerText || '').trim().length,
        )
        return { html, contentLength }
    } finally {
        await page.close()
    }
}

async function writePage(route, html) {
    const clean = route.replace(/\/+$/, '')
    const dir = clean === '' ? DIST : path.join(DIST, clean)
    await mkdir(dir, { recursive: true })
    await writeFile(path.join(dir, 'index.html'), stripThemeClass(html), 'utf8')
}

async function main() {
    const indexPath = path.join(DIST, 'index.html')
    if (!existsSync(indexPath)) {
        throw new Error('dist 不存在，请先执行 npm run build')
    }

    SHELL = await readFile(indexPath, 'utf8')
    if (SHELL.includes('data-v-app')) {
        throw new Error('dist/index.html 已是预渲染产物，请先执行 npm run build 生成干净的壳')
    }

    const urls = await fetchSitemapUrls()
    const mismatched = urls.find((u) => !u.startsWith(SITE_URL))
    if (mismatched) {
        throw new Error(`sitemap 地址与 VITE_SITE_URL(${SITE_URL}) 不一致: ${mismatched}`)
    }

    const routes = [...new Set(urls.map((u) => u.replace(SITE_URL, '') || '/'))]
    console.log(`[prerender] 待渲染 ${routes.length} 个页面`)

    const server = http.createServer((req, res) => {
        serveStatic(req, res).catch((err) => {
            if (!res.headersSent) {
                res.writeHead(500)
                res.end(String(err))
            }
        })
    })

    await new Promise((resolve, reject) => {
        server.once('error', reject)
        server.listen(PORT, '127.0.0.1', resolve)
    }).catch((err) => {
        throw new Error(`本地静态服务启动失败（端口 ${PORT} 可能被占用）: ${err.message}`)
    })

    const results = []
    let failed = 0

    try {
        const executablePath = findChrome()
        console.log(`[prerender] 使用浏览器: ${executablePath}`)

        const browser = await puppeteer.launch({
            executablePath,
            headless: true,
            args: ['--no-sandbox', '--disable-dev-shm-usage'],
        })

        try {
            for (const route of routes) {
                try {
                    const { html, contentLength } = await renderPage(browser, route)
                    if (contentLength < MIN_CONTENT_LENGTH) {
                        failed++
                        console.error(`  ✗ ${route}  内容过少（${contentLength} 字符），已跳过写盘`)
                        continue
                    }
                    results.push([route, html])
                    console.log(`  ✓ ${route}`)
                } catch (err) {
                    failed++
                    console.error(`  ✗ ${route}  ${err.message}`)
                }
            }
        } finally {
            await browser.close()
        }
    } finally {
        // 无论成功失败都释放端口，否则进程会因 listen handle 常驻而挂死
        await new Promise((resolve) => server.close(resolve))
    }

    // 全部渲染完再统一落盘，避免中途失败留下半成品
    for (const [route, html] of results) {
        await writePage(route, html)
    }

    console.log(`[prerender] 完成：成功 ${results.length} 个，失败 ${failed} 个`)
    if (failed > 0) process.exitCode = 1
}

main().catch((err) => {
    console.error('[prerender] 失败:', err.message)
    process.exitCode = 1
})
