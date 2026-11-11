# Yun Blog Java 后端

基于 Spring Boot 2.5.0 + MyBatis-Plus 的前后端分离个人博客系统后端。项目采用 Maven 多模块结构，包含前台接口服务 `mengze-blog`（端口 `1314`）、后台管理接口服务 `mengze-admin`（端口 `7616`），以及公共模块 `mengze-framework`。

> 说明：本仓库是**后端工程**，不包含前台博客页面和后台管理页面。前台/后台页面需要由独立的前端项目构建并部署；本 README 中的命令默认都在项目根目录执行。

## 1. 项目简介与功能特性

`yun-blog-java` 提供一套较完整的个人博客后端能力，主要包括：

- **文章与内容管理**：文章新增、编辑、删除、查询、详情展示、浏览量统计、文章搜索、按分类/标签筛选、文章归档、文章与标签关联。
- **分类与标签**：分类和标签的增删改查、文章数量统计、前台展示。
- **评论互动**：文章评论、友链评论、留言板评论、评论回复、评论点赞、头像生成，以及本地敏感词库 + 开源词库 + AI 敏感词检测。
- **用户与权限**：用户注册、登录、登出、JWT 鉴权、获取当前用户信息、邮箱/手机验证码登录、忘记密码与重置密码、角色/菜单/部门权限管理。
- **站点运营**：公告发布与已读状态、友链申请与审核、系统配置管理、PV/UV/在线人数统计、访问日志、趋势统计。
- **文件与上传**：用户头像、文章图片上传，文件元数据管理，七牛云对象存储。
- **扩展能力**：WebSocket 在线人数与实时通知、天气查询与 IP 定位、EasyExcel 数据导出、图形/行为验证码。
- **后台管理**：用户、角色、菜单、部门、字典、文件、日志、通知、友链、评论、分类、标签、文章、配置、统计、天气等管理接口。

## 2. 技术栈

| 类别 | 技术/组件 |
| --- | --- |
| 开发语言 | Java 17 |
| 应用框架 | Spring Boot 2.5.0、Spring MVC、Spring AOP、Bean Validation |
| 安全认证 | Spring Security、JWT（JJWT 0.9.1）、BCrypt 密码加密 |
| 数据访问 | MyBatis-Plus 3.5.3、MySQL 8.x |
| 缓存/状态 | Redis 6+、Spring Data Redis |
| 实时通信 | Spring WebSocket |
| 接口文档 | Springfox Swagger 2.9.2 |
| 邮件 | Spring Boot Mail（QQ 邮箱 SMTP） |
| 对象存储 | 七牛云 OSS SDK 7.19.x |
| 短信 | 阿里云号码认证服务 `dypnsapi20170525` 2.0.0 |
| 天气 | 和风天气 API，JWT（EdDSA/Ed25519）认证 |
| AI 能力 | DeepSeek Spring Boot Starter 1.5.0、deepseek4j-core 1.5.0 |
| 其他工具 | Lombok、Fastjson 1.2.83、Gson、Guava 32.1.3-jre、EasyExcel 4.0.3、ip2region 3.3.6、sensitive-word 0.29.5、TIANAI-CAPTCHA 1.5.5 |
| 构建 | Maven 多模块，`spring-boot-maven-plugin` 2.5.0（`mengze-admin`、`mengze-blog` 执行 `repackage`） |

## 3. 项目结构

```text
yun-blog-java/
├─ pom.xml                         # 根 POM，聚合三个子模块并统一版本
├─ .env.template                   # 环境变量模板
├─ sql/                            # 数据库表结构与可选初始化数据
├─ mengze-framework/               # 公共模块：实体、Mapper、Service、工具类、安全/Redis/WebSocket 配置
├─ mengze-admin/                   # 后台管理接口服务
│  └─ src/main/resources/
│     ├─ application.yml.template
│     └─ application.yml           # 由模板复制生成，已被 .gitignore 忽略
└─ mengze-blog/                    # 前台博客接口服务
   └─ src/main/resources/
      ├─ application.yml.template
      └─ application.yml           # 由模板复制生成，已被 .gitignore 忽略
```

各模块说明：

| 模块 | 作用 | 启动类 | 端口 | 构建产物 |
| --- | --- | --- | --- | --- |
| `mengze-framework` | 公共基础模块，提供实体、DTO/VO、Mapper、Service 实现、统一响应、异常处理、安全认证、Redis、WebSocket、文件上传、天气、统计等公共能力；没有启动类，也不打可执行 jar | — | — | `mengze-framework-1.0-SNAPSHOT.jar` |
| `mengze-admin` | 后台管理端接口服务 | `com.mengze.MengZeBlogAdminApplication` | `7616` | `mengze-admin/target/mengze-admin-1.0-SNAPSHOT.jar` |
| `mengze-blog` | 前台博客端接口服务 | `com.mengze.MengZeBlogApplication` | `1314` | `mengze-blog/target/mengze-blog-1.0-SNAPSHOT.jar` |

