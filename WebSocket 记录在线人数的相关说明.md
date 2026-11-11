# WebSocket 记录在线人数的相关说明

## 1. 功能概述

项目中的 WebSocket 在线人数功能，主要用于**实时统计并推送不同身份用户的在线数量**，包括：

- 管理后台在线人数
- 博客前台登录用户在线人数
- 游客在线人数

该功能基于 **STOMP + WebSocket + Redis** 实现，核心思路是：

1. 用户建立 WebSocket 连接时进行身份识别与会话登记；
2. 前台与后台分别连接不同的 WebSocket 端点，避免来源混淆；
3. 通过 Redis 维护在线用户、在线会话的映射关系；
4. 用户连接/断开时动态更新在线人数；
5. 当页面订阅在线人数主题时，服务端将最新人数主动推送给前端。

---

## 2. 相关代码位置

与该功能直接相关的核心类如下：

- `mengze-framework/src/main/java/com/mengze/interceptor/WebSocketAuthInterceptor.java`
- `mengze-framework/src/main/java/com/mengze/listener/WebSocketEventListener.java`
- `mengze-framework/src/main/java/com/mengze/service/OnlineUserService.java`
- `mengze-framework/src/main/java/com/mengze/service/impl/OnlineUserServiceImpl.java`
- `mengze-framework/src/main/java/com/mengze/constants/WebSocketConstants.java`
- `mengze-framework/src/main/java/com/mengze/constants/RedisKeyConstants.java`
- `mengze-framework/src/main/java/com/mengze/domain/vo/OnlineCountVo.java`

---

## 3. 功能运行流程

### 3.1 WebSocket 连接鉴权

在客户端发起 STOMP `CONNECT` 请求时，`WebSocketAuthInterceptor` 会拦截连接请求，并根据请求头中的 `token` 判断当前连接身份。

#### 处理逻辑

- 如果携带了 `token`：
  - 解析 JWT 获取 `userId`；
  - 优先从后台登录缓存中读取用户信息；
  - 如果后台没有，再从前台登录缓存中读取；
  - 成功后，将当前连接标记为 `admin` 或 `blog` 用户。

- 如果没有携带 `token`：
  - 视为游客连接；
  - 如果请求头带有 `visitorId`，则直接复用；
  - 如果没有，则服务端生成一个新的 `visitorId`；
  - 将当前连接标记为 `visitor`。

#### 端点区分补充

当前项目已经将 WebSocket 连接入口拆分为两个地址：

- 前台：`/ws/blog`
- 后台：`/ws/admin`

握手阶段会把端点类型写入会话属性，随后在 `CONNECT` 阶段做进一步校验：

- 访问 `/ws/admin` 时，只允许后台用户连接；
- 访问 `/ws/blog` 时，只允许前台用户连接；
- 游客访问前台端点时仍按游客处理。

#### 作用

这一步的目的不仅是做“登录校验”，还为后续统计在线人数提供了**用户身份标签**，同时通过端点隔离避免前台和后台的连接混淆，方便区分不同类型的在线人数。

---

### 3.2 连接成功后记录在线状态

在 `WebSocketEventListener` 中监听 `SessionConnectEvent`：

- 如果是登录用户，则调用 `online(sessionId, userId, loginType)`；
- 如果是游客，则调用 `online(sessionId, visitorId, visitor)`。

#### 记录内容

`OnlineUserServiceImpl.online(...)` 会将以下关系写入 Redis：

1. **全局会话映射**
   - key：`ws:online:sessions`
   - 记录：`sessionId -> userId:loginType`

2. **用户会话集合**
   - key：`ws:online:user:sessions:{loginType}:{userId}`
   - 记录：某个用户当前持有的所有 WebSocket 会话 `sessionId`

3. **在线用户集合**
   - key：`ws:online:users:{loginType}`
   - 记录：某种身份下的在线用户 ID 集合

#### 这样设计的好处

- 支持一个用户多端同时在线；
- 通过集合实现天然去重；
- 断开连接时可以准确判断“用户是否真正离线”；
- 可以按身份类型分别统计在线人数。

---

### 3.3 订阅在线人数时主动推送

在 `WebSocketEventListener` 中监听 `SessionSubscribeEvent`：

当用户订阅以下主题时，服务端会立即推送当前最新在线人数：

- 管理后台：`/topic/admin/online-count`
- 博客前台：`/topic/blog/online-count`
- 游客：`/topic/blog/visitor/online-count`

> 注意：前台与后台现在只是 WebSocket 连接入口不同，在线人数的订阅主题仍然按业务身份区分，因此前端页面只需要订阅对应主题即可。

对应调用：

- `pushOnlineCount("admin")`
- `pushOnlineCount("blog")`
- `pushOnlineCount("visitor")`

#### 作用

这意味着前端页面一旦进入并订阅该主题，就能立刻拿到最新在线人数，而不需要等下一次连接变更事件。

---

### 3.4 断开连接后更新在线人数

在 `WebSocketEventListener` 中监听 `SessionDisconnectEvent`：

- 根据 `sessionId` 调用 `offline(sessionId)`；
- 删除当前会话对应的 Redis 记录；
- 从用户会话集合中移除该会话；
- 如果该用户已经没有其他活跃会话，则从在线用户集合中移除该用户；
- 最后重新计算并推送最新在线人数。

#### 为什么要先判断“是否还有其他会话”

