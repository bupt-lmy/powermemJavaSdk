# powermem 转 Java SDK 实现方案

| 文档版本 | 修改日期 | 状态 | 作者 |
| :--- | :--- | :--- | :--- |
| v1.0 | 2026-01-16 | 草稿 | 架构组 |

## 1. 需求背景

### 1.1 需求来源
具体见 https://github.com/oceanbase/powermem/issues/142，基于 powermem Python SDK，在不依赖 Python 运行时和 HTTP 包装器的前提下，以原生 Java SDK 方式接入智能记忆能力，面向 Spring Boot / Jakarta EE 等框架提供类型安全与性能一致的体验，并与 Maven/Gradle 的构建体系深度集成。

### 1.2 功能边界
本方案聚焦“Java SDK 的核心能力与调用方式”，边界如下：
- **包含**：同步/异步 Memory API、配置管理、向量存储接入、图存储接入、基于艾宾浩斯遗忘曲线的记忆演化机制、用户与智能体记忆管理、用户记忆画像管理、多智能体支持。
- **不包含**：服务端部署、MCP Server 的实现与运维、前端/控制台、可视化工具。
- **依赖**：外部 LLM 与 Embedding 供应商、向量数据库/关系库，SDK 不替代其部署与运维。
- **质量要求**：Java 11+、线程安全、异常处理机制、JUnit5 测试、JavaDoc、示例工程、Maven Central 发布。

---

## 2. 技术架构与选型

### 2.1 技术栈规范
* **JDK 版本**：Java 11.0.19 。
* **构建工具**：Maven 3.9.9。
* **核心依赖**：
    * `OkHttp 4.x`: 高性能 HTTP 客户端。
    * `Jackson 2.x`: JSON 序列化标准库。
    * `SLF4J`: 日志门面。
    * `Lombok`: 消除样板代码 (Scope: Provided)。
    * `JUnit 5`: 单元测试框架。

### 2.2 Maven 坐标规划
采用单模块发布策略，降低接入复杂度。
* **GroupId**: `com.oceanbase.powermem.sdk`
* **ArtifactId**: `powermem-java-sdk`
* **Base Package**: `com.oceanbase.powermem.sdk`

### 2.3 异常体系设计
所有 SDK 抛出的异常均继承自 `PowermemException` (RuntimeException)，实现统一错误处理。

* `PowermemException` (根异常)
    * `ApiException`: API 层请求或响应异常。
    * `HttpException`: 传输层异常（超时、不可达）。
    * `SerializationException`: JSON 解析/序列化失败。

---

## 3. 数据模型与配置体系

### 3.1 配置类设计 (`com.powermem.sdk.config`)
配置对象采用 POJO + Builder 模式，支持从 `.env`、环境变量、代码构建三种方式加载。

* **`MemoryConfig`** (聚合根)
    * `vectorStore`: `VectorStoreConfig` (provider / host / database / collection / dims)
    * `llm`: `LlmConfig` (provider / apiKey / model / baseUrl / temperature)
    * `embedder`: `EmbedderConfig` (provider / apiKey / model / embeddingDims)
    * `intelligentMemory`: `IntelligentMemoryConfig` (启用标志, 衰减系数, 阈值)
    * `agentMemory`: `AgentMemoryConfig` (多智能体默认 scope 与权限)
    * `telemetry` / `audit` / `logging`
    * `reranker` / `graphStore`
    * `customFactExtractionPrompt` / `customImportanceEvaluationPrompt`

### 3.2 存储模型规范
SDK 需保证与 Python 版本的数据表结构兼容。

#### 3.2.1 向量存储表 (Generic Schema)
适用于 SQLite, 。

| 字段名 | 类型 | 说明 |
| :--- | :--- | :--- |
| `id` | Varchar(64) | **主键**，统一使用 Snowflake 算法生成，确保跨语言唯一性。 |
| `vector` | Text/Vector | 向量数据 (若 DB 支持向量类型则使用原生类型，否则存 JSON String)。 |
| `payload` | JSON/Text | 核心元数据 (content, scope, user_id, timestamp)。 |
| `created_at` | Timestamp | 创建时间。 |

#### 3.2.2 审计历史表 (`history`)
用于记录记忆的生命周期变更（ADD/UPDATE/DELETE），对齐 Python 结构：

| 字段名 | 类型 | 说明 |
| :--- | :--- | :--- |
| `id` | Varchar(64) | 审计记录主键。 |
| `memory_id` | Varchar(64) | 关联的记忆 ID。 |
| `old_memory` | Text | 变更前内容。 |
| `new_memory` | Text | 变更后内容。 |
| `event` | Varchar(10) | 事件类型: `ADD`, `UPDATE`, `DELETE`。 |
| `created_at` | Datetime | 创建时间。 |
| `updated_at` | Datetime | 更新时间。 |
| `is_deleted` | Integer | 是否删除标记。 |
| `actor_id` | Varchar(64) | 操作人/代理标识。 |
| `role` | Varchar(32) | 角色标识。 |

---

## 4. 核心功能详细设计

### 4.1 记忆添加管道 (Pipeline)
`add()` 方法的内部执行流程如下，旨在确保数据的一致性与记忆的去重：

1.  **Input Normalization (输入标准化)**: 接收 `messages` (String 或 List<Map>)，转换为标准文本格式。
2.  **Fact Extraction (事实抽取)**: 
    * 加载内置的 `FACT_RETRIEVAL_PROMPT`。
    * 调用 `LlmProvider` 将非结构化文本提取为独立的事实列表。
