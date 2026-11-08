# 寓见公寓 · 租客端 H5

Vue 3 + TypeScript + Vite + Vant 4 实现的移动端租房页面，包含公寓/房间浏览、搜索、看房预约、租约、浏览历史和个人中心。

## 开发

```bash
npm ci
npm run dev
```

默认通过 Vite proxy 把 `/app` 转发到 `http://localhost:8081`，可在 `.env.development` 中修改。

## 构建

```bash
npm run build
```

产物输出到 `dist/`，由 Nginx 托管；`/app` 反向代理到用户端后端。

## 说明

UI 基础来自 MIT 协议的 [vue3-h5-template](https://github.com/yulimchen/vue3-h5-template)，原始许可证见 [LICENSE](LICENSE)。业务页面与接口联调代码属于本项目。
