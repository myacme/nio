package netty;

import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 连接管理器
 * 用于管理所有客户端连接
 * @author MyAcme
 */
public class ConnectionManager {

    // 单例实例
    private static final ConnectionManager INSTANCE = new ConnectionManager();

    /**
     * ChannelGroup: Netty提供的Channel组管理工具
     * 可以批量操作所有Channel（如广播消息）
     */
    private final ChannelGroup allChannels =
            new DefaultChannelGroup("all-channels", GlobalEventExecutor.INSTANCE);

    /**
     * 连接映射表
     * Key: Channel ID
     * Value: 连接信息
     */
    private final Map<String, ConnectionInfo> connections =
            new ConcurrentHashMap<>();

    /**
     * 连接计数器
     */
    private final AtomicInteger connectionCounter = new AtomicInteger(0);

    /**
     * 私有构造函数（单例模式）
     */
    private ConnectionManager() {}

    /**
     * 获取单例实例
     */
    public static ConnectionManager getInstance() {
        return INSTANCE;
    }

    /**
     * 添加新连接
     */
    public void addConnection(Channel channel) {
        String channelId = channel.id().asShortText();

        ConnectionInfo info = new ConnectionInfo(
                channelId,
                channel.remoteAddress().toString(),
                System.currentTimeMillis()
        );

        connections.put(channelId, info);
        allChannels.add(channel);

        int count = connectionCounter.incrementAndGet();
        System.out.println("添加连接: " + info + ", 当前连接数: " + count);
    }

    /**
     * 移除连接
     */
    public void removeConnection(Channel channel) {
        String channelId = channel.id().asShortText();

        ConnectionInfo info = connections.remove(channelId);
        allChannels.remove(channel);

        if (info != null) {
            int count = connectionCounter.decrementAndGet();
            System.out.println("移除连接: " + info + ", 当前连接数: " + count);
        }
    }

    /**
     * 获取连接信息
     */
    public ConnectionInfo getConnectionInfo(String channelId) {
        return connections.get(channelId);
    }

    /**
     * 获取所有活跃连接
     */
    public ChannelGroup getAllChannels() {
        return allChannels;
    }

    /**
     * 获取连接数量
     */
    public int getConnectionCount() {
        return connectionCounter.get();
    }

    /**
     * 向所有客户端广播消息
     */
    public void broadcast(String message) {
        if (allChannels.isEmpty()) {
            System.out.println("没有活跃连接，无法广播");
            return;
        }

        System.out.println("广播消息到 " + allChannels.size() + " 个客户端: " + message);
        allChannels.writeAndFlush(message + "\r\n");
    }

    /**
     * 连接信息类
     */
    public static class ConnectionInfo {
        private final String channelId;
        private final String remoteAddress;
        private final long connectTime;
        private long lastActivityTime;

        public ConnectionInfo(String channelId, String remoteAddress, long connectTime) {
            this.channelId = channelId;
            this.remoteAddress = remoteAddress;
            this.connectTime = connectTime;
            this.lastActivityTime = connectTime;
        }

        public void updateActivityTime() {
            this.lastActivityTime = System.currentTimeMillis();
        }

        // Getter方法
        public String getChannelId() { return channelId; }
        public String getRemoteAddress() { return remoteAddress; }
        public long getConnectTime() { return connectTime; }
        public long getLastActivityTime() { return lastActivityTime; }

        @Override
        public String toString() {
            return String.format("Connection[channelId=%s, address=%s, connectTime=%d]",
                    channelId, remoteAddress, connectTime);
        }
    }
}