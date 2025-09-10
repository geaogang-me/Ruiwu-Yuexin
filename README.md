# RuiWu-Yuexin

> **前后端分离架构**
>
> - **前端**: Vue 3
> - **后端**: Spring Boot

## ⚙️ 技术栈

- **前端**
  - Vue 3 + Composition API
  - Vue Router
  - Vuex
  - Element Plus
- **后端**
  - Spring Boot 3
  - MyBatis
  - Spring Security
  - JWT 身份验证
  - Redis 缓存
  - RabbitMQ
- **数据库**
  - MySQL
- **环境**
  - Java JDK 17
  - Node.js v18

## 🔐 安全与认证

- 使用 Spring Security + JWT 实现用户登录、权限管理。
- 接口使用 Bearer Token 传递。

## 💾 缓存

- 接口数据和用户 Session 信息使用 Redis 缓存，提高性能。

##数据同步
-RabbitMQ监控binlog日志，实现Redis、Mysql数据同步。

## ⭐ 贡献指南

欢迎提交 Issue 和 PR！

1. Fork 本仓库
2. 创建新分支：`git checkout -b feature/YourFeature`
3. 提交改动：`git commit -m "feat: add your feature"`
4. 推送分支：`git push origin feature/YourFeature`
5. 提交 PR

## 📄 许可证

本项目遵循 MIT 许可证。详情见 [LICENSE](./LICENSE)。
