package selector;


import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

/**
 * @author ljx
 * @version 1.0.0
 * @create 2024/8/13 下午2:37
 */
public class ClientDemo {

    public static void main(String[] args) throws IOException {
        //连接服务器
        SocketChannel socketChannel = SocketChannel.open(new InetSocketAddress("127.0.0.1",8080));
        socketChannel.configureBlocking(false);

        //发送数据
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        buffer.put("hello".getBytes());
        buffer.flip();
        socketChannel.write(buffer);
        buffer.clear();

        //接收数据
        Selector selector = Selector.open();
        socketChannel.register(selector, SelectionKey.OP_READ);
        int read = socketChannel.read(buffer);
        if (read > 0) {
            buffer.flip();
            System.out.println(new String(buffer.array(), 0, read));
            buffer.clear();
        }
    }


}