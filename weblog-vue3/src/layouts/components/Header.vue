<template>
    <header class="sticky top-0 z-100">
        <nav
            class="bg-white border-gray-200 border-b dark:bg-gray-900 dark:border-gray-800 transition-colors duration-300">
            <div class="max-w-screen-xl flex flex-wrap items-center justify-between mx-auto p-4">

                <!-- LOGO 区域 -->
                <a href="/" class="flex items-center">
                    <span
                        class="self-center text-2xl font-semibold whitespace-nowrap dark:text-white transition-colors">
                        {{ $store.state.setting.blogName }}
                    </span>
                </a>

                <!-- 右侧操作区：暗黑模式 + 搜索 + 用户 -->
                <div class="flex items-center md:order-2">

                    <!-- 暗黑模式切换按钮 (已移至此处，始终可见) -->
                    <button @click="toggleDarkMode" type="button"
                        class="p-2 mr-1 text-gray-500 rounded-lg hover:bg-gray-100 dark:text-gray-400 dark:hover:bg-gray-700 focus:outline-none focus:ring-2 focus:ring-gray-200 dark:focus:ring-gray-600 transition-colors"
                        :aria-label="isDark ? '切换到明亮模式' : '切换到暗黑模式'">
                        <!-- 太阳图标 (当前是亮色模式时显示) -->
                        <svg v-if="!isDark" xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none"
                            viewBox="0 0 24 24" stroke="currentColor">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                d="M12 3v1m0 16v1m8.66-8.66l-.71.71M4.05 19.07l-.71.71M21 12h-1M4 12H3m16.95 7.07l-.71-.71M4.05 4.93l-.71-.71M16 12a4 4 0 11-8 0 4 4 0 018 0z" />
                        </svg>
                        <!-- 月亮图标 (当前是暗色模式时显示) -->
                        <svg v-else xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 text-yellow-400" fill="none"
                            viewBox="0 0 24 24" stroke="currentColor">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                d="M21 12.79A9 9 0 1111.21 3a7 7 0 009.79 9.79z" />
                        </svg>
                    </button>

                    <!-- 移动端搜索触发按钮 -->
                    <button type="button" data-collapse-toggle="mobile-search-panel" aria-controls="mobile-search-panel"
                        aria-expanded="false"
                        class="md:hidden text-gray-500 dark:text-gray-400 hover:bg-gray-100 dark:hover:bg-gray-700 focus:outline-none focus:ring-4 focus:ring-gray-200 dark:focus:ring-gray-700 rounded-lg text-sm p-2.5 mr-1">
                        <svg class="w-5 h-5" aria-hidden="true" xmlns="http://www.w3.org/2000/svg" fill="none"
                            viewBox="0 0 20 20">
                            <path stroke="currentColor" stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                d="m19 19-4-4m0-7A7 7 0 1 1 1 8a7 7 0 0 1 14 0Z" />
                        </svg>
                        <span class="sr-only">搜索</span>
                    </button>

                    <!-- 桌面端搜索框 -->
                    <div class="relative hidden md:block search-container mr-3">
                        <div class="absolute inset-y-0 left-0 flex items-center pl-3 pointer-events-none">
                            <svg class="w-4 h-4 text-gray-500 dark:text-gray-400" aria-hidden="true"
                                xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 20 20">
                                <path stroke="currentColor" stroke-linecap="round" stroke-linejoin="round"
                                    stroke-width="2" d="m19 19-4-4m0-7A7 7 0 1 1 1 8a7 7 0 0 1 14 0Z" />
                            </svg>
                        </div>
                        <input type="text" id="search-navbar" ref="searchInputRef" v-model="searchKeyword"
                            @click="handleSearchClick"
                            class="block w-full p-2 pl-10 text-sm text-gray-900 border border-gray-300 rounded-lg bg-gray-50 focus:ring-blue-500 focus:border-blue-500 dark:bg-gray-700 dark:border-gray-600 dark:placeholder-gray-400 dark:text-white dark:focus:ring-blue-500 dark:focus:border-blue-500 transition-colors"
                            placeholder="搜索文章...">

                        <!-- 桌面端搜索模态框 (结果展示) -->
                        <div v-if="showSearchResults"
                            class="fixed inset-0 z-50 flex items-start justify-center pt-20 pointer-events-none">
                            <div class="absolute inset-0 bg-black bg-opacity-30 dark:bg-opacity-50"
                                @click="showSearchResults = false"></div>
                            <div class="relative z-50 w-full max-w-2xl mx-4 pointer-events-auto">
                                <div
                                    class="bg-white rounded-xl shadow-2xl dark:bg-gray-800 border border-gray-200 dark:border-gray-700 max-h-[80vh] overflow-hidden">
                                    <!-- 搜索头部 -->
                                    <div
                                        class="sticky top-0 bg-white dark:bg-gray-800 border-b border-gray-200 dark:border-gray-700 p-4">
                                        <div class="flex items-center">
                                            <button @click="showSearchResults = false"
                                                class="mr-3 text-gray-500 dark:text-gray-400 hover:text-gray-700 dark:hover:text-gray-300">
                                                <svg class="w-6 h-6" fill="none" stroke="currentColor"
                                                    viewBox="0 0 24 24">
                                                    <path stroke-linecap="round" stroke-linejoin="round"
                                                        stroke-width="2" d="M6 18L18 6M6 6l12 12" />
                                                </svg>
                                            </button>
                                            <div class="flex-1 relative">
                                                <input type="text" v-model="searchKeyword" ref="searchInputRef"
                                                    class="w-full p-4 pl-12 text-lg text-gray-900 bg-gray-50 dark:bg-gray-900 dark:text-white rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                                                    placeholder="搜索文章..." autofocus>
                                                <div class="absolute left-4 top-4 text-gray-500 dark:text-gray-400">
                                                    <svg class="w-5 h-5" fill="none" stroke="currentColor"
                                                        viewBox="0 0 24 24">
                                                        <path stroke-linecap="round" stroke-linejoin="round"
                                                            stroke-width="2"
                                                            d="m19 19-4-4m0-7A7 7 0 1 1 1 8a7 7 0 0 1 14 0Z" />
                                                    </svg>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                    <!-- 搜索结果内容 -->
                                    <div class="overflow-y-auto max-h-[calc(80vh-80px)]">
                                        <template v-if="searchKeyword.trim()">
                                            <div v-if="isLoading" class="py-12 text-center">
                                                <div
                                                    class="inline-block animate-spin rounded-full h-10 w-10 border-b-3 border-blue-600">
                                                </div>
                                                <p class="mt-4 text-gray-600 dark:text-gray-400">搜索中...</p>
                                            </div>
                                            <div v-else>
                                                <div v-if="searchResults.length > 0">
                                                    <div
                                                        class="px-6 py-4 text-sm text-gray-500 dark:text-gray-400 border-b border-gray-100 dark:border-gray-700">
                                                        找到 {{ totalResults }} 条相关结果</div>
                                                    <div class="divide-y divide-gray-100 dark:divide-gray-700">
                                                        <div v-for="article in searchResults" :key="article.id"
                                                            @click="handleResultClick(article.id)"
                                                            class="px-6 py-4 hover:bg-gray-50 dark:hover:bg-gray-700 cursor-pointer">
                                                            <div
                                                                class="font-semibold text-lg text-gray-900 dark:text-white mb-1"
                                                                v-html="highlightKeyword(article.title, searchKeyword)">
                                                            </div>
                                                            <div v-if="article.description"
                                                                class="text-gray-600 dark:text-gray-300 mb-2 line-clamp-2"
                                                                v-html="highlightKeyword(article.description, searchKeyword)">
                                                            </div>
                                                            <div
                                                                class="flex items-center text-sm text-gray-400 dark:text-gray-500">
                                                                <span>{{ article.createTime }}</span><span
                                                                    class="mx-2">•</span><span>阅读 {{ article.readNum ||
                                                                        0 }}</span>
                                                            </div>
                                                        </div>
                                                    </div>
                                                    <div v-if="totalResults > 5" @click="viewAllResults"
                                                        class="p-4 text-center text-blue-600 dark:text-blue-400 hover:bg-gray-50 dark:hover:bg-gray-700 cursor-pointer border-t border-gray-100 dark:border-gray-700">
                                                        <div class="font-medium">查看全部 {{ totalResults }} 条结果 →</div>
                                                        <div class="text-sm text-gray-500 dark:text-gray-400 mt-1">
                                                            点击进入搜索结果页面</div>
                                                    </div>
                                                </div>
                                                <div v-else class="py-16 text-center">
                                                    <svg class="w-20 h-20 mx-auto text-gray-300 dark:text-gray-600"
                                                        fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                                        <path stroke-linecap="round" stroke-linejoin="round"
                                                            stroke-width="1.5"
                                                            d="M9.172 16.172a4 4 0 015.656 0M9 10h.01M15 10h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                                                    </svg>
                                                    <h3 class="mt-6 text-xl font-medium text-gray-900 dark:text-white">
                                                        没有找到相关文章</h3>
                                                    <p class="mt-2 text-gray-600 dark:text-gray-400">尝试其他关键词</p>
                                                </div>
                                            </div>
                                        </template>
                                        <template v-else>
                                            <div class="py-16 text-center">
                                                <svg class="w-24 h-24 mx-auto text-gray-300 dark:text-gray-600"
                                                    fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                                    <path stroke-linecap="round" stroke-linejoin="round"
                                                        stroke-width="1.5"
                                                        d="m19 19-4-4m0-7A7 7 0 1 1 1 8a7 7 0 0 1 14 0Z" />
                                                </svg>
                                                <h3 class="mt-6 text-2xl font-medium text-gray-900 dark:text-white">搜索文章
                                                </h3>
                                                <p class="mt-4 text-gray-600 dark:text-gray-400 max-w-md mx-auto">
                                                    输入文章标题、描述关键词进行搜索<br>支持模糊搜索</p>
                                                <div class="mt-8 max-w-md mx-auto">
                                                    <div
                                                        class="text-left text-sm text-gray-500 dark:text-gray-400 mb-2">
                                                        热门搜索：</div>
                                                    <div class="flex flex-wrap gap-2 justify-center">
                                                        <span
                                                            v-for="tag in ['Spring Boot', 'Vue.js', 'Java', 'MySQL', 'Docker']"
                                                            :key="tag" @click="searchKeyword = tag"
                                                            class="px-4 py-2 bg-gray-100 dark:bg-gray-800 hover:bg-gray-200 dark:hover:bg-gray-700 rounded-lg cursor-pointer transition-colors">{{
                                                                tag }}</span>
                                                    </div>
                                                </div>
                                            </div>
                                        </template>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- 登录/用户菜单 -->
                    <span v-if="!isLogin"
                        class="text-gray-900 hover:text-blue-700 dark:text-gray-300 dark:hover:text-blue-400 cursor-pointer mr-3"
                        @click="$router.push('/login')">登录</span>

                    <button v-else type="button"
                        class="flex text-sm rounded-full md:mr-0 focus:ring-4 focus:ring-gray-300 dark:focus:ring-gray-600"
                        id="user-menu-button" aria-expanded="false" data-dropdown-toggle="user-dropdown"
                        data-dropdown-placement="bottom">
                        <span class="sr-only">Open user menu</span>
                        <img class="w-8 h-8 rounded-full" :src="$store.state.setting.avatar" alt="user photo">
                    </button>

                    <!-- 用户下拉菜单 -->
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
                                <a data-modal-target="logout-modal" data-modal-toggle="logout-modal"
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

                    <!-- 移动端菜单按钮 -->
                    <button data-collapse-toggle="navbar-search" type="button"
                        class="inline-flex items-center p-2 w-10 h-10 justify-center text-sm text-gray-500 rounded-lg md:hidden hover:bg-gray-100 focus:outline-none focus:ring-2 focus:ring-gray-200 dark:text-gray-400 dark:hover:bg-gray-700 dark:focus:ring-gray-600"
                        aria-controls="navbar-search" aria-expanded="false">
                        <span class="sr-only">Open main menu</span>
                        <svg class="w-5 h-5" aria-hidden="true" xmlns="http://www.w3.org/2000/svg" fill="none"
                            viewBox="0 0 17 14">
                            <path stroke="currentColor" stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                d="M1 1h15M1 7h15M1 13h15" />
                        </svg>
                    </button>
                </div>

                <!-- 导航链接 (桌面端) & 移动端搜索输入框容器 -->
                <div class="items-center justify-between hidden w-full md:flex md:w-auto md:order-1" id="navbar-search">
                    <ul
                        class="flex flex-col font-medium p-4 md:p-0 mt-4 border border-gray-100 rounded-lg bg-gray-50 md:flex-row md:space-x-8 md:mt-0 md:border-0 md:bg-white dark:bg-gray-800 md:dark:bg-gray-900 dark:border-gray-700">
                        <li>
                            <a @click="$router.push('/')"
                                :class="[currPath == '/' ? 'text-blue-700 dark:text-blue-500' : 'text-gray-900 dark:text-white']"
                                class="block py-2 pl-3 pr-4 rounded md:bg-transparent md:p-0" aria-current="page">首页</a>
                        </li>
                        <li>
                            <a @click="$router.push('/category')"
                                :class="[currPath == '/category' ? 'text-blue-700 dark:text-blue-500' : 'text-gray-900 dark:text-white']"
                                class="block py-2 pl-3 pr-4 rounded hover:bg-gray-100 md:hover:bg-transparent md:hover:text-blue-700 md:p-0 dark:hover:bg-gray-700 dark:hover:text-white md:dark:hover:text-blue-500">分类</a>
                        </li>
                        <li>
                            <a @click="$router.push('/tag')"
                                :class="[currPath == '/tag' ? 'text-blue-700 dark:text-blue-500' : 'text-gray-900 dark:text-white']"
                                class="block py-2 pl-3 pr-4 rounded hover:bg-gray-100 md:hover:bg-transparent md:hover:text-blue-700 md:p-0 dark:hover:bg-gray-700 dark:hover:text-white md:dark:hover:text-blue-500">标签</a>
                        </li>
                        <li>
                            <a @click="$router.push('/archive')"
                                :class="[currPath == '/archive' ? 'text-blue-700 dark:text-blue-500' : 'text-gray-900 dark:text-white']"
                                class="block py-2 pl-3 pr-4 rounded hover:bg-gray-100 md:hover:bg-transparent md:hover:text-blue-700 md:p-0 dark:hover:bg-gray-700 dark:hover:text-white md:dark:hover:text-blue-500">归档</a>
                        </li>
                        <li>
                            <a @click="$router.push('/ai')"
                                :class="[currPath == '/ai' ? 'text-blue-700 dark:text-blue-500' : 'text-gray-900 dark:text-white']"
                                class="block py-2 pl-3 pr-4 rounded hover:bg-gray-100 md:hover:bg-transparent md:hover:text-blue-700 md:p-0 dark:hover:bg-gray-700 dark:hover:text-white md:dark:hover:text-blue-500">小智</a>
                        </li>
                    </ul>
                </div>

                <!-- 移动端搜索面板 (展开后显示) -->
                <div id="mobile-search-panel" class="hidden md:hidden mt-4">
                    <!-- 这里只放移动端的搜索输入框，不放导航菜单，导航菜单在上面已经通过 collapse 控制了 -->
                    <div class="relative">
                        <div class="absolute inset-y-0 left-0 flex items-center pl-3 pointer-events-none">
                            <svg class="w-4 h-4 text-gray-500 dark:text-gray-400" aria-hidden="true"
                                xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 20 20">
                                <path stroke="currentColor" stroke-linecap="round" stroke-linejoin="round"
                                    stroke-width="2" d="m19 19-4-4m0-7A7 7 0 1 1 1 8a7 7 0 0 1 14 0Z" />
                            </svg>
                        </div>
                        <input type="text" v-model="searchKeyword" @click="handleSearchClick"
                            class="block w-full p-2 pl-10 text-sm text-gray-900 border border-gray-300 rounded-lg bg-gray-50 focus:ring-blue-500 focus:border-blue-500 dark:bg-gray-700 dark:border-gray-600 dark:placeholder-gray-400 dark:text-white"
                            placeholder="搜索文章...">
                    </div>
                    <!-- 移动端搜索结果全屏覆盖 -->
                    <div v-if="showSearchResults && searchKeyword.trim()"
                        class="fixed inset-0 z-50 bg-white dark:bg-gray-900 overflow-y-auto top-16">
                        <div class="p-4">
                            <div class="flex items-center mb-4">
                                <button @click="showSearchResults = false"
                                    class="mr-3 text-gray-500 dark:text-gray-400">
                                    <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                            d="M15 19l-7-7 7-7" />
                                    </svg>
                                </button>
                                <div class="flex-1 relative">
                                    <input type="text" v-model="searchKeyword" ref="searchInputRef"
                                        class="w-full p-3 pl-10 text-gray-900 bg-gray-100 dark:bg-gray-800 dark:text-white rounded-lg"
                                        placeholder="搜索文章..." autofocus>
                                    <div class="absolute left-3 top-3.5 text-gray-500 dark:text-gray-400">
                                        <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                                d="m19 19-4-4m0-7A7 7 0 1 1 1 8a7 7 0 0 1 14 0Z" />
                                        </svg>
                                    </div>
                                </div>
                            </div>

                            <!-- 复用搜索逻辑结果展示 -->
                            <div v-if="isLoading" class="py-8 text-center">
                                <div class="inline-block animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600">
                                </div>
                                <p class="mt-3 text-gray-500 dark:text-gray-400">搜索中...</p>
                            </div>
                            <div v-else-if="searchResults.length > 0"
                                class="divide-y divide-gray-100 dark:divide-gray-700">
                                <div v-for="article in searchResults" :key="article.id"
                                    @click="handleResultClick(article.id)" class="py-4">
                                    <div class="font-semibold text-gray-900 dark:text-white"
                                        v-html="highlightKeyword(article.title, searchKeyword)"></div>
                                    <div class="text-sm text-gray-500 dark:text-gray-400 mt-1">{{ article.createTime }}
                                        • {{
                                            article.readNum }} 阅读</div>
                                </div>
                            </div>
                            <div v-else-if="searchKeyword.trim()"
                                class="py-16 text-center text-gray-500 dark:text-gray-400">无结果</div>
                        </div>
                    </div>
                </div>

            </div>
        </nav>

        <!-- 退出登录 Modal -->
        <div id="logout-modal" tabindex="-1"
            class="fixed top-0 left-0 right-0 z-50 hidden p-4 overflow-x-hidden overflow-y-auto md:inset-0 h-[calc(100%-1rem)] max-h-full">
            <div class="relative w-full max-w-md max-h-full">
                <div class="relative bg-white rounded-lg shadow dark:bg-gray-700">
                    <button type="button"
                        class="absolute top-3 right-2.5 text-gray-400 bg-transparent hover:bg-gray-200 hover:text-gray-900 rounded-lg text-sm w-8 h-8 ml-auto inline-flex justify-center items-center dark:hover:bg-gray-600 dark:hover:text-white"
                        data-modal-hide="logout-modal">
                        <svg class="w-3 h-3" aria-hidden="true" xmlns="http://www.w3.org/2000/svg" fill="none"
                            viewBox="0 0 14 14">
                            <path stroke="currentColor" stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                d="m1 1 6 6m0 0 6 6M7 7l6-6M7 7l-6 6" />
                        </svg>
                        <span class="sr-only">Close modal</span>
                    </button>
                    <div class="p-6 text-center">
                        <svg class="mx-auto mb-4 text-gray-400 w-12 h-12 dark:text-gray-200" aria-hidden="true"
                            xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 20 20">
                            <path stroke="currentColor" stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                d="M10 11V6m0 8h.01M19 10a9 9 0 1 1-18 0 9 9 0 0 1 18 0Z" />
                        </svg>
                        <h3 class="mb-5 text-lg font-normal text-gray-500 dark:text-gray-400">是否退出登录?</h3>
                        <button @click="logout" data-modal-hide="logout-modal" type="button"
                            class="text-white bg-blue-700 hover:bg-blue-800 focus:ring-4 focus:ring-blue-300 dark:bg-blue-600 dark:hover:bg-blue-700 focus:outline-none dark:focus:ring-blue-800 font-medium rounded-lg text-sm inline-flex items-center px-5 py-2.5 text-center mr-2">确认</button>
                        <button data-modal-hide="logout-modal" type="button"
                            class="text-gray-500 bg-white hover:bg-gray-100 focus:ring-4 focus:outline-none focus:ring-gray-200 rounded-lg border border-gray-200 text-sm font-medium px-5 py-2.5 hover:text-gray-900 focus:z-10 dark:bg-gray-700 dark:text-gray-300 dark:border-gray-500 dark:hover:text-white dark:hover:bg-gray-600 dark:focus:ring-gray-600">取消</button>
                    </div>
                </div>
            </div>
        </div>
    </header>