补充说明：

- 根 `pom.xml` 的 `<packaging>` 为 `pom`，通过 `<modules>` 聚合 `mengze-framework`、`mengze-admin`、`mengze-blog` 三个模块，并在 `<dependencyManagement>` 中导入 Spring Boot 2.5.0 BOM。
- `mengze-admin` 和 `mengze-blog` 都依赖 `mengze-framework`；两个启动模块只负责各自的启动类、`SecurityConfig`、`SwaggerConfig` 等模块级配置。
- `mengze-admin` 与 `mengze-blog` 都配置了 `spring-boot-maven-plugin` 的 `repackage` 目标，因此可以生成可直接通过 `java -jar` 启动的可执行 jar。
- 两个模块的 `application.yml.template` 是提交到仓库的配置模板；真实的 `application.yml` 被 `.gitignore` 忽略，需要按第 5 节自行复制生成。

### 3.1 数据库说明

- 数据库名：`yun_blog`
- 默认连接地址：`jdbc:mysql://localhost:3306/yun_blog?characterEncoding=utf8&serverTimezone=Asia/Shanghai`
- 仓库提供两份 SQL 脚本，均位于 `sql/` 目录：
  - `sql/yun_blog_schema.sql`：MySQL 8.0 表结构（仅结构，不含业务数据）；脚本包含 `DROP TABLE`，已有数据的数据库请先备份。
  - `sql/yun_blog_seed.sql`：可选的最小初始化数据，包含角色、管理员账号 `admin / Admin@123456`、后台菜单及角色菜单关系；首次登录后请立即修改密码。
- 两份脚本均使用 `utf8mb4`，适用于 MySQL 8.x；表结构以仓库脚本为准。

### 3.2 项目内其他文档

- [项目文档.md](项目文档.md)：功能模块、技术栈与代码结构的补充说明
- [前台接口文档.md](前台接口文档.md)：前台接口说明
- [后台接口地址汇总.md](后台接口地址汇总.md)：后台接口地址汇总
- [后台接口文档1.md](后台接口文档1.md)、[后台接口文档2.md](后台接口文档2.md)、[后台接口文档3.md](后台接口文档3.md)、[后台接口文档4.md](后台接口文档4.md)：后台接口详细文档
- [WebSocket 记录在线人数的相关说明.md](<WebSocket 记录在线人数的相关说明.md>)：WebSocket 在线人数说明

## 4. 环境要求

| 软件/组件 | 版本要求 | 说明 |
| --- | --- | --- |
| JDK | **17** | 根 POM 通过 `maven.compiler.source/target=17` 指定；请确保 `java -version` 与 `JAVA_HOME` 指向 JDK 17 |
| Maven | **3.8+** | 用于构建多模块工程，确保 `mvn -version` 可正常执行 |
| MySQL | **8.x** | 默认地址 `localhost:3306`，数据库名 `yun_blog` |
| Redis | **6+** | 默认地址 `localhost:6379`，用于验证码、登录状态、统计、配置缓存等 |
| 操作系统 | Windows 10/11、Linux、macOS | 本文同时给出 PowerShell 与 Bash 命令 |
| 可选工具 | IntelliJ IDEA + EnvFile 插件 | 在 IDE 中运行时可用 EnvFile 读取 `.env`；命令行运行仍需手动导出环境变量 |

端口要求：`1314`（前台接口）、`7616`（后台接口）需可被本机或反向代理访问；`3306`、`6379` 通常只需本机访问，不建议直接暴露到公网。

## 5. 快速开始

### 5.1 获取代码并进入项目根目录

```bash
git clone https://github.com/hgt12/yun-blog_java.git yun-blog-java
cd yun-blog-java
```

如果代码已经在本地，直接进入项目根目录即可。后续所有命令都默认在该目录执行。

### 5.2 创建数据库并导入表结构

先登录 MySQL 创建数据库（数据库名必须为 `yun_blog`，否则需要同步修改 `application.yml` 中的连接地址）：

```bash
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS yun_blog DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
```

然后导入表结构和可选初始化数据（在项目根目录执行）：

```bash
mysql -u root -p yun_blog < sql/yun_blog_schema.sql
# 可选：初始化管理员 admin / Admin@123456、角色和后台菜单
mysql -u root -p yun_blog < sql/yun_blog_seed.sql
```

Windows PowerShell 如果 `<` 重定向不可用，可以使用 MySQL 客户端的 `source` 命令（路径使用 `/`）：

```powershell
mysql -u root -p yun_blog -e "source sql/yun_blog_schema.sql"
mysql -u root -p yun_blog -e "source sql/yun_blog_seed.sql"
```

> 注意：`yun_blog_schema.sql` 内含 `DROP TABLE`，只适合新建数据库或确认可清空的开发库；对已有数据的生产库执行前必须先备份。`yun_blog_seed.sql` 会创建默认管理员 `admin / Admin@123456`，登录后请立即修改密码。

导入后可以检查数据库是否存在：

