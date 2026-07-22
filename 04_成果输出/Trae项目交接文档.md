# Trae 项目交接文档：校园活动预约与核销平台

生成日期：2026-07-21 | 最后更新：2026-07-22

## 1. 当前背景

用户是一名计算机专业准大四学生，Java 基础薄弱，目标是在秋招或实习前尽快完成一个可以写进简历的 Java 后端项目。

原本学习路线是 Java 基础、SQL、JDBC、Maven、Spring Boot 逐步推进。由于时间和 Token 限制，现在策略调整为：

1. 项目完成优先。
2. SQL、JDBC、Maven、Spring Boot 不再做大量分散练习。
3. 每个技术板块只保留项目必需知识点和一个综合练习。
4. 最终项目从“账户管理系统”切换为“校园活动预约与核销平台”。

项目目录：

```text
C:\Users\孙一凡\Desktop\WorkSpace\project003校园活动预约与核销平台
```

需求摘要已归档到：

```text
C:\Users\孙一凡\Desktop\WorkSpace\project003校园活动预约与核销平台\01_需求与目标\校园活动预约与核销平台-项目设计沟通摘要.md
```

## 2. 最终简历项目定位

项目名称建议：

```text
校园活动预约与核销平台
```

简历定位：

```text
基于 Spring Boot + MySQL + Redis 的校园活动预约后端系统
```

该项目用于证明 Java 后端工程能力，不作为主打 AI 项目。后续可扩展为校园 AI 活动推荐、RAG 活动问答等，但第一轮 MVP 不强行加入 AI 功能。

## 3. 已完成内容

### Java 基础阶段

已完成：

1. Java 基础语法。
2. if / for / while。
3. 方法、返回值、boolean。
4. 数组。
5. String。
6. 类和对象。
7. 构造方法、封装、getter/setter。
8. ArrayList。
9. HashMap。
10. Scanner。
11. 异常基础。
12. 文件 IO。
13. 控制台账户管理系统 v1。

### MySQL 环境

已完成：

1. MySQL Server 8.4.10 已安装。
2. `mysql --version` 可用。
3. 能登录 MySQL。
4. `SELECT VERSION();` 返回 `8.4.10`。

### SQL 项目建模练习

已在 MySQL 中创建数据库：

```sql
CREATE DATABASE IF NOT EXISTS campus_activity;
```

已创建 4 张核心表：

```text
app_user
activity_category
activity
activity_reservation
```

已验证：

1. 插入用户、活动分类、活动数据。
2. 查询活动。
3. 创建预约记录。
4. 扣减活动名额。
5. `uk_user_activity(user_id, activity_id)` 唯一索引可以防止一人重复预约同一活动。
6. 使用 `WHERE id = ? AND available_quota > 0` 可以防止库存扣成负数。
7. 取消预约后，预约状态改为 `CANCELED`，活动名额返还。

关键 SQL：

```sql
INSERT INTO activity_reservation (user_id, activity_id, reservation_code, status)
VALUES (?, ?, ?, 'RESERVED');
```

```sql
UPDATE activity
SET available_quota = available_quota - 1
WHERE id = ? AND available_quota > 0;
```

```sql
UPDATE activity_reservation
SET status = 'CANCELED', canceled_at = NOW()
WHERE id = ? AND status = 'RESERVED';
```

## 4. 新学习策略

后续不要按传统课程铺开讲太多细节，应按以下原则推进：

1. 每次只推进一个可验证阶段。
2. 每个技术板块先讲项目必须用到的知识点。
3. 每个板块只做一个大练习。
4. 不写和最终项目无关的小玩具功能。
5. 不提前堆复杂架构。
6. 每一步都要能运行、能验证、能截图或记录输出。

## 5. 后续技术板块压缩方案

### 5.1 SQL

只保留项目必需内容：

1. 建库建表。
2. 主键、自增。
3. `NOT NULL`。
4. `UNIQUE`。
5. `INSERT / SELECT / UPDATE / DELETE`。
6. `WHERE / ORDER BY / LIMIT`。
7. 一对多关系。
8. 唯一索引防重复预约。
9. 条件更新防超卖。

SQL 总练习：

```text
用 SQL 完成校园活动预约平台核心表设计，并手动跑通：插入用户、插入活动、预约活动、扣减库存、防重复预约、防超卖、取消预约。
```

当前 SQL 总练习已基本完成。

### 5.2 JDBC

只保留项目必需内容：

1. Java 连接 MySQL。
2. `Connection`。
3. `PreparedStatement`。
4. `ResultSet`。
5. SQL 注入风险。
6. DAO 的基本概念。

