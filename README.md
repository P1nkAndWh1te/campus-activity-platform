# 校园活动预约与核销平台

一个基于 Spring Boot 3 的校园活动预约与现场核销后端服务，适用于讲座、比赛、社团活动等校园活动场景。系统覆盖活动发布、用户预约、名额扣减、预约码核销、活动详情缓存和预约通知等核心流程。

## 技术栈

| 技术 | 用途 |
| --- | --- |
| Java 17+ | 开发语言 |
| Spring Boot 3.3.5 | Web 应用框架 |
| MyBatis-Plus 3.5.9 | 数据访问 |
| MySQL 8.x | 业务数据存储 |
| Redis | 活动详情缓存 |
| RabbitMQ | 预约通知异步处理 |
| JWT | 登录认证 |
| BCrypt | 密码加密 |
| Knife4j | API 文档 |
| Maven | 构建管理 |
| Docker Compose | 本地服务编排 |

## 功能模块

### 用户认证

- 手机号注册
- 登录并签发 JWT
- 查询当前登录用户
- BCrypt 密码加密存储

### 活动管理

- 活动分类查询
- 活动列表分页查询
- 活动详情查询
- 管理员创建、修改、上下架活动

### 活动预约

- 用户预约活动
- 同一用户同一活动只允许预约一次
- 活动名额条件扣减
- 用户查询个人预约
- 用户取消预约并返还名额

### 现场核销

- 管理员按预约码查询预约记录
- 管理员核销预约
- 拦截已取消预约的核销
- 拦截重复核销
- 保存核销记录

### 缓存与消息

- 活动详情使用 Redis Cache-Aside 模式
- 对不存在活动写入短 TTL 空值缓存，降低缓存穿透风险
- 正常缓存使用随机 TTL 抖动，降低集中失效风险
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

核心数据表：

| 表名 | 说明 |
| --- | --- |
| app_user | 用户表 |
| activity_category | 活动分类表 |
| activity | 活动表 |
| activity_reservation | 预约表 |
| verification_record | 核销记录表 |
| notification | 通知记录表 |

### 一人一单

预约表通过唯一索引限制重复预约：

```sql
UNIQUE KEY uk_user_activity (user_id, activity_id)
```

即使并发请求同时进入，数据库层也能保证同一用户不能重复预约同一活动。

### 名额扣减

预约时使用条件更新扣减名额：

```sql
UPDATE activity
SET available_quota = available_quota - 1
WHERE id = ? AND available_quota > 0;
```

业务层根据影响行数判断扣减是否成功，避免剩余名额被扣成负数。

## API 概览

### 用户接口

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| POST | `/api/auth/register` | 注册 |
| POST | `/api/auth/login` | 登录 |
| GET | `/api/users/me` | 当前用户信息 |

### 活动接口

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/api/activity-categories` | 活动分类 |
| GET | `/api/activities` | 活动列表 |
| GET | `/api/activities/{id}` | 活动详情 |
| POST | `/api/admin/activities` | 创建活动 |
| PUT | `/api/admin/activities/{id}` | 修改活动 |
| PUT | `/api/admin/activities/{id}/status` | 上下架活动 |

### 预约和核销接口

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| POST | `/api/activities/{id}/reservations` | 预约活动 |
| DELETE | `/api/reservations/{id}` | 取消预约 |
| GET | `/api/users/me/reservations` | 我的预约 |
| GET | `/api/admin/reservations/by-code/{code}` | 按预约码查询 |
| POST | `/api/admin/verifications` | 核销预约 |
| GET | `/api/admin/verifications` | 核销记录 |

API 文档地址：

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

### Docker Compose

复制环境变量示例：

```powershell
copy .env.example .env
```

设置 `.env`：

```text
MYSQL_ROOT_PASSWORD=your_mysql_password
JWT_SECRET=replace-with-at-least-32-characters
```

启动依赖和应用：

```powershell
docker compose up -d
```

停止：

```powershell
docker compose down
```

### 手动运行

创建数据库：

```sql
CREATE DATABASE IF NOT EXISTS campus_activity DEFAULT CHARSET utf8mb4;
```

导入表结构和演示数据：

```powershell
Get-Content -Encoding UTF8 src\main\resources\schema.sql | mysql -u root -p --default-character-set=utf8mb4 campus_activity
Get-Content -Encoding UTF8 src\main\resources\reset-demo-data.sql | mysql -u root -p --default-character-set=utf8mb4 campus_activity
```

设置环境变量：

```powershell
$env:MYSQL_PASSWORD="your_mysql_password"
$env:JWT_SECRET="replace-with-at-least-32-characters"
```

构建：

```powershell
powershell -ExecutionPolicy Bypass -File scripts\build-local.ps1
```

启动：

```powershell
powershell -ExecutionPolicy Bypass -File scripts\start-local.ps1
```

健康检查：

```text
GET http://localhost:8080/api/health
```

演示账号：

| 角色 | 手机号 | 密码 |
| --- | --- | --- |
| 管理员 | 13900001111 | test123 |
| 普通用户 | 13800001111 | test123 |

## 接口验证

项目提供 PowerShell 验证脚本，用于执行核心业务链路回归：

```powershell
$env:MYSQL_PASSWORD="your_mysql_password"
powershell -ExecutionPolicy Bypass -File scripts\verify-api.ps1
```

验证范围包括：

- 用户注册和登录
- JWT 鉴权
- 活动分类查询
- 管理员创建活动
- 活动详情查询
- 用户预约活动
- 重复预约拦截
- 管理员按预约码查询
- 管理员核销预约
- 重复核销拦截
- 取消预约

## 实现要点

### 事务一致性

预约流程使用 `@Transactional`，确保名额扣减和预约记录写入处于同一个事务中。若预约记录写入失败，名额扣减会随事务回滚。

### JWT 鉴权

登录成功后签发 JWT，拦截器解析 `Authorization: Bearer <token>`，并将用户 ID 和角色写入线程上下文，供业务层进行身份和权限判断。

### 活动详情缓存

活动详情采用 Cache-Aside 模式：

```text
Read Redis -> Read MySQL on cache miss -> Write Redis
```

活动更新和上下架时主动删除对应缓存，避免读取旧数据。

### 异步通知

预约成功后在事务提交后发送 RabbitMQ 消息，消费者异步写入通知记录，避免通知写入阻塞预约主流程。
