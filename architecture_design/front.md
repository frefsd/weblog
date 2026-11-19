# WeBlog 前端技术栈分析报告

## 项目概述

WeBlog 前端是一个基于 **Vue 3** 生态构建的现代化博客系统前端，采用前后端分离架构，通过 RESTful API 与后端通信。项目使用 **Vite 4** 作为构建工具，**Element Plus** 作为主要 UI 组件库，并集成了丰富的第三方库（富文本编辑器、图表、动画、代码高亮等）。

---

## 一、核心技术栈

### 1. 前端框架与构建工具

| 技术 | 版本 | 用途 |
|------|------|------|
| Vue | 3.2.47 | 核心框架（Composition API + `<script setup>`） |
| Vite | 4.1.4 | 构建工具与开发服务器 |
| @vitejs/plugin-vue | 4.0.0 | Vite 的 Vue 3 插件 |

**关键配置**（[vite.config.js](weblog-vue3/vite.config.js)）：
- 开发服务器端口：**6066**
- API 代理：`/api` → `http://localhost:8081`（路径重写去掉 `/api` 前缀）
- `@` 别名指向 `src/` 目录
- 自动导入 Element Plus 组件和 API

### 2. 状态管理与路由

| 技术 | 版本 | 用途 |
|------|------|------|
| Vue Router | 4.1.6 | 前端路由（`createWebHistory` 模式） |
| Vuex | 4.0.2 | 集中式状态管理 |

**Vuex Store**（[store/index.js](weblog-vue3/src/store/index.js)）：
- `user` — 当前用户信息
- `setting` — 博客设置信息
- `menuWidth` — 后台侧边栏宽度（折叠控制）

**路由结构**（[router/index.js](weblog-vue3/src/router/index.js)）：
- **前台路由**：首页、文章详情、分类列表、标签列表、归档、搜索结果、关于
- **后台路由**：仪表盘、文章管理、分类管理、标签管理、博客设置、监控中心（实时日志/告警规则/告警记录）（嵌套在 `admin.vue` 布局内）
- **认证路由**：登录页
- **404** 通配匹配

**导航守卫**（[permission.js](weblog-vue3/src/permission.js)）：
- 全局前置守卫：检查 Token → 获取用户信息 → 加载博客设置 → 权限校验
- 全局后置守卫：设置页面标题、隐藏加载进度条

### 3. UI 组件与样式

| 技术 | 版本 | 用途 |
|------|------|------|
| Element Plus | 2.3.3 | **主要 UI 组件库**（后台管理界面） |
| element-plus/icons-vue | 2.1.0 | Element Plus 图标库 |
| Naive UI | 2.34.3 | 备用 UI 组件库（devDependency） |
| @vicons/ionicons5 | 0.12.0 | Naive UI 配套图标 |
| Tailwind CSS | 3.3.2 | **实用优先的 CSS 框架** |
| WindiCSS | 3.5.6 | 按需生成的 CSS 框架（`vite-plugin-windicss`） |
| Flowbite | 1.7.0 | 基于 Tailwind CSS 的 UI 组件库 |
| Animate.css | 4.1.1 | CSS 动画库 |
| PostCSS | 8.4.25 | CSS 后处理器 |
| autoprefixer | 10.4.14 | 自动添加 CSS 浏览器前缀 |

**Element Plus 自动导入**：通过 `unplugin-auto-import` 和 `unplugin-vue-components` 自动解析，无需手动引入。

**Element Plus 配置**：
- 全局中文语言包（`zh-cn`）
- 全局注册所有图标组件
- 后台菜单使用 Element Plus 的 `el-menu` 组件

**Tailwind 配置**（[tailwind.config.js](weblog-vue3/tailwind.config.js)）：
- `preflight: false`（不重置默认样式，避免与 Element Plus 冲突）
- 自定义字体尺寸（`xxs` ~ `7xl`）
- 自定义间距、最大宽度、行高
- `flowbite/plugin` 插件

**样式文件**：
- [main.css](weblog-vue3/src/assets/main.css) — Tailwind 指令入口（`@tailwind base/components/utilities`）
- [enhancements.css](weblog-vue3/src/assets/enhancements.css) — 全局增强样式（卡片、按钮、文章内容排版等）
- [App.vue](weblog-vue3/src/App.vue) — 全局基础字体与 NProgress 进度条颜色

