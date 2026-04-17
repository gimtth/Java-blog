# Java 博客项目

一个基于前后端分离架构实现的博客管理项目，包含公开博客页面和后台管理台。项目围绕博客内容管理的典型场景展开，支持文章发布、分类维护、评论审核、分页查询和管理员登录鉴权，适合作为 Java 后端开发方向的个人项目展示。

## 项目简介

该项目分为两个部分：

- 公开博客页：面向访客展示已发布文章，支持分类筛选、关键词搜索、分页浏览、文章详情查看和评论提交
- 后台管理台：面向管理员进行文章、分类、评论等内容管理，并支持分页维护数据

## 技术栈

### 后端

- Java 17
- Spring Boot 3.5
- Spring Security
- MyBatis-Plus
- MySQL
- Redis
- JWT
- Maven

### 前端

- Vue 3
- Vite
- TypeScript
- Element Plus

### 测试与开发

- JUnit / Spring Boot Test
- H2（仅测试环境使用）

## 核心功能

### 公开博客页

- 展示已发布文章列表
- 支持按分类筛选文章
- 支持关键词搜索标题和摘要
- 支持分页浏览文章列表
- 查看文章详情与评论列表
- 提交评论并进入待审核状态

### 后台管理台

- 管理员账号登录
- 查看文章、分类、评论统计概览
- 新建、编辑、删除文章
- 新建文章分类
- 支持后台文章、评论分页管理
- 审核评论、删除评论

## 项目亮点

- 采用前后端分离架构，职责划分明确，便于独立开发和部署
- 使用 MyBatis-Plus 实现数据库访问，完成文章、分类、评论、用户等模块的基础 CRUD 与分页查询
- 使用 JWT 替代 Basic Auth，实现更符合实际业务场景的登录鉴权方式
- 使用 MySQL 持久化博客数据，支持用户、文章、分类、评论等实体间关系管理
- 使用 Redis 缓存热点文章详情，在本地 Redis 可用时优先走缓存，减少重复查询压力
- 使用 Spring Security 统一处理接口访问控制，区分公开接口与后台管理接口
- 后台支持文章发布状态管理和评论审核流程，具备基础内容运营能力
- 前端基于 Vue 3 + Element Plus 构建，包含公开页与管理台两套视图

## 项目结构

```text
online-blog-system/
  backend/   Spring Boot 后端服务
  frontend/  Vue 3 前端项目
```

## 快速启动

### 1. 启动后端

先准备 MySQL 数据库：

- 数据库名：`blog_system`
- 用户名：`root`
- 密码：`123456`

默认配置见 [application.yml](./backend/src/main/resources/application.yml)。

启动后端：

```bash
cd backend
mvn spring-boot:run
```

后端默认地址：

- `http://localhost:8080`

默认管理员账号：

- 用户名：`admin`
- 密码：`admin123`

可通过环境变量覆盖配置：

- `MYSQL_URL`
- `MYSQL_USERNAME`
- `MYSQL_PASSWORD`
- `REDIS_HOST`
- `REDIS_PORT`
- `REDIS_DATABASE`
- `ADMIN_USERNAME`
- `ADMIN_PASSWORD`
- `JWT_SECRET`

说明：

- 项目启动时会自动执行 `schema.sql`，在数据库中初始化用户表、文章表、分类表、评论表等核心表结构
- 如果数据库中没有分类数据，会自动初始化演示分类、文章、评论和管理员账号
- Redis 在本地不可用时不会阻塞项目启动，热点缓存会自动降级为直接查询数据库

### 2. 启动前端

```bash
cd frontend
npm install
npm run dev
```

前端默认地址：

- `http://localhost:5173`

## 构建与测试

### 后端测试

```bash
cd backend
mvn test
```

### 前端构建

```bash
cd frontend
npm run build
```