JDBC 总练习：

```text
用 Java + JDBC 操作 campus_activity 数据库，完成查询活动、创建预约、扣减库存、查询我的预约。
```

注意：JDBC 练习只是为了理解 Java 如何操作数据库，最终项目建议直接使用 Spring Boot + MyBatis-Plus。

### 5.3 Maven

只保留项目必需内容：

1. Maven 标准目录。
2. `pom.xml`。
3. dependency。
4. Maven 下载依赖。
5. `mvn -v`。
6. `mvn spring-boot:run`。

Maven 总练习：

```text
创建标准 Maven 项目，导入 MySQL 驱动，跑通一次数据库连接。
```

重要风险：

之前检查过本机可能存在 `java` / `javac` 是 Java 25，但 Maven 使用 Java 8 的风险。正式进入 Spring Boot 3 前必须检查：

```powershell
java -version
javac -version
mvn -v
```

Spring Boot 3 要求 Maven 使用 JDK 17+。

### 5.4 Spring Boot

Spring Boot 是主战场，不要过度压缩，但必须围绕项目推进。

只讲项目必需内容：

1. Controller。
2. Service。
3. Mapper。
4. Entity。
5. DTO / VO。
6. 统一响应。
7. 统一异常。
8. 参数校验。
9. MySQL CRUD。
10. 登录和 JWT。
11. Redis 缓存活动详情。
12. Knife4j 接口文档。
13. RabbitMQ 消息队列。

Spring Boot 总练习：

```text
完成校园活动预约与核销平台 MVP。
```

## 6. 第一轮 MVP 范围

### 必须完成

1. 用户注册。
2. 用户登录。
3. JWT 鉴权。
4. 查询当前用户。
5. 活动分类查询。
6. 活动列表查询。
7. 活动详情查询。
8. 管理员创建活动。
9. 管理员修改活动。
10. 管理员上下架活动。
11. 用户预约活动。
12. 一人一单。
13. 库存防超卖。
14. 查询我的预约。
15. 取消预约。
16. 预约码生成。
17. 管理员根据预约码查询预约。
18. 管理员核销预约码。
19. 防止重复核销。
20. Redis 缓存活动详情。
21. Knife4j 接口文档。
22. README。

### 第一轮先不做

1. 优惠券。
2. 秒杀。
3. Redisson。
4. 前端页面。
5. 复杂 RBAC。
6. BitMap 签到。
7. GEO 附近活动。
8. 排行榜。

### 额外完成（超出第一轮计划）

- Docker 容器化部署（多阶段构建 + docker-compose 编排 MySQL + Redis + App）
- RabbitMQ 消息队列（预约成功异步通知 + 事务提交后发送 + 消费者写入通知记录）

**RabbitMQ 验证结果（2026-07-22）：**

完整链路已通过实际预约操作验证：

```
用户预约 → 事务提交 → afterCommit 发送消息 → Consumer 接收 → notification 表写入
```

