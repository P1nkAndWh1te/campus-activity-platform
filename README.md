# 校园活动预约与核销平台

基于 Spring Boot 3 + MySQL + Redis 的校园活动预约后端系统，用于 Java 后端实习简历展示。

## 技术栈

| 技术 | 说明 |
|------|------|
| Java 17+ | 开发语言 |
| Spring Boot 3.3.5 | 应用框架 |
| MySQL 8.4.10 | 关系型数据库 |
| MyBatis-Plus 3.5.9 | ORM 框架 |
| Redis 5.0.14 | 缓存 |
| RabbitMQ 3.12 | 消息队列（异步通知） |
| JWT (jjwt 0.12.6) | 登录认证 |
| BCrypt | 密码加密 |
| Knife4j 4.5.0 | 接口文档 |
| Jackson | JSON 序列化 |
| Maven | 构建工具 |
| Docker Compose | 容器化部署 |

## 功能列表

### 用户模块
- [x] 手机号注册
- [x] 账号登录（返回 JWT）
- [x] 查询当前用户信息

### 活动模块
- [x] 活动分类查询
- [x] 活动列表分页查询（仅上架活动）
- [x] 活动详情查询
- [x] 管理员创建活动
- [x] 管理员修改活动
- [x] 管理员上下架活动

### 预约模块
- [x] 用户预约活动
- [x] 一人一单（唯一索引防重）
- [x] 条件扣库存防超卖
- [x] 事务保证数据一致
- [x] 查询我的预约
- [x] 取消预约 + 库存回滚

### 核销模块
- [x] 管理员按预约码查询预约
- [x] 管理员核销预约
- [x] 状态校验（已取消/已核销不可重复核销）
- [x] 核销记录持久化

### 缓存模块
- [x] Redis Cache-Aside 模式缓存活动详情
- [x] 空值缓存防穿透（5 分钟 TTL）
- [x] 随机 TTL 防缓存雪崩（30 分钟 + 0~5 分钟随机抖动）
- [x] 管理员更新/上下架时自动清除缓存

### 消息队列模块
- [x] RabbitMQ Topic Exchange 声明（campus.reservation）
- [x] 预约成功后事务提交时异步发送通知消息
- [x] 消费者监听写入通知记录表
- [x] 失败重试（最多 3 次）

### 基础设施
- [x] 统一响应封装（Result）
- [x] 全局异常处理
- [x] JWT 登录拦截器
- [x] Knife4j 接口文档

## 项目结构

```
src/main/java/com/campus/
├── common/                          # 公共组件
│   ├── BusinessException.java       # 业务异常
│   ├── GlobalExceptionHandler.java  # 全局异常处理器
│   ├── Result.java                  # 统一响应
│   └── UserContext.java             # 用户上下文（ThreadLocal）
├── config/                          # 配置类
│   ├── AppConfig.java               # BCrypt 配置
│   ├── CacheConfig.java             # Redis 序列化配置
│   └── WebConfig.java               # 拦截器配置
├── controller/                      # 控制器
│   ├── ActivityCategoryController.java
│   ├── ActivityController.java
│   ├── AdminActivityController.java
│   ├── AdminVerificationController.java
│   ├── AuthController.java
│   ├── HealthController.java
│   ├── ReservationController.java
│   └── UserController.java
├── dto/                             # 数据传输对象
│   ├── ActivityCreateRequest.java
│   ├── ActivityVO.java
│   ├── LoginRequest.java
│   ├── LoginResponse.java
│   ├── RegisterRequest.java
│   ├── ReservationVO.java
│   └── UserVO.java
├── entity/                          # 实体类
│   ├── Activity.java
│   ├── ActivityCategory.java
│   ├── ActivityReservation.java
│   ├── AppUser.java
│   └── VerificationRecord.java
├── interceptor/
│   └── LoginInterceptor.java        # JWT 拦截器
├── mapper/                          # MyBatis-Plus Mapper
│   ├── ActivityCategoryMapper.java
│   ├── ActivityMapper.java
│   ├── ActivityReservationMapper.java
│   ├── AppUserMapper.java
│   └── VerificationRecordMapper.java
├── service/                         # 业务逻辑层
│   ├── ActivityService.java
│   ├── ReservationService.java
│   ├── UserService.java
│   └── VerificationService.java
├── util/
│   └── JwtUtil.java                 # JWT 工具类
├── CampusActivityApplication.java   # 启动类
└── DataInitCheck.java               # 数据库连接检查（启动时）
```

## 数据库设计

### app_user（用户表）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | 主键 |
| phone | VARCHAR(20) UNIQUE | 手机号 |
| password | VARCHAR(200) | BCrypt 加密密码 |
| nickname | VARCHAR(50) | 昵称 |
| role | VARCHAR(20) | 角色（ADMIN/USER） |
| status | TINYINT | 状态（1 正常 0 禁用） |
| created_at | DATETIME | 创建时间 |