```bash
mysql -u root -p -e "SHOW DATABASES LIKE 'yun_blog';"
```

### 5.3 启动 MySQL 和 Redis

确保 MySQL 与 Redis 已启动，并确认端口与 `application.yml` 中的默认配置一致（MySQL `3306`、Redis `6379`）。

Windows（PowerShell，管理员权限；服务名可能因安装方式不同而变化）：

```powershell
net start MySQL80
net start Redis
```

Linux（以 systemd 为例）：

```bash
sudo systemctl start mysql
sudo systemctl start redis-server
```

检查连通性：

```bash
mysqladmin -u root -p ping
redis-cli ping
```

`mysqladmin` 正常应返回 `mysqld is alive`，`redis-cli ping` 正常应返回 `PONG`。

### 5.4 创建配置文件并填写 `.env`

1）复制环境变量模板：

Windows PowerShell：

```powershell
Copy-Item -Path ".env.template" -Destination ".env"
```

Linux/macOS：

```bash
cp .env.template .env
```

2）复制两个模块的 `application.yml` 模板（这一步不能省略，否则构建出的 jar 中没有配置文件）：

Windows PowerShell：

```powershell
Copy-Item -Path "mengze-admin\src\main\resources\application.yml.template" -Destination "mengze-admin\src\main\resources\application.yml"
Copy-Item -Path "mengze-blog\src\main\resources\application.yml.template" -Destination "mengze-blog\src\main\resources\application.yml"
```

Linux/macOS：

```bash
cp mengze-admin/src/main/resources/application.yml.template mengze-admin/src/main/resources/application.yml
cp mengze-blog/src/main/resources/application.yml.template mengze-blog/src/main/resources/application.yml
```

3）编辑 `.env`，按实际环境填写。变量说明如下：

| 变量名 | 是否必填 | 对应配置项 | 说明 |
| --- | --- | --- | --- |
| `SQL_USERNAME` | 是 | `spring.datasource.username` | MySQL 用户名 |
| `SQL_PASSWORD` | 是 | `spring.datasource.password` | MySQL 密码 |
| `SECURITY_USER_NAME` | 可选（模板默认值） | `spring.security.user.name` | 模板中的默认安全账号用户名 |
| `SECURITY_USER_PASSWORD` | 可选（模板默认值） | `spring.security.user.password` | 模板中的默认安全账号密码 |
| `JWT_SECRET` | 生产环境必填 | `JwtUtil.JWT_KEY`（系统环境变量） | JWT 签名密钥，必须为合法的 Base64 字符串；可用 `openssl rand -base64 32` 生成。不设置时会使用源码中的开发默认值，不可用于生产 |
| `QQ_EMAIL_USERNAME` | 使用邮件功能时必填 | `spring.mail.username` | QQ 邮箱地址，例如 `admin@example.com` |
| `QQ_EMAIL_KEY` | 使用邮件功能时必填 | `spring.mail.password` | QQ 邮箱 SMTP 授权码（**不是 QQ 登录密码**） |
| `OSS_ACCESS_KEY` | 使用文件上传时必填 | `oss.accessKey` | 七牛云 AccessKey |
| `OSS_SECRET_KEY` | 使用文件上传时必填 | `oss.secretKey` | 七牛云 SecretKey |
| `OSS_BUCKET_NAME` | 使用文件上传时必填 | `oss.bucket` | 七牛云存储空间名称 |
| `OSS_VISIT_DOMAIN` | 使用文件上传时必填 | `oss.domain` | 七牛云访问域名，按模板注释要求以 `/` 结尾 |
| `ALIYUN_ACCESS_KEY_ID` | 使用短信功能时必填 | `aliyun.sms.access-key-id` | 阿里云 AccessKey ID |
| `ALIYUN_ACCESS_KEY_SECRET` | 使用短信功能时必填 | `aliyun.sms.access-key-secret` | 阿里云 AccessKey Secret |
| `ALIYUN_SMS_SIGN_NAME` | 使用短信功能时必填 | `aliyun.sms.sign-name` | 阿里云短信签名，需在控制台申请 |
| `BLOG_FRONTEND_URL` | 建议填写 | `blog.frontend.url` | 前台站点地址，用于邮件中的链接；模板示例为 `http://localhost:3000` |
| `WEATHER_KEY_ID` | 使用天气功能时必填 | `weather.api.key-id` | 和风天气凭据 ID |
| `WEATHER_PROJECT_ID` | 使用天气功能时必填 | `weather.api.project-id` | 和风天气项目 ID |
| `WEATHER_API_HOST` | 使用天气功能时必填 | `weather.api.host` | 和风天气 API Host |
| `WEATHER_PRIVATE_KEY` | 使用天气功能时必填 | `weather.api.private-key` | 和风天气 Ed25519 私钥（PEM 格式，代码支持将字面 `\n` 转为换行） |
| `DEEPSEEK_API_KEY` | 使用 AI 敏感词检测时必填 | `deepseek.api-key` | DeepSeek API Key |

