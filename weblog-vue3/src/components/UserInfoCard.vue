<template>
    <div class="mb-3 w-full bg-white border border-gray-200 rounded-lg dark:bg-gray-800 dark:border-gray-700">
        <div class="flex justify-end px-4 pt-4">
        </div>
        <div class="flex flex-col items-center pb-6">
            <img class="w-15 h-15 mb-3 rounded-full shadow-lg" :src="$store.state.setting.avatar" alt="Bonnie image" />
            <h5 class="mb-1 text-xl font-medium text-gray-900 dark:text-white">
                {{ $store.state.setting.author }}
            </h5>
            <span class="text-sm text-gray-500 dark:text-gray-400">
                {{ $store.state.setting.introduction }}
            </span>
            <div class="flex mt-4 space-x-3 md:mt-5">
                <span v-if="$store.state.setting.githubHome">
                    <a data-tooltip-target="github-tooltip-bottom" data-tooltip-placement="bottom"
                        :href="$store.state.setting.githubHome" target="_blank">
                        <svg t="1682482283799" class="icon" viewBox="0 0 1024 1024" version="1.1"
                            xmlns="http://www.w3.org/2000/svg" p-id="8307" width="28" height="28">
                            <path d="M512 512m-512 0a512 512 0 1 0 1024 0 512 512 0 1 0-1024 0Z" fill="#4186F5"
                                p-id="8308">
                            </path>
                            <path
                                d="M611.944 302.056c0-15.701 2.75-30.802 7.816-44.917a384.238 384.238 0 0 0-186.11 2.956c-74.501-50.063-93.407-71.902-107.975-39.618a136.243 136.243 0 0 0-3.961 102.287 149.515 149.515 0 0 0-39.949 104.806c0 148.743 92.139 181.875 179.961 191.61a83.898 83.898 0 0 0-25.192 51.863c-40.708 22.518-91.94 8.261-115.181-32.058a83.117 83.117 0 0 0-60.466-39.98s-38.871-0.361-2.879 23.408a102.97 102.97 0 0 1 43.912 56.906s23.398 75.279 133.531 51.863v65.913c0 10.443 13.548 42.63 102.328 42.63 71.275 0 94.913-30.385 94.913-42.987V690.485a90.052 90.052 0 0 0-26.996-72.03c83.996-9.381 173.328-40.204 179.6-176.098a164.706 164.706 0 0 1-21.129 1.365c-84.07 0-152.223-63.426-152.223-141.666z"
                                fill="#FFFFFF" p-id="8309"></path>
                            <path
                                d="M743.554 322.765a136.267 136.267 0 0 0-3.961-102.289s-32.396-10.445-107.979 39.618a385.536 385.536 0 0 0-11.853-2.956 132.623 132.623 0 0 0-7.816 44.917c0 78.24 68.152 141.667 152.222 141.667 7.171 0 14.222-0.472 21.129-1.365 0.231-5.03 0.363-10.187 0.363-15.509a149.534 149.534 0 0 0-42.105-104.083z"
                                fill="#FFFFFF" opacity=".4" p-id="8310"></path>
                        </svg>
                    </a>
                    <div id="github-tooltip-bottom" role="tooltip"
                        class="absolute z-10 invisible inline-block px-3 py-2 text-sm font-medium text-white bg-gray-900 rounded-lg shadow-sm opacity-0 tooltip dark:bg-gray-700">
                        我的 GitHub
                        <div class="tooltip-arrow" data-popper-arrow></div>
                    </div>
                </span>
                <span v-if="$store.state.setting.giteeHome">
                    <a :href="$store.state.setting.giteeHome" data-tooltip-target="gitee-tooltip-bottom"
                        data-tooltip-placement="bottom" target="_blank">
                        <svg t="1682483184788" class="icon" viewBox="0 0 1024 1024" version="1.1"
                            xmlns="http://www.w3.org/2000/svg" p-id="8999" width="28" height="28">
                            <path
                                d="M512 1024C229.222 1024 0 794.778 0 512S229.222 0 512 0s512 229.222 512 512-229.222 512-512 512z m259.149-568.883h-290.74a25.293 25.293 0 0 0-25.292 25.293l-0.026 63.206c0 13.952 11.315 25.293 25.267 25.293h177.024c13.978 0 25.293 11.315 25.293 25.267v12.646a75.853 75.853 0 0 1-75.853 75.853h-240.23a25.293 25.293 0 0 1-25.267-25.293V417.203a75.853 75.853 0 0 1 75.827-75.853h353.946a25.293 25.293 0 0 0 25.267-25.292l0.077-63.207a25.293 25.293 0 0 0-25.268-25.293H417.152a189.62 189.62 0 0 0-189.62 189.645V771.15c0 13.977 11.316 25.293 25.294 25.293h372.94a170.65 170.65 0 0 0 170.65-170.65V480.384a25.293 25.293 0 0 0-25.293-25.267z"
                                fill="#C71D23" p-id="9000"></path>
                        </svg>
                    </a>
                    <div id="gitee-tooltip-bottom" role="tooltip"
                        class="absolute z-10 invisible inline-block px-3 py-2 text-sm font-medium text-white bg-gray-900 rounded-lg shadow-sm opacity-0 tooltip dark:bg-gray-700">
                        我的 Gitee
                        <div class="tooltip-arrow" data-popper-arrow></div>
                    </div>
                </span>
            </div>
        </div>
    </div>
</template>

<script setup>
import { useStore } from 'vuex'
import { initTooltips } from 'flowbite'
import { onMounted } from 'vue'

// initialize components based on data attribute selectors
onMounted(() => {
    initTooltips();
})

const store = useStore()

</script>