</template>

<script setup>
import { useStore } from 'vuex'
import { useRoute, useRouter } from 'vue-router';
import { ref, watch, onMounted, nextTick } from 'vue'
import { showModel, showMessage } from '@/composables/util'
import { highlightKeyword } from '@/composables/util'
import { initDropdowns, initCollapses, initModals } from 'flowbite'
import { searchArticles } from '@/api/frontend/article'
import { debounce } from 'lodash-es'

// --- 暗黑模式逻辑 ---
const isDark = ref(false)

const toggleDarkMode = () => {
    isDark.value = !isDark.value
    if (isDark.value) {
        document.documentElement.classList.add('dark')
        localStorage.setItem('theme', 'dark')
    } else {
        document.documentElement.classList.remove('dark')
        localStorage.setItem('theme', 'light')
    }
}

// --- 初始化 ---
onMounted(() => {
    initCollapses()
    initDropdowns()
    initModals()

    // 读取主题偏好
    const theme = localStorage.getItem('theme')
    const systemPrefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches

    if (theme === 'dark' || (!theme && systemPrefersDark)) {
        isDark.value = true
        document.documentElement.classList.add('dark')
    }
})

// --- 业务逻辑 ---
const store = useStore()
const route = useRoute()
const router = useRouter()

const currPath = ref(route.path)