### activity_category（活动分类表）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | 主键 |
| name | VARCHAR(50) | 分类名称 |

### activity（活动表）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | 主键 |
| category_id | BIGINT FK | 分类 ID |
| title | VARCHAR(200) | 活动标题 |
| description | TEXT | 活动描述 |
| location | VARCHAR(200) | 活动地点 |
| total_quota | INT | 总名额 |
| available_quota | INT | 剩余名额 |
| start_time | DATETIME | 活动开始时间 |
| end_time | DATETIME | 活动结束时间 |
| reservation_start_time | DATETIME | 预约开始时间 |
| reservation_end_time | DATETIME | 预约截止时间 |
| status | TINYINT | 状态（1 上架 0 下架） |

### activity_reservation（预约表）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | 主键 |
| user_id | BIGINT FK | 用户 ID |
| activity_id | BIGINT FK | 活动 ID |
| reservation_code | VARCHAR(50) UNIQUE | 预约码 |
| status | VARCHAR(20) | 状态（RESERVED/CANCELED/VERIFIED） |
| reserved_at | DATETIME | 预约时间 |
| canceled_at | DATETIME | 取消时间 |
| verified_at | DATETIME | 核销时间 |

**约束：** `UNIQUE KEY uk_user_activity (user_id, activity_id)` 保证一人一单。

### verification_record（核销记录表）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | 主键 |
| reservation_id | BIGINT UNIQUE | 预约 ID |
| reservation_code | VARCHAR(50) | 预约码 |
| activity_id | BIGINT | 活动 ID |
| user_id | BIGINT | 用户 ID |
| operator_id | BIGINT | 核销操作员 ID |
| verified_at | DATETIME | 核销时间 |

## 接口列表

### 健康检查

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| GET | `/api/health` | 健康检查 | 否 |

### 用户接口

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| POST | `/api/auth/register` | 用户注册 | 否 |
| POST | `/api/auth/login` | 用户登录 | 否 |
| GET | `/api/users/me` | 查询当前用户 | JWT |

### 活动接口

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| GET | `/api/activity-categories` | 活动分类列表 | 否 |
| GET | `/api/activities` | 活动分页列表 | 否 |
| GET | `/api/activities/{id}` | 活动详情 | 否 |

### 管理端活动接口

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| POST | `/api/admin/activities` | 创建活动 | 管理员 JWT |
| PUT | `/api/admin/activities/{id}` | 修改活动 | 管理员 JWT |
| PUT | `/api/admin/activities/{id}/status` | 上下架活动 | 管理员 JWT |

### 预约接口

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| POST | `/api/activities/{id}/reservations` | 预约活动 | JWT |
| DELETE | `/api/reservations/{id}` | 取消预约 | JWT |
| GET | `/api/users/me/reservations` | 我的预约列表 | JWT |
| GET | `/api/reservations/{id}` | 预约详情 | JWT |

### 管理端核销接口

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| GET | `/api/admin/reservations/by-code/{code}` | 按预约码查询 | 管理员 JWT |
| POST | `/api/admin/verifications` | 核销预约 | 管理员 JWT |
| GET | `/api/admin/verifications` | 核销记录列表 | 管理员 JWT |

## 接口文档

启动项目后访问：

```
http://localhost:8080/doc.html
```

## 本地运行

### 方式一：Docker Compose（推荐，一键启动）

```powershell
# 首次使用先复制环境变量示例，并设置自己的 MySQL root 密码
copy .env.example .env

# 启动所有服务（MySQL + Redis + App）
docker compose up -d

# 查看日志
docker compose logs -f app

# 停止
docker compose down
```

首次启动会自动：
- 拉取 MySQL 8.0、Redis 7 Alpine、OpenJDK 17 镜像
- Maven 多阶段构建打包应用
- 执行建表脚本初始化数据库
- 健康检查通过后启动应用

服务就绪后访问：
- 应用：http://localhost:8080
- 接口文档：http://localhost:8080/doc.html

### 方式二：本地手动运行

### 环境要求

- JDK 17+
- MySQL 8.0+
- Redis 5.0+（Windows 用户见下方说明）

### 1. 创建数据库

```sql
CREATE DATABASE IF NOT EXISTS campus_activity DEFAULT CHARSET utf8mb4;
```

### 2. 执行建表脚本

PowerShell：

```powershell
Get-Content -Encoding UTF8 src\main\resources\schema.sql | mysql -u root -p --default-character-set=utf8mb4 campus_activity
```

### 2.1 可选：导入开发种子数据

`seed-dev.sql` 会创建一个开发管理员账号和 3 个活动分类。它不会自动执行，需要时手动导入：

