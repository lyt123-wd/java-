# 寓见公寓 · 管理后台

Vue 3 + TypeScript + Vite + Element Plus 实现的公寓运营管理后台，覆盖公寓、房间、属性、租约、预约、租客和系统管理。

## 开发

```bash
npm ci
npm run dev
```

默认通过 Vite proxy 把 `/admin` 转发到 `http://localhost:8080`，可在 `.env.development` 中修改。

## 构建

```bash
npm run build
```

产物输出到 `dist/`，由 Nginx 托管；`/admin` 反向代理到管理端后端。

## 目录

- `src/api`：接口定义
- `src/views`：业务页面
- `src/layouts`：后台布局
- `src/components`：通用组件
- `nginx.conf`：生产环境 Nginx 配置