> **重要提示：**
> 1. 两个 `application.yml.template` 中的 `spring.security.user.name/password` 都已使用 `${SECURITY_USER_NAME:...}` / `${SECURITY_USER_PASSWORD:...}` 占位符，复制为 `application.yml` 后会由环境变量控制。
> 2. 当前 Java 代码的登录校验以数据库中的用户数据为准，`spring.security.user.*` 属于 Spring Security 的默认账号配置；实际可登录账号请使用 `sql/yun_blog_seed.sql` 初始化或自行在数据库中创建。
> 3. MySQL 连接地址（含数据库名、主机、端口）、Redis 连接信息、两个服务的端口都直接写在 `application.yml` 中，不在 `.env` 里。若数据库不是本机 `localhost:3306`、Redis 不是本机 `6379`，请修改对应 `application.yml`。
> 4. `.env` 和两个 `application.yml` 都已被 `.gitignore` 忽略，**不要将它们提交到 Git**，也不要把真实密钥发到公开仓库或聊天工具中。

### 5.5 导出环境变量（关键步骤）

**`.env` 不会被 Spring Boot 自动加载。** 根 `pom.xml` 中只有 `<spring-dotenv.version>4.0.0</spring-dotenv.version>` 这个属性，没有任何 `spring-dotenv` 依赖，也没有其他读取 `.env` 的组件。因此，使用命令行启动前必须在**同一个终端窗口**里导出变量。

Linux/macOS（Bash/Zsh）：

```bash
cd /path/to/yun-blog-java
set -a
source .env
set +a
```

检查变量是否已导出：

```bash
printenv SQL_USERNAME
printenv SQL_PASSWORD
```

Windows PowerShell（逐项设置，示例值请替换为你的真实配置）：

```powershell
$env:SQL_USERNAME = "your_sql_username"
$env:SQL_PASSWORD = "your_sql_password"
$env:SECURITY_USER_NAME = "your_security_username"
$env:SECURITY_USER_PASSWORD = "your_security_password"
$env:JWT_SECRET = "your_base64_jwt_secret"
$env:QQ_EMAIL_USERNAME = "admin@example.com"
$env:QQ_EMAIL_KEY = "your_qq_email_key"
$env:OSS_ACCESS_KEY = "your_access_key"
$env:OSS_SECRET_KEY = "your_secret_key"
$env:OSS_BUCKET_NAME = "your_bucket_name"
$env:OSS_VISIT_DOMAIN = "https://cdn.example.com/"
$env:ALIYUN_ACCESS_KEY_ID = "your_access_key_id"
$env:ALIYUN_ACCESS_KEY_SECRET = "your_access_key_secret"
$env:ALIYUN_SMS_SIGN_NAME = "your_sms_sign_name"
$env:BLOG_FRONTEND_URL = "http://localhost:3000"
$env:WEATHER_KEY_ID = "your_key_id"
$env:WEATHER_PROJECT_ID = "your_project_id"
$env:WEATHER_API_HOST = "your_api_host"
$env:WEATHER_PRIVATE_KEY = "your_weather_private_key"
$env:DEEPSEEK_API_KEY = "your_deepseek_api_key"
```

也可以在当前 PowerShell 窗口用下面的方式批量读取 `.env`（效果等同于逐项设置，适合本地开发）：

```powershell
Get-Content .env | ForEach-Object {
    if ($_ -match '^\s*#' -or $_ -match '^\s*$') { return }
    $name, $value = $_ -split '=', 2
    Set-Item -Path "Env:$($name.Trim())" -Value $value.Trim()
}
```

注意事项：

- 环境变量只在当前终端窗口有效；关闭窗口、重启 IDE 或新开终端后需要重新设置。
- 如果使用 IntelliJ IDEA 运行启动类，可以安装 **EnvFile** 插件，在 Run Configuration 中勾选项目根目录的 `.env`；或者在 Run Configuration 的 Environment variables 中逐项配置。仅把 `.env` 放在项目根目录而不配置 EnvFile 是不会生效的。
- `.env` 中如包含空格或 `#`，请按 shell 规则加引号；在 Linux 上如果文件是 Windows 换行符（CRLF），可先执行 `dos2unix .env`。

### 5.6 Maven 构建

在项目根目录执行：

```bash
mvn clean package -DskipTests
```

说明：

- 该命令会依次构建 `mengze-framework`、`mengze-admin`、`mengze-blog`，并在两个启动模块中通过 `spring-boot-maven-plugin` 生成可执行 jar。
- 跳过测试是因为测试环境未内置数据库/Redis 等依赖；如需运行测试，请先准备完整环境。
- 请确认在 5.4 已经把两个 `application.yml.template` 复制为 `application.yml`，否则打包出的 jar 中不会包含数据源等配置。
- 构建成功后默认产物为：
  - `mengze-admin/target/mengze-admin-1.0-SNAPSHOT.jar`
  - `mengze-blog/target/mengze-blog-1.0-SNAPSHOT.jar`
