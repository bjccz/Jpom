<p align="center">
	<a href="https://jpom.io/"  target="_blank">
	    <img src="https://cdn.jsdelivr.net/gh/jiangzeyin/Jpom-site/images/jpom_logo.png" width="400" alt="logo">
	</a>
</p>
<p align="center">
	<strong>简而轻的低侵入式在线构建、自动部署、日常运维、项目监控软件</strong>
</p>

<p align="center">
    <a target="_blank" href="https://gitee.com/dromara/Jpom">
        <img src='https://img.shields.io/github/license/dromara/Jpom.svg?style=flat' alt='license'/>
    </a>
    <a target="_blank" href="https://gitee.com/dromara/Jpom">
        <img src='https://img.shields.io/badge/JDK-1.8.0_40+-green.svg' alt='jdk'/>
    </a>
    <a target="_blank" href="https://travis-ci.org/dromara/Jpom">
        <img src='https://travis-ci.org/dromara/Jpom.svg?branch=master' alt='travis'/>
    </a>
    <a target="_blank" href="https://www.codacy.com/gh/dromara/Jpom/dashboard?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=dromara/Jpom&amp;utm_campaign=Badge_Grade">
      <img src="https://app.codacy.com/project/badge/Grade/843b953f1446449c9a075e44ea778336" alt="codacy"/>
    </a>
     <img src='https://img.shields.io/badge/%E5%BE%AE%E4%BF%A1%E7%BE%A4(%E8%AF%B7%E5%A4%87%E6%B3%A8%3AJpom)-jpom66-yellowgreen.svg' alt='jpom66 请备注jpom'/>
    <a target="_blank" href="https://gitee.com/dromara/Jpom">
        <img src='https://gitee.com/dromara/Jpom/badge/star.svg?theme=gvp' alt='gitee star'/>
    </a>
    <a target="_blank" href="https://github.com/dromara/Jpom">
        <img src='https://img.shields.io/badge/Github-Github-red.svg' alt='github'/>
    </a>

</p>
<p align="center">
	<a href="https://jpom.io/">https://jpom.io/</a> | <a href="https://jpom-site.keepbx.cn/">https://jpom-site.keepbx.cn/</a> | <a href="https://jpom.keepbx.cn/">https://jpom.keepbx.cn/</a>
</p>

#### 您为什么需要 [Jpom](https://gitee.com/dromara/Jpom)

>  大部分项目在实际部署运维，通用的方法是登录服务器上传新的项目包，执行相应命令管理，如果管理多个项目则重复操作上述步骤

> 此方法不足的是：
> 1. 需要每次登录服务器（专业软件）
> 2. 多个项目有多个管理命令（不易记、易混淆）
> 3. 查看项目运行状态需要再次使用命令
> 4. 同时面对多个运维都需要知道服务器密码（安全性低）
> 5. 集群项目需要挨个操作项目步骤

> 在使用Jpom后：
> 1. 使用浏览器登录方便快捷管理项目
> 2. 界面形式实时查看项目运行状态以及控制台日志
> 3. 运维有对应的账号密码不需要知道服务器密码（并且有操作日志）
> 4. 集群项目使用项目分发一键搞定多机部署
> 5. 项目状态监控异常自动报警
> 6. 在线构建不用手动上传项目包

#### 项目主要功能及特点

1. 创建、修改、删除项目、Jar包管理、在线修改文本文件
2. 实时查看控制台日志、备份日志、删除日志、导出日志
3. 节点脚本模版功能、定时执行脚本模版
4. 在线构建项目发布项目一键搞定、仓库统一管理
5. 多节点管理、多节点自动分发
6. 在线 SSH 终端，并且有终端日志和禁用命令、在线修改文本文件、定时脚本、自动解压缩包
7. 在线 SSH 轻量的实现了简单的堡垒机功能
8. 实时监控项目状态异常自动报警、自动尝试重启
9. ~~cpu、ram 监控、(已经取消该功能)~~ 导出堆栈信息、查看项目进程端口、服务器状态监控
10. 多用户管理，用户项目权限独立(上传、删除权限可控制),完善的操作日志,使用工作空间隔离权限
11. 系统路径白名单模式，杜绝用户误操作系统文件
12. 在线管理 Nginx 配置文件、ssl 证书文件
13. ~~Tomcat状态、文件、war包在线实时管理 (不再长期维护)~~
14. 升级 Jpom 版本方便：手动上传文件、在线上传文件、远程下载更新

