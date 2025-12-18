package netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.timeout.IdleStateHandler;

import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

/**
 * Netty客户端示例
 * 主要功能：
 * 1. 连接到服务器
 * 2. 发送消息到服务器
 * 3. 接收服务器响应
 */
public class NettyClient {

    /*
     * 服务器地址
     */
    private final String host;

    /*
     * 服务器端口

     */
    private final int port;

    /*
     * 与服务器的连接通道

     */
    private Channel channel;

    /**
     * 构造函数
     *
     * @param host 服务器主机地址
     * @param port 服务器端口号
     */
    public NettyClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    /**
     * 连接到服务器
     *
     * @return 连接是否成功
     */
    public boolean connect() throws Exception {
        /*
         * EventLoopGroup处理所有I/O操作
         * 对于客户端，通常只需要一个EventLoopGroup
         */
        EventLoopGroup group = new NioEventLoopGroup();

        try {
            /*
             * Bootstrap是客户端的启动引导类
             * 与服务端的ServerBootstrap对应
             */
            Bootstrap bootstrap = new Bootstrap();
            // 设置线程组
            bootstrap.group(group)
                    // 使用NIO Socket通道
                    .channel(NioSocketChannel.class)
                    /*
                     * TCP_NODELAY: 禁用Nagle算法，立即发送小数据包
                     * 对于实时性要求高的应用很重要
                     */
                    .option(ChannelOption.TCP_NODELAY, true)
                    /*
                     * SO_KEEPALIVE: 启用TCP心跳机制
                     */
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    /*
                     * CONNECT_TIMEOUT_MILLIS: 连接超时时间（毫秒）
                     */
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                    /*
                     * 设置通道处理器
                     */
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();

                            // 添加编解码器
                            pipeline.addLast("decoder", new StringDecoder(StandardCharsets.UTF_8));
                            pipeline.addLast("encoder", new StringEncoder(StandardCharsets.UTF_8));

                            /*
                             * 添加空闲状态检测处理器
                             * 参数说明：
                             * 1. readerIdleTime: 读空闲时间（秒），0表示禁用
                             * 2. writerIdleTime: 写空闲时间（秒）
                             * 3. allIdleTime: 所有类型空闲时间（秒）
                             * 当连接空闲时间超过设定值，会触发IdleStateEvent事件
                             */
                            pipeline.addLast("idleStateHandler",
                                    new IdleStateHandler(0, 30, 0, TimeUnit.SECONDS));

                            // 添加自定义业务处理器
                            pipeline.addLast("clientHandler", new ClientHandler());
                        }
                    });

            System.out.println("正在连接到服务器 " + host + ":" + port + "...");

            /*
             * connect(): 异步连接到服务器
             * sync(): 等待连接完成
             * 返回的ChannelFuture包含连接结果
             */
            ChannelFuture future = bootstrap.connect(host, port).sync();

            if (future.isSuccess()) {
                this.channel = future.channel();
                System.out.println("✅ 连接服务器成功!");
                System.out.println("本地地址: " + channel.localAddress());
                System.out.println("远程地址: " + channel.remoteAddress());
                return true;
            } else {
                System.err.println("❌ 连接服务器失败: " + future.cause().getMessage());
                return false;
            }
        } catch (Exception e) {
            System.err.println("❌ 连接过程中发生异常: " + e.getMessage());
            throw e;
        }
        // 注意：这里没有关闭group，因为连接需要保持
    }

    /**
     * 发送消息到服务器
     *
     * @param message 要发送的消息
     */
    public void sendMessage(String message) {
        if (channel == null || !channel.isActive()) {
            System.err.println("错误: 连接未建立或已断开");
            return;
        }

        // 添加回车换行符，便于服务器按行读取
        String msgWithNewline = message + "\r\n";

        /*
         * 异步发送消息
         * 发送操作不会阻塞当前线程
         */
        ChannelFuture future = channel.writeAndFlush(msgWithNewline);

        // 添加发送结果监听器
        future.addListener(f -> {
            if (f.isSuccess()) {
                System.out.println("✅ 消息发送成功: " + message);
            } else {
                System.err.println("❌ 消息发送失败: " + f.cause().getMessage());
            }
        });
    }

    /**
     * 断开连接
     */
    public void disconnect() {
        if (channel != null && channel.isActive()) {
            System.out.println("正在断开连接...");
            channel.close().awaitUninterruptibly();
            System.out.println("连接已断开");
        }
    }

    /**
     * 检查连接状态
     */
    public boolean isConnected() {
        return channel != null && channel.isActive();
    }

    /**
     * 启动交互式客户端
     */
    public void startInteractiveMode() {
        if (!isConnected()) {
            System.err.println("错误: 请先建立连接");
            return;
        }

        System.out.println("\n" + "=".repeat(50));
        System.out.println("Netty 客户端交互模式");
        System.out.println("输入 'quit' 退出客户端");
        System.out.println("输入 'help' 查看可用命令");
        System.out.println("=".repeat(50) + "\n");

        Scanner scanner = new Scanner(System.in);

        while (isConnected()) {
            try {
                System.out.print("请输入消息: ");
                String input = scanner.nextLine().trim();

                if (input.isEmpty()) {
                    continue;
                }

                // 处理特殊命令
                if ("quit".equalsIgnoreCase(input)) {
                    System.out.println("正在退出...");
                    // 通知服务器
                    sendMessage("quit");
                    // 等待消息发送
                    Thread.sleep(500);
                    break;
                } else if ("help".equalsIgnoreCase(input)) {
                    printHelp();
                    continue;
                } else if ("status".equalsIgnoreCase(input)) {
                    printConnectionStatus();
                    continue;
                }

                // 发送普通消息
                sendMessage(input);
            } catch (Exception e) {
                System.err.println("输入处理异常: " + e.getMessage());
                break;
            }
        }

        scanner.close();
        disconnect();
    }

    /**
     * 打印帮助信息
     */
    private void printHelp() {
        System.out.println("\n可用命令:");
        System.out.println("  quit    - 退出客户端");
        System.out.println("  help    - 显示帮助信息");
        System.out.println("  status  - 显示连接状态");
        System.out.println("  其他    - 发送消息到服务器\n");
    }

    /**
     * 打印连接状态
     */
    private void printConnectionStatus() {
        if (channel != null) {
            System.out.println("连接状态:");
            System.out.println("  是否活跃: " + channel.isActive());
            System.out.println("  是否可写: " + channel.isWritable());
            System.out.println("  是否打开: " + channel.isOpen());
            System.out.println("  本地地址: " + channel.localAddress());
            System.out.println("  远程地址: " + channel.remoteAddress());
        } else {
            System.out.println("未建立连接");
        }
    }

    /**
     * 客户端业务处理器
     */
    private static class ClientHandler extends ChannelInboundHandlerAdapter {

        /**
         * 连接建立成功后调用
         */
        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            System.out.println("✅ 连接已激活");
            super.channelActive(ctx);
        }

        /**
         * 接收到服务器消息时调用
         */
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            String response = (String) msg;
            System.out.println("\n📥 服务器响应: " + response.trim());
            System.out.print("请输入消息: "); // 重新显示提示符
        }

        /**
         * 读取完成时调用
         */
        @Override
        public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
            ctx.flush();
            super.channelReadComplete(ctx);
        }

        /**
         * 发生异常时调用
         */
        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            System.err.println("❌ 客户端异常: " + cause.getMessage());
            cause.printStackTrace();
            ctx.close();
        }

        /**
         * 连接断开时调用
         */
        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            System.out.println("🔌 连接已断开");
            super.channelInactive(ctx);
        }

        /**
         * 用户事件触发（如空闲检测）
         */
        @Override
        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
            if (evt instanceof io.netty.handler.timeout.IdleStateEvent) {
                io.netty.handler.timeout.IdleStateEvent event =
                        (io.netty.handler.timeout.IdleStateEvent) evt;

                if (event.state() == io.netty.handler.timeout.IdleState.WRITER_IDLE) {
                    // 发送心跳包保持连接
                    ctx.writeAndFlush("ping\r\n");
                    System.out.println("发送心跳包...");
                }
            }

            super.userEventTriggered(ctx, evt);
        }
    }

    /**
     * 主方法 - 程序入口
     *
     * @param args 命令行参数：[服务器地址] [端口号]
     */
    public static void main(String[] args) throws Exception {
        // 默认配置
        String host = "127.0.0.1";
        int port = 8888;

        // 解析命令行参数
        if (args.length >= 1) {
            host = args[0];
        }
        if (args.length >= 2) {
            try {
                port = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                System.err.println("错误: 端口号必须是数字，使用默认端口8888");
            }
        }

        System.out.println("=".repeat(50));
        System.out.println("Netty 客户端启动配置");
        System.out.println("服务器: " + host + ":" + port);
        System.out.println("=".repeat(50));

        // 创建客户端
        NettyClient client = new NettyClient(host, port);

        try {
            // 连接服务器
            if (client.connect()) {
                // 启动交互模式
                client.startInteractiveMode();
            } else {
                System.err.println("无法连接到服务器，程序退出");
            }
        } catch (Exception e) {
            System.err.println("客户端运行异常: " + e.getMessage());
            e.printStackTrace();
        }
    }
}