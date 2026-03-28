<template>
    <header class="sticky top-0 z-100">


        <nav class="bg-white border-gray-200 border-b dark:bg-gray-900">
            <div class="max-w-screen-xl flex flex-wrap items-center justify-between mx-auto p-4">
                <!-- LOGO -->
                <a href="/" class="flex items-center">
                    <img src="https://flowbite.com/docs/images/logo.svg" class="h-8 mr-3" alt="Flowbite Logo" />
                    <span class="self-center text-2xl font-semibold whitespace-nowrap dark:text-white">
                        {{ $store.state.setting.blogName }}
                    </span>
                </a>
                <div class="flex items-center md:order-2">

                    <span class="text-gray-900 hover:text-blue-700" v-if="isLogin == false"
                        @click="$router.push('/login')">登录</span>
                    <button type="button" v-else
                        class="flex mr-3 text-sm rounded-full md:mr-0 focus:ring-4 focus:ring-gray-300 dark:focus:ring-gray-600"
                        id="user-menu-button" aria-expanded="false" data-dropdown-toggle="user-dropdown"
                        data-dropdown-placement="bottom">
                        <span class="sr-only">Open user menu</span>
                        <!-- 用户登录头像 -->
                        <img class="w-8 h-8 rounded-full" :src="$store.state.setting.avatar" alt="user photo">
                    </button>
                    <!-- Dropdown menu -->
                    <div class="z-50 hidden my-4 text-base list-none bg-white divide-y divide-gray-100 rounded-lg shadow dark:bg-gray-700 dark:divide-gray-600"
                        id="user-dropdown">
                        <ul class="py-2" aria-labelledby="user-menu-button">
                            <li>
                                <a @click="$router.push('/admin')"
                                    class="block px-4 py-2 text-sm text-gray-700 hover:bg-gray-100 dark:hover:bg-gray-600 dark:text-gray-200 dark:hover:text-white">
                                    <svg class="inline w-3 h-3 mb-2px mr-1 text-gray-700 dark:text-white"
                                        aria-hidden="true" xmlns="http://www.w3.org/2000/svg" fill="none"
                                        viewBox="0 0 20 20">
                                        <path stroke="currentColor" stroke-linecap="round" stroke-linejoin="round"
                                            stroke-width="2"
                                            d="M10 14v4m-4 1h8M1 10h18M2 1h16a1 1 0 0 1 1 1v11a1 1 0 0 1-1 1H2a1 1 0 0 1-1-1V2a1 1 0 0 1 1-1Z" />
                                    </svg>
                                    进入后台
                                </a>
                            </li>
                            <li>
                                <a @click="logout"
                                    class="block px-4 py-2 text-sm text-gray-700 hover:bg-gray-100 dark:hover:bg-gray-600 dark:text-gray-200 dark:hover:text-white">
                                    <svg class="inline w-3 h-3 mb-2px mr-1 text-gray-700 dark:text-white"
                                        aria-hidden="true" xmlns="http://www.w3.org/2000/svg" fill="none"
                                        viewBox="0 0 16 16">
                                        <path stroke="currentColor" stroke-linecap="round" stroke-linejoin="round"
                                            stroke-width="2"
                                            d="M4 8h11m0 0-4-4m4 4-4 4m-5 3H3a2 2 0 0 1-2-2V3a2 2 0 0 1 2-2h3" />
                                    </svg>
                                    退出登录
                                </a>
                            </li>
                        </ul>
                    </div>
                    <button data-collapse-toggle="navbar-user" type="button"
                        class="inline-flex items-center p-2 w-10 h-10 justify-center text-sm text-gray-500 rounded-lg md:hidden hover:bg-gray-100 focus:outline-none focus:ring-2 focus:ring-gray-200 dark:text-gray-400 dark:hover:bg-gray-700 dark:focus:ring-gray-600"
                        aria-controls="navbar-user" aria-expanded="false">
                        <span class="sr-only">Open main menu</span>
                        <svg class="w-5 h-5" aria-hidden="true" xmlns="http://www.w3.org/2000/svg" fill="none"
                            viewBox="0 0 17 14">
                            <path stroke="currentColor" stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                d="M1 1h15M1 7h15M1 13h15" />
                        </svg>
                    </button>
                </div>
                <div class="items-center justify-between hidden w-full md:flex md:w-auto md:order-1" id="navbar-user">
                    <ul
                        class="flex flex-col font-medium p-4 md:p-0 mt-4 border border-gray-100 rounded-lg bg-gray-50 md:flex-row md:space-x-8 md:mt-0 md:border-0 md:bg-white dark:bg-gray-800 md:dark:bg-gray-900 dark:border-gray-700">
                        <li>
                            <a @click="$router.push('/')" :class="[currPath == '/' ? 'text-blue-700' : 'text-gray-900']"
                                class="block py-2 pl-3 pr-4 rounded md:bg-transparent md:p-0 md:dark:text-blue-500"
                                aria-current="page">首页</a>
                        </li>
                        <li>
                            <a @click="$router.push('/category')"
                                :class="[currPath == '/category' ? 'text-blue-700' : 'text-gray-900']"
                                class="block py-2 pl-3 pr-4 rounded hover:bg-gray-100 md:hover:bg-transparent md:hover:text-blue-700 md:p-0 dark:text-white md:dark:hover:text-blue-500 dark:hover:bg-gray-700 dark:hover:text-white md:dark:hover:bg-transparent dark:border-gray-700">分类</a>
                        </li>
                        <li>
                            <a @click="$router.push('/tag')"
                                :class="[currPath == '/tag' ? 'text-blue-700' : 'text-gray-900']"
                                class="block py-2 pl-3 pr-4 rounded hover:bg-gray-100 md:hover:bg-transparent md:hover:text-blue-700 md:p-0 dark:text-white md:dark:hover:text-blue-500 dark:hover:bg-gray-700 dark:hover:text-white md:dark:hover:bg-transparent dark:border-gray-700">标签</a>
                        </li>
                    </ul>
                </div>
            </div>
        </nav>


    </header>


