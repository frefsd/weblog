# WeBlog 后端架构设计文档

## 1. 架构概述

WeBlog 后端采用 **Spring Boot 3.5.9** 构建，基于 **Maven 多模块** 分层架构，遵循 **Controller → Service → Mapper** 的三层设计模式。项目使用 **JWT + Spring Security 6** 实现无状态认证，通过 **Redis** 提供缓存加速，通过 **AOP 切面** 实现监控日志采集与告警通知，整体采用前后端分离架构，通过 RESTful API 与前端通信。

### 架构特点

- **多模块分离**：拆分为 4 个 Maven 模块，职责清晰，避免循环依赖
- **无状态认证**：基于 JWT Bearer Token，服务端不存储会话状态
- **分层缓存**：使用 Spring Cache + Redis 多级缓存策略，按数据特征设置不同 TTL
- **异步非阻塞**：@Async + 自定义线程池处理非核心业务（访客记录），不阻塞主流程
- **AOP 监控告警**：@Aspect 切面拦截 Controller 和外部 API，自动采集日志，定时任务扫描触发告警
- **RESTful API**：全 POST 接口设计，统一返回格式

### 架构图

```
┌─────────────────────────────────────────────────────────────┐
│                        前端 (Vue 3)                          │
│                    weblog-vue3/                             │
└─────────────────────────┬───────────────────────────────────┘
                          │ HTTP POST / RESTful API
                          ▼
┌─────────────────────────────────────────────────────────────┐
│                      Nginx (反向代理)                         │
│          代理 /api/* → localhost:8081                        │
│          X-Forwarded-For 传递真实客户端 IP                    │
└─────────────────────────┬───────────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────────────┐
│                   WeBlog 后端 (Spring Boot)                   │
├──────────┬──────────┬──────────┬──────────┬──────────────────┤
│  parent  │ common   │  pojo    │ monitor  │     server       │
│ (父模块)  │(公共模块) │(数据模型) │ (监控模块)│   (业务模块)      │
│ 依赖版本  │配置/工具  │DTO/VO   │AOP日志采集│  控制器/服务层     │
│ 插件管理  │安全/Redis│ 实体类   │告警管理   │  Mapper/XML      │
└──────────┴──────────┴──────────┴──────────┴──────────────────┘
        │              │              │              │
        │              │              │              │
        ▼              ▼              ▼              ▼
┌─────────────────────────────────────────────────────────────┐
│                       中间件层                                │
├───────────────────┬───────────────────┬─────────────────────┤
│    MySQL 8.0      │     Redis 7.x     │   Aliyun OSS        │
│   (主数据库)       │   (缓存 + Token)  │   (图片/文件存储)    │
└───────────────────┴───────────────────┴─────────────────────┘
```

---

## 2. 技术栈

### 核心框架与语言

| 技术 | 版本 | 说明 |
|------|------|------|
| Java | 17+ | 使用 Records、Sealed Classes 等新特性 |
| Spring Boot | 3.5.9 | 基于 Spring Framework 6.x，支持 Jakarta EE |
| Spring Security | 6.x | 认证授权框架，Method Security |
| Spring Data Redis | 3.x | Redis 操作与缓存抽象 |
| Maven | 3.6+ | 多模块构建管理 |

### 数据访问

| 技术 | 版本 | 说明 |
|------|------|------|
| MyBatis-Plus | 3.5.10.1 | ORM 框架，增强 MyBatis，提供 BaseMapper |
| MySQL Connector | 8.0.33 | JDBC 驱动 |
| HikariCP | 4.0.3 | 默认连接池（性能优先） |
| Druid | 1.2.27 | 备选连接池（生产环境，提供监控） |

### 安全与认证

| 技术 | 版本 | 说明 |
|------|------|------|
| Spring Security 6 | 6.x | RBAC 权限控制 |
| JWT (jjwt) | 0.12.5 | 无状态 Token 认证 |
| BCrypt | - | 密码哈希 |

### 缓存

| 技术 | 版本 | 说明 |
|------|------|------|
| Redis | 7.x | 缓存数据库 |
| Lettuce | - | Redis 客户端（连接池） |
| Spring Cache | - | 声明式缓存注解 |

### 文件存储

