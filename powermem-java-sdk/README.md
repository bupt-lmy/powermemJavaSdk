# powermem-java-sdk

Java SDK for PowerMem.

## Quickstart (当前最小可用闭环)

本仓库当前已打通一个**纯 JDK11、无三方依赖**的最小闭环：

- `core/Memory`：支持 `add/search/update/delete/getAll/deleteAll`
- `storage/sqlite/SQLiteVectorStore`：暂时用**内存实现**（后续再替换为真实 SQLite/JDBC）
- `integrations/embeddings/MockEmbedder`：用于本地演示（真实 OpenAI/Qwen 仍待实现）
- `intelligence/EbbinghausAlgorithm`：在 search 结果上做可选的遗忘曲线衰减后处理

运行示例：

```bash
cd powermem-java-sdk
mvn -q package
mvn -q exec:java -Dexec.mainClass=com.powermem.sdk.Main
# 或者使用打包出来的 fat-jar（包含 sqlite-jdbc 等依赖）：
java -jar target/powermem-java-sdk-1.0-SNAPSHOT-all.jar
```