- 如果提示 `mvn` 不是命令，请安装 Maven 并配置 `PATH`，或使用 IntelliJ IDEA 的 Maven 工具窗口执行同样的 `clean package -DskipTests`。

### 5.7 启动两个服务

先按 5.5 导出环境变量，然后分别打开两个终端窗口，在**项目根目录**执行：

后台管理服务（端口 `7616`）：

```bash
java -jar mengze-admin/target/mengze-admin-1.0-SNAPSHOT.jar
```

前台博客服务（端口 `1314`）：

```bash
java -jar mengze-blog/target/mengze-blog-1.0-SNAPSHOT.jar
```

说明：

- 建议始终在项目根目录执行 `java -jar`，这样 `.env` 文件、日志路径和构建产物路径都保持统一。
- **需要再次强调：Spring Boot 不会自动读取项目根目录的 `.env`。** 真正生效的是第 5.5 节已经在当前终端中导出的环境变量；请确保“导出变量”和“执行 java -jar”发生在同一个终端（或同一个 IDE Run Configuration）中。
- `application.yml` 已打包进 jar，工作目录不会改变 Spring Boot 读取 classpath 配置的行为；工作目录主要影响相对路径的文件（如日志、上传临时文件）以及 .env 的查找习惯。
- 启动成功后可看到类似 `Started MengZeBlogAdminApplication`、`Started MengZeBlogApplication` 的日志。

检查端口是否监听：

Windows PowerShell：

```powershell
netstat -ano | findstr ":7616"
netstat -ano | findstr ":1314"
```

Linux：

```bash
ss -lntp | grep -E '7616|1314'
```

## 6. 访问地址与接口文档

| 服务 | 地址 | 说明 |
| --- | --- | --- |
| 前台博客接口 | http://localhost:1314 | 前台文章、分类、标签、评论、友链、用户、配置、统计、归档等接口 |
| 后台管理接口 | http://localhost:7616 | 后台登录、用户、角色、菜单、文章、评论、配置、统计、天气等管理接口 |
| 前台 Swagger UI | http://localhost:1314/swagger-ui.html | 前台接口在线文档 |
| 后台 Swagger UI | http://localhost:7616/swagger-ui.html | 后台接口在线文档 |

Swagger 说明：项目使用 Springfox 2.9.2。**启动后访问 `/swagger-ui.html`（本项目实测返回 200）；原始 OpenAPI JSON 可通过 `/v2/api-docs` 查看。** 两个模块的 `SecurityConfig` 都已放行 `/swagger-ui.html`、`/swagger-ui/**`、`/swagger-resources/**`、`/v2/api-docs`、`/webjars/**` 等路径。

> 本仓库只是后端接口服务。直接浏览器打开 `http://localhost:1314` 或 `http://localhost:7616` 的根路径时，如果没有映射首页，出现 404 或空白页属于正常现象；请访问具体接口或 Swagger UI。前台页面需另行部署前端工程。

### 6.1 主要控制器基础路径

前台 `mengze-blog`：

| 基础路径 | 功能 |
| --- | --- |
| `/article` | 文章列表、详情等 |
| `/category` | 分类相关接口 |
| `/comment` | 评论相关接口 |
| `/links` | 友链相关接口 |
| `/tag` | 标签相关接口 |
| `/user` | 前台用户相关接口 |
| `/config` | 站点配置相关接口 |
| `/statistics` | 访问统计相关接口 |
| `/archive` | 文章归档相关接口 |

后台 `mengze-admin`：

| 基础路径 | 功能 |
| --- | --- |
| `/auth` | 后台登录、验证码、Token 刷新、找回密码 |
| `/users` | 后台用户管理 |
| `/posts` | 文章管理 |
| `/categories` | 分类管理 |
| `/comments` | 评论管理 |
| `/links` | 友链管理 |
| `/tags` | 标签管理 |
| `/configs` | 系统配置管理 |
| `/depts` | 部门管理 |
| `/dicts` | 字典管理 |
| `/files` | 文件管理 |
| `/logs` | 操作日志 |
| `/menus` | 菜单管理 |
| `/notices` | 公告管理 |
| `/roles` | 角色管理 |
| `/statistics` | 统计管理 |
| `/weather` | 天气查询（方法路径 `/weather/now`） |

此外，前台登录、验证码、上传、搜索、公告、天气等部分接口没有统一类级前缀，直接挂在根路径（例如 `/login`、`/sendCode`、`/uploadAvatar`、`/search`、`/notice/list`、`/weather/current`）；完整清单以 Swagger 文档和第 3.2 节列出的接口文档为准。

## 7. 第三方服务配置说明

第三方服务都是可选的：不配置时应用仍可启动，但依赖该服务的功能不可用。请根据实际需要配置。

### 7.1 QQ 邮箱 SMTP（邮件验证码、通知）

相关变量：`QQ_EMAIL_USERNAME`、`QQ_EMAIL_KEY`