- Erlang 26.2.5.3 安装于：`downloads\Erlang OTP\`，ERLANG_HOME 已设系统环境变量
- RabbitMQ 3.12.14 安装于：`C:\Program Files\RabbitMQ Server\rabbitmq_server-3.12.14\`
- RabbitMQ 以 Windows 服务运行，端口 5672
- JSON 消息转换器（Jackson2JsonMessageConverter）已配置，解决默认 Java 序列化兼容问题
- notification 表已验证有通知记录写入

## 7. 推荐开发阶段

### 阶段 0：项目初始化

目标：

```text
创建 Spring Boot 3 项目骨架，接入 MySQL、MyBatis-Plus、Knife4j。
```

验收标准：

1. 项目能启动。
2. `/api/health` 返回成功。
3. MySQL 连接正常。
4. Knife4j 页面能打开。
5. 运行命令：`$env:JAVA_HOME="C:\Program Files\Java\jdk-25"; java "-Djava.io.tmpdir=target/tmp" -jar target/campus-activity-1.0.0-SNAPSHOT.jar`

**状态：✅ 已完成（2026-07-21）**

### 阶段 1：数据库和实体

目标：

```text
把已验证的 SQL 表结构整理为 schema.sql，并创建对应 Entity。
```

验收标准：

1. `schema.sql` 可重复执行或有明确初始化方式。
2. Entity 字段和表字段对应。
3. Mapper 能查询到活动数据。

**状态：✅ 已完成（2026-07-21）**

### 阶段 2：用户模块

目标：

```text
完成注册、登录、JWT、查询当前用户。
```

验收标准：

1. 注册成功写入 `app_user`。
2. 手机号重复注册失败。
3. 登录成功返回 token。
4. 不带 token 访问 `/api/users/me` 失败。
5. 带 token 访问 `/api/users/me` 成功。

**状态：✅ 已完成（2026-07-21）**

### 阶段 3：活动模块

目标：

```text
完成活动分类、活动列表、活动详情、管理员活动管理。
```

验收标准：

1. 能查活动分类。
2. 能分页查活动列表。
3. 能查活动详情。
4. 管理员能创建活动。
5. 活动上下架后，用户端展示和预约逻辑受状态控制。

**状态：✅ 已完成（2026-07-21）**

### 阶段 4：预约模块

目标：

```text
完成预约、取消预约、一人一单、库存防超卖、查询我的预约。
```

验收标准：

1. 用户预约成功后生成预约记录。
2. 预约成功后 `available_quota` 减 1。
3. 同一用户重复预约同一活动失败。
4. 库存为 0 时预约失败。
5. 取消预约后状态改为 `CANCELED`，名额加回。

**状态：✅ 已完成（2026-07-21）**

核心实现建议：

1. 先执行条件扣减库存：

```sql
UPDATE activity
SET available_quota = available_quota - 1
WHERE id = ? AND available_quota > 0;
```

2. 判断影响行数。
3. 影响 0 行则返回库存不足。
4. 影响 1 行后再插入预约记录。
5. 插入预约记录时依赖唯一索引兜底防重复。
6. 整个预约方法必须加事务。

### 阶段 5：核销模块

目标：

```text
管理员根据预约码查询并核销预约。
```

验收标准：

1. 有效预约码可以查到预约。
2. 错误预约码返回失败。
3. `RESERVED` 状态可以核销。
4. `CANCELED` 或 `VERIFIED` 状态不能核销。
5. 核销成功后预约状态变为 `VERIFIED`。
6. 写入 `verification_record`。

**状态：✅ 已完成（2026-07-21）**

### 阶段 6：Redis 缓存

目标：

```text
活动详情接入 Redis 缓存。
```

验收标准：

1. 第一次查活动详情：查 MySQL，再写 Redis。
2. 第二次查同一活动详情：命中 Redis。
3. 查不存在活动：写入空值缓存，防止缓存穿透。
4. 正常缓存 TTL 增加随机秒数。

**状态：✅ 已完成（2026-07-21）**

**实际实现：** 使用 `RedisTemplate<String, Object>` + `GenericJackson2JsonRedisSerializer`，TTL = 30 分钟基础 + 随机 0~5 分钟抖动，空值缓存 5 分钟。管理员修改活动时自动清除缓存。

Redis Key：
```text
campus:activity:{id}
campus:activity:null:{id}
```

### 阶段 7：README 和简历证据

目标：

```text
整理成可以展示的项目。
```

验收标准：

1. README 包含项目介绍。
2. README 包含技术栈。
3. README 包含功能列表。
4. README 包含数据库表结构。
5. README 包含接口列表。
6. README 包含本地运行步骤。
7. 保留接口测试截图或测试记录。

**状态：✅ 已完成（2026-07-21）**

README 已包含：项目介绍、技术栈、功能列表、项目结构、5 张数据库表设计、16 个 API 接口清单、Docker / 本地两种运行方式、6 项核心实现亮点。

### 额外阶段：Docker 容器化部署

目标：

```text
使用 Docker Compose 编排 MySQL + Redis + 应用服务，实现一键部署。
```

验收标准：

1. Dockerfile 多阶段构建（Maven 编译 → JRE 运行镜像）。
2. docker-compose.yml 编排 MySQL 8.0 + Redis 7 + App。
3. 健康检查控制服务启动顺序。
4. Schema.sql 自动初始化数据库。
5. application-docker.yml profile 隔离 Docker 环境配置。

**状态：✅ 已完成（2026-07-21）**

新增文件：
- `Dockerfile`：多阶段构建，最终镜像基于 eclipse-temurin:17-jre-alpine
- `docker-compose.yml`：MySQL (3306) + Redis (6379) + App (8080)
- `.dockerignore`：排除 target、md、日志等
- `src/main/resources/application-docker.yml`：Docker 环境专用配置

## 8. 推荐接口清单

### 用户接口

```text
POST /api/auth/register
POST /api/auth/login
GET  /api/users/me
```

### 活动接口

```text
GET  /api/activity-categories
GET  /api/activities
GET  /api/activities/{id}
POST /api/admin/activities
PUT  /api/admin/activities/{id}
PUT  /api/admin/activities/{id}/status
```

### 预约接口

```text
POST   /api/activities/{id}/reservations
DELETE /api/reservations/{id}
GET    /api/users/me/reservations
GET    /api/reservations/{id}
```

### 核销接口

```text
GET  /api/admin/reservations/by-code/{code}
POST /api/admin/verifications
GET  /api/admin/verifications
```

## 9. 数据库表设计（最终版）

已建表（6 张）：

```text
app_user              -- 用户表
activity_category     -- 活动分类表
activity              -- 活动表
activity_reservation  -- 预约表（UNIQUE KEY uk_user_activity 保证一人一单）
verification_record   -- 核销记录表
notification          -- 通知记录表（RabbitMQ 消费者写入）
```

表结构详见 `src/main/resources/schema.sql`。

注意：

1. 不建议使用 `user` 作为项目用户表名，继续使用 `app_user`。
2. `activity_reservation` 必须保留唯一索引 `uk_user_activity(user_id, activity_id)`。
3. `reservation_code` 必须唯一。
4. 预约、取消预约、核销都需要事务。
5. `notification` 表由 RabbitMQ 消费者异步写入，不参与业务事务。

## 10. RabbitMQ 消息队列模块

### 实现内容

- 声明 Topic 交换机 `campus.reservation` + 持久化队列 `campus.reservation.notify`
- 路由键：`reservation.success`
- 预约成功后通过 `TransactionSynchronization.afterCommit()` 发送 JSON 消息
- 消费者 `ReservationNotificationConsumer` 监听队列，写入 `notification` 表
- `Jackson2JsonMessageConverter` JSON 序列化（替代默认 Java 序列化，解决 allowlist 反序列化失败）
- 失败重试：最多 3 次，初始间隔 1 秒

### 新增/修改文件

| 文件 | 说明 |
|------|------|
| `RabbitMQConfig.java` | 队列、交换机、绑定 + JSON 消息转换器 + RabbitTemplate |
| `ReservationMessage.java` | 预约消息 DTO（userId, activityId, reservationId, code, title, time） |
| `ReservationNotificationConsumer.java` | 消费者，`@RabbitListener` 监听 + 写入 notification 表 |
| `Notification.java` | 通知实体 |
| `NotificationMapper.java` | MyBatis-Plus Mapper |
| `ReservationService.java` | 注入 RabbitTemplate，afterCommit 发送消息 |
| `pom.xml` | 新增 `spring-boot-starter-amqp` |
| `application.yml` | 新增 RabbitMQ 连接 + 重试配置 |
| `application-docker.yml` | RabbitMQ host=rabbitmq |
| `docker-compose.yml` | 新增 rabbitmq 服务 |
| `schema.sql` | 新增 notification 表 |

### 环境安装

1. Erlang 26.2.5.3：下载 `otp_win64_26.2.5.3.exe`，安装后设 `ERLANG_HOME` 系统变量
2. RabbitMQ 3.12.14：下载 `rabbitmq-server-3.12.14.exe`，默认路径安装
3. 启用管理插件：`rabbitmq-plugins.bat enable rabbitmq_management`
4. 管理后台：http://localhost:15672（guest/guest）

## 11. Trae 执行要求

请 Trae 后续按以下规则推进：

1. 不要一次生成过多功能。
2. 每次只完成一个阶段。
3. 编码前先说明本阶段目标和验收方式。
4. 修改代码前先阅读已有文件。
5. 不做无关重构。
6. 不新增非必要依赖。
7. 每个阶段完成后必须给出运行或测试证据。
8. 如果遇到环境问题，先记录完整报错，不要盲目改代码。

## 12. 推荐给 Trae 的起始提示词

可以直接复制以下内容给 Trae：

```text
请接手这个 Java 后端项目：校园活动预约与核销平台。