| 技术 | 版本 | 说明 |
|------|------|------|
| Aliyun OSS SDK | 3.17.4 | 阿里云对象存储 |

### 工具与扩展

| 技术 | 版本 | 说明 |
|------|------|------|
| Lombok | 1.18.42 | @Data、@RequiredArgsConstructor 等 |
| SpringDoc OpenAPI | 2.8.9 | API 文档（Swagger UI） |
| Jakarta Validation | - | @NotBlank、@Valid 参数校验 |
| Jackson | - | JSON 序列化 |

---

## 3. 模块依赖关系

```
WeBlog-parent (pom)
 │  依赖版本统一管理，不包含业务代码
 │
 ├── WeBlog-common (jar)
 │   ├── spring-boot-starter-web
 │   ├── spring-boot-starter-security
 │   ├── spring-boot-starter-data-redis
 │   ├── mybatis-plus-spring-boot3-starter
 │   ├── aliyun-sdk-oss
 │   ├── jjwt-api (JWT)
 │   ├── springdoc-openapi-starter-webmvc-ui
 │   └── jackson-databind
 │
 ├── WeBlog-pojo (jar)
 │   ├── spring-boot-starter-validation
 │   ├── mybatis-plus-core
 │   └── jackson-databind
 │
 ├── WeBlog-monitor (jar)
 │   ├── WeBlog-common (编译依赖)
 │   ├── WeBlog-pojo (编译依赖)
 │   ├── spring-boot-starter-aop
 │   ├── spring-boot-starter-web
 │   └── mybatis-plus-spring-boot3-starter
 │
 └── WeBlog-server (jar, 可执行)
      ├── WeBlog-common (编译依赖)
      ├── WeBlog-pojo (编译依赖)
      ├── WeBlog-monitor (编译依赖)
      ├── spring-boot-starter-web
      ├── mybatis-plus-spring-boot3-starter
      ├── mysql-connector-j
      ├── druid (可选，生产环境)
      ├── spring-boot-starter-data-redis
      └── lombok

依赖方向: WeBlog-server → WeBlog-monitor → WeBlog-common → WeBlog-pojo
```

### 模块职责

| 模块 | 类型 | 职责 |
|------|------|------|
| **WeBlog-parent** | pom | 统一管理所有依赖版本、构建插件、Maven 配置 |
| **WeBlog-common** | jar | 配置类、工具类、异常处理、安全过滤、统一返回结果、Redis 配置 |
| **WeBlog-pojo** | jar | 数据传输对象（DTO）、视图对象（VO）、查询参数（Query）、实体类 |
| **WeBlog-monitor** | jar | AOP 日志采集切面、监控控制器、告警规则/记录管理、日志服务 |
| **WeBlog-server** | jar | 控制器、服务层、数据访问层、MyBatis XML 映射、启动类、配置文件 |

---

## 4. 包结构详解

### WeBlog-common (公共模块)

```
com.blog.config/
├── CorsConfig.java          # 全局 CORS 跨域配置
├── SecurityConfig.java      # Spring Security 安全配置
├── RedisConfig.java         # Redis 连接 + Spring Cache 配置
├── AsyncConfig.java         # @Async 异步线程池配置
└── OpenApiConfig.java       # Swagger/OpenAPI 文档配置

com.blog.exception/
├── BusinessException.java   # 业务异常（可携带错误码）
└── GlobalExceptionHandler.java  # 全局异常拦截器

com.blog.filter/
└── JwtAuthenticationFilter.java  # JWT 认证过滤器（OncePerRequestFilter）

com.blog.result/
├── Result.java              # 统一响应体 {code, message, data}
└── PageResult.java          # 分页响应体

com.blog.utils/
├── JwtUtil.java             # JWT 生成/解析/验证
├── RedisConstants.java      # Redis 缓存常量集中管理
├── IpUtil.java              # 客户端真实 IP 提取
├── AliyunOSSOperator.java   # OSS 文件上传操作
└── AliyunOSSProperties.java # OSS 配置属性绑定
```

### WeBlog-pojo (数据模型模块)

