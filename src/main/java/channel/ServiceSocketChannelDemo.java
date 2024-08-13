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
public class ServiceSocketChannelDemo {

    public static void main(String[] args) throws IOException {
        int port = 8080;
        ByteBuffer wrap = ByteBuffer.wrap("heoll world".getBytes());
        ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.socket().bind(new InetSocketAddress(port));
        //设置非阻塞模式
        ssc.configureBlocking(false);
        while (true) {
            SocketChannel accept = ssc.accept();
            if (accept != null) {
                System.out.println(accept.socket().getLocalAddress());
                wrap.rewind();
                accept.write(wrap);
                accept.close();
            }
        }
    }
}