---

## 二、HTTP 通信

### Axios 封装（[axios.js](weblog-vue3/src/axios.js)）

| 配置项 | 值 |
|--------|-----|
| baseURL | `import.meta.env.VITE_APP_BASE_API`（即 `/api`） |
| 超时时间 | 7000ms |

**请求拦截器**：
- 自动从 Cookie 获取 Token，添加到 `Authorization: Bearer <token>` 请求头

**响应拦截器**：
- 成功：直接返回 `response.data`
- 失败：网络错误提示 / 401 自动登出 / 错误消息提示

### API 模块

**后台 API**（`src/api/admin/`）：

| 文件 | 主要接口 |
|------|---------|
| [article.js](weblog-vue3/src/api/admin/article.js) | 文章详情、发布、分页列表、删除、更新 |
| [category.js](weblog-vue3/src/api/admin/category.js) | 分类增删改查 |
| [tag.js](weblog-vue3/src/api/admin/tag.js) | 标签增删改查 |
| [user.js](weblog-vue3/src/api/admin/user.js) | 登录、获取管理员信息、修改密码 |
| [dashboard.js](weblog-vue3/src/api/admin/dashboard.js) | 文章统计、发布统计、PV 统计、分类/标签统计 |
| [blogsetting.js](weblog-vue3/src/api/admin/blogsetting.js) | 博客设置更新 |
| [file.js](weblog-vue3/src/api/admin/file.js) | 文件上传 |
| [monitor.js](weblog-vue3/src/api/admin/monitor.js) | 监控日志分页/清空、告警规则CRUD、告警记录分页 |

**前台 API**（`src/api/frontend/`）：

| 文件 | 主要接口 |
|------|---------|
| [index.js](weblog-vue3/src/api/frontend/index.js) | 首页文章列表 |
| [article.js](weblog-vue3/src/api/frontend/article.js) | 文章详情、文章搜索 |
| [category.js](weblog-vue3/src/api/frontend/category.js) | 分类列表、分类文章 |
| [tag.js](weblog-vue3/src/api/frontend/tag.js) | 标签列表、标签文章 |
| [blogsetting.js](weblog-vue3/src/api/frontend/blogsetting.js) | 博客设置详情 |
| [archive.js](weblog-vue3/src/api/frontend/archive.js) | 文章归档 |

---

## 三、功能模块

### 1. 前台展示

| 页面 | 路由 | 功能 |
|------|------|------|
| 首页 | `/` | 文章列表（分页）、分类/标签侧边栏 |
| 文章详情 | `/article/detail` | Markdown 渲染、代码高亮、图片预览 |
| 分类列表 | `/category` | 全部分类展示 |
| 标签列表 | `/tag` | 全部标签展示 |
| 分类文章 | `/category/list` | 按分类筛选文章 |
| 标签文章 | `/tag/list` | 按标签筛选文章 |
| 归档 | `/archive` | 文章归档（按时间） |
| 搜索结果 | `/search` | 文章搜索结果 |
| 关于 | `/about` | 关于页面 |

### 2. 后台管理

| 页面 | 路由 | 功能 |
|------|------|------|
| 仪表盘 | `/admin` | ECharts 图表统计（PV、发布趋势、分类/标签分布） |
| 文章管理 | `/admin/article/list` | 文章列表、发布（Markdown）、编辑、删除 |
| 分类管理 | `/admin/category/list` | 分类 CRUD |
| 标签管理 | `/admin/tag/list` | 标签 CRUD |
| 博客设置 | `/admin/blog/setting` | 博客名称、作者、头像、社交链接等配置 |
| 实时日志 | `/admin/monitor/log` | 监控日志查看（级别/类型筛选、分页、清空、堆栈展开） |
| 告警规则 | `/admin/monitor/rule` | 告警规则 CRUD（级别/时间窗口/阈值配置、启用/禁用） |
| 告警记录 | `/admin/monitor/alert` | 告警触发历史记录查看（分页、只读） |
| 登录 | `/login` | 管理员登录 |

### 3. 公共功能

