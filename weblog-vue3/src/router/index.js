import { createRouter, createWebHistory } from 'vue-router';
const AdminIndex = () => import('@/pages/admin/index.vue')
const About = () => import('@/pages/about.vue')
const NotFound = () => import('@/pages/404.vue')
const AdminLogin = () => import('@/pages/admin/login.vue')
const Admin = () => import('@/layouts/admin.vue')
const AdminArticleList = () => import('@/pages/admin/article-list.vue')
const AdminCategoryList = () => import('@/pages/admin/category-list.vue')
const AdminTagList = () => import('@/pages/admin/tag-list.vue')
const AdminBlogSetting = () => import('@/pages/admin/blog-setting.vue')
const AdminMonitorLog = () => import('@/pages/admin/monitor-log.vue')
const AdminMonitorRule = () => import('@/pages/admin/monitor-rule.vue')
const AdminMonitorAlert = () => import('@/pages/admin/monitor-alert.vue')
const Index = () => import('@/pages/frontend/index.vue')
const ArticleDetail = () => import('@/pages/frontend/article-detail.vue')
const CategoryList = () => import('@/pages/frontend/category-list.vue')
const TagList = () => import('@/pages/frontend/tag-list.vue')
const CategoryArticleList = () => import('@/pages/frontend/category-article-list.vue')
const TagArticleList = () => import('@/pages/frontend/tag-article-list.vue')
const ArchiveList = () => import('@/pages/frontend/archive-list.vue')
const SearchResults = () => import('@/pages/frontend/search-results.vue')
const AiChat = () => import('@/pages/frontend/ai-chat.vue')

const routes = [
    {
        // 指定访问路径
        path: '/admin',
        component: Admin,
        // 使用到 admin.vue 布局的，都需要放置在其子路由下面
        children: [{
            path: '/admin',
            component: AdminIndex,
            meta: {
                title: '仪表盘'
            }
        }, {
            path: '/admin/article/list',
            component: AdminArticleList,
            meta: {
                title: '文章管理'
            }
        }, {
            path: '/admin/category/list',
            component: AdminCategoryList,
            meta: {
                title: '分类管理'
            }
        }, {
            path: '/admin/tag/list',
            component: AdminTagList,
            meta: {
                title: '标签管理'
            }
        }, {
            path: '/admin/blog/setting',
            component: AdminBlogSetting,
            meta: {
                title: '博客设置'
            }
        }, {
            path: '/admin/monitor/log',
            component: AdminMonitorLog,
            meta: {
                title: '实时日志'
            }
        }, {
            path: '/admin/monitor/rule',
            component: AdminMonitorRule,
            meta: {
                title: '告警规则'
            }
        }, {
            path: '/admin/monitor/alert',
            component: AdminMonitorAlert,
            meta: {
                title: '告警记录'
            }
        }]

    },
    {
        path: '/about',
        component: About
    },
    {
        path: '/login',
        component: AdminLogin,
        meta: {
            title: '登录页'
        }
    },
    {
        path: '/',
        component: Index,
        meta: {
            title: 'WeBlog首页'
        }
    },
    {
        path: '/article/detail',
        component: ArticleDetail,
        meta: {
            title: '文章详情页'
        },
    },
    {
        path: '/category',
        component: CategoryList,
        meta: {
            title: '分类'
        },
    },
    {
        path: '/tag',
        component: TagList,
        meta: {
            title: '标签'
        },
    },
    {
        path: '/category/list',
        component: CategoryArticleList,
        meta: {
            title: '分类文章列表'
        },
    },
    {
        path: '/tag/list',
        component: TagArticleList,
        meta: {
            title: '标签文章列表'
        },
    },
    {
        path: '/archive',
        component: ArchiveList,
        meta: {
            title: '归档'
        },
    },
    {
        path: '/search',
        component: SearchResults,
        meta: {
            title: '搜索结果'
        },
    },
    {
        path: '/ai',
        component: AiChat,
        meta: {
            title: '小智'
        },
    },
    // 将匹配所有内容并将其放在 `$route.params.pathMatch` 下
    // 注意：404路由必须放在所有具体路由之后
    {
        path: '/:pathMatch(.*)*',
        name: 'NotFound',
        component: NotFound
    }
]

const router = createRouter({
    // 4. 内部提供了 history 模式的实现。为了简单起见，我们在这里使用 hash 模式。
    history: createWebHistory(),
    scrollBehavior: (_to, _from, savedPosition) => savedPosition || { top: 0 },
    routes, // `routes: routes` 的缩写
})

// ES6 模块导出语句，它用于将 router 对象导出，以便其他文件可以导入和使用这个对象
export default router