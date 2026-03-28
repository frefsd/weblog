import axios from "@/axios"

export function getArticleDetail(articleId) {
    return axios.post("/article/detail", { articleId })
}

export function searchArticles(keyword, current = 1, size = 10) {
    return axios.post("/article/search", {
        keyword,
        current,
        size
    })
}


