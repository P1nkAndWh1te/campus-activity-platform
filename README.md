# 校园活动预约与核销平台

基于 Spring Boot 3、MySQL、Redis 和 RabbitMQ 的校园活动预约与核销后端系统。项目围绕校园讲座、比赛、社团活动等场景，完成用户报名、名额扣减、预约码核销和活动详情缓存等核心链路，用于 Java 后端实习项目展示。

## 项目定位

本项目第一版 MVP 聚焦后端主链路，不做前端页面，不加入优惠券、秒杀、复杂 RBAC 等扩展能力。目标是证明以下能力：

- 能使用 Spring Boot 3 搭建标准后端项目
- 能设计 MySQL 表结构并完成业务 CRUD
- 能通过唯一索引和条件更新保证预约一致性
- 能使用 JWT 完成登录态校验
- 能使用 Redis 做活动详情缓存
- 能使用 RabbitMQ 解耦预约成功后的通知写入
- 能提供可复现的本地运行和接口验收脚本

## 技术栈

| 技术 | 用途 |
| --- | --- |
| Java 17+ | 开发语言 |
| Spring Boot 3.3.5 | Web 应用框架 |
| MyBatis-Plus 3.5.9 | 数据访问 |
| MySQL 8.x | 业务数据存储 |
| Redis | 活动详情缓存 |
| RabbitMQ | 预约成功通知异步处理 |
| JWT | 登录态认证 |
| BCrypt | 密码加密 |
| Knife4j | 接口文档 |
| Maven | 构建管理 |
| Docker Compose | 本地容器化运行 |

## 核心功能

### 用户模块

- 手机号注册
- 登录并返回 JWT
- 查询当前登录用户信息

### 活动模块

- 查询活动分类
- 分页查询上架活动
- 查询活动详情
- 管理员创建活动
- 管理员修改活动
- 管理员上下架活动

### 预约模块

- 用户预约活动
- 一人一单限制
- 活动名额条件扣减
- 查询我的预约
- 取消预约并返还名额

### 核销模块

- 管理员按预约码查询预约
- 管理员核销预约
- 防止取消后核销
- 防止重复核销
- 保存核销记录

### 缓存与消息

- Redis Cache-Aside 查询活动详情
- 空值缓存防止缓存穿透
- TTL 随机抖动降低集中失效风险
- 预约成功后通过 RabbitMQ 异步写入通知记录

## 项目结构

```text
src/main/java/com/campus
├── common       # 统一响应、业务异常、全局异常、用户上下文
├── config       # Web、Redis、RabbitMQ、密码加密配置
├── consumer     # RabbitMQ 消费者
├── controller   # HTTP 接口
├── dto          # 请求和响应 DTO
├── entity       # MyBatis-Plus 实体
├── interceptor  # JWT 登录拦截器
├── mapper       # MyBatis-Plus Mapper
├── service      # 业务逻辑
└── util         # JWT 工具
```

## 数据库设计

核心表：

| 表名 | 说明 |
| --- | --- |
| app_user | 用户表，保存手机号、密码、昵称、角色和状态 |
| activity_category | 活动分类表 |
| activity | 活动表，保存名额、时间、地点和上下架状态 |
| activity_reservation | 预约表，保存预约码和预约状态 |
| verification_record | 核销记录表 |
| notification | 预约成功通知记录表 |

关键约束：

```sql
UNIQUE KEY uk_user_activity (user_id, activity_id)
```

该唯一索引用于兜底保证同一用户不能重复预约同一活动。

库存扣减使用条件更新：

```sql
UPDATE activity
SET available_quota = available_quota - 1
WHERE id = ? AND available_quota > 0;
```

业务层通过影响行数判断是否扣减成功，避免名额被扣成负数。

## 核心接口

### 用户接口

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| POST | `/api/auth/register` | 注册 |
| POST | `/api/auth/login` | 登录 |
| GET | `/api/users/me` | 查询当前用户 |

