# AGENTS.md — WeBlog 博客系统

## Project Overview

WeBlog 是一个前后端分离的个人博客系统，集成了 AI 智能问答（RAG + 智谱 GLM）、运维监控告警、文件上传、仪表盘统计等功能。

## 开发与测试协作

本仓库的功能开发、需求变更和缺陷修复由主会话协调 `weblog-architect` 与 `weblog-test-engineer`。单独的测试、回归或发布验收任务直接委派给 `weblog-test-engineer`。

1. 先委派 `weblog-architect` 澄清需求和拟定简短方案，补充涉及的数据结构、接口和验收条件。主会话将待决事项与方案提交给用户，收到方案确认后再进入实现。
2. `weblog-architect` 实现并自查，将已确认的验收条件、改动文件、运行条件和检查结果交回主会话。主会话将交接信息交给 `weblog-test-engineer`，由其独立运行与变更相匹配的自动化、接口或页面测试。
3. 主会话依据测试证据协调架构师修复已确认范围内的缺陷，再请测试工程师复验。交付结论以全部约定验收通过为准。存在失败或必需测试阻塞时，向用户说明影响、证据和需要满足的前置条件。

## Tech Stack

### Backend

- **框架**: Spring Boot 3.5.9 + Java 17
- **ORM**: MyBatis-Plus 3.5.10.1（逻辑删除、分页、驼峰映射）
- **安全**: Spring Security + JWT (jjwt 0.12.5) + Redis 白名单
- **数据库**: MySQL 8.0（Druid 1.2.27 或 HikariCP）
- **缓存**: Spring Data Redis（Lettuce 连接池）
- **文档**: SpringDoc OpenAPI 2.8.9（/swagger-ui.html）
- **对象存储**: Aliyun OSS SDK 3.18.0
- **AI**: 智谱 GLM-4-Flash（RestClient + Jackson，纯 API 调用，非 LangChain4j）

### Frontend

- **框架**: Vue 3.2.47 + Vite 4.1.4 + Vue Router 4 + Vuex 4
- **UI 库**: Element Plus 2.3.3（后台管理）+ Flowbite 1.7.0（前台组件）
- **CSS**: WindiCSS 3.5.6（vite-plugin-windicss 按需加载）+ TailwindCSS 3.3.2
- **HTTP**: Axios 1.3.5（请求拦截器自动注入 Token，响应拦截器处理 401/403）
- **图表**: ECharts 5.4.2（仪表盘统计）
- **编辑器**: mavon-editor / md-editor-v3 / editor.md
- **动画**: GSAP 3.11.5 / animate.css 4.1.1
- **代码高亮**: highlight.js 11.8（tokyo-night-dark 主题）
- **图片预览**: v-viewer + viewerjs 3.0.11
- **工具**: @vueuse/core 10.0.2（useCookies 管理 Token）

## Ports

- **后端**: 8081
- **前端**: 6066（Vite 代理 `/api` → `http://localhost:8081`，自动剥掉 `/api` 前缀）

## Project Structure