项目目标：完成一个基于 Spring Boot 3 + MySQL + Redis 的校园活动预约与核销平台 MVP，用于 Java 后端实习简历展示。

当前策略：项目完成优先。SQL、JDBC、Maven、Spring Boot 不做大量散练习，只学习项目必需知识点，并直接落到项目代码。

已完成：MySQL 8.4.10 已安装；数据库 campus_activity 已创建；app_user、activity_category、activity、activity_reservation 四张表已建好；SQL 已手动验证预约、扣库存、一人一单、防超卖、取消预约。

请先执行阶段 0：创建 Spring Boot 3 项目骨架，接入 MySQL、MyBatis-Plus、Knife4j。

要求：
1. 先说明本阶段目标、假设和验收标准。
2. 修改代码前先阅读已有项目文件。
3. 只完成阶段 0，不要顺手实现用户登录、活动预约等后续功能。
4. 项目必须能本地启动。
5. 提供 /api/health 测试接口。
6. 验证 MySQL 连接正常。
7. 完成后告诉我运行命令、测试地址和本阶段改了哪些文件。
```

## 13. 回到 Codex 后需要同步的信息

等额度重置后，把以下内容同步回来：

1. 项目当前目录路径。
2. `pom.xml` 内容。
3. `src/main/resources/application.yml` 内容，注意不要暴露真实密码，可以打码。
4. 已完成的模块。
5. 能成功访问的接口列表。
6. 当前数据库表结构是否有变化。
7. 当前最大的报错或卡点。
8. Git 状态，如果已经建仓则提供分支和最近提交信息。

最小同步格式：

```text
当前进度：全部 7 阶段 + Docker + RabbitMQ 已完成并验证通过

