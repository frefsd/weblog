/**
 * 路由级 SEO 元信息。
 *
 * 说明：SPA 的 HTML 是空壳，百度蜘蛛不执行 JS，这里的设置对它无效；
 * 它主要服务 Google（能执行 JS）以及微信/知乎等分享卡片的抓取。
 * 要让百度真正读到内容，必须靠构建期预渲染或 SSR。
 */

const SITE_URL = (import.meta.env.VITE_SITE_URL || 'https://www.fanchen.tech').replace(/\/+$/, '')
const SITE_NAME = 'WeBlog'
const DEFAULT_DESC = '记录 Java、Spring Boot、Vue 3 与 AI 实践的个人技术博客。'
const DEFAULT_IMAGE = `${SITE_URL}/cat-logo.svg`

function upsertMeta(attrName, attrValue, content) {
    if (content == null) return
    let el = document.head.querySelector(`meta[${attrName}="${attrValue}"]`)
    if (!el) {
        el = document.createElement('meta')
        el.setAttribute(attrName, attrValue)
        document.head.appendChild(el)
    }
    el.setAttribute('content', content)
}

/**
 * 写入 canonical。
 *
 * index.html 里刻意不预置这个 link——SPA 所有路由共用同一份 HTML，
 * 预置成首页地址会让不执行 JS 的爬虫把每篇文章都当成首页副本，比不写更糟。
 * 所以这里改为"缺失即创建"，由 JS 按当前路由生成。
 */
function setCanonical(url) {
    let link = document.getElementById('canonical-link')
    if (!link) {
        link = document.createElement('link')
        link.id = 'canonical-link'
        link.rel = 'canonical'
        document.head.appendChild(link)
    }
    link.setAttribute('href', url)
}

function absoluteUrl(path) {
    if (!path) return `${SITE_URL}/`
    if (/^https?:\/\//i.test(path)) return path
    return SITE_URL + (path.startsWith('/') ? path : `/${path}`)
}

/**
 * 路由切换时套用通用 SEO 信息（标题 / 分享信息 / canonical）。
 */
export function applyRouteSeo(to) {
    const pageTitle = to?.meta?.title
    const title = pageTitle ? `${pageTitle} - ${SITE_NAME}` : `${SITE_NAME} - 个人技术博客`
    document.title = title

    const url = absoluteUrl(to?.path || '/')

    upsertMeta('name', 'description', DEFAULT_DESC)
    upsertMeta('property', 'og:title', title)
    upsertMeta('property', 'og:description', DEFAULT_DESC)
    upsertMeta('property', 'og:url', url)
    // 必须重置：否则从文章页跳到列表页后，分享卡片仍是上一篇文章的封面
    upsertMeta('property', 'og:image', DEFAULT_IMAGE)
    setCanonical(url)
}

/**
 * 文章详情加载完成后，用文章自身的标题与摘要覆盖通用信息。
 */
export function applyArticleSeo(article) {
    if (!article?.id) return
    const title = `${article.title} - ${SITE_NAME}`
    const desc = article.description || DEFAULT_DESC
    const url = absoluteUrl(`/article/${article.id}`)
    const image = article.titleImage ? absoluteUrl(article.titleImage) : DEFAULT_IMAGE

    document.title = title
    upsertMeta('name', 'description', desc)
    upsertMeta('property', 'og:title', title)
    upsertMeta('property', 'og:description', desc)
    upsertMeta('property', 'og:url', url)
    upsertMeta('property', 'og:image', image)
    setCanonical(url)
}
