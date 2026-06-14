# 助农商城

基于 Spring Boot + Vue 3 的农产品电商平台，采用前后端分离架构。项目包含用户端商城、管理端后台、订单评价、对象存储上传、AI 购物助手、首页轮播图管理和数据统计等功能。

## 功能模块

- 用户端：注册登录、商品浏览、分类筛选、商品详情、购物车、下单、我的订单、收藏、订单评价、个人信息维护。
- 管理端：首页概览、用户管理、分类管理、商品管理、订单管理、轮播图管理。
- 数据统计：用户数、商品数、订单数、分类数、近 7 日销售额、订单状态、用户增长、热销商品、营收统计。
- 文件上传：支持商品图、分类图标、轮播图、评价图上传到对象存储，并限制图片格式。
- AI 助手：接入 DeepSeek API，用于商品推荐和购物咨询；未配置 API Key 时可不启用。
- 缓存：Redis 缓存分类和热门商品；Redis 不可用时回退到数据库查询。

## 技术栈

- 后端：Spring Boot 3.2、Spring Security、MyBatis、MySQL、Redis、JWT、Lombok。
- 前端：Vue 3、Vue Router、Pinia、Element Plus、Axios、ECharts、Vite。
- 运行环境：JDK 17+、Node.js 18+、MySQL 8.0+、Maven 3.6+。

## 目录结构

```text
agricultural-products/
├── backend/                         # Spring Boot 后端
├── frontend/                        # Vue 3 前端
├── docs/                            # 课程作业要求文档
├── database.sql                     # 主数据库脚本
├── database_extension.sql           # 收藏、评价等扩展脚本
├── database_review_extension.sql    # 评价回复与点赞扩展脚本
├── database_optimization.sql        # 索引、外键、唯一约束优化脚本
└── README.md
```

## 数据库初始化

先启动 MySQL，然后按顺序导入：

```bash
mysql -u root -p < database.sql
mysql -u root -p agricultural_products < database_extension.sql
mysql -u root -p agricultural_products < database_review_extension.sql
mysql -u root -p agricultural_products < database_optimization.sql
```

`database_optimization.sql` 用于补充外键、索引、唯一约束和检查约束，只需要在初始化后执行一次。

## 后端配置

后端配置文件位于 `backend/src/main/resources/application.yml`。敏感信息通过环境变量注入：

```bash
DB_URL=jdbc:mysql://localhost:3306/agricultural_products?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true&useSSL=false
DB_USERNAME=root
DB_PASSWORD=你的数据库密码
REDIS_PASSWORD=你的 Redis 密码
JWT_SECRET=请替换成足够长的随机字符串
ZOS_ACCESS_KEY=对象存储 AccessKey
ZOS_SECRET_KEY=对象存储 SecretKey
DEEPSEEK_API_KEY=DeepSeek 接口 Key
```

对象存储和 AI 助手不配置时，不影响基础商城功能。

## 启动项目

后端：

```bash
cd backend
mvn spring-boot:run
```

默认地址：`http://localhost:8080`

前端：

```bash
cd frontend
npm install
npm run dev
```

默认地址：`http://localhost:5174`

## 构建验证

```bash
cd backend
mvn test

cd ../frontend
npm run build
```

## 测试账号

| 角色 | 用户名 | 密码 | 说明 |
| --- | --- | --- | --- |
| 管理员 | admin | admin | 可访问管理后台 |
| 普通用户 | user1 | 123456 | 可演示购物、订单和评价流程 |

## 已做的后端优化

- 将订单创建、支付、取消、确认收货等复杂业务下沉到 Service 层。
- 修复购物车、用户信息、订单访问中的越权风险。
- 下单扣库存检查数据库更新结果，库存不足时回滚事务。
- 统计接口限制为管理员访问。
- 新增统一业务异常和更合理的错误码。
- 数据库增加外键、索引、唯一约束和检查约束。
- 登录、注册、修改密码接口改用 DTO，并接入参数校验。
- 上传接口增加图片格式白名单。
- 新增订单服务单元测试，覆盖下单和库存失败场景。

## 报告可写重点

- 前后端分离架构：Vue 调用 Spring Boot REST API。
- 权限认证：JWT + Spring Security，区分普通用户和管理员。
- 数据库设计：用户、分类、商品、购物车、订单、订单明细、收藏、评价、轮播图。
- 业务流程：注册登录、浏览商品、加入购物车、下单、后台发货、用户确认收货、订单评价。
- 工程优化：BCrypt 密码加密、对象存储上传、Redis 缓存回退、真实数据库统计、事务一致性、权限校验、单元测试。
