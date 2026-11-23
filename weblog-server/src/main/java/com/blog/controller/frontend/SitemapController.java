package com.blog.controller.frontend;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blog.entity.Article;
import com.blog.service.IArticleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 站点地图接口。
 *
 * <p>百度/Google 通过 {@code /robots.txt} 里的 Sitemap 声明找到这里。
 * 文章增删后无需重新构建前端，sitemap 始终反映数据库最新状态。</p>
 *
 * <p><b>部署要求：</b>Nginx 需要把 {@code /sitemap.xml} 反代到后端，
 * 否则会被 SPA 的 {@code try_files ... /index.html} 兜底成首页 HTML。</p>
 */
@Tag(name = "SEO 接口", description = "站点地图，供搜索引擎抓取")
@Slf4j
@RestController
@RequiredArgsConstructor
public class SitemapController {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final IArticleService articleService;

    /** 站点根地址，不含结尾斜杠 */
    @Value("${seo.site-url:https://www.fanchen.tech}")
    private String siteUrl;

    @Operation(summary = "站点地图")
    @GetMapping(value = "/sitemap.xml", produces = "application/xml;charset=UTF-8")
    public String sitemap() {
        String base = siteUrl.endsWith("/") ? siteUrl.substring(0, siteUrl.length() - 1) : siteUrl;

        StringBuilder xml = new StringBuilder(2048);
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">\n");

        // 固定页面
        appendUrl(xml, base + "/", null, "daily", "1.0");
        appendUrl(xml, base + "/archive", null, "weekly", "0.8");
        appendUrl(xml, base + "/category", null, "weekly", "0.8");
        appendUrl(xml, base + "/tag", null, "weekly", "0.8");
        appendUrl(xml, base + "/about", null, "monthly", "0.5");

        // 文章详情
        // 注意：全局配置的 logic-delete-field 是 is_deleted，而实体属性名是 isDeleted，两者不匹配
        // （Article 也没有 @TableLogic），因此必须显式过滤，否则已删除文章会进入 sitemap
        List<Article> articles = articleService.list(
                new LambdaQueryWrapper<Article>()
                        .select(Article::getId, Article::getCreateTime, Article::getUpdateTime)
                        .eq(Article::getIsDeleted, 0)
                        .orderByDesc(Article::getUpdateTime)
        );
        for (Article article : articles) {
            LocalDateTime lastModified = article.getUpdateTime() != null
                    ? article.getUpdateTime()
                    : article.getCreateTime();
            appendUrl(xml, base + "/article/" + article.getId(), lastModified, "weekly", "0.7");
        }

        xml.append("</urlset>");
        log.debug("生成 sitemap: {} 个固定页 + {} 篇文章", 5, articles.size());
        return xml.toString();
    }

    private void appendUrl(StringBuilder xml, String loc, LocalDateTime lastModified,
                           String changeFreq, String priority) {
        xml.append("  <url><loc>").append(escapeXml(loc)).append("</loc>");
        if (lastModified != null) {
            xml.append("<lastmod>").append(DATE_FORMATTER.format(lastModified)).append("</lastmod>");
        }
        xml.append("<changefreq>").append(changeFreq).append("</changefreq>");
        xml.append("<priority>").append(priority).append("</priority></url>\n");
    }

    private String escapeXml(String value) {
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }
}