- **暗黑模式**：手动切换 + 跟随系统偏好，状态持久化到 `localStorage`
- **文章搜索**：防抖搜索（300ms），桌面端模态框展示结果，移动端全屏展示
- **JWT 认证**：Cookie 存储 Token，Axios 拦截器统一添加请求头
- **页面加载进度条**：NProgress（蓝色 `#409eff`）
- **代码高亮**：Highlight.js（tokyo-night-dark 主题）
- **图片预览**：v-viewer（文章富文本图片点击放大）
- **Keep-alive 缓存**：后台页面最大缓存 10 个组件

---

## 四、布局组件

### 后台布局（[admin.vue](weblog-vue3/src/layouts/admin.vue)）

```
el-container
├── el-aside (菜单栏)
│   └── AdminMenu (仪表盘/文章/分类/标签/博客设置/监控中心子菜单)
├── el-container
│   ├── el-header
│   │   └── AdminHeader
│   ├── el-main
│   │   ├── AdminTagList (标签导航)
│   │   └── <router-view> (keep-alive 缓存)
│   └── el-footer
│       └── AdminFooter
```

### 前台布局

- **Header**（[Header.vue](weblog-vue3/src/layouts/components/Header.vue)）：Logo、导航菜单、搜索框、暗黑模式切换、登录/用户菜单
- **Footer**（[Footer.vue](weblog-vue3/src/layouts/components/Footer.vue)）：版权信息

---

## 五、Composable 工具函数

| 文件 | 导出函数 | 功能 |
|------|---------|------|
| [auth.js](weblog-vue3/src/composables/auth.js) | `getToken/setToken/removeToken` | JWT Token 的 Cookie 读写（`@vueuse/integrations/useCookies`） |
| [util.js](weblog-vue3/src/composables/util.js) | `notification/showModel/showMessage` | Element Plus 通知/对话框/消息封装 |
| | `showPageLoading/hidePageLoading` | NProgress 加载进度条控制 |

---

## 六、可复用组件（`src/components/`）

| 组件 | 用途 |
|------|------|
| [MDEditor.vue](weblog-vue3/src/components/MDEditor.vue) | mavon-editor Markdown 编辑器封装 |
| [UserInfoCard.vue](weblog-vue3/src/components/UserInfoCard.vue) | 用户信息卡片（首页侧边栏） |
| [FormDrawer.vue](weblog-vue3/src/components/FormDrawer.vue) | 抽屉表单组件 |
| [CountTo.vue](weblog-vue3/src/components/CountTo.vue) | 数字滚动动画组件 |
| [PVChart.vue](weblog-vue3/src/components/PVChart.vue) | PV 统计折线图（ECharts） |
| [ArticlePublishChart.vue](weblog-vue3/src/components/ArticlePublishChart.vue) | 文章发布趋势图（ECharts） |
| [CategoryArticleChart.vue](weblog-vue3/src/components/CategoryArticleChart.vue) | 分类文章扇形图（ECharts） |
| [TagArticleChart.vue](weblog-vue3/src/components/TagArticleChart.vue) | 标签文章柱状图（ECharts） |

---

## 七、富文本编辑器

项目集成了 **3 个** Markdown 编辑器：

| 编辑器 | 安装版本 | 用途 |
|--------|---------|------|
| **mavon-editor** | 3.0.1 | 主要编辑器，用于后台文章发布（[MDEditor.vue](weblog-vue3/src/components/MDEditor.vue)） |
| md-editor-v3 | 3.0.1 | 备用编辑器 |
| editor.md | 1.5.0 | 备用编辑器 |

---

## 八、数据可视化

使用 **ECharts 5.4.2** 实现后台仪表盘的数据图表：

- **PVChart** — 最近 7 天 PV 访问量折线图
- **ArticlePublishChart** — 最近 7 天文章发布趋势图
- **CategoryArticleChart** — 文章分类占比扇形图
- **TagArticleChart** — 文章标签分布柱状图

---

## 九、认证与安全

### JWT 流程

```
登录 → 后端返回 Token → 存入 Cookie (@vueuse/useCookies)
  → Axios 请求拦截器添加 Bearer Token → 后端验证
    → 401 响应 → Cookie 清除 → 跳转登录页
    → 403 响应（含 JSON body）→ 权限不足提示，不跳登录
    → 403 响应（空 body）→ 视为未登录，跳转登录页
```

**401/403 区分逻辑**：SecurityConfig 配置了自定义 JSON 响应，认证失败返回 401 + `{"success":false,"message":"请先登录"}`，权限不足返回 403 + `{"success":false,"message":"权限不足：您没有执行该操作的权限"}`。前端 Axios 响应拦截器检查 `success` 字段判断是否跳登录。

