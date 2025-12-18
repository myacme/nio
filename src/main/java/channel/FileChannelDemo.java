package channel;


import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

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

    /**
     * 文件复制Files.copy
     * @throws IOException
     */
    public void FilesCopy() throws IOException {
        Path source = Paths.get("r0.txt");
        Path target = Paths.get("w0.txt");
        Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
    }

    /**
     * 零拷贝
     *
     * @throws Exception
     */
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

    /**
     * 内存映射mmap
     *
     * @throws IOException
     */
    public void map() throws IOException {
        try (RandomAccessFile sourceFile = new RandomAccessFile("r0.txt", "r");
             RandomAccessFile targetFile = new RandomAccessFile("w0.txt", "rw")) {
            FileChannel sourceChannel = sourceFile.getChannel();
            MappedByteBuffer buffer = sourceChannel.map(FileChannel.MapMode.READ_ONLY, 0, sourceChannel.size());
            targetFile.getChannel().write(buffer);
        }
    }

    public static void main(String[] args) throws Exception {
        read();
    }
}