package selector;


import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.Set;

/**
 * @author ljx
 * @version 1.0.0
 * @create 2024/8/12 下午5:10
 */
public class SelectorDemo {


    public static void main(String[] args) throws IOException {
        Selector selector = Selector.open();
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        //非阻塞
        serverSocketChannel.configureBlocking(false);
        //绑定连接
        serverSocketChannel.bind(new InetSocketAddress(8080));
        //注册channel
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        //选择器的选定键集
        Set<SelectionKey> selectionKeys = selector.selectedKeys();
        selectionKeys.forEach(selectionKey -> {
            System.out.println(selectionKey);
        });
    }
}