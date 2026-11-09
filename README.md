# 全栈项目合集

三个可独立运行的项目，覆盖 **Java 后端 / Redis 高并发 / 多模块企业应用 / AI 模型服务 / Vue 3 前端**：

| 项目 | 目录 | 技术栈 | 核心亮点 |
| --- | --- | --- | --- |
| **TanDian 本地生活服务平台** | [`tandian/`](tandian/README.md) | Spring Boot 3.1、Java 21、MySQL 8、Redis 7、Redisson、Lua、Redis Stream、Nginx | 商铺缓存、GEO 附近、点赞/Feed、秒杀限流/异步下单/死信重放、Prometheus、前端页面 |
| **寓见公寓租赁服务平台** | [`yujian/`](yujian/README.md) | Spring Boot 3.0、Java 17、MyBatis-Plus、MySQL、Redis、MinIO、Vue 3、Vant | 多模块后端、JWT/验证码/BCrypt、报修工单、Docker Compose + Nginx |
| **植物叶片病害识别与防治系统** | [`plant-disease/`](plant-disease/README.md) | PyTorch MobileNetV2、ONNX Runtime、Spring Boot 2.7、Vue 3、ECharts | 39 类模型训练/导出、ONNX Java 推理、批量识别、CSV 导出 |

## 目录结构

```text
.
├── tandian/                       # 本地生活服务平台
│   ├── src/                       # Spring Boot 后端
│   ├── frontend/                  # 前端静态页（Vue 2 + Element UI）
│   ├── deploy/nginx.conf          # 前端 Nginx 配置
│   ├── docs/                      # 缓存、秒杀设计文档
│   ├── Dockerfile
│   └── docker-compose.yml
├── yujian/                        # 智慧公寓租赁平台
│   ├── lease/                     # Spring Boot 多模块后端
│   ├── rentHouseAdmin/            # Vue 3 管理后台
│   ├── rentHouseH5/               # Vue 3 + Vant 用户端
│   └── docker-compose.yml
├── plant-disease/                 # 植物病害识别
│   ├── ai_model/                  # 训练、评估、ONNX 导出
│   ├── plant-disease-server/      # Spring Boot + ONNX Runtime
│   ├── plant-disease-web/         # Vue 3 前端
│   └── docker-compose.yml
└── .github/workflows/ci.yml       # 三个项目的统一 CI
```

## 项目简介

### TanDian 本地生活服务平台

登录、商铺、探店笔记、关注 Feed、评论、优惠券秒杀。重点在 Redis 实战：

- 缓存穿透/击穿处理、GEO 附近商铺、点赞 SortedSet、推模式 Feed。
- 秒杀：Lua 原子校验 + Redis Stream 异步落库 + Redisson 用户锁 + pending 重试 + 死信队列/重放 + 唯一索引兜底。
- 用户级限流、Prometheus 指标、Swagger、Docker Compose、Nginx 托管前端页面。

详见 [`tandian/README.md`](tandian/README.md)。

### 寓见公寓租赁服务平台

管理端 + 用户端的完整租房平台：

- Spring Boot 3 多模块（common/model/web-admin/web-app）。
- JWT、Redis 图形/短信验证码、MinIO 文件存储、BCrypt 密码迁移、登录失败锁定。
- 新增报修工单模块：状态机、用户端提交/取消、管理端分页/处理、管理端页面。
- MySQL 初始化脚本（29 张表）、Docker Compose、Nginx 同源代理。

详见 [`yujian/README.md`](yujian/README.md)。

### 植物叶片病害识别与防治系统

从训练到部署的 AI 应用：

- MobileNetV2 + PlantVillage 39 类，训练/评估/ONNX 导出。
- Java ONNX Runtime 在线推理，224×224 + ImageNet 归一化。
- 类别映射修复：按 `class_names.txt` 的类名匹配 `disease_info.en_name`，避免下标与数据库 id 错位。
- 单张/批量识别、识别历史、统计图表、CSV 导出。

详见 [`plant-disease/README.md`](plant-disease/README.md)。

## 运行方式

三个项目相互独立，进入对应目录查看 README：

```bash
# TanDian
cd tandian
cp .env.example .env
docker compose up -d --build

# Yujian
cd yujian
cp .env.example .env
docker compose up -d --build

# Plant disease
cd plant-disease
cp .env.example .env
docker compose up -d --build
```

> 三套 Compose 的端口可能重叠（8080/8081 等），建议一次只启动一套，或按各项目 `.env` 调整端口。

## 工程化说明

- 配置全部通过环境变量注入，仓库不包含真实数据库密码、JWT 密钥、云服务 AccessKey。
- 每个项目都有独立 README、Dockerfile、Docker Compose、单元测试/配置测试和 CI。
- 根目录 CI 会分别构建三个项目的后端和前端。

## 说明

- TanDian、Yujian 的业务原型来自公开学习资料，个人完成了工程化重构、安全改造和功能扩展，各项目 README 保留了开发说明。
- 植物病害项目使用公开的 PlantVillage 数据集训练；数据集本身不进入仓库。
- 识别结果/演示数据仅用于学习和展示，生产使用请替换密钥、密码和示例数据。
