# WeBlog - Java Web博客系统

## 📖 项目简介

WeBlog 是一个基于 Spring Boot 3 和 Vue 3 构建的现代化前后端分离博客系统，集成了 **AI 智能问答（RAG + 智谱 GLM）**、**运维监控告警**、**文件上传**、**数据统计**等功能，适合个人博客、技术分享平台等场景使用。

该项目的前端界面设计与交互参考了开源项目 [WeBlog](https://gitee.com/AllenJiang/WeBlog)（在此向原作者表示感谢），但后端核心架构、数据库设计及业务逻辑完全由本人独立开发。

本项目旨在记录我的技术成长，同时也是我对 **Spring Boot 3** 与 **Vue 3** 生态的一次深度实践。

##### 访客账号：

- 用户名：vistor
- 密码：vistor

## 🏗️ 系统架构

### 架构概述

WeBlog 采用现代化的前后端分离架构，后端基于 Spring Boot 3.5.9 构建，前端使用 Vue 3.2.47 框架。项目采用 **6 模块 Maven 管理**，包括 AI 智能问答、监控告警等独立模块。

**架构特点**：
- **前后端分离**：前端独立部署，通过 RESTful API 与后端通信
- **多模块设计**：后端拆分为 6 个 Maven 模块，职责清晰
- **分层架构**：遵循 Controller-Service-Mapper 三层架构
- **安全认证**：基于 JWT + Redis 白名单 + Spring Security 的认证授权机制
- **响应式前端**：使用 Vue 3 Composition API + Element Plus + TailwindCSS
- **AI RAG 问答**：基于向量检索 + 智谱 AI API 的智能问答系统
- **事件驱动**：AOP 切面采集日志 + ApplicationEventPublisher 解耦模块依赖
- **流式输出**：Server-Sent Events（SSE）实现 AI 逐 token 输出

### 架构图

```
┌─────────────────────────────────────────────────────────────┐
│                       前端 (Vue 3)                           │
│                       weblog-vue3/                           │
│  ├── api/          # API 接口定义（admin + frontend）       │
│  ├── pages/        # 页面组件（管理员 + 前台）              │
│  ├── layouts/      # 布局组件（Header/Footer/AdminLayout）  │
│  ├── components/   # 公共组件（骨架屏/图表/编辑器）         │
│  ├── router/       # 17 条路由（9 前台 + 8 管理员 + 404）  │
│  ├── store/        # Vuex 状态管理                          │
│  └── composables/  # Token 管理/UI 工具封装                  │
└────────────────────────┬────────────────────────────────────┘
                         │ HTTP/RESTful API（端口: 8081）
                         ▼
┌─────────────────────────────────────────────────────────────────┐
│                      后端 (Spring Boot 3.5.9 + JDK 17)           │
├────────────────┬────────────────┬────────────────┬──────────────┤
│  weblog-common │  weblog-pojo   │ weblog-monitor │  weblog-ai   │
│  (公共模块)     │  (数据模型)     │  (监控告警)     │  (AI 问答)   │
│  配置/工具/安全 │  DTO/Entity/VO  │  AOP日志/告警   │  RAG/智谱/SSE│
├────────────────┴────────────────┴────────────────┴──────────────┤
│                          weblog-server                           │
│               (启动入口 + Controller + Service + Mapper)          │
└─────────────────────────────────────────────────────────────────┘
                         │
                         │ JDBC + MyBatis-Plus
                         ▼
┌────────────────────────────────────────────────────────────┐
│                    数据库 (MySQL 8.0)                       │
│                     15 张核心表                             │
│  ├── article / article_content / article_category_rel ...  │
│  ├── category / tag / user / user_role / blog_setting ...  │
│  ├── monitor_log / monitor_alert_rule / monitor_alert_record│
│  ├── ai_chat_memory / statistics_article_pv / visitor_record│
│  └── 所有业务表支持逻辑删除（is_deleted）                   │
└────────────────────────────────────────────────────────────┘
```

### 模块说明

| 模块 | 说明 | 关键功能 |
|------|------|----------|
| **weblog-common** | 公共模块 | SecurityConfig（JWT+Spring Security）、CorsConfig、RedisConfig、GlobalExceptionHandler、JwtUtil、AliyunOSSOperator |
| **weblog-pojo** | 数据模型模块 | 25 个 DTO、15 个 Entity、15 个 VO |
| **weblog-monitor** | 监控告警模块 | AOP 切面采集 Controller 请求日志、告警规则引擎（定时扫描+阈值触发+邮件通知） |
| **weblog-ai** | AI 智能问答模块 | RAG（向量检索 + 余弦相似度）、智谱 GLM API、SSE 流式输出、24h 会话过期、事件驱动增量索引 |
| **weblog-server** | 主启动模块 | 7 个管理控制器 + 7 个前台控制器 + 14 个 Service + 11 个 Mapper |
| **weblog-vue3** | 前端展示层 | Vue 3 + Vite + Element Plus + TailwindCSS |

### 模块依赖关系

```
WeBlog-parent（聚合 POM）
  ├── weblog-common（jar，编译跳过 repackage）
  │     └── Spring Security / Redis / JWT / OSS / 工具类
  ├── weblog-pojo（jar，编译跳过 repackage）
  │     └── DTO / Entity / VO / 校验注解
  ├── weblog-monitor（jar，编译跳过 repackage）
  │     ├── weblog-common（依赖）
  │     └── Spring AOP / 事件驱动
  ├── weblog-ai（jar，编译跳过 repackage）
  │     ├── weblog-common（依赖）
  │     └── RestClient / MyBatis-Plus / Jackson
  └── weblog-server（jar，可执行）
        ├── weblog-common / weblog-pojo / weblog-monitor / weblog-ai
        └── Spring Boot Starter / MyBatis-Plus / MySQL / Swagger

依赖方向: weblog-server → weblog-ai → weblog-monitor → weblog-common → weblog-pojo
```

## 🚀 技术栈

### 后端技术

| 技术 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 3.5.9 | 核心框架（Spring Framework 6.x） |
| Java | 17 | |
| MyBatis-Plus | 3.5.10.1 | ORM 框架（逻辑删除、分页、驼峰映射） |
| Spring Security | 6.x | 认证授权（JWT + Redis 白名单） |
| JWT (jjwt) | 0.12.5 | Token 签发与验证 |
| MySQL | 8.0.33 | 关系数据库 |
| Druid | 1.2.27 | 生产环境连接池 |
| HikariCP | - | 开发环境连接池 |
| Spring Data Redis | - | Token 白名单缓存（Lettuce 连接池） |
| SpringDoc OpenAPI | 2.8.9 | API 文档（/swagger-ui.html） |
| Aliyun OSS SDK | 3.18.0 | 文件存储 |
| Lombok | 1.18.42 | 代码简化 |
| Jakarta Servlet | 6.0.0 | Servlet 规范 |
| RestClient + Jackson | - | AI API 调用（纯 HTTP，非 LangChain4j） |

### 前端技术

| 技术 | 版本 | 说明 |
|------|------|------|
| Vue | 3.2.47 | 核心框架（Composition API + `<script setup>`） |
| Vite | 4.1.4 | 构建工具（端口 6066） |
| Vue Router | 4.1.6 | 路由（History 模式） |
| Vuex | 4.0.2 | 状态管理（user / setting / menuWidth） |
| Element Plus | 2.3.3 | 后台 UI 组件库 |
| TailwindCSS | 3.3.2 | 前台 CSS 框架 |
| WindiCSS | 3.5.6 | 按需 CSS（vite-plugin-windicss） |
| Flowbite | 1.7.0 | Tailwind 组件库（导航/下拉菜单） |
| Axios | 1.3.5 | HTTP 客户端（请求 ↓ Token 注入 / 响应 ↑ 401/403 拦截） |
| ECharts | 5.4.2 | 仪表盘图表 |
| highlight.js | 11.8.0 | 代码高亮（tokyo-night-dark 主题） |
| mavon-editor / md-editor-v3 | 3.0.x | Markdown 编辑器 |
| v-viewer + viewerjs | 3.0.11 | 图片点击预览 |
| GSAP / animate.css | 3.11.5 / 4.1.1 | 动画库 |
| @vueuse/core | 10.0.2 | 工具库（useCookies 管理 Token） |
| NProgress | 0.2.0 | 页面顶部进度条 |

## ✨ 功能特性

### 后台管理功能
- **仪表盘**：文章发布统计、PV 访问量图表（ECharts）
- **文章管理**：文章的增删改查、分类/标签关联
- **分类管理**：文章分类 CRUD
- **标签管理**：文章标签 CRUD
- **博客设置**：博客名称、作者、头像、社交链接配置
- **监控中心**：
  - **实时日志**：HTTP 请求日志采集（URI/耗时/IP/异常）
  - **告警规则**：按日志级别/时间窗口/阈值配置告警
  - **告警记录**：告警触发历史 + 邮件通知
- **AI 索引管理**：重建文章向量索引（供 RAG 检索使用）
- **认证授权**：JWT + Redis 白名单 + 角色控制（ROLE_ADMIN / ROLE_VISITOR）

### 前台展示功能
- **文章展示**：首页文章列表、详情页（代码高亮 + 图片预览）
- **分类浏览**：按分类查看文章列表
- **标签云**：按标签筛选文章
- **归档**：按年月时间线展示文章（可折叠）
- **搜索**：全站文章搜索
- **AI 智能问答（小智）**：
  - RAG 检索增强：基于博客内容回答问题
  - SSE 流式输出：逐 token 显示 AI 回答
  - 会话管理：24 小时过期 + localStorage 持久化
  - 引用来源：AI 回答附带博客文章引用
  - 中断控制：用户可随时停止 AI 生成
- **AI 文字游戏**：交互式文字冒险游戏

### 访客统计
- PV 统计：每日页面浏览量
- 访客记录：IP 地址、地区、访问时间

## 📊 数据库设计（15 张表）

### 文章相关表（4 张）
- `article` — 文章基本信息（标题、描述、题图、阅读数、逻辑删除）
- `article_content` — 文章内容（分表设计，TEXT 存储）
- `article_category_rel` — 文章-分类关联（一篇文章一个分类）
- `article_tag_rel` — 文章-标签关联（多对多）

### 分类标签表（2 张）
- `category` — 文章分类（name 唯一约束）
- `tag` — 文章标签（name 唯一约束）

### 系统配置表（2 张）
- `user` — 用户（BCrypt 加密密码）
- `blog_setting` — 博客设置（名称/作者/头像/社交链接）

### 用户权限表（1 张）
- `user_role` — 用户角色（ROLE_ADMIN / ROLE_VISITOR）

### 监控告警表（3 张）
- `monitor_log` — HTTP 请求日志（URI/方法/耗时/异常/IP）
- `monitor_alert_rule` — 告警规则（日志级别/时间窗口/阈值）
- `monitor_alert_record` — 告警触发记录

### AI 问答表（1 张）
- `ai_chat_memory` — AI 聊天记录（session_id / 用户消息 / AI 回答 / 引用来源 JSON）

### 统计相关表（2 张）
- `statistics_article_pv` — PV 统计（日维度）
- `visitor_record` — 访客记录

**设计特点**：
- 使用 `utf8mb4` 字符集，支持 Emoji
- 所有表拥有 `create_time` / `update_time` 自动填充
- 业务表软删除（`is_deleted` 字段），MyBatis-Plus 全局配置
- 主键均为 `AUTO_INCREMENT`

## 🏃 快速开始

### 环境要求

- **JDK**: 17+
- **MySQL**: 8.0+
- **Node.js**: 16+
- **Maven**: 3.6+

### 1. 数据库初始化

```sql
CREATE DATABASE weblog CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
```

```bash
mysql -u root -p weblog < sql/weblog.sql
```

### 2. 后端配置

编辑 `weblog-server/src/main/resources/application-local.yaml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/weblog?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai
    username: root
    password: 123456  # 修改为你的数据库密码

# AI 配置（API Key 需替换）
ai:
  zhipu:
    api-key: your-api-key
```

### 3. 启动后端

```bash
# 完整构建（根目录执行）
mvn clean install -DskipTests

# 开发运行
mvn spring-boot:run -pl weblog-server -DskipTests
```

后端默认运行在 `http://localhost:8081`，API 文档：`http://localhost:8081/swagger-ui.html`

### 4. 启动前端

```bash
cd weblog-vue3
npm install
npm run dev
```

前端默认运行在 `http://localhost:6066`，Vite 自动代理 `/api` → `http://localhost:8081`

## 📚 API 文档

启动后端后访问：`http://localhost:8081/swagger-ui.html`

### 后台接口（需 JWT 认证）
- `POST /admin/article/*` — 文章 CRUD
- `POST /admin/category/*` — 分类管理
- `POST /admin/tag/*` — 标签管理
- `POST /admin/user/*` — 用户管理
- `POST /admin/dashboard/*` — 仪表盘数据
- `POST /admin/blog/setting` — 博客设置
- `POST /ai/admin/rebuild` — AI 重建向量索引

### 前台接口（免认证）
- `POST /index/**` — 首页文章列表
- `POST /article/**` — 文章详情/搜索
- `POST /category/**` — 分类列表/分类下文章
- `POST /tag/**` — 标签列表/标签下文章
- `POST /archive/list` — 归档数据
- `POST /blog/**` — 博客设置
- `POST /game/**` — AI 文字游戏
- `POST /ai/chat` — AI 非流式对话
- `POST /ai/chat/stream` — AI 流式对话（SSE）
- `GET /ai/session/validate` — 校验会话有效性
- `GET /ai/session/history` — 获取会话历史

## 🚢 部署说明

### 后端部署

```bash
# 构建可执行 JAR
mvn clean package -DskipTests

# 启动（指定生产配置）
java -jar weblog-server/target/weblog-server-0.0.1-SNAPSHOT.jar --spring.profiles.active=local
```

### 前端部署

```bash
cd weblog-vue3
npm run build
# 部署 dist/ 目录到 Nginx
```

### Nginx 配置示例

```nginx
server {
    listen 80;
    server_name your-domain.com;

    location / {
        root /path/to/weblog-vue3/dist;
        index index.html;
        try_files $uri $uri/ /index.html;
    }

    location /api/ {
        proxy_pass http://localhost:8081/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

## 🔧 关键设计说明

### 认证流程

1. 登录 → `AuthenticationManager` 验证 → 签发 JWT → 写入 Redis 白名单 → 返回 Token
2. 请求 → `JwtAuthenticationFilter` 提取 Bearer token → 解析 JWT → 校验 Redis → 设置 `SecurityContextHolder`
3. 前端 Cookie 存储 Token，Axios 请求拦截器自动注入

### AI 问答（RAG）流程

1. **索引构建**：文章发布/编辑 → `ArticleChangeEvent` → `@TransactionalEventListener(AFTER_COMMIT)` → `RagService` 更新向量索引
2. **首次部署**：`VectorIndexInitializer` 启动时自动构建空索引，从数据库加载所有已发布文章
3. **用户提问**：问题 → 向量检索 Top-K 相关文章 → 组装 Prompt（系统指令 + 历史对话 + RAG 上下文） → 智谱 API 返回回答
4. **流式输出**：SSE（`SseEmitter` + `exchange()` 原生流解析），`event:chunk` 逐 token 返回
5. **会话管理**：24 小时过期，自动创建新会话，localStorage 持久化 sessionId

### 监控告警流程

1. `ControllerMonitorAspect`（@Aspect）拦截所有 Controller 请求，记录耗时/异常
2. 慢请求（>3s）日志 WARN 级别，发布 `LogEvent`
3. `AlertService` 定时扫描告警规则，按时间窗口统计日志命中数
4. 超过阈值 → 写入 `monitor_alert_record` + 邮件通知

## 📄 许可证

本项目采用 MIT 许可证。

## 🙏 致谢

- [Spring Boot](https://spring.io/projects/spring-boot)
- [Vue.js](https://vuejs.org/)
- [Element Plus](https://element-plus.org/)
- [MyBatis-Plus](https://baomidou.com/)
- [智谱 AI](https://open.bigmodel.cn/)
- [WeBlog（参考项目）](https://gitee.com/AllenJiang/WeBlog)
- 以及其他所有依赖的开源项目

---

**提示**：首次使用请确保修改数据库密码、JWT 密钥和 AI API Key 等敏感信息。