```
WeBlog/
├── pom.xml                          # 聚合 POM（Spring Boot 3.5.9）
├── sql/weblog.sql                   # 数据库 DDL（15 张表）
│
├── weblog-common/                   # 公共模块（配置+工具+安全）
│   └── src/main/java/com/blog/
│       ├── config/                  # SecurityConfig, CorsConfig, RedisConfig, AsyncConfig, OpenApiConfig
│       ├── exception/               # BusinessException + GlobalExceptionHandler (@RestControllerAdvice)
│       ├── filter/                  # JwtAuthenticationFilter (OncePerRequestFilter)
│       ├── result/                  # Result<T> {success,message,data} + PageResult<T>
│       └── utils/                   # JwtUtil, AliyunOSSOperator, IpUtil, RedisConstants, RateLimiter, MailNotifier, DingTalkNotifier
│
├── weblog-pojo/                     # 数据模型（25 DTO + 15 Entity + 15 VO）
│
├── weblog-monitor/                  # 监控告警模块
│   ├── aspect/                      # @Aspect 拦截 Controller + 外部 API 请求
│   ├── controller/                  # MonitorController（日志/告警规则/告警记录 CRUD）
│   ├── service/                     # MonitorLogService + AlertService（定时扫描规则）
│   └── LogEvent/LogEventListener    # 事件驱动：AOP → 发布事件 → 异步持久化日志
│
├── weblog-ai/                       # AI 智能问答模块（RAG + 智谱 GLM）
│   ├── config/                      # AiConfig（EmbeddingStore Bean）, AiProperties, VectorIndexInitializer
│   ├── controller/                  # ChatController（/ai/chat, /ai/chat/stream, /ai/session/*）
│   │                                # AdminAiController（/ai/admin/rebuild）
│   ├── service/                     # ChatService（非流式+SSE流式）, RagService（向量检索+索引管理）
│   │                                # ChatMemoryService（会话历史+24h过期）, InMemoryEmbeddingStore
│   ├── event/                       # ArticleChangeEvent（文章变更→增量索引）
│   ├── listener/                    # ArticleIndexListener (@TransactionalEventListener AFTER_COMMIT)
│   ├── mapper/                      # AiChatMemoryMapper
│   └── util/                        # TextSplitter（段落→句子→chunk 分块）
│
├── weblog-server/                   # 主启动模块（聚合所有子模块）
│   ├── controller/admin/            # 7 个管理员接口（文章/分类/标签/用户/仪表盘/博客设置）
│   ├── controller/frontend/         # 7 个前台接口（文章/分类/标签/归档/索引/游戏/博客设置）
│   ├── mapper/                      # 11 个 MyBatis-Plus Mapper
│   ├── service/                     # 14 个 Service 接口+实现
│   ├── listener/                    # ArticleDataProviderImpl（为 AI 模块提供文章数据）
│   └── resources/                   # application.yaml + application-{local,druid,example}.yaml
│
└── weblog-vue3/                     # Vue 3 前端
    ├── vite.config.js               # Vite 配置（proxy, WindiCSS, Element Plus auto-import）
    ├── tailwind.config.js           # TailwindCSS 配置（Flowbite 插件）
    └── src/
        ├── main.js                  # 入口（注册 Element Plus 图标/highlight.js/v-viewer）
        ├── App.vue                  # 根组件（el-config-provider + 路由过渡动画）
        ├── axios.js                 # Axios 实例（Token 注入 + 401/403 拦截 + 7s 超时）
        ├── permission.js            # 路由鉴权守卫（beforeEach: Token 校验 + 加载博客设置）
        ├── router/index.js          # 17 条路由（前台 9 + 后台 8 + 404）
        ├── store/index.js           # Vuex（user + setting + menuWidth）
        ├── composables/             # auth.js（Cookie Token 管理）+ util.js（UI 工具封装）
        ├── api/
        │   ├── admin/               # 7 个后台 API 模块
        │   └── frontend/            # 8 个前台 API 模块（含 chat.js SSE 流式）
        ├── pages/
        │   ├── admin/               # 9 个后台页面（仪表盘/文章/分类/标签/设置/监控日志/告警）
        │   └── frontend/            # 11 个前台页面（首页/文章详情/归档/搜索/AI 聊天/游戏等）
        ├── layouts/                 # admin.vue 布局 + Header/Footer/AdminMenu 等组件
        └── components/              # 骨架屏/图表/花瓣动画/MDEditor/FormDrawer 等

```

## Database Tables（15 tables）

| 表 | 说明 | 关键字段 |
| --- | --- | --- |
| article | 文章表 | id, title, title_image, description, create_time, update_time, is_deleted, read_num |
| article_content | 文章内容表 | id, article_id, content (TEXT) |
| article_category_rel | 文章-分类关联表 | id, article_id, category_id |
| article_tag_rel | 文章-标签关联表 | id, article_id, tag_id |
| category | 分类表 | id, name (UNIQUE), is_deleted |
| tag | 标签表 | id, name (UNIQUE), is_deleted |
| user | 用户表 | id, username, avatar, password (BCrypt), is_deleted |
| user_role | 用户角色表 | id, username, role (ROLE_ADMIN / ROLE_VISITOR) |
| blog_setting | 博客设置 | id, blog_name, author, avatar, social links |
| statistics_article_pv | PV 统计 | id, pv_date (UNIQUE), pv_count |
| visitor_record | 访客记录 | id, visitor, ip_address, ip_region, visit_time |
| monitor_log | 监控日志 | id, service_name, level, type, method, uri, http_method, duration, error_message, ip |
| monitor_alert_rule | 告警规则 | id, name, log_level, time_window, threshold, status |
| monitor_alert_record | 告警记录 | id, rule_id, log_level, trigger_count, threshold, notify_status |
| ai_chat_memory | AI 聊天记录 | id, session_id, user_message, ai_message, sources (JSON), create_time |

All business tables: `is_deleted` logical delete, `create_time`/`update_time` auto-fill, `AUTO_INCREMENT` primary key.

## Build & Run Commands

### Backend

