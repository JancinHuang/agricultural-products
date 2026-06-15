# 前端 UI 架构约定

## 目标

前端允许后续替换组件库，也允许大规模 UI/UX 改版。为降低改造成本，业务页面不应直接承担复杂样式、组件库服务调用和字段适配。

## 分层边界

- `views/`：页面容器，只负责路由上下文、页面组合和少量页面级状态。
- `components/base/`：基础 UI 能力，优先封装可复用的按钮组、分页、预览、空状态、表格操作等。
- `components/admin/`：后台业务组件，承载后台表单、CRUD 页面片段、后台导航。
- `components/shop/`：商城业务组件，承载商品卡片、订单卡片、购物车条目、商城导航等。
- `components/business/`：跨后台和商城的业务组件，例如登录、个人资料、AI 会话、评价编辑。
- `composables/`：可复用业务流程，例如 CRUD、上传、购物车、订单、收藏、评价。
- `services/`：适配层。后端字段、图片 URL、UI 提示服务、组件库服务都应优先从这里隔离。
- `constants/`：状态、导航、接口端点、选项枚举。
- `assets/styles/`：主题 token、布局和通用样式。页面内只保留局部布局。

## 组件库替换策略

当前组件库是 Element Plus。后续如果替换组件库，优先按下面顺序处理：

1. 替换 `services/uiFeedback.js` 中的消息提示和确认弹窗实现。
2. 替换 `components/base/` 中的基础组件实现。
3. 替换 `components/admin/` 和 `components/shop/` 中仍直接使用 Element Plus 的业务组件。
4. 最后处理 `views/` 中少量残留的 Element Plus 表格、表单、弹窗。

避免在新增 composable 中直接引入 `element-plus`。需要提示、确认、错误反馈时，使用 `services/uiFeedback.js`。

## 样式改版策略

大改 UI/UX 时，优先修改：

1. `assets/styles/tokens.css`：颜色、字号、间距、圆角、阴影。
2. `assets/styles/layout.css`：页面布局、容器宽度、响应式节奏。
3. `assets/styles/components.css`：通用组件视觉。
4. 业务组件内 scoped 样式。

不建议直接在页面里堆大段视觉样式。新视觉应沉淀到 token、基础组件或业务组件中。

## 新代码约定

- 新增业务流程优先写 composable。
- 新增重复 UI 优先写组件，不在页面复制模板。
- 新增提示/确认优先走 `notify`、`confirmAction`、`confirmDanger`。
- 新增图片展示优先走 `imageUtils` 或领域适配层，避免页面直接拼 URL。
- 新增接口字段处理优先走 `services/domainAdapters.js`。