```powershell
Get-Content -Encoding UTF8 src\main\resources\seed-dev.sql | mysql -u root -p --default-character-set=utf8mb4 campus_activity
```

开发管理员账号：

| 手机号 | 密码 |
|--------|------|
| 13900001111 | test123 |

注意：该账号只用于本地开发和演示，正式部署前必须修改密码。

### 2.2 可选：重置为干净演示数据

`reset-demo-data.sql` 会清空本地业务表并重新写入演示数据，只能用于本地演示库：

```powershell
Get-Content -Encoding UTF8 src\main\resources\reset-demo-data.sql | mysql -u root -p --default-character-set=utf8mb4 campus_activity
```

### 3. 配置运行环境变量

本地运行不再把数据库密码写死到 `application.yml`，请在当前 PowerShell 会话中设置：

```powershell
$env:MYSQL_PASSWORD="你的MySQL密码"
$env:JWT_SECRET="至少32位的随机字符串"
```

默认 Redis 配置为：

```text
127.0.0.1:6380
```

### 4. 启动 Redis（Windows）

本项目使用 `tporadowski/redis`（Redis 5.0.14.1 for Windows）：

```powershell
# 下载解压后启动
C:\Redis\redis-server.exe --port 6380
```

### 5. 安装 RabbitMQ（Windows）

RabbitMQ 需要 Erlang 运行环境，两步安装：

1. 下载并安装 Erlang 26.2：
   https://erlang.org/download/otp_win64_26.2.5.3.exe

2. 下载并安装 RabbitMQ 3.12：
   https://github.com/rabbitmq/rabbitmq-server/releases/download/v3.12.14/rabbitmq-server-3.12.14.exe

3. 启用管理插件并启动：
```powershell
cd "C:\Program Files\RabbitMQ Server\rabbitmq_server-3.12.14\sbin"
.\rabbitmq-plugins.bat enable rabbitmq_management
.\rabbitmq-server.bat
```

4. 管理后台：http://localhost:15672（guest/guest）

### 6. 编译运行

```powershell
# 推荐：自动临时切换到 JDK 25，避免 Maven 误用 Java 8
$env:MYSQL_PASSWORD="你的MySQL密码"
$env:JWT_SECRET="至少32位的随机字符串"
powershell -ExecutionPolicy Bypass -File scripts\build-local.ps1

# 启动应用也需要提供数据库密码
$env:MYSQL_PASSWORD="你的MySQL密码"
$env:JWT_SECRET="至少32位的随机字符串"
java -jar target/campus-activity-1.0.0-SNAPSHOT.jar
```

### 6. 验证

```bash
# 健康检查
curl http://localhost:8080/api/health

# 注册测试用户
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"phone":"13800001111","password":"test123","nickname":"测试用户"}'

# 登录获取 Token
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"phone":"13800001111","password":"test123"}'
```

也可以运行项目内置验收脚本：

```powershell
$env:MYSQL_PASSWORD="你的MySQL密码"
powershell -ExecutionPolicy Bypass -File scripts\verify-api.ps1
```

## 核心实现亮点

1. **库存防超卖**：使用 MyBatis-Plus `UpdateWrapper` 实现 `UPDATE activity SET available_quota = available_quota - 1 WHERE id = ? AND available_quota > 0`，判断影响行数。
2. **一人一单**：`activity_reservation` 表 `UNIQUE KEY (user_id, activity_id)` 唯一索引兜底。
3. **事务一致性**：`@Transactional` 保证库存扣减和预约记录写入原子操作。
4. **Cache-Aside 缓存模式**：读缓存 → 缓存未命中查 MySQL → 写 Redis，TTL 30 分钟 + 随机 0~5 分钟抖动；空值缓存 5 分钟防穿透。
5. **Docker 容器化部署**：多阶段构建（Maven 编译 + JRE 运行镜像），docker-compose 编排 MySQL + Redis + App，健康检查依赖启动顺序。
6. **RabbitMQ 异步解耦**：预约成功后通过事务同步注册 + 事务提交后发送消息，消费者异步写入通知记录；失败重试 3 次保证消息可靠投递。
7. **JWT 认证 + 角色控制**：`LoginInterceptor` 从 Token 解析用户 ID 和角色，`UserContext` ThreadLocal 存储，Service 层校验权限。

## 接口验收

项目提供可复用接口验收脚本：

```text
scripts/verify-api.ps1
```

验收说明：

```text
04_成果输出/接口验收说明.md
```

截至 2026-07-22，已通过脚本验证：

- 注册、登录、JWT、查询当前用户
- 活动分类查询、管理员创建活动、活动详情查询
- 用户预约活动、重复预约拦截、查询我的预约
- 管理员按预约码查询、核销、重复核销拦截
- 取消预约
