package pipe;


import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Pipe;

/**
 * @author ljx
 * @version 1.0.0
 * @create 2024/9/4 下午3:32
 */
public class PipeDemo {
    public static void main(String[] args) throws IOException {
        Pipe pipe = Pipe.open();
        Pipe.SinkChannel sink = pipe.sink();
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        buffer.put("hello".getBytes());
        buffer.flip();
        sink.write(buffer);
        Pipe.SourceChannel source = pipe.source();
        ByteBuffer buffer1 = ByteBuffer.allocate(1024);
        int read = source.read(buffer1);
        System.out.println(new String(buffer1.array(),0,read));
        buffer1.clear();
        source.close();
        buffer.clear();
        sink.close();
    }


}