因为一个用户可能同时在多个浏览器窗口、多个设备上保持 WebSocket 连接。

例如：

- 用户 A 在电脑和手机上都打开了页面；
- 电脑端断开时，不能直接认为用户离线；
- 只有当该用户所有会话都断开，才算真正离线。

这也是“用户集合 + 会话集合”双层设计的原因。

---

## 4. Redis 结构说明

### 4.1 在线用户统计 Key

`RedisKeyConstants` 中定义了以下 WebSocket 相关 Key：

- `WS_ONLINE_USERS_KEY_PREFIX = "ws:online:users:"`
- `WS_ONLINE_SESSIONS_KEY = "ws:online:sessions"`
- `WS_ONLINE_USER_SESSIONS_KEY_PREFIX = "ws:online:user:sessions:"`

### 4.2 含义说明

#### 1）`ws:online:sessions`

全局 session 映射表，用于保存：

- `sessionId -> userId:loginType`

作用：

- 连接断开时快速定位该 session 属于哪个用户、哪个身份类型。

#### 2）`ws:online:user:sessions:{loginType}:{userId}`

用户会话集合，用于保存：

- 某个用户对应的所有 WebSocket sessionId

作用：

- 判断该用户是否仍有其他在线会话。

#### 3）`ws:online:users:{loginType}`

按身份分类的在线用户集合，用于保存：

- 当前在线的 userId

作用：

- 用集合大小直接计算在线人数；
- 天然去重，不会因为重复连接而重复计数。

---

## 5. 在线人数计算方式

`OnlineUserServiceImpl.getOnlineCount(String loginType)` 的实现逻辑是：

1. 根据登录类型获取对应的在线用户集合；
2. 读取 Redis Set 中的元素数量；
3. 组装成 `OnlineCountVo(count, timestamp)` 返回。

### 返回对象

`OnlineCountVo` 包含两个字段：

- `count`：当前在线人数
- `timestamp`：服务端生成的时间戳

#### 时间戳的作用

前端可以用它来判断数据是否为最新推送，也可以用于页面渲染时显示更新时间。

---

## 6. 在线人数推送方式

服务端通过 `SimpMessagingTemplate.convertAndSend(...)` 主动向指定主题发送消息。

### 对应主题

- 管理后台在线人数：`/topic/admin/online-count`
- 博客前台在线人数：`/topic/blog/online-count`
- 游客在线人数：`/topic/blog/visitor/online-count`

### 推送内容

推送的数据是 `OnlineCountVo`，即：

```json
{
  "count": 12,
  "timestamp": 1710000000000
}
```

---

## 7. 身份分类说明

项目将 WebSocket 在线用户分为三类：

### 7.1 管理后台用户

- 登录类型：`admin`
- WebSocket 端点：`/ws/admin`
- 在线人数主题：`/topic/admin/online-count`
- Redis Key 前缀：`adminLogin:`

### 7.2 博客前台登录用户

- 登录类型：`blog`
- WebSocket 端点：`/ws/blog`
- 在线人数主题：`/topic/blog/online-count`
- Redis Key 前缀：`blogLogin:`

### 7.3 游客

- 登录类型：`visitor`
- 一般通过前台端点 `/ws/blog` 连接
- 在线人数主题：`/topic/blog/visitor/online-count`
- 不依赖 token 登录
- 通过 `visitorId` 标识匿名会话

---

## 8. 该实现的特点

### 8.1 支持多端同时在线

同一用户可在多个端保持连接，不会因为重复连接导致人数统计错误。

### 8.2 支持按身份分类统计

后台、前台、游客分别统计，便于前端按场景展示。

### 8.3 断线后自动清理

连接断开时会同步清理：

- session 映射
- 用户会话集合
- 在线用户集合

避免 Redis 中长期残留脏数据。

### 8.4 订阅即推送

用户订阅主题后即可获得最新在线人数，前端体验更实时。

---

## 9. 可能的前端使用方式

前端页面一般会：

1. 建立 STOMP WebSocket 连接；
2. 根据页面类型选择对应端点：前台用 `/ws/blog`，后台用 `/ws/admin`；
3. 订阅对应在线人数主题；
4. 接收 `OnlineCountVo` 数据；
5. 实时刷新页面展示的在线人数。

不同页面订阅不同主题，例如：

- 后台管理页面连接 `/ws/admin`，订阅后台在线人数主题；
- 博客首页连接 `/ws/blog`，订阅前台在线人数主题；
- 游客访问页面通常也连接 `/ws/blog`，订阅游客在线人数主题。

---

## 10. 总结

这个 WebSocket 在线人数功能的核心，是通过 **双端点连接隔离 + 连接鉴权 + Redis 会话管理 + 订阅推送** 的组合，实现了一个可区分身份、可多端在线、可实时更新的在线人数统计方案。

其关键设计点包括：

- 使用 `WebSocketConfig` 将前台和后台连接入口拆分为不同端点；
- 使用 `WebSocketAuthInterceptor` 在连接阶段识别身份，并校验连接端点是否匹配；
- 使用 `WebSocketEventListener` 在连接、订阅、断开时驱动在线状态变化；
- 使用 Redis 的 Set 和 Hash 结构维护在线用户及会话；
- 使用 `SimpMessagingTemplate` 向前端实时广播在线人数。

这样既能保证在线人数统计准确实时，也能避免前台和后台连接混淆，适合博客和后台管理等场景使用。
