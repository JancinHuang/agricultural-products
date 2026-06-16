# 助农商城

基于 Spring Boot 3 + Vue 3 的农产品电商平台，采用前后端分离架构。系统包含用户端商城、管理员后台、购物车、订单、收藏评价、图片上传、数据统计、首页轮播图管理和 AI 购物助手等功能，适合作为 Web 应用课程设计、毕业设计原型或农产品电商系统学习项目。

## 项目功能

### 用户端

- 注册、登录、登录状态校验和个人信息维护
- 首页轮播图、分类入口、热门商品展示
- 商品浏览、关键词搜索、分类筛选、价格筛选、排序
- 商品详情、商品收藏、评价列表、评分统计
- 购物车添加、数量修改、选中结算、批量删除
- 创建订单、支付模拟、取消订单、确认收货、我的订单
- 商品评价、评价图片上传、评价回复与点赞
- AI 购物助手，支持基于当前商城商品数据的推荐和咨询

### 管理端

- 首页概览与数据统计
- 用户管理：分页查询、新增、编辑、删除、启用/禁用
- 分类管理：分类名称、描述、图标、排序、状态维护
- 商品管理：商品分类、价格、库存、单位、产地、图片、上下架管理
- 订单管理：订单查询、详情查看、状态维护
- 轮播图管理：图片、标题、副标题、按钮、跳转链接和展示样式维护

### 支撑能力

- JWT 无状态认证与 Spring Security 权限控制
- BCrypt 密码加密
- MyBatis + MySQL 数据持久化
- Redis 缓存分类列表和热门商品，Redis 不可用时回退数据库查询
- S3 兼容对象存储上传商品图、分类图标、轮播图和评价图
- ECharts 后台图表统计
- 统一响应、分页结果、业务异常处理

## 技术栈

| 层级 | 技术 |
| --- | --- |
| 后端 | Java 17、Spring Boot 3.2、Spring Security、MyBatis、MySQL、Redis、JWT、Lombok、AWS SDK S3 |
| 前端 | Vue 3、Vue Router、Pinia、Element Plus、Axios、ECharts、Vite |
| 数据库 | MySQL 8.0+ |
| 可选服务 | Redis、S3 兼容对象存储、DeepSeek API |

## 环境要求

请先安装以下环境：

- JDK 17+
- Maven 3.6+
- Node.js 18+
- npm 9+
- MySQL 8.0+
- Redis 6+，可选

可用以下命令检查本机环境：

```bash
java -version
mvn -v
node -v
npm -v
mysql --version
redis-server --version
```

## 目录结构

```text
agricultural-products/
├── backend/                         # Spring Boot 后端
│   ├── src/main/java/               # Java 源码
│   ├── src/main/resources/
│   │   ├── application.yml          # 后端配置
│   │   ├── agricultural_products.sql# 数据库初始化脚本
│   │   └── mapper/                  # MyBatis XML
│   └── pom.xml
├── frontend/                        # Vue 3 前端
│   ├── src/
│   │   ├── api/                     # 接口封装
│   │   ├── components/              # 组件
│   │   ├── composables/             # 复用业务逻辑
│   │   ├── router/                  # 路由
│   │   ├── store/                   # Pinia 状态
│   │   └── views/                   # 页面
│   ├── package.json
│   └── vite.config.js
├── docs/                            # 课程资料与项目报告
└── README.md
```

## 快速开始

### 1. 克隆项目

```bash
git clone <your-repository-url>
cd agricultural-products
```

### 2. 初始化数据库

先启动 MySQL，然后导入数据库脚本：

```bash
mysql -u root -p < backend/src/main/resources/agricultural_products.sql
```

该脚本会创建 `agricultural_products` 数据库并写入演示数据。

如果你的 MySQL 用户不是 `root`，请替换命令中的用户名。导入后可以用以下命令确认：

```bash
mysql -u root -p -e "USE agricultural_products; SHOW TABLES;"
```

### 3. 配置后端环境变量

