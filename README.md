# WeBlog - Java Web博客系统

## 📖 项目简介

WeBlog是一个基于Spring Boot和Vue 3构建的现代化前后端分离博客系统。系统提供了完整的博客管理功能，包括文章管理、分类标签、用户管理、数据统计等，适合个人博客、技术分享平台等场景使用。

该项目的**前端界面设计与交互**参考了开源项目 [WeBlog](https://gitee.com/AllenJiang/WeBlog)（在此向原作者表示感谢），但**后端核心架构、数据库设计及业务逻辑完全由本人独立开发**。

本项目旨在记录我的技术成长，同时也是我对 **Spring Boot 3** 与 **Vue 3** 生态的一次深度实践。

##### 访客账号：

用户名：vistor

密码：vistor

## 🏗️ 系统架构

### 架构概述
WeBlog采用现代化的前后端分离架构，后端基于Spring Boot 3.5.9构建，前端使用Vue 3.2.47框架。项目采用多模块Maven管理，实现了清晰的代码分层和模块化设计。

**架构特点**：
- **前后端分离**：前端独立部署，通过RESTful API与后端通信
- **多模块设计**：后端拆分为4个Maven模块，职责清晰
- **分层架构**：遵循Controller-Service-Mapper三层架构
- **安全认证**：基于JWT和Spring Security的认证授权机制
- **响应式前端**：使用Vue 3组合式API和Element Plus组件库

### 架构图
```
┌─────────────────────────────────────────────────────────────┐
│                        前端 (Vue 3)                          │
│                    weblog-vue3/ 目录                         │
│  ├── src/                                                   │
│  │   ├── api/          # API接口定义                        │
│  │   ├── pages/        # 页面组件                           │
│  │   ├── components/   # 公共组件                           │
│  │   └── store/        # Vuex状态管理                       │
│  └── package.json      # 前端依赖配置                       │
└────────────────────────────┬────────────────────────────────┘
                             │ HTTP/RESTful API (端口: 8081)
                             ▼
┌─────────────────────────────────────────────────────────────┐
│                       后端 (Spring Boot)                     │
├──────────────┬──────────────┬──────────────┬──────────────┤
│ WeBlog-parent │ WeBlog-common │ WeBlog-pojo  │ WeBlog-server │
│   (父模块)    │  (公共模块)   │ (数据模型)   │  (业务模块)   │
│   pom.xml    │ 配置/工具/异常 │ DTO/VO/参数  │ 控制器/服务层 │
└──────────────┴──────────────┴──────────────┴──────────────┘
                             │
                             │ JDBC + MyBatis-Plus
                             ▼
┌─────────────────────────────────────────────────────────────┐
│                       数据库 (MySQL 8.0)                     │
│                    11个核心表，支持事务                      │
│  ├── article        # 文章表                                │
│  ├── category       # 分类表                                │
│  ├── tag            # 标签表                                │
│  └── ...            # 其他表                                │
└─────────────────────────────────────────────────────────────┘
```

### 模块说明
1. **WeBlog-parent**：父模块，统一管理依赖版本和构建配置，不包含业务代码
2. **WeBlog-common**：公共模块，包含配置类、工具类、异常处理、JWT工具、OSS操作等通用组件
3. **WeBlog-pojo**：数据模型模块，包含DTO（数据传输对象）、VO（视图对象）、查询参数等
4. **WeBlog-server**：业务模块，包含控制器、服务层、数据访问层、实体类等核心业务逻辑

### 模块依赖关系
```
WeBlog-parent (pom)
    ├── WeBlog-common (jar)
    │    ├── spring-boot-starter-web
    │    ├── spring-boot-starter-security
    │    ├── mybatis-plus-spring-boot3-starter
    │    ├── aliyun-sdk-oss
    │    ├── jjwt-api (JWT)
    │    └── 配置类、工具类、异常处理
    │
    ├── WeBlog-pojo (jar)
    │    ├── spring-boot-starter-validation
    │    ├── mybatis-plus-core
    │    ├── jackson-databind
    │    └── DTO、VO、查询参数
    │
    └── WeBlog-server (jar, 可执行)
         ├── WeBlog-common
         ├── WeBlog-pojo
         ├── spring-boot-starter-web
         ├── mybatis-plus-spring-boot3-starter
         ├── spring-boot-starter-security
         ├── mysql-connector-j
         ├── springdoc-openapi-starter-webmvc-ui
         └── 控制器、服务层、Mapper、实体类

依赖方向: WeBlog-server → WeBlog-common → WeBlog-pojo
```

## 🚀 技术栈

### 后端技术
- **核心框架**: Spring Boot 3.5.9 (基于Spring Framework 6.x)
- **Java版本**: JDK 17+
- **构建工具**: Maven 3.6+
- **ORM框架**: MyBatis-Plus 3.5.10.1 (增强MyBatis功能)
- **安全框架**:
  - Spring Security 6.x (认证授权)
  - JWT 0.12.5 (Token认证)
- **数据库**:
  - MySQL 8.0.33 (主数据库)
  - HikariCP 4.0.3 (连接池)
- **API文档**: SpringDoc OpenAPI 2.8.9 (替代Swagger)
- **文件存储**: 阿里云OSS SDK 3.17.4
- **开发工具**:
  - Lombok 1.18.42 (代码简化)
  - Validation API (参数校验)
- **其他依赖**:
  - Servlet API 6.0.0 (Jakarta EE)
  - JAXB 2.3.3 (XML处理)
  - Jackson (JSON处理)

### 前端技术
- **核心框架**: Vue 3.2.47 (组合式API)
- **构建工具**: Vite 4.1.4 (下一代前端构建工具)
- **UI框架**:
  - Element Plus 2.3.3 (主要UI组件库)
  - Naive UI 2.34.3 (备用组件库)
- **样式框架**:
  - Tailwind CSS 3.3.2 (实用优先的CSS框架)
  - WindiCSS 3.5.6 (按需生成的Tailwind)
- **状态管理**: Vuex 4.0.2 (集中式状态管理)
- **路由管理**: Vue Router 4.1.6
- **HTTP客户端**: Axios 1.3.5
- **富文本编辑器**:
  - mavon-editor 3.0.1 (Markdown编辑器)
  - md-editor-v3 3.0.1
  - editor.md 1.5.0
- **图表库**: ECharts 5.4.2 (数据可视化)
- **工具库**:
  - VueUse 10.0.2 (Vue组合式工具)
  - GSAP 3.11.5 (动画库)
  - Highlight.js 11.8.0 (代码高亮)
  - Moment.js 2.29.4 (时间处理)
  - NProgress 0.2.0 (进度条)

## 📊 数据库设计

### 数据库ER图
```
┌─────────────┐      ┌──────────────────┐      ┌─────────────┐
│   article   │◄────┤article_category_rel├────►│  category   │
├─────────────┤      ├──────────────────┤      ├─────────────┤
│ id          │      │ id               │      │ id          │
│ title       │      │ article_id       │      │ name        │
│ title_image │      │ category_id      │      │ create_time │
│ description │      └──────────────────┘      │ update_time │
│ create_time │                                 │ is_deleted  │
│ update_time │      ┌──────────────────┐      └─────────────┘
│ is_deleted  │      │ article_tag_rel  │
│ read_num    │      ├──────────────────┤      ┌─────────────┐
└─────────────┘      │ id               │      │    tag      │
        │            │ article_id       │      ├─────────────┤
        │            │ tag_id           │      │ id          │
        ▼            └──────────────────┘      │ name        │
┌─────────────┐                                │ create_time │
│article_content│                               │ update_time │
├─────────────┤                                │ is_deleted  │
│ id          │                                └─────────────┘
│ article_id  │
│ content     │      ┌─────────────┐
└─────────────┘      │   user      │
                     ├─────────────┤
┌─────────────┐      │ id          │      ┌─────────────┐
│blog_setting │      │ username    │      │ user_role   │
├─────────────┤      │ password    │      ├─────────────┤
│ id          │      │ avatar      │      │ id          │
│ blog_name   │      │ create_time │      │ username    │
│ author      │      │ update_time │      │ role        │
│ introduction│      │ is_deleted  │      │ create_time │
│ avatar      │      └─────────────┘      └─────────────┘
│ github_home │            │
│ csdn_home   │            │
│ gitee_home  │      ┌─────┴─────┐
│ zhihu_home  │      │           │
└─────────────┘      ▼           ▼
              ┌─────────────┐ ┌─────────────┐
              │statistics_  │ │ visitor_    │
              │article_pv   │ │ record      │
              ├─────────────┤ ├─────────────┤
              │ id          │ │ id          │
              │ pv_date     │ │ visitor     │
              │ pv_count    │ │ ip_address  │
              │ create_time │ │ ip_region   │
              │ update_time │ │ visit_time  │
              └─────────────┘ └─────────────┘
```

### 核心表结构说明
项目包含11个核心表，主要分为以下几类：

**文章相关表 (4个)**:
1. `article` - 文章基本信息表（标题、描述、题图等）
2. `article_content` - 文章内容表（分表设计，存储正文内容）
3. `article_category_rel` - 文章分类关系表（多对多关联）
4. `article_tag_rel` - 文章标签关系表（多对多关联）

**分类标签表 (2个)**:
5. `category` - 文章分类表
6. `tag` - 文章标签表

**系统配置表 (2个)**:
7. `blog_setting` - 博客设置表（博客名称、作者、头像、社交链接等）
8. `user` - 用户表（用户名、密码、头像等）

**用户权限表 (1个)**:
9. `user_role` - 用户角色表（用户角色管理）

**统计相关表 (2个)**:
10. `statistics_article_pv` - 文章访问统计表
11. `visitor_record` - 访客记录表

**设计特点**:
- 使用`utf8mb4`字符集，支持Emoji表情
- 所有表都有`create_time`和`update_time`字段，记录创建和更新时间
- 使用软删除设计（`is_deleted`字段），避免物理删除数据
- 文章内容与基本信息分离，优化查询性能
- 支持多对多关系（文章-分类，文章-标签）

## 📁 项目结构

### 多模块Maven结构
```
WeBlog/                                  # 项目根目录
├── WeBlog-parent/                       # 父模块（pom）
│   ├── pom.xml                         # 父模块配置，统一管理依赖版本
│   └── (无源码，仅管理子模块)
│
├── WeBlog-common/                       # 公共模块（jar）
│   ├── src/main/java/com/blog/
│   │   ├── config/                     # 配置类
│   │   │   ├── CorsConfig.java         # 跨域配置
│   │   │   ├── SecurityConfig.java     # 安全配置
│   │   │   └── OpenApiConfig.java      # API文档配置
│   │   ├── exception/                  # 异常处理
│   │   │   ├── BusinessException.java  # 业务异常
│   │   │   └── GlobalExceptionHandler.java
│   │   ├── filter/                     # 过滤器
│   │   │   └── JwtAuthenticationFilter.java
│   │   ├── result/                     # 统一返回结果
│   │   │   ├── Result.java
│   │   │   └── PageResult.java
│   │   └── utils/                      # 工具类
│   │       ├── AliyunOSSOperator.java  # OSS操作工具
│   │       ├── AliyunOSSProperties.java
│   │       └── JwtUtil.java            # JWT工具
│   └── pom.xml                         # 公共模块依赖
│
├── WeBlog-pojo/                         # 数据模型模块（jar）
│   ├── src/main/java/com/blog/
│   │   ├── dto/                        # 数据传输对象
│   │   │   ├── ArticlePublishDTO.java
│   │   │   ├── ArticleUpdateDTO.java
│   │   │   ├── AuthDTO.java
│   │   │   └── ...
│   │   ├── vo/                         # 视图对象
│   │   └── query/                      # 查询参数
│   └── pom.xml                         # 数据模型模块依赖
│
├── WeBlog-server/                       # 业务模块（jar，可执行）
│   ├── src/main/java/com/blog/
│   │   ├── controller/                 # 控制器层
│   │   │   ├── admin/                  # 后台管理接口
│   │   │   │   ├── AdminArticleController.java
│   │   │   │   ├── AdminAuthController.java
│   │   │   │   ├── AdminCategoryController.java
│   │   │   │   └── ...
│   │   │   └── frontend/               # 前台展示接口
│   │   │       ├── ArticleController.java
│   │   │       ├── CategoryController.java
│   │   │       └── ...
│   │   ├── service/                    # 服务层
│   │   │   ├── impl/                   # 服务实现
│   │   │   └── ...
│   │   ├── mapper/                     # 数据访问层
│   │   │   ├── ArticleMapper.java
│   │   │   ├── CategoryMapper.java
│   │   │   └── ...
│   │   └── entity/                     # 实体类
│   │       ├── Article.java
│   │       ├── Category.java
│   │       └── ...
│   ├── src/main/resources/
│   │   ├── application.yaml            # 应用配置
│   │   ├── application-dev.yaml        # 开发环境配置
│   │   ├── application-prod.yaml       # 生产环境配置
│   │   └── mapper/                     # MyBatis XML映射
│   ├── src/main/java/com/blog/WeBlogApplication.java  # 启动类
│   └── pom.xml                         # 业务模块依赖
│
├── weblog-vue3/                        # 前端项目（独立）
│   ├── src/
│   │   ├── api/                        # API接口定义
│   │   │   ├── admin/                  # 后台管理API
│   │   │   └── frontend/               # 前台API
│   │   ├── assets/                     # 静态资源
│   │   ├── components/                 # 公共组件
│   │   ├── composables/                # 组合式函数
│   │   ├── layouts/                    # 布局组件
│   │   ├── pages/                      # 页面组件
│   │   │   ├── admin/                  # 后台页面
│   │   │   └── front/                  # 前台页面
│   │   ├── router/                     # 路由配置
│   │   ├── store/                      # Vuex状态管理
│   │   └── utils/                      # 工具函数
│   ├── public/                         # 公共资源
│   ├── package.json                    # 前端依赖配置
│   ├── vite.config.js                  # Vite构建配置
│   ├── tailwind.config.js              # Tailwind配置
│   └── postcss.config.js               # PostCSS配置
│
├── sql/                                # 数据库脚本
│   └── weblog.sql                      # 数据库初始化脚本
│
├── pom.xml                             # 根pom.xml（聚合模块）
├── .gitignore
└── README.md
```

### 构建说明
```bash
# 1. 完整构建所有模块（在项目根目录）
mvn clean install

# 2. 仅构建server模块（会自动构建依赖的common和pojo模块）
cd WeBlog-server
mvn clean package

# 3. 运行项目
java -jar target/WeBlog-server-0.0.1-SNAPSHOT.jar

# 4. 开发环境运行（热部署）
cd WeBlog-server
mvn spring-boot:run
```

### 模块职责说明
- **WeBlog-parent**: 依赖管理、统一配置、插件管理，不包含业务代码
- **WeBlog-common**: 通用组件、工具类、配置类、异常处理、安全过滤、OSS操作等
- **WeBlog-pojo**: 数据传输对象（DTO）、视图对象（VO）、查询参数等数据模型
- **WeBlog-server**: 业务逻辑、控制器、服务层、数据访问层、实体类等核心业务
- **weblog-vue3**: 前端展示层，独立部署，通过RESTful API与后端通信

## ✨ 功能特性

### 后台管理功能
- **文章管理**: 文章的增删改查、发布、分类、标签管理
- **用户管理**: 用户信息管理、权限控制
- **分类管理**: 文章分类管理
- **标签管理**: 文章标签管理
- **博客设置**: 博客基本信息配置
- **仪表盘**: 数据统计和可视化
- **认证授权**: 基于JWT和Spring Security的认证系统

### 前台展示功能
- **文章展示**: 文章列表、详情页
- **分类浏览**: 按分类查看文章
- **标签云**: 标签展示和筛选
- **搜索功能**: 文章搜索
- **访客统计**: 访问量统计
- **响应式设计**: 支持移动端和桌面端

## 🏃 快速开始

### 环境要求
- **JDK**: 17+
- **MySQL**: 8.0+
- **Node.js**: 16+
- **Maven**: 3.6+

### 1. 数据库初始化

1. 创建MySQL数据库：
```sql
CREATE DATABASE weblog CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
```

2. 执行初始化脚本：
```bash
mysql -u root -p weblog < sql/weblog.sql
```

### 2. 后端配置和启动

#### 配置文件修改
修改数据库配置（`WeBlog-server/src/main/resources/application.yaml`）：
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/weblog?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai
    username: root
    password: 123456  # 修改为你的数据库密码
```

如果需要使用阿里云OSS，配置OSS信息（同上文件）：
```yaml
aliyun:
  oss:
    endpoint: https://oss-cn-beijing.aliyuncs.com
    bucketName: your-bucket-name
    access-key-id: your-access-key-id
    access-key-secret: your-access-key-secret
```

#### 多模块构建方式
```bash
# 方式一：从根目录构建所有模块（推荐）
# 确保在项目根目录（包含pom.xml的目录）
mvn clean install

# 方式二：单独构建server模块（会自动构建依赖模块）
cd WeBlog-server
mvn clean package

# 运行项目
java -jar target/WeBlog-server-0.0.1-SNAPSHOT.jar
```

#### 开发环境运行
```bash
# 在WeBlog-server目录下直接运行（支持热部署）
cd WeBlog-server
mvn spring-boot:run
```

后端默认运行在 `http://localhost:8081`

### 3. 前端配置和启动

1. 进入前端目录：
```bash
cd weblog-vue3
```

2. 安装依赖：
```bash
npm install
```

3. 启动前端开发服务器：
```bash
npm run dev
```

前端默认运行在 `http://localhost:5173`

4. 配置代理（如果需要，在vite.config.js中添加）：
```javascript
server: {
  proxy: {
    '/api': {
      target: 'http://localhost:8081',
      changeOrigin: true,
      rewrite: (path) => path.replace(/^\/api/, '')
    }
  }
}
```

## 📚 API文档

项目集成了Swagger UI，启动后端服务后访问：
- **Swagger UI**: http://localhost:8081/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8081/v3/api-docs

## 🚢 部署说明

### 后端部署

1. 生产环境配置：
创建 `application-druid.yaml` 文件并设置：
```yaml
spring:
  profiles:
    active: druid
```

2. 使用Docker部署（Dockerfile示例）：
```dockerfile
FROM openjdk:17-jdk-slim
COPY target/WeBlog-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

### 前端部署

1. 构建生产版本：
```bash
npm run build
```

2. 部署构建产物（在 `dist/` 目录）到Web服务器。

### Nginx配置示例

```nginx
server {
    listen 80;
    server_name your-domain.com;

    # 前端静态文件
    location / {
        root /path/to/weblog-vue3/dist;
        index index.html;
        try_files $uri $uri/ /index.html;
    }

    # 后端API代理
    location /api/ {
        proxy_pass http://localhost:8081/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

## 🤝 贡献指南

欢迎提交Issue和Pull Request！

1. Fork 本仓库
2. 创建功能分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启一个Pull Request

### 开发规范

- 后端代码遵循Java开发规范
- 前端代码使用ESLint进行代码检查
- 提交信息使用英文描述
- 确保所有测试通过

## 📄 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情

## 📞 联系方式

如有问题或建议，请通过以下方式联系：

- 提交 [Issue](https://github.com/frefsd/WeBlog/issues)

## 🙏 致谢

感谢以下开源项目的支持：
- Spring Boot
- Vue.js
- Element Plus
- MyBatis-Plus
- 以及其他所有依赖的开源项目

---

**提示**: 首次使用请确保修改数据库配置、JWT密钥和OSS密钥等敏感信息。