```
com.blog.dto/                # 数据传输对象（接收请求参数）
├── ArticlePublishDTO.java   # 文章发布
├── ArticleUpdateDTO.java    # 文章修改
├── ArticlePageDTO.java      # 文章分页查询
├── ArticleDetailDTO.java    # 文章详情查询
├── ArticleSearchDTO.java    # 文章搜索
├── CategoryAddDTO.java      # 新增分类
├── CategoryDeleteDTO.java   # 删除分类
├── CategoryPageDTO.java     # 分类分页查询
├── CategorySearchDTO.java   # 分类搜索
├── TagAddDTO.java           # 新增标签
├── TagDeleteDTO.java        # 删除标签
├── TagPageDTO.java          # 标签分页查询
├── TagArticleDTO.java       # 标签下文章列表
├── AuthDTO.java             # 登录认证
├── BlogSettingDTO.java      # 博客设置更新
├── LogSearchDTO.java         # 日志分页查询
├── AlertPageDTO.java         # 告警记录分页查询
└── UserPasswordDTO.java     # 修改密码

com.blog.vo/                 # 视图对象（返回响应数据）
├── ArticleIndexVO.java      # 文章列表项
├── ArticleDetailVO.java     # 文章详情（管理后台）
├── ArticleFrontendDetailVO.java  # 文章详情（前台展示）
├── CategorySimpleVO.java    # 分类简项
├── TagSimpleVO.java         # 标签简项
├── CategoryArticleCountVO.java  # 分类文章数统计
├── TagArticleCountVO.java       # 标签文章数统计
├── SelectOptionVO.java      # 下拉选择项
├── ArchiveItemVO.java       # 归档项
├── DashboardArticleStatisticsVO.java  # 仪表盘统计
└── UserDetailVO.java        # 用户详情

com.blog.entity/             # 实体类（在 weblog-pojo 中定义）
├── Article.java
├── ArticleContent.java
├── ArticleCategoryRel.java
├── ArticleTagRel.java
├── Category.java
├── Tag.java
├── User.java
├── UserRole.java
├── BlogSetting.java
├── VisitorRecord.java
├── StatisticsArticlePv.java
├── LogRecord.java              # 监控日志记录
├── AlertRule.java              # 告警规则配置
└── AlertRecord.java            # 告警触发记录
```

### WeBlog-monitor (监控模块)

```
com.blog/
├── aspect/
│   ├── ControllerMonitorAspect.java   # HTTP Controller 请求日志 AOP 切面
│   └── ExternalApiMonitorAspect.java  # 外部 API 调用日志 AOP 切面
├── controller/
│   └── MonitorController.java         # 监控/告警 REST API (有 @PreAuthorize 保护)
├── service/
│   ├── MonitorLogService.java         # 日志查询/清空/写入
│   ├── AlertService.java              # 告警规则 CRUD/记录查询/告警触发判断
│   └── impl/
│       ├── MonitorLogServiceImpl.java
│       └── AlertServiceImpl.java
├── mapper/
│   ├── MonitorLogMapper.java
│   ├── AlertRuleMapper.java
│   └── AlertRecordMapper.java
├── LogEvent.java                      # 日志采集事件对象
├── LogEventListener.java              # BusinessException 监听，按规则触发告警
└── MonitorExternal.java               # 外部调用监控标记接口
```

### WeBlog-server (业务模块)

```
com.blog.controller/
├── admin/                   # 后台管理接口（需认证）
│   ├── AdminAuthController.java       # 登录/登出
│   ├── AdminUserController.java       # 用户管理、头像上传
│   ├── AdminArticleController.java    # 文章 CRUD
│   ├── AdminCategoryController.java   # 分类管理
│   ├── AdminTagController.java        # 标签管理
│   ├── AdminBlogSettingController.java # 博客设置
│   └── AdminDashBoardController.java  # 仪表盘统计
│
└── frontend/                # 前台展示接口（公开）
    ├── IndexController.java           # 首页文章列表
    ├── ArticleController.java         # 文章详情/搜索
    ├── CategoryController.java        # 分类列表/分类文章
    ├── TagController.java             # 标签列表/标签文章
    ├── ArchiveController.java         # 文章归档
    └── BlogSettingController.java     # 博客信息展示

com.blog/service/
├── impl/
│   ├── ArticleServiceImpl.java           # 文章业务
│   ├── CategoryServiceImpl.java          # 分类业务
│   ├── TagServiceImpl.java               # 标签业务
│   ├── UserServiceImpl.java              # 用户业务
│   ├── BlogSettingServiceImpl.java       # 博客设置业务
│   ├── VisitorRecordServiceImpl.java     # 访客记录（异步）
│   ├── DashboardServiceImpl.java         # 仪表盘统计
│   └── StatisticsArticlePvServiceImpl.java # PV 统计
└── (对应的接口类)

com.blog.mapper/
├── ArticleMapper.java
├── ArticleContentMapper.java
├── ArticleCategoryRelMapper.java
├── ArticleTagRelMapper.java
├── CategoryMapper.java
├── TagMapper.java
├── UserMapper.java
├── UserRoleMapper.java
├── BlogSettingMapper.java
├── VisitorRecordMapper.java
└── StatisticsArticlePvMapper.java

resources/
├── application.yaml               # 主配置（引用 local profile）
├── application-dev.yaml           # 开发环境配置
├── application-druid.yaml         # 生产环境配置（Druid+DruidMonitor）
├── application-example.yaml       # 配置模板
└── mapper/                        # MyBatis XML 映射文件
    ├── ArticleMapper.xml
    ├── StatisticsArticlePvMapper.xml
    └── ...
```

