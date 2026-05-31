# Mahjong Bot - QQ 麻将比赛 Elo 评分系统机器人

本项目是一个基于 [Shiro](https://github.com/MisakaTAT/Shiro) 框架开发的 QQ 机器人插件，专为麻将类比赛提供自动化记分、Elo 评分计算与排名更新功能。适用于多人麻将对战群组，支持自动结算比赛结果、更新 Elo 排名，并在群内发送结构化比赛信息。

---

## 📌 功能概览

### ✅ 核心功能
- **一键创建并结算比赛**：通过一条消息同时创建比赛并提交所有玩家分数，自动完成结算。
- **昵称自动注册**：首次出现的昵称自动创建用户，无需手动绑定 QQ 号。
- **自动结算**：提交分数后自动进行精算并结束比赛。
- **Elo 评分更新**：根据比赛结果使用 Elo 算法动态调整玩家评分。
- **消息推送**：比赛结束后自动向群组发送结构化比赛结果与 Elo 变动情况。
- **数据持久化**：使用 MyBatis Plus + MySQL 存储比赛记录、Elo 历史等信息。

---

## 🧩 技术栈

| 技术 | 描述 |
|------|------|
| Java 17+ | 主语言 |
| Spring Boot 3.5 | 后端框架 |
| Shiro | QQ 机器人框架（OneBot 协议） |
| MyBatis Plus | ORM 数据库操作 |
| MySQL | 数据存储 |
| Lombok | 自动生成 Getter/Setter |
| Gradle | 构建工具 |

---

## 📁 项目结构

```
mahjong-bot/
├── src/
│   ├── main/
│   │   ├── java/org/bot/
│   │   │   ├── biz/            # 业务处理框架（Handler、Request、Result）
│   │   │   ├── config/         # 配置类
│   │   │   ├── mapper/         # MyBatis Mapper 接口
│   │   │   ├── model/          # 数据模型（Contest, ContestRecord, Elo, User）
│   │   │   ├── plugin/         # Shiro 插件类（处理群聊指令）
│   │   │   ├── service/        # 业务逻辑接口及实现
│   │   │   ├── util/           # 工具类（Elo 计算、点数规则、渲染等）
│   │   │   └── Main.java       # Spring Boot 启动类
│   │   └── resources/
│   │       ├── application.yaml # 配置文件
│   │       └── mapper/         # MyBatis XML 映射文件
│   └── test/                   # 单元测试
├── build.gradle.kts             # 构建配置
├── gradlew / gradlew.bat        # Gradle Wrapper
└── README.md                    # 项目说明文档
```

---

## 🛠️ 功能模块介绍

### 1. 业务处理框架 (`biz`)
- 基于模板模式的业务处理架构
- 统一的 Request/Result 模型与异常处理

### 2. 比赛管理模块 (`ContestPlugin`)
- 创建比赛并提交记录 `创建比赛 <规则类型>`
- 查询比赛 `查询比赛`
- 支持 RCR、MLeague 等多种麻将规则

### 3. Elo 评分模块 (`util/elo`)
- 使用 Elo 算法动态更新玩家评分
- 支持不同麻将类型独立评分（如 RCR、MLeague）

### 4. 点数计算模块 (`util/point`)
- 支持多种麻将规则（RCR、MLeague 等）的点数结算
- 通过 SPI 机制加载不同规则的计算实现

---

## 🧪 示例流程

### 创建比赛并提交记录：

一条消息完成比赛创建和分数提交，格式为：

```text
@bot 创建比赛 RCR
南4 2本场
bbb 46400
ccc 4800
aaa 20400
ddd 27400
```

说明：
- 第一行：`创建比赛` + 规则类型（如 `RCR`、`M_LEAGUE`）
- 第二行：场况信息（仅作记录，不参与计算）
- 第三至六行：玩家昵称 + 空格 + 分数（按东南西北顺序）

### 比赛结束：
提交后系统自动结算并推送如下消息：

```
🏆 比赛结束！以下是比赛结果：
------------------------------
比赛类型：RCR
比赛ID：42
------------------------------
👤 bbb
📈 点数情况 +46400
📈 ELO: 1000 → 1030.398 (+30.398)
------------------------------
👤 ccc
📈 点数情况 +4800
📈 ELO: 1000 → 1007.742 (+7.742)
------------------------------
```

---

## 🚀 快速开始

### 1. 安装依赖

确保你已安装以下环境：

- Java 17+
- Gradle
- MySQL 5.7+

### 2. 初始化数据库

导入 SQL 表结构，见 `/doc/table.sql`

### 3. 修改配置文件

编辑 `application.yaml`：

```yaml
server:
  port: 5000
shiro:
  bots:
    bot-id: your_bot_id
  ws:
    client:
      enable: true
      url: "ws://your_onebot_ws_url"

spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/bot
    username: root
    password: your_password
```

### 4. 编译运行

```bash
./gradlew bootRun
```

---

## 🐳 Docker 部署

### 构建 Docker 镜像

项目使用 `com.bmuschko.docker-spring-boot-application` Gradle 插件来构建 Docker 镜像。

```bash
# 构建镜像（默认版本 1.0.0）
./gradlew dockerBuildImage

# 指定版本号构建
./gradlew dockerBuildImage -PVERSION=1.2.0

# 推送镜像到阿里云容器镜像服务
export DOCKERHUB_USERNAME=your_username
export DOCKERHUB_PASSWORD=your_password
./gradlew dockerPushImage
```

构建完成后会生成以下镜像标签：
- `registry.cn-beijing.aliyuncs.com/1328411791/mahjong-bot:<version>`
- `registry.cn-beijing.aliyuncs.com/1328411791/mahjong-bot:latest`

### 使用已有镜像运行

```bash
docker pull registry.cn-beijing.aliyuncs.com/1328411791/mahjong-bot:latest
docker run -d -p 5000:5000 \
  -e bots.bot-id=your_bot_id \
  -e bots.ws-url=your_ws_url \
  -e mysql.url=your_mysql_url \
  -e mysql.username=your_username \
  -e mysql.password=your_password \
  registry.cn-beijing.aliyuncs.com/1328411791/mahjong-bot:latest
```

### Docker Compose

```yaml
version: '3'
services:
  mahjong-bot:
    image: registry.cn-beijing.aliyuncs.com/1328411791/mahjong-bot:latest
    ports:
      - "5000:5000"
    environment:
      - bots.bot-id=your_bot_id
      - bots.ws-url=your_ws_url
      - mysql.url=your_mysql_url
      - mysql.username=your_username
      - mysql.password=your_password
    restart: always
```

### OneBot 协议端

部署时需要 OneBot 协议的机器人运行，推荐使用 [Lagrange.OneBot](https://lagrangedev.github.io/Lagrange.Doc/v1/Lagrange.OneBot/)，具体部署参考对应文档。

---

## 📬 命令列表

| 命令 | 参数 | 说明 |
|------|------|------|
| help | 无 | 显示所有可用命令 |
| 创建比赛 | `<规则类型>` + 多行玩家记录 | 创建比赛并提交所有玩家分数，自动结算 |
| 查询比赛 | `[比赛ID]`（可选） | 查看群内最近比赛，或指定ID查看ELO详情 |
| 查看排名 | `[规则类型]`（可选，默认 M） | 查看指定规则的 Elo 排名榜 |

---

## 📊 Elo 系统说明

### Elo 公式参考：

```java
expectedScore = 1 / (1 + Math.pow(10, (opponentRating - playerRating) / 400));
newRating = currentRating + kFactor * (actualScore - expectedScore);
```

### K 因子计算方式：

```java
kFactor = 16 * (1 + Math.abs(ratingDifference) / 400);
```

---

## 🤝 开发贡献

欢迎 Fork & PR！

你可以参与的方向包括：

- 支持更多麻将规则（如国标、立直A/R等规则、广东麻将等）
- 实现 Elo 的 Glicko-2 替代算法
- 排行榜展示、历史战绩查询等功能

---

## 📜 License

MIT License