### Token 存储方案

- 使用 `@vueuse/integrations/useCookies` 操作 Cookie
- Cookie key：`Authorization`

### 路由守卫权限控制

- 访问 `/admin/*` 无 Token → 跳转 `/login` 并附带 `redirect` 参数
- 已登录访问 `/login` → 跳转回来源页或首页
- 页面标题自动设置为 `路由meta.title - WeBlog`

---

## 十、开发与构建配置

### 环境变量

| 文件 | 内容 |
|------|------|
| [.env.development](weblog-vue3/.env.development) | `VITE_APP_BASE_API=/api` |
| [.env.production](weblog-vue3/.env.production) | `VITE_APP_BASE_API=/api` |

### 构建命令

| 命令 | 用途 |
|------|------|
| `npm run dev` | 启动开发服务器（热重载，端口 6066） |
| `npm run build` | 生产构建 |
| `npm run preview` | 预览生产构建 |

### Vite 插件

| 插件 | 用途 |
|------|------|
| `@vitejs/plugin-vue` | Vue 3 SFC 编译 |
| `vite-plugin-windicss` | WindiCSS 按需生成 |
| `unplugin-auto-import` | 自动导入 API（Element Plus 解析器） |
| `unplugin-vue-components` | 自动导入组件（Element Plus 解析器） |

---

## 十一、项目结构总览

```
weblog-vue3/
├── index.html                      # HTML 入口
├── vite.config.js                  # Vite 构建配置
├── tailwind.config.js              # Tailwind CSS 配置
├── postcss.config.js               # PostCSS 配置
├── package.json                    # 依赖管理
├── .env.development                # 开发环境变量
├── .env.production                 # 生产环境变量
│
└── src/
    ├── main.js                     # 应用入口（注册插件、全局组件）
    ├── App.vue                     # 根组件（Element Plus 中文配置）
    ├── axios.js                    # Axios 实例（拦截器封装）
    ├── permission.js               # 路由导航守卫
    │
    ├── api/                        # API 接口层
    │   ├── admin/                  # 后台 API（8 个模块）
    │   │   ├── article.js
    │   │   ├── blogsetting.js
    │   │   ├── category.js
    │   │   ├── dashboard.js
    │   │   ├── file.js
    │   │   ├── tag.js
    │   │   ├── user.js
    │   │   └── monitor.js
    │   └── frontend/               # 前台 API（6 个模块）
    │       ├── article.js
    │       ├── archive.js
    │       ├── blogsetting.js
    │       ├── category.js
    │       ├── index.js
    │       └── tag.js
    │
    ├── router/
    │   └── index.js                # 路由配置（WebHistory 模式）
    │
    ├── store/
    │   └── index.js                # Vuex 状态管理
    │
    ├── composables/                # 组合式函数
    │   ├── auth.js                 # Token Cookie 操作
    │   └── util.js                 # 工具函数封装
    │
    ├── layouts/                    # 布局组件
    │   ├── admin.vue               # 后台布局（侧边栏+顶栏+主体+底栏）
    │   └── components/
    │       ├── AdminMenu.vue       # 后台菜单
    │       ├── AdminHeader.vue     # 后台顶栏
    │       ├── AdminTagList.vue    # 后台标签导航
    │       ├── AdminFooter.vue     # 后台底栏
    │       ├── Header.vue          # 前台顶栏（导航+搜索+暗黑模式）
    │       └── Footer.vue          # 前台底栏
    │
    ├── pages/                      # 页面组件
    │   ├── admin/                  # 后台页面（9 个）
    │   │   ├── index.vue           # 仪表盘
    │   │   ├── login.vue           # 登录
    │   │   ├── article-list.vue    # 文章管理
    │   │   ├── category-list.vue   # 分类管理
    │   │   ├── tag-list.vue        # 标签管理
    │   │   ├── blog-setting.vue    # 博客设置
    │   │   ├── monitor-log.vue     # 实时日志
    │   │   ├── monitor-rule.vue     # 告警规则
    │   │   └── monitor-alert.vue    # 告警记录
    │   ├── frontend/               # 前台页面（8 个）
    │   │   ├── index.vue           # 首页
    │   │   ├── article-detail.vue  # 文章详情
    │   │   ├── category-list.vue   # 分类列表
    │   │   ├── tag-list.vue        # 标签列表
    │   │   ├── category-article-list.vue
    │   │   ├── tag-article-list.vue
    │   │   ├── archive-list.vue    # 归档
    │   │   └── search-results.vue  # 搜索结果
    │   ├── 404.vue                 # 404 页面
    │   └── about.vue               # 关于页面
    │
    └── components/                 # 公共组件
        ├── MDEditor.vue            # Markdown 编辑器
        ├── UserInfoCard.vue        # 用户信息卡片
        ├── FormDrawer.vue          # 抽屉表单
        ├── CountTo.vue             # 数字滚动
        ├── PVChart.vue             # PV 统计折线图
        ├── ArticlePublishChart.vue # 文章发布趋势图
        ├── CategoryArticleChart.vue# 分类扇形图
        └── TagArticleChart.vue     # 标签柱状图
```

