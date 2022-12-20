# imitation-bilibili
<p align="center">
<a href='https://docs.spring.io/spring-boot/docs/3.0.0/reference/html'><img alt="Spring Boot 3" src="https://img.shields.io/badge/Spring%20Boot%203-%23000000.svg?logo=springboot"></a>
<a href='https://staging-cn.vuejs.org'><img alt="Vue 3" src="https://img.shields.io/badge/Vue%203%20-%232b3847.svg?logo=vue.js"></a><br/>
<a href='https://github.com/JuneQQQ/imitation-bilibili'><img alt="Github stars" src="https://img.shields.io/github/stars/JuneQQQ/imitation-bilibili?logo=github"></a>
<a href='https://github.com/JuneQQQ/imitation-bilibili'><img alt="Github forks" src="https://img.shields.io/github/forks/JuneQQQ/imitation-bilibili?logo=github"></a>
<a href="https://github.com/201206030/novel"><img src="https://visitor-badge.glitch.me/badge?page_id=JuneQQQ.imitation-bilibili" alt="visitors"></a>
</p>

[//]: # (<a href='https://github.com/JuneQQQ/imitation-bilibili'><img alt="Gitee stars" src="https://gitee.com/novel_dev_team/novel/badge/star.svg?theme=gitee"></a>)

[//]: # (<a href='https://github.com/JuneQQQ/imitation-bilibili'><img alt="Gitee forks" src="https://gitee.com/novel_dev_team/novel/badge/fork.svg?theme=gitee"></a>)


## 项目简介
`imitation-bilibili` 是一套基于当下前沿技术的的前后端分离的**仿B站**的的项目，旨在搭建起一个高性能的弹幕视频网站，目前仅包括后台系统，主要包括 用户/视频检索模块、文件模块、用户订阅、用户中心、评论模块、视频投稿、视频播放和实时防遮挡弹幕等主要功能模块。

## 开发环境
- MinIO Latest
- ElasticSearch/Kibana 8.5.0
- MySQL 8.0.27
- Redis 7.0
- JDK 17
- RocketMQ 5.0
- XXL-JOB 2.3.0
- Node 12.14.0

## 技术选型
## 后端技术选型

| 技术              |  版本   | 说明                       | 官网                                           |
| :---------------- | :-----: | -------------------------- | ---------------------------------------------- |
| Spring Boot       |  3.0.0  | 容器 + MVC 框架            | https://spring.io/projects/spring-boot         |
| MyBatis           | 3.5.11  | ORM 框架                   | http://www.mybatis.org                         |
| MyBatis-Plus      |  3.5.2  | MyBatis 增强工具           | https://baomidou.com/                          |
| JJWT              | 0.11.5  | JWT 登录支持               | https://github.com/jwtk/jjwt                   |
| Lombok            | 1.18.24 | 简化对象封装工具           | https://github.com/projectlombok/lombok        |
| Caffeine          |  3.1.2  | 本地缓存支持               | https://github.com/ben-manes/caffeine          |
| Redis             |   7.0   | 分布式缓存支持             | https://redis.io                               |
| RocketMQ          |  5.0.0  | 开源消息中间件             | https://rocketmq.apache.org                    |
| MinIO             | latest  | 文件存储服务               | https://www.minio.org.cn                       |
| Docker            |    -    | 应用容器引擎               | https://www.docker.com/                        |
| Springdoc-openapi |  2.0.0  | Swagger 3 接口文档自动生成 | https://github.com/springdoc/springdoc-openapi |
| Sentinel          |  1.8.6  | 流量控制组件               | https://github.com/alibaba/Sentinel            |
| MySQL             | 8.0.27  | 数据库服务                 | https://www.mysql.com                          |
| XXL-JOB           |  2.3.1  | 分布式任务调度平台         | https://www.xuxueli.com/xxl-job                |
| Elasticsearch     |  8.5.0  | 搜索引擎服务               | https://www.elastic.co                         |


