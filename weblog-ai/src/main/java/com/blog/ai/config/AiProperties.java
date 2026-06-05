package com.blog.ai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * AI 智能问答模块的配置属性绑定类。
 *
 * <p>所有以 {@code ai.} 开头的配置项都会映射到此类的字段中。
 * 支持在 {@code application.yaml}、{@code application-local.yaml}、
 * {@code application-druid.yaml} 中配置。</p>
 *
 * <h3>配置示例</h3>
 * <pre>{@code
 * ai:
 *   zhipu:
 *     api-key: 你的智谱API密钥
 *     model-name: glm-4-flash
 *   chat-memory:
 *     max-rounds: 5
 *   rag:
 *     top-k: 3
 *   api:
 *     timeout-seconds: 30
 *   embedding:
 *     chunk-size: 500
 *     chunk-overlap: 50
 * }</pre>
 */
@Data
@ConfigurationProperties(prefix = "ai")
public class AiProperties {

    /** 智谱 AI 相关配置（对话模型 + Embedding 模型） */
    private Zhipu zhipu = new Zhipu();

    /** 聊天记忆配置（历史对话轮数等） */
    private ChatMemory chatMemory = new ChatMemory();

    /** RAG 检索配置（向量搜索相关参数） */
    private Rag rag = new Rag();

    /** API 调用配置（超时等） */
    private Api api = new Api();

    /** 文本分块配置（向量化前的文本切分参数） */
    private Embedding embedding = new Embedding();

    // ==================== 嵌套配置类 ====================

    /**
     * 智谱 AI 调用配置。
     *
     * <p>使用 OpenAI 兼容接口调用智谱 AI 的 GLM 系列模型。
     * API 文档：https://open.bigmodel.cn/dev/api</p>
     *
     * <p><b>配置前缀：</b>{@code ai.zhipu.*}</p>
     */
    @Data
    public static class Zhipu {
        /**
         * 智谱 AI API 密钥。
         * <p>从智谱AI开放平台获取：https://open.bigmodel.cn/usercenter/apikeys</p>
         */
        private String apiKey;

        /**
         * 对话模型名称。
         * <p>默认使用 GLM-4-Flash（免费模型，速率限制较宽松）。</p>
         * <p>可选值参考：https://open.bigmodel.cn/dev/api#language-model</p>
         */
        private String modelName = "glm-4-flash";

        /**
         * Embedding 模型名称。
         * <p>用于将文本转为向量进行语义检索。</p>
         */
        private String embeddingModel = "embedding-3";

        /**
         * API 请求的基础地址。
         * <p>智谱AI 的 OpenAI 兼容接口入口，一般无需修改。</p>
         */
        private String baseUrl = "https://open.bigmodel.cn/api/paas/v4";
    }

    /**
     * 聊天记忆配置。
     *
     * <p>控制每次对话时携带多少轮历史记录。
     * 历史记录存储在 MySQL {@code ai_chat_memory} 表中。</p>
     *
     * <p><b>配置前缀：</b>{@code ai.chat-memory.*}</p>
     */
    @Data
    public static class ChatMemory {
        /**
         * 每次对话携带的历史轮数（用户+AI 算一轮）。
         * <p>例如 5 表示携带最近 5 条用户问题和 5 条 AI 回复。
         * 值越大 AI 能记得更多上下文，但消耗更多 Token。</p>
         */
        private int maxRounds = 5;

        /**
         * 会话过期时间（秒）。
         * <p>超过此时间未活跃的会话将被视为过期，
         * 前端在加载聊天页面时校验到过期后会清空聊天记录并生成新会话。</p>
         */
        private int sessionTimeoutSeconds = 86400; // 默认 24 小时
    }

    /**
     * RAG 检索配置。
     *
     * <p>RAG（Retrieval-Augmented Generation）在每次对话前
     * 从向量库中检索与用户问题最相关的博客文章片段，作为上下文提供给 AI。</p>
     *
     * <p><b>配置前缀：</b>{@code ai.rag.*}</p>
     */
    @Data
    public static class Rag {
        /**
         * 向量检索返回的最相似文本块数量（Top-K）。
         * <p>例如 3 表示检索出最相关的 3 个文本块拼接为上下文。
         * 值越大 AI 能看到的参考信息越多，但可能会引入不相关的噪音。</p>
         */
        private int topK = 3;
    }

    /**
     * API 调用配置。
     *
     * <p>控制调用智谱 AI 接口的 HTTP 超时时间。</p>
     *
     * <p><b>配置前缀：</b>{@code ai.api.*}</p>
     */
    @Data
    public static class Api {
        /**
         * 调用智谱 AI API 的超时时间（秒）。
         * <p>超过此时间未响应则返回超时错误。
         * 注意流式模式下此超时控制的是整个流式连接的总时长。</p>
         */
        private int timeoutSeconds = 30;
    }

    /**
     * 文本分块配置。
     *
     * <p>将博客文章正文切分为适合向量化的文本块。
     * 分块策略：先按段落拆分，超长段落再按句子拆分。</p>
     *
     * <p><b>配置前缀：</b>{@code ai.embedding.*}</p>
     */
    @Data
    public static class Embedding {
        /**
         * 文本块的最大字符数。
         * <p>超过此长度的段落会进一步按句子拆分。
         * 不宜过大（否则向量化精度下降），也不宜过小（否则语义不完整）。</p>
         */
        private int chunkSize = 500;

        /**
         * 相邻文本块之间的重叠字符数。
         * <p>重叠是为了避免在分块边界处切断完整的语义。
         * 例如块1 取 1-500 字符，块2 取 450-950 字符，重叠 50 字符。</p>
         */
        private int chunkOverlap = 50;
    }
}
