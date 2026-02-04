# Java NIO 示例项目

本项目展示了Java NIO（New I/O）的各种核心概念和实际应用，包括缓冲区（Buffer）、通道（Channel）、选择器（Selector）等关键组件，并结合Netty框架的实际应用案例。

## 项目概述

Java NIO 是 Java 提供的一种新的 I/O 操作方式，相比传统的 I/O 模型，它具有更高的性能和更好的可扩展性。本项目通过多个示例程序详细展示了 NIO 的各种特性。

## 技术栈

- Java 11
- Netty 4.1.108.Final
- Maven 项目管理
- JUnit 4.13.2 测试框架

## 核心功能模块

### 1. Buffer（缓冲区）

- [CopyFile.java](src/main/java/buffer/CopyFile.java) - 展示如何使用缓冲区进行文件复制操作

### 2. Channel（通道）

- [AsynchronousFileChannelDemo.java](src/main/java/channel/AsynchronousFileChannelDemo.java) - 异步文件通道操作
- [DatagramChannelDemo.java](src/main/java/channel/DatagramChannelDemo.java) - UDP 数据报通道操作
- [FileChannelDemo.java](src/main/java/channel/FileChannelDemo.java) - 文件通道操作
- [ServiceSocketChannelDemo.java](src/main/java/channel/ServiceSocketChannelDemo.java) - 服务端套接字通道操作
- [SocketChannelDemo.java](src/main/java/channel/SocketChannelDemo.java) - 客户端套接字通道操作

### 3. Selector（选择器）

- [ClientDemo.java](src/main/java/selector/ClientDemo.java) - 客户端选择器使用示例
- [SelectorDemo.java](src/main/java/selector/SelectorDemo.java) - 选择器基本操作示例
- [ServerDemo.java](src/main/java/selector/ServerDemo.java) - 服务端选择器使用示例

### 4. File（文件操作）

- [FileReadWrite.java](src/main/java/file/FileReadWrite.java) - NIO 方式的文件读写操作

### 5. File Lock（文件锁）

- [FileLockDemo.java](src/main/java/filelock/FileLockDemo.java) - 文件锁定机制示例

### 6. Pipe（管道）

- [PipeDemo.java](src/main/java/pipe/PipeDemo.java) - 管道通信示例

### 7. Charset（字符集）

- [CharsetDemo.java](src/main/java/charset/CharsetDemo.java) - 字符集编码转换示例

### 8. Netty（高性能网络框架）

- [HttpServer.java](src/main/java/netty/HttpServer.java) - 基于Netty的HTTP服务器实现
- [NettyClient.java](src/main/java/netty/NettyClient.java) - Netty客户端实现
- [NettyServer.java](src/main/java/netty/NettyServer.java) - Netty服务端实现
- [ConnectionManager.java](src/main/java/netty/ConnectionManager.java) - 连接管理器
- [CustomMessage.java](src/main/java/netty/CustomMessage.java) - 自定义消息协议
- [CustomProtocol.java](src/main/java/netty/CustomProtocol.java) - 自定义协议实现
- [BatchTestClient.java](src/main/java/netty/BatchTestClient.java) - 批量测试客户端

## 项目特点

1. **全面覆盖**：涵盖了Java NIO的主要组件和API
2. **实践性强**：每个模块都有具体的代码示例
3. **进阶应用**：结合了Netty这一流行的网络编程框架
4. **易于理解**：代码注释详细，便于学习和理解

## 运行方式

1. 确保已安装JDK 11+ 和 Maven
2. 克隆或下载本项目
3. 在项目根目录执行 `mvn clean compile`
4. 运行具体的示例类，如：
   ```bash
   mvn exec:java -Dexec.mainClass="channel.FileChannelDemo"
   ```

## 学习目标

通过本项目的学习，您将能够：

- 理解Java NIO的基本概念和核心组件
- 掌握Buffer、Channel、Selector的使用方法
- 了解非阻塞I/O的工作原理
- 学会使用Netty框架开发高性能网络应用程序
- 理解网络编程中的常见问题和解决方案

## 注意事项

- 部分网络相关的示例需要确保端口未被占用
- 运行Netty相关示例时，请注意防火墙设置
- 推荐在学习时按模块顺序逐步阅读代码