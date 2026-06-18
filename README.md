# ClipFlow

ClipFlow 是一个基于 Spring Boot 开发的短视频社交后端项目，采用模块化单体架构。

项目实现了用户注册登录、JWT 鉴权、视频投稿与查询、评论、点赞、关注信息流、私信，以及 RabbitMQ 异步资源清理等核心功能。

## 技术栈

- Java 17、Spring Boot、Spring MVC
- MyBatis-Plus、MySQL
- Redis
- RabbitMQ
- MinIO
- JWT、BCrypt
- Knife4j
- Docker Compose

## 功能模块

- 用户模块：注册、登录、BCrypt 密码哈希
- 鉴权模块：JWT 签发与解析、拦截器、ThreadLocal 用户上下文
- 视频模块：视频投稿、详情查询、分页查询和权限删除
- 评论模块：发布、分页查询和权限删除
- 点赞模块：基于 Redis Set 实现点赞、取消点赞、状态和数量查询
- 关注模块：关注关系管理及基于 Redis ZSet 的关注信息流
- 私信模块：发送私信、会话列表和分页消息历史
- 异步任务：RabbitMQ 异步清理 MinIO 文件和 Redis 数据，并支持重试与死信队列
- 接口文档：通过 Knife4j 查看和调试 API

## 本地启动

### 环境要求

- JDK 17
- Docker Desktop

### 启动依赖服务

```bash
docker compose up -d
```

该命令会启动 MySQL、Redis、MinIO 和 RabbitMQ。首次创建 MySQL 数据卷时，会自动执行 `docker/mysql/init/01-schema.sql`。

### 启动后端

Windows：

```bash
mvnw.cmd spring-boot:run
```

启动成功后可以访问：

- Knife4j：http://localhost:8080/doc.html
- MinIO 控制台：http://localhost:9001
- RabbitMQ 控制台：http://localhost:15672

## 项目结构

- `auth`：JWT 鉴权、拦截器和用户上下文
- `user`：用户注册与登录
- `video`：视频投稿、查询和删除
- `comment`：评论业务
- `like`：点赞业务
- `follow`：关注关系
- `feed`：关注信息流
- `message`：私信与会话
- `mq`：RabbitMQ 消息生产与消费
- `storage`：MinIO 文件存储
- `common`：统一响应、异常处理和公共配置

## 核心设计

- MySQL 保存核心业务数据，Redis 作为可重建的缓存和状态存储。
- 使用 Cache-Aside 模式处理关注关系缓存。
- 使用 Redis Set 保证重复点赞不会重复计数。
- 使用 Redis ZSet 按发布时间维护关注信息流。
- 私信会话统一按较小用户 ID、较大用户 ID 存储，避免重复会话。
- 使用批量查询避免会话列表中的 N+1 查询。
- RabbitMQ 消费者采用幂等清理，并通过重试和死信队列保存失败任务。
- MinIO 保存视频文件，MySQL 只保存视频元数据。

## 接口使用

1. 启动项目后访问 Knife4j 接口文档。
2. 调用注册接口创建用户。
3. 调用登录接口获取 JWT。
4. 点击 Knife4j 的 `Authorize` 按钮，填写登录返回的 Token。
5. 调用视频、评论、点赞、关注、私信等受保护接口。

请求受保护接口时，HTTP 请求头格式为：

```text
Authorization: Bearer <JWT>
```