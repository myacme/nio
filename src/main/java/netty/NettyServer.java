package netty;


import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.AttributeKey;

import java.util.concurrent.TimeUnit;

/**
 * Netty服务端
 *
 * @author MyAcme
 */
public class NettyServer {

    private final int port;

    public NettyServer(int port) {
        this.port = port;
    }

    public void start() throws Exception {
        /*
         * 创建两个EventLoopGroup：
         * 1. bossGroup: 用于处理客户端的连接请求
         * 2. workerGroup: 用于处理客户端的I/O操作和业务逻辑
         *
         * 为什么需要两个线程组？
         * - bossGroup专门处理连接，可以提高连接处理效率
         * - workerGroup处理业务，避免业务处理阻塞连接接受
         */

        // 通常只需一个线程处理连接
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        // 默认CPU核心数*2个线程
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            /*
             * ServerBootstrap是Netty的服务器启动引导类
             * 用于简化服务器的配置和启动过程
             */
            ServerBootstrap bootstrap = new ServerBootstrap();
            // 设置线程组
            bootstrap.group(bossGroup, workerGroup)
                    // 使用NIO传输通道
                    .channel(NioServerSocketChannel.class)
                    /*
                     * SO_BACKLOG参数说明：
                     * 1. 当服务端处理连接请求较慢时，可以排队等待的连接数
                     * 2. 超过队列长度的连接会被拒绝
                     * 3. 默认值根据平台不同而不同，通常设置为128
                     */
                    .option(ChannelOption.SO_BACKLOG, 128)
                    /*
                     * 设置子通道（客户端连接）的选项
                     * SO_KEEPALIVE: 启用TCP心跳机制，检测连接是否存活
                     */
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    /*
                     * TCP_NODELAY: 禁用Nagle算法
                     * Nagle算法会缓冲小数据包，延迟发送以提高网络效率
                     * 在要求低延迟的场景下需要禁用
                     */
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    /*
                     * 为服务器通道添加日志处理器
                     * 用于记录连接建立、断开等事件
                     */
                    .handler(new LoggingHandler(LogLevel.INFO))
                    /*
                     * 设置子通道的处理器链
                     * ChannelInitializer是一个特殊的ChannelHandler
                     * 用于初始化新建立的连接通道
                     */
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            /*
                             * ChannelPipeline是Netty的核心组件之一
                             * 它是一个Handler的链表，按顺序处理入站和出站事件
                             * 类似于Servlet的Filter链
                             */
                            ChannelPipeline pipeline = ch.pipeline();
                            /*
                             * 添加编解码器：
                             * 1. StringDecoder: 将ByteBuf解码为String
                             * 2. StringEncoder: 将String编码为ByteBuf
                             * 注意：编解码器的顺序很重要，必须按照处理流程添加
                             */
                            pipeline.addLast(new StringDecoder());
                            pipeline.addLast(new StringEncoder());
                            /*
                             * 添加空闲状态检测处理器
                             * 参数说明：
                             * 1. readerIdleTime: 读空闲时间（秒），0表示禁用
                             * 2. writerIdleTime: 写空闲时间（秒）
                             * 3. allIdleTime: 所有类型空闲时间（秒）
                             * 当连接空闲时间超过设定值，会触发IdleStateEvent事件
                             */
                            pipeline.addLast("idleStateHandler",
                                    new IdleStateHandler(30, 0, 0, TimeUnit.SECONDS));
                            /*
                             * 添加自定义的业务处理器
                             * 这是服务器处理业务逻辑的核心
                             */
                            pipeline.addLast(new ServerHandler());
                        }
                    });

            /*
             * bind(): 绑定端口，开始接受连接
             * sync(): 同步等待绑定完成
             * 返回的ChannelFuture表示异步操作的结果
             */
            ChannelFuture future = bootstrap.bind(port).sync();

            System.out.println("✅ Netty服务器启动成功，监听端口: " + port);
            System.out.println("服务器地址: " + future.channel().localAddress());

            /*
             * 等待服务器通道关闭
             * 这会使当前线程阻塞，直到服务器通道关闭
             * 通常在主线程中调用，防止程序退出
             */
            future.channel().closeFuture().sync();
        } finally {
            /*
             * 优雅关闭线程组
             * shutdownGracefully()会：
             * 1. 不再接受新任务
             * 2. 等待已提交任务完成
             * 3. 释放所有资源
             *
             * 参数说明：
             * quietPeriod: 安静期，单位时间
             * timeout: 超时时间
             * 在安静期内没有任务到达则关闭，否则等待超时
             */
            System.out.println("正在关闭服务器...");
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
            System.out.println("服务器已关闭");
        }
    }

    /**
     * 服务器业务处理器
     * 继承自ChannelInboundHandlerAdapter，处理入站事件
     * 也可以实现ChannelInboundHandler接口
     */
    private static class ServerHandler extends ChannelInboundHandlerAdapter {

        /**
         * 当新的客户端连接建立时调用
         * @param ctx ChannelHandlerContext，包含处理器链的上下文信息
         */
        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            /*
             * 获取客户端地址
             * remoteAddress(): 客户端地址和端口
             * localAddress(): 服务器本地地址和端口
             */
            String clientAddress = ctx.channel().remoteAddress().toString();
            System.out.println("🔗 新客户端连接: " + clientAddress);

            // 统计当前连接数（示例）
            int activeConnections = ((NioEventLoopGroup) ctx.channel().eventLoop().parent()).executorCount();
            System.out.println("当前活跃连接数: " + activeConnections);

            // 向客户端发送欢迎消息
            String welcomeMsg = "欢迎连接到Netty服务器！\r\n" +
                    "服务器时间: " + new java.util.Date() + "\r\n" +
                    "输入 'quit' 断开连接\r\n";
            ctx.writeAndFlush(welcomeMsg);

            /*
             * 将连接信息保存到Channel的属性中
             * Channel.attr()用于给Channel添加自定义属性
             */
            ctx.channel().attr(ChannelAttributes.CLIENT_ID).set("client_" + System.currentTimeMillis());

            // 调用父类方法，确保事件可以继续传播
            super.channelActive(ctx);
        }

        /**
         * 当从客户端读取到数据时调用
         * @param ctx ChannelHandlerContext
         * @param msg 解码后的消息对象（经过StringDecoder处理，这里是String）
         */
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            String message = (String) msg;
            String clientId = ctx.channel().attr(ChannelAttributes.CLIENT_ID).get();

            System.out.println("📥 收到来自 " + clientId + " 的消息: " + message);

            // 处理特殊命令
            String trimmedMsg = message.trim();
            if ("quit".equalsIgnoreCase(trimmedMsg)) {
                handleQuitCommand(ctx);
                return;
            } else if ("ping".equalsIgnoreCase(trimmedMsg)) {
                handlePingCommand(ctx);
                return;
            } else if ("help".equalsIgnoreCase(trimmedMsg)) {
                handleHelpCommand(ctx);
                return;
            }

            // 处理普通消息
            handleNormalMessage(ctx, message);
        }

        /**
         * 处理退出命令
         */
        private void handleQuitCommand(ChannelHandlerContext ctx) {
            String response = "服务器: 连接即将关闭，再见！\r\n";
            ctx.writeAndFlush(response);

            /*
             * 关闭连接
             * 会触发channelInactive()和channelUnregistered()方法
             */
            ctx.close();
        }

        /**
         * 处理Ping命令
         */
        private void handlePingCommand(ChannelHandlerContext ctx) {
            String response = "服务器: Pong! 时间: " + new java.util.Date() + "\r\n";
            ctx.writeAndFlush(response);
        }

        /**
         * 处理帮助命令
         */
        private void handleHelpCommand(ChannelHandlerContext ctx) {
            String helpMsg = "可用命令:\r\n" +
                    "  ping    - 测试连接\r\n" +
                    "  help    - 显示帮助信息\r\n" +
                    "  quit    - 断开连接\r\n" +
                    "  其他    - 原样返回大写形式\r\n";
            ctx.writeAndFlush(helpMsg);
        }

        /**
         * 处理普通消息
         */
        private void handleNormalMessage(ChannelHandlerContext ctx, String message) {
            // 模拟业务处理
            String processedMsg = message.toUpperCase();
            String response = "服务器回应: " + processedMsg + "\r\n";

            /*
             * writeAndFlush()方法：
             * 1. write(): 将数据写入发送缓冲区
             * 2. flush(): 刷新缓冲区，立即发送数据
             *
             * 注意：Netty的写操作是异步的
             * 返回的ChannelFuture可以添加监听器处理发送结果
             */
            ChannelFuture future = ctx.writeAndFlush(response);

            // 添加发送完成监听器
            future.addListener(f -> {
                if (f.isSuccess()) {
                    System.out.println("✅ 消息发送成功: " + processedMsg);
                } else {
                    System.err.println("❌ 消息发送失败: " + f.cause().getMessage());
                }
            });
        }

        /**
         * 当一次读取操作完成时调用
         * 通常用于批量处理后的刷新操作
         */
        @Override
        public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
            /*
             * 刷新通道
             * 将缓冲区中的数据写入SocketChannel
             */
            ctx.flush();

            // 调用父类方法
            super.channelReadComplete(ctx);
        }

        /**
         * 当连接发生异常时调用
         * @param cause 异常对象
         */
        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            System.err.println("❌ 服务器异常 - 客户端: " +
                    ctx.channel().attr(ChannelAttributes.CLIENT_ID).get());
            System.err.println("异常信息: " + cause.getMessage());

            // 打印异常堆栈（生产环境应该记录日志）
            cause.printStackTrace();

            /*
             * 关闭发生异常的连接
             * 防止异常传播，影响其他连接
             */
            ctx.close();
        }

        /**
         * 当客户端连接断开时调用
         */
        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            String clientId = ctx.channel().attr(ChannelAttributes.CLIENT_ID).get();
            System.out.println("🔌 客户端断开连接: " + clientId);

            // 清理资源（如果有的话）
            cleanupResources(ctx);

            super.channelInactive(ctx);
        }

        /**
         * 用户事件触发时调用
         * 用于处理IdleStateHandler触发的事件
         */
        @Override
        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
            if (evt instanceof IdleStateEvent) {
                IdleStateEvent event = (IdleStateEvent) evt;

                switch (event.state()) {
                    case READER_IDLE:
                        System.out.println("⏰ 读空闲超时，关闭连接: " +
                                ctx.channel().attr(ChannelAttributes.CLIENT_ID).get());
                        ctx.close();
                        break;
                    case WRITER_IDLE:
                        // 发送心跳包
                        ctx.writeAndFlush("心跳检测\r\n");
                        break;
                    case ALL_IDLE:
                        // 读写都空闲
                        break;
                }
            }

            super.userEventTriggered(ctx, evt);
        }

        /**
         * 清理资源
         */
        private void cleanupResources(ChannelHandlerContext ctx) {
            // 这里可以清理与连接相关的资源
            // 例如：数据库连接、文件句柄、缓存数据等
            System.out.println("清理客户端资源: " +
                    ctx.channel().attr(ChannelAttributes.CLIENT_ID).get());
        }
    }

    /**
     * Channel属性常量类
     * 用于定义Channel的自定义属性键
     */
    private static class ChannelAttributes {
        static final AttributeKey<String> CLIENT_ID = AttributeKey.valueOf("clientId");
    }

    /**
     * 主方法 - 程序入口
     * @param args 命令行参数：[端口号]，默认8888
     */
    public static void main(String[] args) throws Exception {
        // 默认端口
        int port = 8888;

        // 解析命令行参数
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
                if (port < 1 || port > 65535) {
                    System.err.println("错误: 端口号必须在1-65535之间");
                    System.exit(1);
                }
            } catch (NumberFormatException e) {
                System.err.println("错误: 端口号必须是数字");
                System.exit(1);
            }
        }
        System.out.println("\n" + "=".repeat(50));
        System.out.println("Netty 服务器启动配置");
        System.out.println("端口: " + port);
        System.out.println("Java版本: " + System.getProperty("java.version"));
        System.out.println("Netty版本: 4.1.108.Final");
        System.out.println("=".repeat(50) + "\n");

        // 创建并启动服务器
        NettyServer server = new NettyServer(port);
        server.start();
    }
}