已完成：
- 阶段 0：Spring Boot 3 + MySQL + MyBatis-Plus + Knife4j 项目骨架
- 阶段 1：6 张数据库表 + 对应 Entity/Mapper
- 阶段 2：用户注册、登录、JWT、查询当前用户
- 阶段 3：活动分类、列表分页、详情、管理员 CRUD、上下架
- 阶段 4：预约、一人一单、防超卖、取消预约 + 库存回滚
- 阶段 5：按码查询、核销、重复核销拦截、核销记录
- 阶段 6：Redis 缓存（Cache-Aside、空值防穿透、随机 TTL）
- 阶段 7：README（技术栈、架构、API、运行指南）
- 额外：Docker Compose 容器化部署（多阶段构建 + 健康检查）
- 额外：RabbitMQ 消息队列（预约成功异步通知 + TransactionSynchronization + 消费者写入通知记录 + JSON 序列化）

未完成：无核心功能未完成。可选扩展：单元测试、前端页面。

运行命令：
$env:JAVA_HOME="C:\Program Files\Java\jdk-25"; & "C:\Program Files\Java\jdk-25\bin\java.exe" "-Djava.io.tmpdir=target/tmp" -jar target/campus-activity-1.0.0-SNAPSHOT.jar

Redis：已作为 Windows 服务运行，端口 6379
RabbitMQ：已作为 Windows 服务运行，端口 5672（管理后台 http://localhost:15672）

Docker 启动：docker compose up -d

已验证接口：16 个全部通过 + RabbitMQ 消息通知链路验证通过

当前问题：无。Erlang/RabbitMQ 下载依赖 GitHub，国内网络可能较慢。

关键文件：
- pom.xml（依赖管理，含 spring-boot-starter-amqp）
- src/main/resources/application.yml（本地配置，含 RabbitMQ 连接）
- src/main/resources/application-docker.yml（Docker 配置，RabbitMQ 主机=rabbitmq）
- src/main/resources/schema.sql（6 张表建表脚本，含 notification 表）
- src/main/java/com/campus/config/RabbitMQConfig.java（队列/交换机/JSON转换器）
- src/main/java/com/campus/consumer/ReservationNotificationConsumer.java（消息消费者）
- src/main/java/com/campus/dto/ReservationMessage.java（消息 DTO）
- src/main/java/com/campus/entity/Notification.java（通知实体）
- Dockerfile（多阶段构建）
- docker-compose.yml（容器编排，含 RabbitMQ 服务）
- README.md（项目文档）
```

## 14. 简历描述草稿

当前不能直接写入正式简历，必须等项目真实完成后再按实际证据调整。

候选描述：

```text
校园活动预约与核销平台
- 基于 Spring Boot 3、MySQL、Redis、RabbitMQ 开发校园活动预约与核销后端系统，支持用户注册登录、活动查询、活动预约、取消预约和管理员核销。
- 使用 Docker Compose 编排 MySQL 8.0 + Redis 7 + RabbitMQ 3.12 + 应用服务，多阶段构建减小镜像体积，健康检查控制依赖启动顺序，实现一键部署。
- 使用 MySQL 设计用户、活动、预约、核销、通知等核心表，通过唯一索引限制同一用户重复预约同一活动。
- 在预约流程中使用条件更新扣减库存，并结合 @Transactional 保证预约记录和库存状态一致，避免活动名额超卖。
- 使用 RabbitMQ 实现预约成功后异步通知，通过 TransactionSynchronization 事务提交后发送消息，消费者写入通知记录，失败重试保证可靠投递。
- 使用 Redis 实现 Cache-Aside 缓存模式，通过空值缓存防穿透、随机 TTL 防雪崩，管理员更新活动时主动清除缓存。
- 使用 JWT 实现登录态校验，拦截器解析用户 ID 和角色并通过 ThreadLocal 传递，Service 层校验权限。
```