---

## 十二、依赖清单

### 生产依赖（dependencies）

| 包名 | 版本 | 分类 |
|------|------|------|
| vue | ^3.2.47 | 核心框架 |
| vue-router | ^4.1.6 | 路由 |
| vuex | ^4.0.2 | 状态管理 |
| element-plus | ^2.3.3 | UI 组件库 |
| @element-plus/icons-vue | ^2.1.0 | 图标库 |
| axios | ^1.3.5 | HTTP 客户端 |
| echarts | ^5.4.2 | 数据图表 |
| mavon-editor | ^3.0.1 | Markdown 编辑器 |
| md-editor-v3 | ^3.0.1 | Markdown 编辑器 |
| editor.md | ^1.5.0 | Markdown 编辑器 |
| tailwindcss | ^3.3.2 | CSS 框架 |
| windicss | ^3.5.6 | 按需 CSS |
| flowbite | ^1.7.0 | UI 组件 |
| @vueuse/core | ^10.0.2 | Vue 工具库 |
| @vueuse/integrations | ^10.0.2 | 工具集成 |
| @headlessui/vue | ^1.7.14 | 无样式 UI |
| @heroicons/vue | ^2.0.18 | 图标库 |
| highlight.js | ^11.8.0 | 代码高亮 |
| vue-highlightjs | ^1.3.3 | 代码高亮集成 |
| vue3-highlightjs | ^1.0.5 | 代码高亮集成 |
| gsap | ^3.11.5 | 动画库 |
| moment | ^2.29.4 | 日期处理 |
| nprogress | ^0.2.0 | 进度条 |
| animate.css | ^4.1.1 | CSS 动画 |
| universal-cookie | ^4.0.4 | Cookie 操作 |
| v-viewer | ^3.0.11 | 图片预览 |

### 开发依赖（devDependencies）

| 包名 | 版本 | 分类 |
|------|------|------|
| vite | ^4.1.4 | 构建工具 |
| @vitejs/plugin-vue | ^4.0.0 | Vite Vue 插件 |
| naive-ui | ^2.34.3 | 备用 UI 组件库 |
| @vicons/ionicons5 | ^0.12.0 | 图标库 |
| unplugin-auto-import | ^0.16.6 | 自动导入 |
| unplugin-vue-components | ^0.25.1 | 自动导入组件 |
| vite-plugin-windicss | ^1.8.10 | WindiCSS 插件 |
| postcss | ^8.4.25 | CSS 后处理器 |
| postcss-import | ^15.1.0 | PostCSS 导入 |
| autoprefixer | ^10.4.14 | 浏览器前缀 |
| vfonts | ^0.0.3 | 字体 |

---

## 十三、架构特点总结

1. **前后端分离**：通过 RESTful API 通信，前端独立部署
2. **组件化开发**：公共组件、布局组件、页面组件三层划分
3. **Composition API**：统一使用 `<script setup>` 语法，逻辑按功能组织
4. **多 UI 库共存**：Element Plus 主导后台 + Tailwind/WindiCSS 主导前台 + Naive UI 备用
5. **多编辑器支持**：集成 3 个 Markdown 编辑器以适应不同场景
6. **自动导入优化**：通过 unplugin 实现 Element Plus 组件和 API 的按需自动导入
7. **JWT 无状态认证**：Cookie 存储 Token，Axios 拦截器统一鉴权
8. **暗黑模式**：支持手动切换和系统偏好检测
