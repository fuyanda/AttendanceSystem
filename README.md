# AttendanceSystem

考勤 / 人脸识别相关 monorepo。

## 目录

| 目录 | 说明 |
|------|------|
| `face-admin` | 管理后台 API（Spring Boot） |
| `face-web` | 管理后台前端（Vue3 + Vite） |
| `face-parent-init` | 家长 H5（绑定 / 查询） |
| `face-teacher` | 教师端 UniApp |
| `face-service` | 本地人脸识别服务（Python） |

## 本地敏感配置（勿提交）

克隆后按示例复制并填写真实密钥 / 域名：

```bash
# 管理后台
cp face-admin/src/main/resources/application-local.yml.example \
   face-admin/src/main/resources/application-local.yml

# 教师端域名
cp face-teacher/shared/config/host.local.js.example \
   face-teacher/shared/config/host.local.js

# 家长 H5
cp face-parent-init/.env.example face-parent-init/.env.production.local

# 管理前端家长 H5 域名（按环境）
echo 'VITE_PARENT_H5_BASE = https://student.example.com' > face-web/.env.development.local
```

`face-admin` 的 `spring.profiles.active` 需包含 `local`（仓库内已设为 `druid,local`）。

## 忽略的本机环境

根目录 `.gitignore` 已排除：`node_modules`、`target`、`.venv`、`uploadPath`、`.env`、`application-local.yml`、`host.local.js`、IDE 与 Cursor 本地目录等。
