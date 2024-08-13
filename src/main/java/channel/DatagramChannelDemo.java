package channel;


import org.junit.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.charset.StandardCharsets;

/**
 * @author ljx
 * @version 1.0.0
 * @create 2024/8/6 下午4:17
 */
public class DatagramChannelDemo {


    @Test
    public void send() throws IOException {
        DatagramChannel channel = DatagramChannel.open();
        InetSocketAddress address = new InetSocketAddress("127.0.0.1",8080);
        channel.send(ByteBuffer.wrap("hello world".getBytes()), address);
        System.out.println("发送成功");
    }


    @Test
    public void receive() throws IOException {
        DatagramChannel channel = DatagramChannel.open();
        InetSocketAddress address = new InetSocketAddress(8080);
        channel.bind(address);
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        buffer.clear();
        SocketAddress receive = channel.receive(buffer);
        buffer.flip();
        System.out.println(StandardCharsets.UTF_8.decode(buffer));
    }
}