---

## 5. 数据库设计

共 14 张核心表，按业务领域分为 6 类。

### 文章相关（4 张）

```
article                  article_content          article_category_rel    article_tag_rel
├── id (PK)              ├── id (PK)              ├── id (PK)             ├── id (PK)
├── title                ├── article_id (FK)      ├── article_id (FK)     ├── article_id (FK)
├── title_image          └── content (LONGTEXT)   └── category_id (FK)    └── tag_id (FK)
├── description
├── read_num
├── is_deleted (软删除)
├── create_time
└── update_time
```

**设计亮点：** 文章元数据与内容分离存储（垂直分表），列表查询只需扫描窄表 `article`，无需加载 `LONGTEXT` 内容字段，减少 IO 开销。

### 分类标签（2 张）

| 表 | 字段 | 说明 |
|----|------|------|
| `category` | id, name, is_deleted, create_time, update_time | 文章分类 |
| `tag` | id, name, is_deleted, create_time, update_time | 文章标签 |

### 系统配置（2 张）

| 表 | 字段 | 说明 |
|----|------|------|
| `blog_setting` | id, blog_name, author, avatar, introduction, github_home, csdn_home, gitee_home, zhihu_home | 博客信息 |
| `user` | id, username, password, avatar, is_deleted, create_time, update_time | 管理员用户 |

### 权限（1 张）

| 表 | 字段 | 说明 |
|----|------|------|
| `user_role` | id, username, role, create_time | 角色分配（单用户单角色） |

### 统计（2 张）

| 表 | 字段 | 说明 |
|----|------|------|
| `statistics_article_pv` | id, pv_date, pv_count, create_time, update_time | 每日 PV 统计 |
| `visitor_record` | id, visitor, ip_address, ip_region, visit_time, is_notify | 访客记录 |

### 监控告警（3 张）

| 表 | 字段 | 说明 |
|----|------|------|
| `monitor_log` | id, service_name, level, type, method, uri, http_method, duration, error_message, stack_trace, ip, create_time | AOP 切面采集的日志记录 |
| `monitor_alert_rule` | id, name, log_level, time_window, threshold, status, create_time, update_time | 告警规则（级别/时间窗口/阈值） |
| `monitor_alert_record` | id, rule_id, log_level, trigger_count, threshold, notify_status, error_message, create_time | 告警触发记录（关联规则） |

### 设计规范

- 所有表使用 `utf8mb4` 字符集，支持 Emoji
- 所有表包含 `create_time` / `update_time` 时间审计字段
- 软删除设计（`is_deleted` 字段，0=正常，1=删除），避免物理删除
- 文章-分类、文章-标签使用关联表实现多对多关系

---

## 6. 安全架构

### 认证流程

```
请求 → JwtAuthenticationFilter → SecurityFilterChain → Controller
         │                              │
         │ 1. 提取 Authorization:       │ 2. 根据路径判断是否需要认证
         │    Bearer <token>            │    - 前台路径 → permitAll()
         │ 2. 解析 JWT → username       │    - 后台路径 → authenticated()
         │ 3. Redis 校验 token 有效性    │    - 写操作 → @PreAuthorize
         │ 4. 设置 SecurityContext       │
```

