package channel;


import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

/**
 * @author ljx
 * @version 1.0.0
 * @create 2024/8/6 下午3:36
 */
public class SocketChannelDemo {

    public static void main(String[] args) throws IOException {
        int port = 8080;
        SocketChannel sc = SocketChannel.open(new InetSocketAddress(port));
//        SocketChannel sc = SocketChannel.open();
//        sc.connect(new InetSocketAddress(port));
        //设置非阻塞模式
        sc.configureBlocking(false);
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        sc.read(buffer);
        sc.close();
    }
}