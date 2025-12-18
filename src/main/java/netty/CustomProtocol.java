package netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.MessageToByteEncoder;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 自定义消息协议
 * 协议格式：
 * +----------------+----------------+----------------+
 * |  消息类型(4B)  |  数据长度(4B)  |   数据内容(NB)  |
 * +----------------+----------------+----------------+
 *
 * 说明：
 * 1. 所有字段都是大端字节序
 * 2. 数据长度 = 数据内容的字节数
 */
public class CustomProtocol {

    /**
     * 消息类型枚举
     */
    public enum MessageType {
        TEXT(1),        // 文本消息
        FILE(2),        // 文件消息
        COMMAND(3),     // 命令消息
        HEARTBEAT(4);   // 心跳消息

        private final int value;

        MessageType(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public static MessageType fromValue(int value) {
            for (MessageType type : values()) {
                if (type.value == value) {
                    return type;
                }
            }
            return TEXT; // 默认返回文本类型
        }
    }

    /**
     * 自定义消息类
     */
    public static class CustomMessage {
        private MessageType type;    // 消息类型
        private int length;          // 数据长度
        private String content;      // 消息内容
        private long timestamp;      // 时间戳

        public CustomMessage(MessageType type, String content) {
            this.type = type;
            this.content = content;
            this.length = content.getBytes(StandardCharsets.UTF_8).length;
            this.timestamp = System.currentTimeMillis();
        }

        // 全参构造函数
        public CustomMessage(MessageType type, int length, String content, long timestamp) {
            this.type = type;
            this.length = length;
            this.content = content;
            this.timestamp = timestamp;
        }

        // Getter方法
        public MessageType getType() { return type; }
        public int getLength() { return length; }
        public String getContent() { return content; }
        public long getTimestamp() { return timestamp; }

        @Override
        public String toString() {
            return String.format("Message[type=%s, length=%d, content='%s', timestamp=%d]",
                    type, length, content, timestamp);
        }
    }

    /**
     * 自定义编码器
     * 将CustomMessage对象编码为字节流
     */
    public static class CustomEncoder extends MessageToByteEncoder<CustomMessage> {

        @Override
        protected void encode(ChannelHandlerContext ctx, CustomMessage msg, ByteBuf out)
                throws Exception {
            /*
             * 编码步骤：
             * 1. 写入消息类型（4字节）
             * 2. 写入数据长度（4字节）
             * 3. 写入时间戳（8字节）
             * 4. 写入数据内容（N字节）
             */

            // 写入消息类型
            out.writeInt(msg.getType().getValue());

            // 写入数据长度
            out.writeInt(msg.getLength());

            // 写入时间戳
            out.writeLong(msg.getTimestamp());

            // 写入数据内容
            if (msg.getLength() > 0) {
                out.writeBytes(msg.getContent().getBytes(StandardCharsets.UTF_8));
            }

            System.out.println("编码消息: " + msg);
        }
    }

    /**
     * 自定义解码器
     * 将字节流解码为CustomMessage对象
     * 注意：解码器需要处理TCP粘包/拆包问题
     */
    public static class CustomDecoder extends ByteToMessageDecoder {

        // 协议头部固定长度：类型(4) + 长度(4) + 时间戳(8) = 16字节
        private static final int HEADER_LENGTH = 16;

        @Override
        protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out)
                throws Exception {
            /*
             * 解码步骤：
             * 1. 检查是否有足够的数据读取头部（16字节）
             * 2. 读取消息类型、数据长度、时间戳
             * 3. 检查是否有足够的数据读取完整消息体
             * 4. 读取数据内容，创建CustomMessage对象
             */

            // 标记当前读取位置
            in.markReaderIndex();

            // 检查是否有足够的数据读取头部
            if (in.readableBytes() < HEADER_LENGTH) {
                // 数据不足，等待更多数据
                System.out.println("数据不足，等待更多数据...");
                return;
            }

            // 读取头部信息（注意：不能改变读指针位置）
            int typeValue = in.readInt();
            int length = in.readInt();
            long timestamp = in.readLong();

            // 验证数据长度（防止恶意攻击）
            if (length < 0 || length > 1024 * 1024) { // 限制1MB
                System.err.println("数据长度异常: " + length);
                in.skipBytes(in.readableBytes()); // 跳过所有数据
                return;
            }

            // 检查是否有足够的数据读取消息体
            if (in.readableBytes() < length) {
                // 数据不足，重置读指针，等待更多数据
                in.resetReaderIndex();
                System.out.println("消息体数据不足，等待更多数据...");
                return;
            }

            // 读取消息体
            byte[] contentBytes = new byte[length];
            in.readBytes(contentBytes);
            String content = new String(contentBytes, StandardCharsets.UTF_8);

            // 创建消息对象
            MessageType type = MessageType.fromValue(typeValue);
            CustomMessage message = new CustomMessage(type, length, content, timestamp);

            // 添加到输出列表
            out.add(message);

            System.out.println("解码消息: " + message);

            /*
             * 注意：这里可能需要处理多个消息
             * 因为一个ByteBuf中可能包含多个完整的消息
             * 继续解码直到没有足够的数据
             */
            if (in.readableBytes() >= HEADER_LENGTH) {
                // 递归解码剩余数据
                decode(ctx, in, out);
            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            System.err.println("解码器异常: " + cause.getMessage());
            super.exceptionCaught(ctx, cause);
        }
    }
}