### 安全配置要点

- **CSRF 关闭**：无状态 JWT 认证，不需要 CSRF 保护
- **无状态会话**：`SessionCreationPolicy.STATELESS`，不创建 HttpSession
- **公开路径**：`/index/**`, `/article/**`, `/category/**`, `/tag/**`, `/archive/**`, `/blog/**`, `/login`, Swagger UI
- **权限控制**：写操作（发布/修改/删除）使用 `@PreAuthorize("hasRole('ADMIN')")` 注解
- **异常处理**：SecurityConfig 配置 `exceptionHandling`，认证失败（401）返回 `{"success":false,"message":"请先登录"}`，权限不足（403）返回 `{"success":false,"message":"权限不足：您没有执行该操作的权限"}`。前端 Axios 拦截器根据 401/403 状态码 + JSON body 区分跳登录（无权限）和提示禁止（有身份但无权限）
- **登录状态**：登录成功生成 JWT，同时将 token 存入 Redis 白名单；登出时从 Redis 删除 token
- **密码加密**：BCryptPasswordEncoder

---

## 7. 缓存架构

使用 Spring Cache + Redis 实现声明式缓存，通过 `RedisCacheManager` 集中管理各缓存区域的 TTL。

### 缓存区域配置

| 缓存名称 | Key 格式 | TTL | 说明 |
|----------|----------|-----|------|
| `weblog:article:detail` | `article:detail::{articleId}` | 30min | 文章详情（前台） |
| `weblog:article:index` | `article:index::{pageParams}` | 5min | 首页文章列表 |
| `weblog:category:list` | `category:list::` | 30min | 全部分类列表 |
| `weblog:tag:list` | `tag:list::` | 30min | 全部标签列表 |
| `weblog:blog:setting` | `blog:setting::` | 30min | 博客设置信息 |

### 缓存一致性策略

- **读操作**：`@Cacheable` — 缓存命中直接返回，未命中查数据库并写入缓存
- **写操作**：`@CacheEvict(allEntries = true)` — 新增/修改/删除后清空整个缓存区域
- **除非条件**：`unless = "#result == null"` 或 `#result?.isEmpty()`，避免缓存空值
- **主动刷新**：文章阅读数（readNum）是高频更新字段，每次请求先自增写库，再走缓存返回（缓存中 readNum 可能滞后，但返回时覆盖为最新值）

### 缓存更新的触发路径

```
用户访问文章详情
    → readNum +1 (写库)
    → 异步记录访客 + PV
    → getCachedArticleDetail() [@Cacheable]
        → 缓存命中？直接返回
        → 未命中？查库组装 VO → 写入 Redis

管理员发布/修改/删除文章
    → @CacheEvict(article:detail, article:index) allEntries = true
    → 下次访问重新查库并缓存
```

---

## 8. 异步任务架构

### 线程池配置

| 参数 | 值 | 说明 |
|------|-----|------|
| 核心线程数 | 2 | 轻量级异步任务 |
| 最大线程数 | 4 | 突发流量时可扩展 |
| 队列容量 | 100 | 缓冲等待执行的任务 |
| 线程名前缀 | `visitor-task-` | 便于日志识别 |
| 优雅关闭 | 开启 | 等待运行中任务完成 |
| 等待秒数 | 10s | 最多等待 10 秒后强制关闭 |

### 异步任务流程

```
HTTP 请求 (文章详情)
    │
    ├── readNum +1 (同步, 立即写库)
    ├── visitorRecordService.recordVisitorAsync(request)  [@Async]
    │       └── 提取 IP、User-Agent → 写入 visitor_record 表
    ├── statisticsArticlePvService.upsertDailyPV(date)    [@Async]
    │       └── INSERT ... ON DUPLICATE KEY UPDATE pv_count +1
    └── 返回文章详情 (不等待异步任务完成)
```

### 原子 upsert PV 统计

日 PV 统计使用 MySQL 原生原子操作，避免并发竞争：

```sql
INSERT INTO statistics_article_pv (pv_date, pv_count, create_time, update_time)
VALUES (#{pvDate}, 1, NOW(), NOW())
ON DUPLICATE KEY UPDATE pv_count = pv_count + 1, update_time = NOW()
```

