# 数据库 SQL 与中文备注

数据库名保留为 **`suixinggou`**，用途为随行带货交易撮合平台业务库。MySQL 不支持数据库级 `COMMENT`，因此库的用途在 SQL 文件头和数据字典中说明；表、字段使用可落库的原生 `COMMENT`。

| 文件 | 包含范围 | 使用场景 |
| --- | --- | --- |
| [完整结构中文备注版](deploy/kuaixieTech_full_schema_commented.sql) | 原有82表＋提议新增31表，共113表/1186字段；包含索引、外键、触发器及011状态扩宽/012默认地址触发器修复，不含种子或业务数据 | 新空库结构审阅、初始化；保留原始触发器定义，不代表已经通过真实MySQL验证 |
| [增量升级版](deploy/kuaixieTech_incremental.sql) | 12份迁移，新增31表及订单状态扩宽；新增表/字段均含中文COMMENT，不重建原有82表 | 已有业务库最终部署升级；执行前比对实际结构，开发期间未执行 |
| [原有82表中文备注副本](reference/current-schema.annotated.sql) | 原始82表结构及完整中文备注；仍含原始种子和触发器 | 对照、审阅旧结构；不要向已有库重复导入 |
| [中文数据字典](DATA_DICTIONARY.md) | 数据库用途、全部113表和1186字段的类型、默认值、约束与中文含义 | 开发、运营和部署验收查阅 |
| [用户原始 SQL](reference/current-schema.sql) | 用户提供的原始结构和内容，保持原样 | 来源追溯与结构对比 |

**文件补齐备注不等于线上已经更新备注。** 增量版的 `CREATE TABLE IF NOT EXISTS` 不会重写已存在表的结构或备注，也不会给原有82表统一执行 `MODIFY COLUMN`。若要更新真实旧库的字段备注，应以其实际 `SHOW CREATE TABLE` 为准生成专用迁移，保留真实类型、默认值、生成表达式、字符集和约束；本次没有使用旧快照覆盖线上属性。

完整结构版有空库检查，已有表时通过错误阻止默认客户端继续导入；不要使用忽略错误的 `--force`。它不是包含业务数据的备份，不代替数据库恢复备份，也不是新库投产所需的完整业务种子配置。

## 维护与验证

中文语义源为 [schema-comments.json](schema-comments.json)，不是自动翻译字段名。修改后在仓库根目录运行：

```bash
python3 server/tools/schema_comments.py --write
python3 server/tools/schema_comments.py --check
python3 server/tools/build_deployment_sql.py --check
python3 -m unittest discover -s server/tools -p 'test_*.py'
```

这些命令只读写本地文件，不连接数据库。此次没有增减表或字段，没有改动业务金额、字段类型或业务逻辑；既有011中的状态扩宽继续保留到最终部署。全部字段仍待真实环境完整验收。

012是在隔离MySQL中实证1442后补充的验证型触发器修复，不修改历史地址记录，不增表/字段。仅隔离库已应用；业务库仍留到最终部署。实际启动证据见 [运行记录](../../docs/audit/RUNTIME_VALIDATION.md)。