// 简单的登录状态检查
const keys = Object.keys(store.state.user || {})
const isLogin = ref(keys.length > 0)

// 搜索相关状态
const searchKeyword = ref('')
const searchResults = ref([])
const showSearchResults = ref(false)
const isLoading = ref(false)
const totalResults = ref(0)
const searchInputRef = ref(null)

// 防抖搜索
const performSearch = debounce(async (keyword) => {
    if (!keyword || keyword.trim().length === 0) {
        searchResults.value = []
        totalResults.value = 0
        return
    }

    try {
        isLoading.value = true
        // 假设您的 API 需要这些参数，根据实际情况调整
        const response = await searchArticles(keyword.trim(), 1, 5)
        if (response && response.data) {
            searchResults.value = response.data.records || []
            totalResults.value = response.data.total || 0
        }
    } catch (error) {
        console.error('搜索失败:', error)
        searchResults.value = []
        totalResults.value = 0
    } finally {
        isLoading.value = false
    }
}, 300)

watch(searchKeyword, (newVal) => {
    if (newVal && newVal.trim().length > 0) {
        performSearch(newVal)
        // 移动端如果没打开结果页，这里可以控制打开，视需求而定
        if (window.innerWidth < 768) {
            showSearchResults.value = true
        }
    } else {
        searchResults.value = []
        totalResults.value = 0
        // 如果清空了，可以考虑关闭结果页，或者保留空状态
        // showSearchResults.value = false 
    }
})

const handleSearchClick = () => {
    showSearchResults.value = true
    nextTick(() => {
        if (searchInputRef.value) searchInputRef.value.focus()
    })
}

const handleResultClick = (articleId) => {
    const id = Number(articleId)
    if (!id || isNaN(id)) return
    router.push(`/article/detail?articleId=${id}`)
    searchKeyword.value = ''
    showSearchResults.value = false
    searchResults.value = []
}

const viewAllResults = () => {
    if (searchKeyword.value.trim()) {
        router.push(`/search?q=${encodeURIComponent(searchKeyword.value.trim())}`)
        searchKeyword.value = ''
        showSearchResults.value = false
        searchResults.value = []
    }
}

const logout = () => {
    store.dispatch('logout')
    showMessage('退出登录成功', 'success')
    isLogin.value = false
    // 可选：刷新页面或跳转首页
    // router.push('/')
}
</script>

<style scoped>
/* 添加过渡效果让暗黑模式切换更丝滑 */
nav,
.bg-white,
.dark\:bg-gray-900,
.text-gray-900,
.dark\:text-white {
    transition-property: background-color, border-color, color, fill, stroke;
    transition-timing-function: cubic-bezier(0.4, 0, 0.2, 1);
    transition-duration: 300ms;
}
</style>