> 特别提醒：
> 1. 在 Windows 服务器中可能有部分功能因为系统特性造成兼容性问题，建议在实际使用中充分测试。Linux 目前兼容良好
> 2. 服务端和插件端请安装到不同目录中，切勿安装到同一目录中
> 3. 卸载 Jpom 插件端或者服务端，先停止对应服务，删除对应的程序文件、日志文件夹、数据目录文件夹即可
> 4. 构建依赖的是系统环境，如果需要 maven 或者 node 需要服务端所在的服务器中有对应插件，如果已经启动服务端再安装的对应环境需要通过命令行重启服务端后才生效。
> 5. 在Ubuntu/Debian服务器作为插件端可能会添加失败，请在当前用户的根目录创建 .bash_profile文件

> 升级 2.7.x 后不建议降级操作,会涉及到数据不兼容到情况
> 
> [2.6.x "稳定版" 分支](https://gitee.com/dromara/Jpom/tree/2.6.x/)

#### 版本更新日志

[CHANGELOG.md](https://gitee.com/dromara/Jpom/blob/master/CHANGELOG.md)

### 一键安装（Linux）（推荐）

#### 插件端

> 如果服务端也需要被管理，在服务端上也需要安装插件端
> 
> 安装的路径位于执行命令目录（数据、日志存放目录默认位于安装路径,如需要修改参考配置文件：[`extConfig.yml`](https://gitee.com/dromara/Jpom/blob/master/modules/agent/src/main/resources/bin/extConfig.yml) ）

```
yum install -y wget && wget -O install.sh https://dromara.gitee.io/jpom/docs/install.sh && bash install.sh Agent

备用地址

yum install -y wget && wget -O install.sh https://cdn.jsdelivr.net/gh/dromara/Jpom/docs/install.sh && bash install.sh Agent

支持自动安装jdk环境

yum install -y wget && wget -O install.sh https://dromara.gitee.io/jpom/docs/install.sh && bash install.sh Agent jdk

```

启动成功后,插件端的端口为 `2123`

#### 服务端

> 安装的路径位于执行命令目录（数据、日志存放目录默认位于安装路径,如需要修改参考配置文件：[`extConfig.yml`](https://gitee.com/dromara/Jpom/blob/master/modules/server/src/main/resources/bin/extConfig.yml) ）
> 
> 如果需要修改数据、日志存储路径请参照 `extConfig.yml` 文件中 `jpom.path` 配置属性

```
yum install -y wget && wget -O install.sh https://dromara.gitee.io/jpom/docs/install.sh && bash install.sh Server

备用地址

yum install -y wget && wget -O install.sh https://cdn.jsdelivr.net/gh/dromara/Jpom/docs/install.sh && bash install.sh Server


支持自动安装jdk环境

yum install -y wget && wget -O install.sh https://dromara.gitee.io/jpom/docs/install.sh && bash install.sh Server jdk

支持自动安装jdk和maven环境

yum install -y wget && wget -O install.sh https://dromara.gitee.io/jpom/docs/install.sh && bash install.sh Server jdk+mvn

```

启动成功后,服务端的端口为 `2122` 访问管理页面 例如`http://127.0.0.1:2122/`

> 特别提醒：一键安装的时候注意执行命令不可在同一目录下，即Server端和Agent端不可安装在同一目录下
> 
> 如无法访问，检查下是否开启了防火墙`systemctl status firewalld`，如状态显示为绿色`Active: active (running)`可临时关闭防火墙`systemctl stop firewalld`，然后重启防火墙`firewall-cmd --reload`（建议仅测试环境下使用，生产环境下慎用）
> 如关闭防火墙后仍无法访问，并且使用的是云服务器，还需要到云服务器管理后台中检查安全组规则(关闭防火墙)

### 容器化安装

> 注意：容器化安装方式需要先安装docker
```
docker pull jpomdocker/jpom
docker volume create jpom-server-vol
docker run -d -p 2122:2122 --name jpom-server -v /etc/localtime:/etc/localtime:ro -v jpom-server-vol:/usr/local/jpom-server jpomdocker/jpom
```

> 容器化安装仅提供服务端版。由于容器和宿主机环境隔离，而导致插件端的很多功能无法正常使用，因此对插件端容器化意义不大。
>
> 安装docker、配置镜像、自动启动、查找安装后所在目录等可参考文档[https://jpom.io/docs/](https://jpom.io/docs/)


### docker-compose 一键启动

- 无需安装任何环境,自动编译构建

> 需要注意修改 `.env` 文件中的 token 值

```shell
git clone https://gitee.com/dromara/Jpom.git
cd Jpom
docker-compose up -d
```

### 下载安装

> [帮助文档](https://jpom-site.keepbx.cn/docs/#/安装使用/开始安装)

1. 下载安装包 [https://gitee.com/dromara/Jpom/attach_files](https://gitee.com/dromara/Jpom/attach_files)
2. 解压文件
3. 安装插件端（ [流程说明](https://jpom-site.keepbx.cn/docs/#/安装使用/开始安装?id=安装插件端) ）
    1. agent-x.x.x-release 目录为插件端的全部安装文件
    2. 上传到对应服务器
    3. 命令运行（Agent.sh、Agent.bat）`出现乱码或者无法正常执行,请优先检查编码格式、换行符是否匹配`
    4. 默认运行端口：`2123`
4. 安装服务端（ [流程说明](https://jpom-site.keepbx.cn/docs/#/安装使用/开始安装?id=安装服务端) ）
    1. server-x.x.x-release 目录为服务端的全部安装文件
    2. 上传到对应服务器
    3. 命令运行（Server.sh、Server.bat）`出现乱码或者无法正常执行,请优先检查编码格式、换行符是否匹配`
    4. 默认运行端口：`2122` 访问管理页面 例如`http://127.0.0.1:2122/`

### 编译安装

> [帮助文档](https://jpom-site.keepbx.cn/docs/#/安装使用/开始安装)

1. 访问 [Jpom](https://gitee.com/dromara/Jpom) 的码云主页,拉取最新完整代码(建议使用master分支)
2. 切换到`web-vue`目录 执行`npm install` (vue环境需要提前搭建和安装依赖包详情可以查看web-vue目录下README.md)
3. 执行`npm run build`进行vue项目打包(vue环境需要提前搭建和安装依赖包详情可以查看web-vue目录下README.md)
4. 切换到项目根目录执行:`mvn clean package`
5. 安装插件端（ [流程说明](https://jpom-site.keepbx.cn/docs/#/安装使用/开始安装?id=安装插件端) ）
    1. 查看插件端安装包 modules/agent/target/agent-x.x.x-release
    2. 打包上传服务器运行
    3. 命令运行（Agent.sh、Agent.bat）`出现乱码或者无法正常执行,请优先检查编码格式、换行符是否匹配`
    4. 默认运行端口：`2123`
6. 安装服务端（ [流程说明](https://jpom-site.keepbx.cn/docs/#/安装使用/开始安装?id=安装服务端) ）
    1. 查看插件端安装包 modules/server/target/server-x.x.x-release
    2. 打包上传服务器运行
    3. 命令运行（Server.sh、Server.bat）`出现乱码或者无法正常执行,请优先检查编码格式、换行符是否匹配`
    4. 默认运行端口：`2122` 访问管理页面 例如`http://127.0.0.1:2122/`

> 也可以使用 `script/release.bat` `script/release.sh` 快速打包

### 编译运行

1. 访问 [Jpom](https://gitee.com/dromara/Jpom) 的码云主页,拉取最新完整代码(建议使用master分支、如果想体验新功能请使用dev分支)
2. 运行插件端
    1. 运行`io.jpom.JpomAgentApplication`
    2. 注意控制台打印的默认账号密码信息
    3. 默认运行端口：`2123`
3. 运行服务端
    1. 运行`io.jpom.JpomServerApplication`
    2. 默认运行端口：`2122`
4. 构建vue页面 切换到`web-vue`目录（前提需要本地开发环境有node、npm环境）
5. 安装项目vue依赖 控制台执行 `npm install`
6. 启动开发模式 控制台执行 `npm serve`
7. 根据控制台输出的地址访问前端页面 例如`http://127.0.0.1:3000/`

### 管理命令

1. windows 中 Agent.bat 、Server.bat

```
# 服务端
Server.bat     启动管理面板(按照面板提示输入操作)

# 插件端
Agent.bat     启动管理面板(按照面板提示输入操作)
```

> windows 中执行启动后需要根据日志取跟进启动的状态、如果出现乱码请检查或者修改编码格式，windows 中 bat 编码格式推荐为 `GB2312`

2. linux 中 Agent.sh 、Server.sh

```
# 服务端
Server.sh start     启动Jpom服务端
Server.sh stop      停止Jpom服务端
Server.sh restart   重启Jpom服务端
Server.sh status    查看Jpom服务端运行状态
Server.sh create    创建Jpom服务端的应用服务（jpom-server）

# 插件端
Agent.sh start     启动Jpom插件端
Agent.sh stop      停止Jpom插件端
Agent.sh restart   重启Jpom插件端
Agent.sh status    查看Jpom插件端运行状态
Agent.sh create    创建Jpom插件端的应用服务（jpom-agent）
```

### linux 服务方式管理

> 在使用 `Server.sh create`/`Agent.sh create` 成功后
> 
> service jpom-server {status | start | stop}
> 
> service jpom-agent {status | start | stop}

### Jpom 的参数配置

在项目运行的根路径下的`extConfig.yml`文件

1. 插件端示例：[`extConfig.yml`](https://gitee.com/dromara/Jpom/blob/master/modules/agent/src/main/resources/bin/extConfig.yml)
2. 服务端示例：[`extConfig.yml`](https://gitee.com/dromara/Jpom/blob/master/modules/server/src/main/resources/bin/extConfig.yml)

### 演示项目

[https://jpom.keepbx.cn](https://jpom.keepbx.cn)

```   
账号：demo
密码：demo123
```    

> 演示系统有部分功能做了限制，完整功能请自行部署体验

> 如果出现登录不上，请联系我们，联系方式在最底部

1. [Jboot案例代码](https://gitee.com/keepbx/Jpom-demo-case/tree/master/jboot-test)
2. [SpringBoot案例代码(ClassPath)](https://gitee.com/keepbx/Jpom-demo-case/tree/master/springboot-test)
3. [SpringBoot案例代码(Jar)](https://gitee.com/keepbx/Jpom-demo-case/tree/master/springboot-test-jar)

### 常见问题、操作说明

[https://jpom-site.keepbx.cn/docs/](https://jpom-site.keepbx.cn/docs/)

[https://jpom-site.keepbx.cn/docs/#/FQA/FQA](https://jpom-site.keepbx.cn/docs/#/FQA/FQA)

[Jpom 插件开发](https://gitee.com/keepbx/Jpom-Plugin)

### 交流讨论 、提供bug反馈或建议

1. 微信群二维码（添加小助手：备注Jpom 进群）

![Alt text](https://cdn.jsdelivr.net/gh/jiangzeyin/Jpom-site/images/wx_qrcode.jpg)

2. 微信公众号：[CodeGzh](https://cdn.jsdelivr.net/gh/jiangzeyin/Jpom-site/docs/images/CodeGzh-QrCode.jpg) 查看一些基础教程

3. 码云： [issues](https://gitee.com/dromara/Jpom/issues)

4. [码云赞赏： 在码云仓库项目首页下方捐赠、打赏](https://gitee.com/dromara/Jpom)

5. 微信赞赏 [赞赏记录](./docs/praise/praise.md)

![微信赞赏](https://cdn.jsdelivr.net/gh/jiangzeyin/Jpom-site/images/wx_praise_small.png)


### 精品项目推荐

| 项目名称          | 项目地址                                                                       | 项目介绍                                          |
|---------------|----------------------------------------------------------------------------|-----------------------------------------------|
| SpringBoot_v2 | [https://gitee.com/bdj/SpringBoot_v2](https://gitee.com/bdj/SpringBoot_v2) | 基于springboot的一款纯净脚手架                          |
| TLog GVP 项目   | [https://gitee.com/dromara/TLog](https://gitee.com/dromara/TLog)           | 一个轻量级的分布式日志标记追踪神器，10分钟即可接入，自动对日志打标签完成微服务的链路追踪 |
| Sa-Token      | [https://gitee.com/dromara/sa-token](https://gitee.com/dromara/sa-token)   | 这可能是史上功能最全的 Java 权限认证框架！                      |
| Erupt         | [https://gitee.com/erupt/erupt](https://gitee.com/erupt/erupt)             | 零前端代码，纯注解开发 admin 管理后台                        |

### giteye

[![Giteye chart](https://chart.giteye.net/gitee/dromara/Jpom/N4VGB7ZB.png)](https://giteye.net/chart/N4VGB7ZB)