- 当天首条访问 → 插入新行，pv_count = 1
- 当天后续访问 → 命中唯一索引 → pv_count 自增 1
- 无需分布式锁，数据库层面保证原子性

---

## 9. AOP 监控告警架构

weblog-monitor 模块通过 @Aspect 切面自动拦截 Controller 和外部 API 调用，采集性能与错误数据，结合告警规则实现异常自动通知。

### 日志采集流程

```
HTTP 请求 → ControllerMonitorAspect @Around
    │  1. 记录进入时间
    │  2. 执行目标方法
    │       ├── 成功 → 提取方法名/URI/HTTP方法/响应耗时 → 写入 monitor_log (level=INFO)
    │       └── 异常 → 提取错误消息/堆栈(最多20行) → 写入 monitor_log (level=ERROR/WARN)
    │
外部 API 调用 → ExternalApiMonitorAspect @Around
    │  1. 标记 @MonitorExternal 的方法被拦截
    │  2. 记录调用耗时
    │  3. 写入 monitor_log (type=EXTERNAL_API)
```

### 告警触发流程

```
定时任务 (每60s) → AlertService.checkAndFireAlerts()
    │  1. 查询所有启用的告警规则 (status=1)
    │  2. 对每条规则:
    │     SELECT COUNT(*) FROM monitor_log
    │     WHERE level = rule.logLevel
    │       AND create_time >= NOW() - INTERVAL rule.timeWindow MINUTE
    │     → 实际数量 >= rule.threshold ?
    │         ├── 是 → 插入 monitor_alert_record (记录触发详情)
    │         └── 否 → 跳过
    │
BusinessException 抛出 → LogEventListener @EventListener
    │  1. 构造 LogRecord (level=ERROR, type=BUSINESS)
    │  2. 写入 monitor_log
    │  3. 调用 alertService 检查是否满足告警规则
```

### 监控模块关键设计

- **AOP 而非 Filter**：@Aspect 可以获取方法参数、返回值、异常，比 Filter 粒度更细，对业务代码零侵入
- **日志不阻塞业务**：当日志写入失败时 catch 异常不抛出，确保监控模块故障不影响主业务流程
- **异步告警检查**：告警规则扫描在独立线程池中执行，避免阻塞请求处理
- **独立模块**：monitor 作为独立 jar 部署在 server 中，可单独禁用（去除依赖即可）

---

## 10. 统一响应格式

### 成功响应

```json
{
  "code": 200,
  "message": "成功",
  "data": { ... }
}
```

### 分页响应

```json
{
  "code": 200,
  "message": "成功",
  "data": {
    "records": [ ... ],
    "total": 100,
    "current": 1,
    "size": 10
  }
}
```

### 错误响应

```json
{
  "code": 500,
  "message": "分类名称不能为空",
  "data": null
}
```

---

## 11. 关键设计决策

### 11.1 为什么所有接口都用 POST？

- 简化安全配置，无需区分 GET/POST 的权限规则
- 避免请求参数长度限制（部分前端代理对 GET URL 长度有限制）
- 统一前后端接口风格

### 11.2 为什么文章内容和元数据分离？

**垂直分表**策略：文章正文是 `LONGTEXT` 大字段，列表查询（只查标题、描述、时间）带上正文会导致：

- 全表扫描更慢
- IO 读取更多数据
- MyBatis-Plus 映射开销更大

分离后列表查询只需扫 `article` 窄表，详情时再 join `article_content`。

### 11.3 为什么访客记录用 @Async 异步？

- 查看文章详情是核心体验，写访客统计属于非核心业务流程
- 同步写数据库会增加响应延迟（尤其在高并发下）
- 异步 + 队列缓冲可以在突发流量时削峰填谷
- 即使访客记录写入失败，也不影响用户查看文章

### 11.4 为什么用 Redis 管理登录状态？ JWT 不是无状态的吗？

纯粹的无状态 JWT 无法主动登出（token 在过期前一直有效）。通过 Redis 白名单机制：

- 登录 → JWT 存入 Redis（`weblog:login.token.{jwt} → username`）
- 请求 → 先解析 JWT，再校验 Redis 中是否存在
- 登出 → 从 Redis 删除 token，JWT 立即失效

这样既保留 JWT 的无状态传输优势，又获得服务端主动失效的能力。