- `QQ_EMAIL_USERNAME`：发件 QQ 邮箱地址。
- `QQ_EMAIL_KEY`：QQ 邮箱 **SMTP 授权码**，不是 QQ 登录密码。登录 QQ 邮箱后，在“设置 → 账户 → POP3/IMAP/SMTP 服务”中开启 SMTP 服务并生成授权码。
- 模板默认：`spring.mail.host=smtp.qq.com`、`spring.mail.port=587`，启用 STARTTLS 认证。

**未配置时：** 邮件验证码、邮箱登录/注册、找回密码中的邮件流程、评论/审核通知等依赖邮件的功能不可用。

### 7.2 七牛云 OSS（文件上传）

相关变量：`OSS_ACCESS_KEY`、`OSS_SECRET_KEY`、`OSS_BUCKET_NAME`、`OSS_VISIT_DOMAIN`

- 在七牛云控制台创建存储空间并获取 AccessKey/SecretKey。
- `OSS_VISIT_DOMAIN` 按模板注释要求以 `/` 结尾，例如 `https://cdn.example.com/`。

**未配置时：** 头像上传、文章图片上传等文件上传功能不可用，已有图片的访问也可能受影响。

### 7.3 阿里云短信（手机验证码）

相关变量：`ALIYUN_ACCESS_KEY_ID`、`ALIYUN_ACCESS_KEY_SECRET`

- 需要在阿里云控制台开通号码认证服务并创建 AccessKey。
- 短信签名通过 `ALIYUN_SMS_SIGN_NAME` 配置（模板默认 `your_sms_sign_name`）；模板 CODE 默认 `100001`，如果使用自己的模板，请同步修改 `application.yml` 中的 `aliyun.sms.template-code`。

**未配置时：** 手机验证码登录、手机号注册、手机号找回密码等短信流程不可用。

### 7.4 和风天气（天气查询）

相关变量：`WEATHER_KEY_ID`、`WEATHER_PROJECT_ID`、`WEATHER_API_HOST`、`WEATHER_PRIVATE_KEY`

- 和风天气使用 JWT 认证，签名算法为 Ed25519；`WEATHER_PRIVATE_KEY` 为 PEM 格式私钥。代码会处理私钥中的字面 `\n` 转义。
- `WEATHER_API_HOST` 使用和风天气控制台分配给你的 API Host。

**未配置时：** `/weather/now`（后台）、`/weather/current`（前台）等天气接口不可用，相关页面无法显示天气。

### 7.5 DeepSeek（AI 敏感词检测）

相关变量：`DEEPSEEK_API_KEY`

- 默认 API 地址：`https://api.deepseek.com`（`deepseek.base-url`，可在 `application.yml` 修改）。
- 项目使用 DeepSeek 对评论、昵称、用户名等内容进行 AI 辅助敏感词检测，检测顺序为：本地敏感词库 → 开源敏感词库 → AI 多模型检测。

**未配置时：** AI 辅助检测层不可用；本地词库和开源词库检测仍然生效，评论等基础功能不会因此完全不可用。若需要 AI 检测能力，请配置有效的 API Key 并确保服务器可访问 DeepSeek API。

## 8. 生产部署建议

> 本节中的 Nginx、systemd 配置均为**示例**，不是仓库自带文件。仓库当前没有 `Dockerfile`、`docker-compose.yml`、部署脚本或 systemd 服务文件，请根据自己的服务器环境创建和调整。

### 8.1 安全配置

1. **修改 JWT 密钥**：`mengze-framework/src/main/java/com/mengze/utils/JwtUtil.java` 中的 `JwtUtil.JWT_KEY` 已支持通过环境变量 `JWT_SECRET` 覆盖，源码中的默认值只适合本地开发。生产部署前必须设置 `JWT_SECRET` 为足够长的随机 Base64 字符串（例如 `openssl rand -base64 32`）；该值会被 `Base64.getDecoder().decode(...)` 解码，因此必须是合法的 Base64 内容。
2. **修改默认密码**：不要使用模板中的默认密码，包括 `application.yml` 的 `spring.security.user.password` 和数据库中的初始管理员密码（`sql/yun_blog_seed.sql` 默认 `admin / Admin@123456`）；数据库账号、邮箱授权码、OSS/短信/天气/DeepSeek 密钥都要使用独立且足够复杂的值。代码中新建/导入用户还存在默认密码逻辑，生产环境应改为随机密码或强制首次登录修改。
3. **保护 `.env`**：`.env` 只放在服务器本地，权限设为仅部署用户可读（例如 `chmod 600 .env`）；不要提交到 Git，不要写入日志。
4. **数据库与 Redis 安全**：为 MySQL、Redis 设置强密码；MySQL 不要使用 `root` 作为应用账号；Redis 不要对公网开放；如使用 Redis 密码，请在 `application.yml` 中补充 `spring.redis.password`。
5. **关闭调试输出**：生产环境建议关闭 `mybatis-plus.configuration.log-impl: org.apache.ibatis.logging.stdout.StdOutImpl`，避免 SQL 明细刷屏和性能损耗。
6. **升级评估**：Spring Boot 2.5.0 属于较旧版本，上线前建议评估升级到仍在维护的版本，并同步测试 Spring Security、Springfox 等依赖的兼容性。

