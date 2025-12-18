package selector;


import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * @author ljx
 * @version 1.0.0
 * @create 2024/8/13 下午2:37
 */
public class ServerDemo {

    public static void main(String[] args) throws IOException {
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.bind(new InetSocketAddress(8080));
        Selector selector = Selector.open();
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        while (selector.select() > 0) {
            Set<SelectionKey> selectionKeys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = selectionKeys.iterator();
            while (iterator.hasNext()) {
                SelectionKey next = iterator.next();
                if (next.isAcceptable()) {
                    SocketChannel accept = serverSocketChannel.accept();
                    accept.configureBlocking(false);
                    accept.register(selector, SelectionKey.OP_READ);
                    System.out.println("客户端连接成功");
                    accept.write(ByteBuffer.wrap("你已连接到服务器！".getBytes()));
                } else if (next.isReadable()) {
                    SocketChannel channel = (SocketChannel) next.channel();
                    ByteBuffer buffer = ByteBuffer.allocate(1024);
                    int read = channel.read(buffer);
                    String msg = null;
                    if (read > 0) {
                        buffer.flip();
                        msg = new String(buffer.array(), 0, read);
                        System.out.println(msg);
                        buffer.clear();
                    }
                    //广播消息
                    channel.register(selector, SelectionKey.OP_WRITE);
                    for (SelectionKey key : selector.keys()) {
                        if (key.isValid() && key.channel() instanceof SocketChannel) {
                            SocketChannel targetChannel = (SocketChannel) key.channel();
                            targetChannel.write(ByteBuffer.wrap(("广播消息：" + msg).getBytes()));
                        }
                    }
                }
                iterator.remove();
            }
        }
    }
}