### 11.5 为什么使用 Spring Cache 而非手动 Redis 操作？

- 声明式注解（`@Cacheable` / `@CacheEvict`）减少样板代码
- 缓存逻辑与业务逻辑分离，代码更清晰
- RedisCacheManager 集中管理 TTL 和序列化配置
- 切换缓存实现（如本地 Caffeine）只需改配置，无需改业务代码

---

## 12. 数据流全景

### 访客浏览首页

```
访客浏览器 → Nginx → IndexController
    → articleService.getArticleIndexPage(pageDTO)
        → [Redis 缓存] 命中？→ 直接返回
        → 未命中？→ ArticleMapper 分页查询 → 写入缓存
    → BlogSettingController (并行)
        → blogSettingService.getBlogSettingDetail()
            → [Redis 缓存] 命中？→ 直接返回
            → 未命中？→ BlogSettingMapper → 写入缓存
    → CategoryController (并行)
        → categoryService.getAllCategories()
            → [Redis 缓存] 命中？→ 直接返回
    → TagController (并行)
        → tagService.getAllTags()
            → [Redis 缓存] 命中？→ 直接返回
```

### 访客查看文章详情

```
访客浏览器 → Nginx → ArticleController.getArticleDetail()
    → articleService.getArticleDetailForFrontend(articleId, request)
        → 1. articleMapper.selectById(articleId) → readNum +1 → update
        → 2. statisticsArticlePvService.upsertDailyPV(date) [@Async]
                → INSERT ... ON DUPLICATE KEY UPDATE pv_count +1
        → 3. visitorRecordService.recordVisitorAsync(request) [@Async]
                → IpUtil.getClientIp(request) → 提取 IP
                → VisitorRecord(ip, userAgent, visitTime) → INSERT
        → 4. self.getCachedArticleDetail(articleId) [@Cacheable]
                → 缓存命中 → 直接返回 VO
                → 未命中 → 组装 VO (文章+内容+分类+标签+前后篇) → 写入缓存
        → 5. 将最新 readNum 覆盖到 VO → 返回前端
```

### 管理员发布文章

```
管理员 → AdminArticleController.publishArticle()
    → @PreAuthorize("hasRole('ADMIN')") 权限校验
    → articleService.publishArticle(dto)
        → 1. 校验参数
        → 2. Article + ArticleContent → INSERT
        → 3. ArticleCategoryRel + ArticleTagRel → INSERT 关联
        → 4. @CacheEvict(article:detail, article:index) allEntries = true
                → 清空相关缓存，前台下次访问重新加载
```

---

## 13. 项目配置

### 配置分层

| 文件 | 激活方式 | 用途 |
|------|----------|------|
| `application.yaml` | 默认 | 核心配置，引用 `local` profile |
| `application-dev.yaml` | `spring.profiles.active: dev` | 开发环境 |
| `application-druid.yaml` | `spring.profiles.include: druid` | 生产环境（Druid + 监控） |
| `application-example.yaml` | 模板 | 新开发者参考配置模板 |

### 核心配置清单

```
服务器端口: 8081
数据库: MySQL 8.0 @ localhost:3306/weblog (HikariCP/Druid)
Redis: 127.0.0.1:6379 (Lettuce 连接池，最大 8)
JWT: 24h 过期，HMAC-SHA 签名
OSS: 阿里云北京节点，java-application01 bucket
文件上传: 最大 10MB/文件，20MB/请求
MyBatis-Plus: 逻辑删除 is_deleted，雪花算法 ID
```

---

## 14. 开发环境与部署

### 开发环境

```bash
# 根目录构建所有模块
mvn clean install

# 开发模式运行
cd WeBlog-server
mvn spring-boot:run
```

### 生产部署

```
Nginx (80) → ProxyPass /api/* → Spring Boot (8081)
                                      │
                                      ├── MySQL (RDS)
                                      ├── Redis
                                      └── Aliyun OSS (图片)
```

Nginx 配置要点：

- 前端静态文件 → `weblog-vue3/dist/`
- API 代理 → `/api/* → localhost:8081/`
- SPA 路由 fallback → `try_files $uri $uri/ /index.html`
- 传递真实 IP → `proxy_set_header X-Forwarded-For $remote_addr`