### 8.2 Nginx 反向代理示例

假设前台接口域名为 `blog.example.com`、后台接口域名为 `admin.example.com`，可分别反向代理到本机的 `1314`、`7616`（仅示例，请替换域名和证书路径）：

```nginx
# 前台接口
server {
    listen 80;
    server_name blog.example.com;

    location / {
        proxy_pass http://127.0.0.1:1314;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}

# 后台接口
server {
    listen 80;
    server_name admin.example.com;

    location / {
        proxy_pass http://127.0.0.1:7616;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

生产环境建议使用 HTTPS（例如通过 Certbot 申请证书），并将 HTTP 重定向到 HTTPS。若前端请求统一走 `/api/` 前缀，请按前端实际路径调整 `location` 和 `proxy_pass` 的路径重写规则。

### 8.3 systemd 示例

以下仅以 `mengze-blog` 为例；`mengze-admin` 同理，只需替换服务名、描述和 jar 路径（`mengze-admin-1.0-SNAPSHOT.jar`，端口 `7616`）。

```ini
# /etc/systemd/system/yun-blog-blog.service （示例，仓库未提供此文件）
[Unit]
Description=Yun Blog Blog API
After=network.target mysql.service redis-server.service

[Service]
Type=simple
User=www-data
WorkingDirectory=/opt/yun-blog-java
EnvironmentFile=/opt/yun-blog-java/.env
ExecStart=/usr/bin/java -jar /opt/yun-blog-java/mengze-blog/target/mengze-blog-1.0-SNAPSHOT.jar
Restart=on-failure
RestartSec=5
SuccessExitStatus=143

[Install]
WantedBy=multi-user.target
```

启用服务（示例）：

```bash
sudo systemctl daemon-reload
sudo systemctl enable --now yun-blog-blog
sudo systemctl status yun-blog-blog
```

注意：systemd 的 `EnvironmentFile` 会按 `KEY=value` 形式读取 `.env` 并注入环境变量，这是与命令行 `source` 不同的加载方式；如果改用 systemd，请确保 `.env` 文件路径、属主和权限正确。

### 8.4 防火墙与端口暴露

- 公网只暴露 Nginx 的 `80`/`443` 端口。
- `1314`、`7616` 建议只监听本机或内网，由 Nginx 反向代理访问。
- MySQL `3306`、Redis `6379` 绝对不要直接暴露到公网；可在防火墙中只允许应用服务器访问。
- Linux 示例（以 ufw 为例）：

```bash
sudo ufw allow 80/tcp
sudo ufw allow 443/tcp
```

### 8.5 日志与备份

- 查看 systemd 日志：`sudo journalctl -u yun-blog-blog -f`
- 如需把应用日志写入文件，可在 `application.yml` 中配置 `logging.file.name` 等 Spring Boot 日志参数；确保日志目录有写权限并做好轮转。
- 定期备份 MySQL 数据库（示例）：

```bash
mysqldump -u root -p --single-transaction --routines --triggers yun_blog > yun_blog_$(date +%F).sql
```

- 同时备份 `.env` 和两个 `application.yml`（注意加密保存，不要放入公开仓库）；上传到七牛云的文件也应按业务需要另行备份。
- 建议使用非 root 用户运行服务，并限制 `target`、日志、`.env` 的访问权限。

## 9. 常见问题

### 9.1 端口被占用（`Address already in use`）

- 查看占用进程：

  Windows PowerShell：

  ```powershell
  netstat -ano | findstr ":1314"
  netstat -ano | findstr ":7616"
  ```

  Linux/macOS：

  ```bash
  lsof -i :1314
  lsof -i :7616
  ```

- 关闭占用进程，或修改对应模块 `application.yml` 中的 `server.port` 后重新构建启动。注意 `.env` 中没有端口变量。

### 9.2 数据库连接失败

常见报错：`Access denied for user ...`、`Unknown database 'yun_blog'`、`Communications link failure`。

- 确认 MySQL 已启动，端口正确：`mysqladmin -u root -p ping`。
- 确认已创建 `yun_blog` 数据库并导入表结构（见 5.2）。
- 确认 `SQL_USERNAME`、`SQL_PASSWORD` 已在当前终端导出，且 `application.yml` 中的 `spring.datasource.url` 与实际 MySQL 地址一致。
- 检查 `serverTimezone=Asia/Shanghai` 是否与服务器时区匹配；如数据库不是本机，修改 URL 中的主机和端口。
- 如果密码中含特殊字符，请正确引用；确认没有把 `.env` 中的示例值原样带进生产环境。

### 9.3 Redis 连接失败

常见报错：`Unable to connect to Redis`、`RedisConnectionFailureException`。

- 执行 `redis-cli ping` 确认 Redis 正常返回 `PONG`。
- 确认 `application.yml` 中 `spring.redis.host`、`spring.redis.port`、`spring.redis.database` 正确。
- 如果 Redis 设置了密码，需要在 `application.yml` 中补充 `spring.redis.password`；如果 Redis 只允许本机访问，请确认应用与 Redis 在同一网络。
- Redis 未启动时，验证码、在线人数、配置缓存等功能会受影响，请先修复连接再启动应用。

### 9.4 邮件发送失败（535 认证错误）

- QQ 邮箱的 `QQ_EMAIL_KEY` 必须填写 **SMTP 授权码**，不是 QQ 登录密码。
- 登录 QQ 邮箱网页版，确认已开启 POP3/SMTP 服务并重新生成授权码。
- 确认 `spring.mail.host=smtp.qq.com`、`spring.mail.port=587`，且网络允许 STARTTLS。
- 535 通常是账号/授权码不匹配或服务未开启；如使用其他邮箱服务商，请同步修改 `spring.mail.host`、端口和 TLS 配置。

### 9.5 Swagger 打开 404

- 先确认应用已成功启动、端口正确。
- 访问 `http://localhost:1314/swagger-ui.html` 或 `http://localhost:7616/swagger-ui.html`；本项目实测该路径返回 200。
- 如果通过 Nginx 访问，确认已代理 `/swagger-ui.html`、`/swagger-ui/**`、`/swagger-resources/**`、`/v2/api-docs`、`/webjars/**` 等路径。
- 本项目使用 Springfox 2.9.2，若后续升级 Spring Boot 到 2.6+，需要额外处理 Spring MVC 路径匹配策略，否则 Swagger 可能无法启动。

