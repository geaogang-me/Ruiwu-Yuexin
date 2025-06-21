# RuiWu-Yuexin

> **前后端分离架构**
>
> - **前端**: Vue 3
> - **后端**: Spring Boot

---

## 📖 项目简介

RuiWu-Yuexin 是一套电商平台示例系统，采用前后端分离架构，旨在展示如何使用现代化技术栈快速搭建高可用、高扩展性的电商服务。

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
- **数据库**
  - MySQL
- **环境**
  - Java JDK 17
  - Node.js v18

## 🚀 快速开始

1. 克隆仓库：
   ```bash
   git clone https://github.com/yourusername/RuiWu-Yuexin.git
   cd RuiWu-Yuexin
   ```

2. 后端启动
   ```bash
   cd backend
   ./mvnw spring-boot:run
   ```

3. 前端启动
   ```bash
   cd frontend
   npm install
   npm run dev
   ```

4. 在浏览器中访问：
   ```
   http://localhost:3000
   ```

## 🗂️ 项目结构

```
RuiWu-Yuexin/
├── backend/             // 后端 Spring Boot 项目
│   ├── src/main/java   // Java 源代码
│   ├── src/main/resources // 配置文件
│   └── pom.xml         // Maven 配置
└── frontend/            // 前端 Vue 3 项目
    ├── src/            // Vue 源代码
    ├── public/         // 静态资源
    └── package.json    // NPM 配置
```

## 🔐 安全与认证

- 使用 Spring Security + JWT 实现用户登录、权限管理。
- 接口使用 Bearer Token 传递。

## 💾 缓存

- 接口数据和用户 Session 信息使用 Redis 缓存，提高性能。

## ⭐ 贡献指南

欢迎提交 Issue 和 PR！

1. Fork 本仓库
2. 创建新分支：`git checkout -b feature/YourFeature`
3. 提交改动：`git commit -m "feat: add your feature"`
4. 推送分支：`git push origin feature/YourFeature`
5. 提交 PR

## 📄 许可证

本项目遵循 MIT 许可证。详情见 [LICENSE](./LICENSE)。