### 活动接口

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/api/activity-categories` | 活动分类 |
| GET | `/api/activities` | 活动列表 |
| GET | `/api/activities/{id}` | 活动详情 |
| POST | `/api/admin/activities` | 管理员创建活动 |
| PUT | `/api/admin/activities/{id}` | 管理员修改活动 |
| PUT | `/api/admin/activities/{id}/status` | 管理员上下架活动 |

### 预约和核销接口

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| POST | `/api/activities/{id}/reservations` | 预约活动 |
| DELETE | `/api/reservations/{id}` | 取消预约 |
| GET | `/api/users/me/reservations` | 我的预约 |
| GET | `/api/admin/reservations/by-code/{code}` | 按预约码查询 |
| POST | `/api/admin/verifications` | 核销预约 |
| GET | `/api/admin/verifications` | 核销记录 |

接口文档地址：

```text
http://localhost:8080/doc.html
```

## 本地运行

### 环境要求

- JDK 17+
- Maven 3.9+
- MySQL 8.x
- Redis
- RabbitMQ

本机 Maven 如果误用 Java 8，可以使用项目内置脚本临时切换到 JDK 25。

### 方式一：Docker Compose

复制环境变量示例：

```powershell
copy .env.example .env
```

编辑 `.env`，设置：

```text
MYSQL_ROOT_PASSWORD=你的MySQL密码
JWT_SECRET=至少32位随机字符串
```

启动：

```powershell
docker compose up -d
```

停止：

```powershell
docker compose down
```

### 方式二：本地手动运行

创建数据库：

```sql
CREATE DATABASE IF NOT EXISTS campus_activity DEFAULT CHARSET utf8mb4;
```

执行建表脚本：

```powershell
Get-Content -Encoding UTF8 src\main\resources\schema.sql | mysql -u root -p --default-character-set=utf8mb4 campus_activity
```

可选：导入开发种子数据：

```powershell
Get-Content -Encoding UTF8 src\main\resources\seed-dev.sql | mysql -u root -p --default-character-set=utf8mb4 campus_activity
```

可选：重置为干净演示数据：

```powershell
Get-Content -Encoding UTF8 src\main\resources\reset-demo-data.sql | mysql -u root -p --default-character-set=utf8mb4 campus_activity
```

开发账号：

| 角色 | 手机号 | 密码 |
| --- | --- | --- |
| 管理员 | 13900001111 | test123 |
| 普通用户 | 13800001111 | test123 |

设置运行环境变量：

```powershell
$env:MYSQL_PASSWORD="你的MySQL密码"
$env:JWT_SECRET="至少32位随机字符串"
```

构建：

```powershell
powershell -ExecutionPolicy Bypass -File scripts\build-local.ps1
```

启动：

```powershell
java -jar target\campus-activity-1.0.0-SNAPSHOT.jar
```

健康检查：

```text
GET http://localhost:8080/api/health
```

## 接口验收

项目提供自动化接口验收脚本：

```powershell
$env:MYSQL_PASSWORD="你的MySQL密码"
powershell -ExecutionPolicy Bypass -File scripts\verify-api.ps1
```

验收脚本会自动执行：

- 注册普通用户和管理员种子用户
- 登录并获取 JWT
- 查询当前用户
- 查询活动分类
- 管理员创建活动
- 查询活动详情
- 用户预约活动
- 验证重复预约被拦截
- 管理员按预约码查询
- 管理员核销预约
- 验证重复核销被拦截
- 取消预约

验收记录：

```text
03_执行过程/2026-07-22_接口验收记录.md
04_成果输出/接口验收说明.md
```

## 实现亮点

### 1. 一人一单

数据库层使用唯一索引：

```sql
UNIQUE KEY uk_user_activity (user_id, activity_id)
```

即使并发请求绕过业务判断，数据库仍能阻止重复预约。

### 2. 防止库存超卖

库存扣减不是先查再减，而是使用条件更新：

```sql
WHERE id = ? AND available_quota > 0
```

如果影响行数为 0，说明库存不足，预约失败。

### 3. 事务一致性

预约流程使用 `@Transactional`，保证库存扣减和预约记录写入处于同一个事务中。

### 4. 活动详情缓存

活动详情采用 Cache-Aside 模式：

```text
查 Redis -> 未命中查 MySQL -> 写入 Redis
```

不存在的活动写入空值缓存，降低缓存穿透风险。

### 5. 异步通知

预约成功后，在事务提交后发送 RabbitMQ 消息，消费者异步写入通知记录，避免通知逻辑阻塞主流程。

## 当前限制

- 第一版没有前端页面，主要通过接口文档和脚本验收
- 管理员权限采用简单角色字段 `role`，没有实现复杂 RBAC
- RabbitMQ 当前用于预约通知，未扩展到秒杀或异步落库
- 项目仍需后续补充接口截图或 Apifox 测试集合，便于简历展示

