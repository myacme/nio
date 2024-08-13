package channel;


import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * @author ljx
 * @version 1.0.0
 * @create 2024/8/6 下午2:43
 */
public class FileChannelDemo {


    public static void read() throws Exception {
        RandomAccessFile rafile = new RandomAccessFile("r0.txt", "rw");
        FileChannel channel = rafile.getChannel();
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        int size = channel.read(buffer);
        while (size != -1) {
            buffer.flip();
            while (buffer.hasRemaining()) {
                System.out.println(StandardCharsets.UTF_8.decode(buffer));
            }
            buffer.clear();
            size = channel.read(buffer);
        }
        channel.close();
        rafile.close();
    }

    public static void write() throws Exception {
        RandomAccessFile rafile = new RandomAccessFile("w0.txt", "rw");
        FileChannel channel = rafile.getChannel();
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        buffer.clear();
        buffer.put("hello world".getBytes());
        buffer.flip();
        while (buffer.hasRemaining()) {
            channel.write(buffer);
        }
        channel.close();
        rafile.close();
    }

    public static void transfer() throws Exception {
        RandomAccessFile rafile = new RandomAccessFile("r0.txt", "rw");
        FileChannel channel = rafile.getChannel();
        RandomAccessFile rafile2 = new RandomAccessFile("w0.txt", "rw");
        FileChannel channel2 = rafile2.getChannel();
        channel.transferTo(0, channel.size(), channel2);
        channel2.transferFrom(channel, 0, channel.size());
        channel2.close();
        channel.close();
        rafile2.close();
        rafile.close();
    }

    public static void main(String[] args) throws Exception {
        read();
    }
}