</template>

<script setup>
import { useStore } from 'vuex'
import { useRoute } from 'vue-router';
import { ref } from 'vue';
import { showModel, showMessage } from '@/composables/util'
import { onMounted } from 'vue'
import { initDropdowns, initCollapses } from 'flowbite'


// initialize components based on data attribute selectors
onMounted(() => {
    initCollapses();
    initDropdowns();
})

const store = useStore()
const route = useRoute()

const currPath = ref(route.path)

console.log(store.state.user)
const keys = Object.keys(store.state.user)
console.log('====== keys')
console.log(keys)
const isLogin = ref(keys.length > 0)

function logout() {
    showModel('是否确定要退出登录？').then(() => {
        console.log('登出')
        store.dispatch('logout')

        // 提示登出成功
        showMessage('退出登录成功', 'success')

        isLogin.value = false
    }).catch(() => { })
}

</script>

<style scoped>
.container {
    max-width: 1260px;
}

.header-container {
    border-bottom: solid 1px var(--el-menu-border-color);
}

.el-menu--horizontal {
    border-bottom: 0;
}

.el-menu {
    /* max-width: 1260px; */
    height: 55px;
}

.el-header {
    border-bottom: 1px solid #dcdfe6;
    ;
    height: 55px;
}

.navbar-wrapper {
    height: 55px;
}

.logo-img {
    height: 40px;
}

.logo-container {
    /* display: flex;
    align-items: center; */
}

.logo-container>a {
    height: 28px;
    width: 128px;
}

.title {
    font-size: 1.6rem;
    font-weight: 800;
}

.title-li:hover {
    border-bottom: 1px solid #fff !important;
}

/* body {
    @apply bg-light-50;
} */

.bg-gray-hover:hover {
    border-bottom: 1px solid #fff !important;
    background-color: #f4f4f5 !important;
}
</style>