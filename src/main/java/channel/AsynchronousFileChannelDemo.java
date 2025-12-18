package channel;


import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.Future;

/**
 * @author ljx
 * @version 1.0.0
 * @create 2024/9/4 下午4:02
 */
public class AsynchronousFileChannelDemo {

    public static void main(String[] args) throws IOException {
        writeUseCompletionHandler();
    }

    public static void readUseFuture() throws IOException {
        Path path = Paths.get("r0.txt");
        AsynchronousFileChannel channel = AsynchronousFileChannel.open(path, StandardOpenOption.READ);
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        Future<Integer> read = channel.read(buffer, 0);
        while (!read.isDone()) {
            System.out.println("waiting...");
        }
        buffer.flip();
        byte[] bytes = new byte[buffer.limit()];
        buffer.get(bytes);
        System.out.println(new String(bytes));
        buffer.clear();
    }

    public static void writeUseFuture() throws IOException {
        Path path = Paths.get("w0.txt");
        AsynchronousFileChannel channel = AsynchronousFileChannel.open(path, StandardOpenOption.WRITE);
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        buffer.put("hello world use Future".getBytes());
        buffer.flip();
        Future<Integer> write = channel.write(buffer, 0);
        while (!write.isDone()) {
            System.out.println("waiting...");
        }
        buffer.clear();
        System.out.println("write success");
    }

    public static void readUseCompletionHandler() throws IOException {
        Path path = Paths.get("r0.txt");
        AsynchronousFileChannel channel = AsynchronousFileChannel.open(path, StandardOpenOption.READ);
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        channel.read(buffer, 0, buffer, new CompletionHandler<Integer, ByteBuffer>() {
            @Override
            public void completed(Integer result, ByteBuffer attachment) {
                System.out.println("result:" + result);
                attachment.flip();
                byte[] bytes = new byte[buffer.limit()];
                buffer.get(bytes);
                System.out.println(new String(bytes));
                buffer.clear();
            }

            @Override
            public void failed(Throwable exc, ByteBuffer attachment) {
                System.out.println("failed");
            }
        });
    }

    public static void writeUseCompletionHandler() throws IOException {
        Path path = Paths.get("w0.txt");
        AsynchronousFileChannel channel = AsynchronousFileChannel.open(path, StandardOpenOption.WRITE);
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        buffer.put("hello world use CompletionHandler".getBytes());
        buffer.flip();
        channel.write(buffer, 0, buffer, new CompletionHandler<Integer, ByteBuffer>() {
            @Override
            public void completed(Integer result, ByteBuffer attachment) {

                System.out.println("write success");
                buffer.clear();
            }

            @Override
            public void failed(Throwable exc, ByteBuffer attachment) {
                System.out.println("failed");
            }
        });
    }
}