```bash
# Full build
mvn clean install -DskipTags

# Run dev
mvn spring-boot:run -pl weblog-server -DskipTests

# Run with debug security logging
mvn spring-boot:run -pl weblog-server -DskipTests \
  -Dspring-boot.run.jvmArguments="-Dlogging.level.org.springframework.security=DEBUG"
```

### Frontend

```bash
cd weblog-vue3
npm install
npm run dev       # → http://localhost:6066
npm run build     # → dist/
```

## Architecture Patterns

### Authentication Flow

1. Login: username+password → `AuthenticationManager` → JWT signed → stored in Redis whitelist → returned as Bearer token
2. Request: `JwtAuthenticationFilter` extracts Bearer token → parses JWT (`JwtUtil`) → checks Redis whitelist → sets `SecurityContextHolder`
3. Stateless session (`SessionCreationPolicy.STATELESS`), roles: `ROLE_ADMIN` / `ROLE_VISITOR`
4. Token stored in Cookie on frontend via `@vueuse/integrations/useCookies`
5. Axios request interceptor automatically attaches `Authorization: Bearer <token>` header
6. Axios response interceptor: 401 → auto-logout; 403 (empty body) → treat as expired; 403 (JSON) → show permission denied

### Unified Response

- `Result<T>`: `{success, message, data}`
- `PageResult<T>`: `{records, total, current, size, pages}`
- All controllers return `Result<T>` directly
- `GlobalExceptionHandler` handles: `DuplicateKeyException`, `MethodArgumentNotValidException`, `AccessDeniedException`, `BusinessException`

### AI Chat (RAG)

1. **Indexing**: Article publish/update → `ArticleChangeEvent` → `@TransactionalEventListener(AFTER_COMMIT)` → `RagService` updates `InMemoryEmbeddingStore`
2. **Startup**: `VectorIndexInitializer` auto-rebuilds empty vector store on first deploy; `ArticleDataProvider` interface (weblog-ai) → `ArticleDataProviderImpl` (weblog-server) avoids circular dependency
3. **Chat**: User question → `RagService.search()` (cosine similarity, top-K) → `buildPrompt()` (system instruction + history + RAG context) → Zhipu API (non-streaming or SSE streaming)
4. **Session**: 24h expiry in `ChatMemoryServiceImpl.getRecentChats()`; auto-create new session ID on expiry; frontend `localStorage('ai_session_id')`
5. **SSE**: `SseEmitter` + `exchange()` callback for raw stream parsing; `event:chunk` for tokens, `event:done` for completion
6. **Vector Store**: `InMemoryEmbeddingStore` persisted to `data/embedding-store.json`; entries physically removed on delete (no soft-delete transient set)

### Surveillance (AOP Monitoring)

- `ControllerMonitorAspect`: intercepts all `com.blog.controller..*` methods, records URI/method/duration/IP
- Slow requests (>3s) log WARN + publish `LogEvent`
- `LogEventListener` persists to `monitor_log` asynchronously
- `AlertService` scans `monitor_alert_rule` periodically, triggers alert when log count exceeds threshold within time window

### Event-Driven Cross-Module Decoupling

- `ApplicationEventPublisher` + `@TransactionalEventListener(AFTER_COMMIT)` pattern
- `ArticleChangeEvent` → `ArticleIndexListener` (ai module async rebuilds vector index)
- `LogEvent` → `LogEventListener` (monitor module persists request log)

## API Security Pattern

- **Public**: `/ai/chat`, `/ai/chat/stream`, `/article/**`, `/category/**`, `/tag/**`, `/archive/**`, `/blog/**`, `/game/**`, `/index/**` — no auth required
- **Admin**: all other endpoints require JWT authentication + `@PreAuthorize("hasRole('ADMIN')")`
- **Swagger**: public (`/swagger-ui/**`, `/v3/api-docs/**`)

## Frontend Routing (17 routes)

- Frontend pages: `/` (home), `/article/detail`, `/category`, `/tag`, `/category/list`, `/tag/list`, `/archive`, `/search`, `/ai` (chat)
- Admin pages: `/admin` (dashboard), `/admin/article/list`, `/admin/category/list`, `/admin/tag/list`, `/admin/blog/setting`, `/admin/monitor/log`, `/admin/monitor/rule`, `/admin/monitor/alert`

## Key Frontend Conventions

- **All pages** use `<script setup>` Composition API
- **Frontend pages** manually include `<Header>` and `<Footer>` components (no layout wrapper)
- **Admin pages** use `layouts/admin.vue` (sidebar + header + tag tabs + content area)
- **API layer**: one file per domain, `axios.post()` for all requests
- **Dark mode**: `localStorage('theme')` + `document.documentElement.classList.toggle('dark')`
