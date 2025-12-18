
package filelock;


import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

/**
 * @author ljx
 * @version 1.0.0
 * @create 2024/9/4 下午3:41
 */
public class FileLockDemo {
    public static void main(String[] args) throws Exception {
        RandomAccessFile rafile = new RandomAccessFile("w0.txt", "rw");
        FileChannel channel = rafile.getChannel();
        //独占锁
        FileLock lock = channel.lock();
        //共享锁 channel.lock(0, Long.MAX_VALUE, false);
        System.out.println("是否共享锁" + lock.isShared());
        channel.write(ByteBuffer.wrap("hello world".getBytes()));
        channel.close();
        System.out.println("文件锁成功");
    }
}