后端配置文件位于 `backend/src/main/resources/application.yml`。建议使用环境变量注入敏感配置。

必需配置：

```bash
DB_URL=jdbc:mysql://localhost:3306/agricultural_products?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true&useSSL=false
DB_USERNAME=root
DB_PASSWORD=你的数据库密码
JWT_SECRET=请替换成足够长的随机字符串
```

可选配置：

```bash
REDIS_PASSWORD=你的 Redis 密码
ZOS_ACCESS_KEY=对象存储 AccessKey
ZOS_SECRET_KEY=对象存储 SecretKey
DEEPSEEK_API_KEY=DeepSeek API Key
```

说明：

- Redis 未启动时，分类和热门商品会直接查询数据库，基础商城功能不受影响。
- 对象存储未配置时，涉及上传和预签名图片访问的功能不可用；已有图片 Key 会按原值返回。
- DeepSeek API Key 未配置时，AI 助手会使用本地商品数据生成兜底回复。

#### Windows PowerShell 示例

```powershell
$env:DB_USERNAME="root"
$env:DB_PASSWORD="你的数据库密码"
$env:JWT_SECRET="please-change-this-to-a-long-random-secret"
```

#### macOS / Linux 示例

```bash
export DB_USERNAME=root
export DB_PASSWORD=你的数据库密码
export JWT_SECRET=please-change-this-to-a-long-random-secret
```

### 4. 启动后端

```bash
cd backend
mvn spring-boot:run
```

默认地址：

```text
http://localhost:8080
```

### 5. 启动前端

另开一个终端：

```bash
cd frontend
npm install
npm run dev
```

默认地址：

```text
http://localhost:5174
```

访问前端地址后，登录不同账号即可进入用户端或管理端。

## 测试账号

| 角色 | 用户名 | 密码 | 说明 |
| --- | --- | --- | --- |
| 管理员 | `admin` | `admin` | 登录后进入后台管理端 |
| 普通用户 | `user1` | `123456` | 可演示购物、订单、收藏和评价流程 |

## 常用命令

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

### 前端预览构建结果

```bash
cd frontend
npm run preview
```

## API 与权限说明

- 公开接口：登录、注册、商品列表、商品详情、分类列表、轮播图列表、商品评价、AI 聊天等。
- 登录接口会返回 JWT Token，前端后续请求通过 `Authorization: Bearer <token>` 携带。
- 管理端统计接口 `/api/statistics/**` 需要管理员权限。
- 购物车、订单、收藏、评价等用户数据以当前登录用户 ID 为准，后端会进行归属校验。

## 配置项说明

| 配置项 | 是否必需 | 说明 |
| --- | --- | --- |
| `DB_URL` | 否 | 数据库连接地址，有默认本地地址 |
| `DB_USERNAME` | 否 | 数据库用户名，默认 `root` |
| `DB_PASSWORD` | 是 | 数据库密码，建议通过环境变量配置 |
| `JWT_SECRET` | 是 | JWT 签名密钥，生产环境必须替换 |
| `REDIS_PASSWORD` | 否 | Redis 密码，没有密码可不配置 |
| `ZOS_ACCESS_KEY` | 否 | 对象存储 AccessKey |
| `ZOS_SECRET_KEY` | 否 | 对象存储 SecretKey |
| `DEEPSEEK_API_KEY` | 否 | AI 购物助手 API Key |

## 项目截图

截图可按需补充：

- 首页
- 商品列表
- 商品详情
- 购物车
- 我的订单
- AI 购物助手
- 管理后台首页
- 商品管理
- 订单管理

## 后续优化方向

- 接入真实支付沙箱和支付回调
- 增加物流单号、配送轨迹和售后退款
- 扩展商家角色和更细粒度 RBAC 权限
- 增加农产品溯源、产地认证和品牌专题页
- 完善移动端适配和图片懒加载
- 增加更多集成测试和端到端测试

## License

本项目用于课程设计与学习交流。若用于公开展示或二次开发，请根据实际情况补充 License。