3.  **Vectorization (向量化)**: 调用 `EmbeddingProvider` 将提取的事实转换为向量 (Float List)。
4.  **Similarity Search (相似检索)**: 在 `VectorStore` 中检索 Top-K 相似记忆，用于上下文比对。
5.  **Update Decision (更新决策)**: 
    * 加载 `DEFAULT_UPDATE_MEMORY_PROMPT`。
    * 将 *新事实* 与 *检索到的旧记忆* 组装，调用 LLM 判断更新策略，结果包含：`ADD` (新增), `UPDATE` (更新旧记忆), `DELETE` (删除无效记忆), `NONE` (无变更)。
6.  **Persistence (持久化)**: 
    * 执行数据库写操作 (Insert/Update/Delete)。
    * 同步写入 `history` 表以留存审计记录。

### 4.2 智能特性：遗忘曲线算法
在 Java 侧复现 Ebbinghaus 遗忘曲线逻辑，用于模拟人类记忆的衰减过程。

* **逻辑位置**: `search()` 方法的后处理 (Post-processing) 阶段。
* **计算公式**: 
    $$S = S_{base} \times e^{-\frac{\Delta t}{Retention}}$$
    * $S$: 最终得分 (Recall Strength)。
    * $S_{base}$: 原始向量相似度 (Similarity Score)。
    * $\Delta t$: 当前时间 - 记忆最后访问时间 (单位：小时)。
    * $Retention$: 记忆保持率系数 (可在 `IntelligentMemoryConfig` 中配置)。
* **实现细节**: 使用 `java.time.Duration` 计算时间差，结合 `Math.exp()` 计算最终权重，重新排序检索结果。

### 4.3 异步与并发模型
* **异步接口**: `AsyncMemory` 接口全线基于 `CompletableFuture` 实现，支持链式调用。
* **线程策略**: SDK 内部维护独立的 IO 密集型线程池 (CachedThreadPool 或 FixedThreadPool)，避免阻塞调用方的 `ForkJoinPool.commonPool()` 或主线程。
* **并发安全**: `VectorStore` 的具体实现类需保证线程安全（建议使用数据库连接池或并发容器 `ConcurrentHashMap`）。

---

## 5. 项目结构与模块划分

| 包路径 (`com.powermem.sdk.*`) | 职责描述 |
| :--- | :--- |
| `.core` | **核心引擎**：包含 `Memory` 和 `AsyncMemory` 的主编排逻辑。 |
| `.config` | **配置管理**：配置实体类定义及 `ConfigLoader` 实现。 |
| `.api` | **接口定义**：`VectorStore`, `LlmProvider`, `EmbeddingProvider` 等顶层接口。 |
| `.storage` | **存储适配**：`SqliteStore`, `OceanBaseStore`, `PgVectorStore` 的具体实现。 |
| `.integrations` | **模型集成**：OpenAI, Qwen 等 LLM/Embedding 服务的 HTTP 调用封装。 |
| `.prompts` | **提示词管理**：内置 Prompt 模板及变量填充工具类。 |
| `.model` | **数据模型**：DTO, POJO, Request/Response 对象。 |
| `.util` | **工具类**：Snowflake ID 生成器, JSON 处理, 向量计算工具 (Cosine Similarity)。 |
| `.exception` | **异常体系**：自定义异常类定义。 |

---

## 6. 实施路线图

| 阶段 | 任务名称 | 交付内容 |
| :--- | :--- | :--- |
| **P1** | **骨架搭建** | 项目初始化、Maven 配置、异常体系定义、配置加载模块。 |
| **P2** | **基础链路** | 实现 `LlmProvider` (OpenAI/Qwen) 和 `EmbeddingProvider`，完成 HTTP 调用链路。 |
| **P3** | **存储适配** | 定义 `VectorStore` 接口，优先实现`SqliteStore`。 |
| **P4** | **核心逻辑** | 移植 Python 的 Prompt 模板，实现事实抽取与更新决策的核心业务逻辑，提供同步下的记忆接口。 |
| **P5** | **生产增强** | 实现 `OceanBaseStore`存储适配、遗忘曲线算法、`AsyncMemory` 异步封装、用户画像、多用户、多智能体记忆管理。 |
| **P6** | **发布交付** | 完善 JavaDoc、编写 Sample App、Maven Central 发布流程。 |

---

## 7. 使用示例

### 7.1 初始化配置
```java
// 使用 Builder 构建配置
MemoryConfig config = MemoryConfig.builder()
    .vectorStore(VectorStoreConfig.sqlite("./data/powermem_dev.db"))
    .llm(LlmConfig.qwen("sk-xxxxxxxx", "qwen-plus"))
    .embedder(EmbedderConfig.qwen("sk-xxxxxxxx", "text-embedding-v4", 1536))
    .build();

// 创建 Memory 实例
Memory memory = new Memory(config);
```

### 7.2 使用流程示例
```java
Memory memory = new Memory(config);
memory.add("用户喜欢简洁的中文回答", "user123");
List<MemoryResult> results = memory.search("用户偏好", "user123");
```

### 7.3 异步使用示例
```java
AsyncMemory asyncMemory = new AsyncMemory(config);
CompletableFuture<SearchMemoriesResponse> future =
    CompletableFuture.supplyAsync(() -> asyncMemory.search(new SearchMemoriesRequest()));
future.thenAccept(result -> {
    // 处理异步结果
}).exceptionally(ex -> {
    // 异常处理
    return null;
});
```
