package charset;


import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.util.SortedMap;

/**
 * @author ljx
 * @version 1.0.0
 * @create 2024/9/4 下午4:20
 */
public class CharsetDemo {
    public static void main(String[] args) throws CharacterCodingException {
        //获取charset
        Charset charset = Charset.forName("utf-8");
        //获取编码器
        CharsetEncoder charsetEncoder = charset.newEncoder();
        //创建缓冲区
        CharBuffer buffer = CharBuffer.allocate(1024);
        buffer.put("hello world");
        buffer.flip();
        //编码
        ByteBuffer byteBuffer = charsetEncoder.encode(buffer);
        System.out.println("编码结果：");
        for (int i = 0; i < byteBuffer.limit(); i++) {
            System.out.println(byteBuffer.get());
        }
        //获取解码器
        byteBuffer.flip();
        CharsetDecoder charsetDecoder = charset.newDecoder();
        //解码
        CharBuffer charBuffer = charsetDecoder.decode(byteBuffer);
        System.out.println("解码结果：");
        System.out.println(charBuffer);

        //获取所有字符编码
        SortedMap<String, Charset> map = Charset.availableCharsets();
        System.out.println("所有字符编码：");
        map.forEach((k,v)->{
            System.out.println(k + ":" + v);
        });
    }
}
