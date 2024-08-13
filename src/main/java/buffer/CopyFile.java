package buffer;


import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

/**
 * @author ljx
 * @version 1.0.0
 * @create 2024/8/12 下午3:02
 */
public class CopyFile {
    /**
     * 创建 3-6 的子缓冲区
     * buffer.position(3);
     * buffer.limit(6);
     * 缓冲区分片
     * ByteBuffer slice = buffer.slice();
     *
     */


    /**
     * @param args
     * @throws IOException
     */


    public static void main(String[] args) throws IOException {
        FileInputStream fis = new FileInputStream("r0.txt");
        FileChannel fisChannel = fis.getChannel();
        FileOutputStream fos = new FileOutputStream("w0.txt");
        FileChannel fosChannel = fos.getChannel();
        //内存映射   零拷贝
        MappedByteBuffer mbb = fisChannel.map(FileChannel.MapMode.READ_ONLY, 0, fisChannel.size());
        //直接缓冲区  零拷贝
        ByteBuffer direct = ByteBuffer.allocateDirect(1024);
        while (true) {
            direct.clear();
            int read = fisChannel.read(direct);
            if (read == -1) {
                break;
            }
            direct.flip();
            fosChannel.write(direct);
        }
        fosChannel.close();
        fos.close();
        fisChannel.close();
        fis.close();
    }
}