### 9.6 `java -jar` 启动时找不到配置 / 数据源未配置

常见报错：`Failed to configure a DataSource: 'url' attribute is not specified`。

- 确认已把 `mengze-admin/src/main/resources/application.yml.template` 和 `mengze-blog/src/main/resources/application.yml.template` 复制为同目录下的 `application.yml`。
- 复制配置后需要重新执行 `mvn clean package -DskipTests`，因为 `application.yml` 是在打包时进入 jar 的。
- 确认启动的是 `mengze-admin/target/mengze-admin-1.0-SNAPSHOT.jar` 或 `mengze-blog/target/mengze-blog-1.0-SNAPSHOT.jar`，而不是 `mengze-framework` 的普通 jar（公共模块没有启动类，不能直接启动）。

### 9.7 `.env` 修改后不生效

- `.env` 不会被 Spring Boot 自动读取；请按 5.5 在**同一个终端**执行 `set -a; source .env; set +a`（Linux/macOS）或逐项设置 PowerShell 环境变量。
- 修改环境变量后需要重启 `java -jar` 进程；仅修改 `.env` 文件不会影响已启动的进程。
- 使用 IntelliJ IDEA 时，检查 Run Configuration 是否启用了 EnvFile 插件或手动配置了 Environment variables。
- 检查变量名大小写是否正确（如 `SQL_USERNAME`），以及 `.env` 是否存在 CRLF 换行导致 `source` 失败。

### 9.8 中文乱码

- 确保所有配置文件、Java 源文件、`.env` 均为 UTF-8 编码。
- Windows PowerShell 可先执行 `chcp 65001` 切换到 UTF-8 代码页；启动时也可增加 `-Dfile.encoding=UTF-8`：

  ```powershell
  java -Dfile.encoding=UTF-8 -jar mengze-blog/target/mengze-blog-1.0-SNAPSHOT.jar
  ```

- MySQL 数据库建议使用 `utf8mb4` 字符集；连接 URL 已包含 `characterEncoding=utf8`，如仍有乱码请检查数据库和表的字符集。
- 终端字体不支持中文时也会显示乱码，可更换支持中文的终端/字体。

### 9.9 Maven 构建失败或提示 `mvn` 不是命令

- 确认已安装 Maven 3.8+，并执行 `mvn -version` 验证。
- 如果使用 IntelliJ IDEA，可直接使用右侧 Maven 工具窗口执行 `clean` 和 `package`。
- 首次构建需要联网下载依赖，网络受限时可配置 Maven 镜像仓库后重试。

## 10. 参与贡献与 License

### 10.1 参与贡献

欢迎提交 Issue 和 Pull Request。建议流程：

1. Fork 本仓库并创建功能分支，例如 `feature/your-feature`。
2. 提交前确认代码可编译：`mvn clean package -DskipTests`。
3. 不要提交 `.env`、`application.yml`、`target/`、IDE 本地配置或任何真实密钥。
4. 在 Pull Request 中说明改动内容、影响范围和验证方式。

### 10.2 License

本项目采用 **MIT License**，完整文本见仓库根目录 [LICENSE](LICENSE)。

> 注意：仓库中包含的第三方依赖、敏感词库、`ip2region` 数据文件等可能分别受其自身许可证约束；再分发或商用前请自行核对其许可证与合规要求。
