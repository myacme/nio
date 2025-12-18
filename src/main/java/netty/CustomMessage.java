package netty;


import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.MessageToByteEncoder;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 自定义消息协议
 */
class CustomMessage {
    private int type;
    private int length;
    private String content;

    public CustomMessage(int type, String content) {
        this.type = type;
        this.content = content;
        this.length = content.getBytes(StandardCharsets.UTF_8).length;
    }

    // getters and setters
    public int getType() { return type; }
    public int getLength() { return length; }
    public String getContent() { return content; }
}

/**
 * 自定义编码器
 */
class CustomEncoder extends MessageToByteEncoder<CustomMessage> {
    @Override
    protected void encode(ChannelHandlerContext ctx, CustomMessage msg, ByteBuf out) throws Exception {
        // 协议格式: type(4字节) + length(4字节) + content
        out.writeInt(msg.getType());
        out.writeInt(msg.getLength());
        out.writeBytes(msg.getContent().getBytes(StandardCharsets.UTF_8));
    }
}

/**
 * 自定义解码器
 */
class CustomDecoder extends ByteToMessageDecoder {
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        // 确保有足够的数据
        if (in.readableBytes() < 8) {
            return;
        }

        in.markReaderIndex();
        int type = in.readInt();
        int length = in.readInt();

        // 检查是否接收到完整消息
        if (in.readableBytes() < length) {
            in.resetReaderIndex();
            return;
        }

        byte[] contentBytes = new byte[length];
        in.readBytes(contentBytes);
        String content = new String(contentBytes, StandardCharsets.UTF_8);

        out.add(new CustomMessage(type, content));
    }
}