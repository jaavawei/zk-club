# 猪客网

## 概述

猪客网是一款专门为程序员打造的沟通交流社区，采用主流的微服务框架+主流C端技术栈来做为技术架构。旨在统一程序员信息差，进行平台统一化。  
程序员可以在猪客网上进行刷题、练题完善自身知识，进行模拟模拟面试测试自身水平，通过“猪圈”交流学习心得。  
“猪”，能吃，肚量大，能消化。“猪客网”也是希望猪友们能孜孜不倦地学，学完迅速沉淀。
## 项目结构介绍

- zk-club-auth：鉴权微服务，集成轻量级鉴权架构 sa-token。
- zk-club-gate：圈子微服务，实现了猪友们在圈子发布动态和评论回复。
- zk-club-gateway：网关微服务，负责对请求的转发和负载均衡。在这里进行了统一鉴权和全局异常处理。
- zk-club-interview：面试微服务，猪友们可选择面试引擎进行模拟面试，不同引擎题目来源不同，评分标准不同。
- zk-club-oss：oss微服务，集成了Minio，负责对程序中涉及的文件相关进行处理。
- zk-club-practice：练题微服务，猪友们可选择自己薄弱的点进行练习，后台会生成相应的模拟套卷。模拟套卷可选预设套卷和实时生成两种。
- zk-club-subject：刷题微服务，负责题目的上传和查询。猪友们可以获取选定分类和标签下的所有题目，进行系统的练习。
- zk-club-wx：微信微服务，该项目集成微信登录。利用微信的 `openId` 作为用户的唯一id，利用微信的回调接口，获取公众号收到的信息。

## 项目调整记录

- 在网关层增加全局异常捕获功能。
- 使用 completionFuture 代替 FutureTask 多线程查询标签。
- 使用 RocketMq 代替 xxl-job+redis 持久化点赞信息
- 增添缓存更新策略保证数据最终一致性。
- 增加分布式事务，保证Mysql与ElasticSearch数据一致性。
- 在RocketMq的消费者实现中新增了对幂等性的考虑。
- 在RocketMq的 TransactionProducer 中更新了对线程池的优雅关闭。

## 项目下载

- git clone https://github.com/jaavawei/zk-club.git 。

- 配置好 Maven 仓库，使用 IntelliJ IDEA 工具打开项目。

- 在配置文件中将配置信息修改为自己的。

## 技术架构使用

本项目使用的技术架构为：SpringBoot+SpringCloud Alibaba+SSM+Mysql+Redis+Nacos+Gateway+Minio+RocketMq+xxl-job

### Redis使用
- 利用 Hash 和 String 存储题目点赞信息。
- 利用 string 存储用户权限信息。
- 利用 Zset 实现贡献排行榜。
- 利用 String 存储验证码。

### Gateway + Nacos 使用
Nacos提供业务项目的服务注册与发现和业务动态配置功能。  
Gateway负责对用户请求的路由转发和负载均衡。

### RocketMq 使用
- 对题目点赞信息的异步持久化。
- 在用户上传题目时异步更新 ElasticSearch 和 Redis。

### xxl-job 使用
题目点赞信息汇总在 redis 的 hash 中，使用 xxl-job 定时对 Hash 中的内容进行持久化，由于Redis宕机可能会丢数据，后面使用 RocketMq
替代。

### ElasticSearch 使用
提供题目关键词搜索功能，搜索出来的题目进行高亮处理。  
题目上传到 Mysql 时异步上传到 ElasticSearch。
