import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * @author ljx
 * @version 1.0.0
 * @create 2024/8/6 下午3:19
 */
public class FileReadWrite {

    public static void main(String[] args) throws IOException {
        Path path = Paths.get("r0.txt");
        List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
        for (String line : lines) {
            System.out.println(line);
        }
        Path path1 = Paths.get("w0.txt");
        Files.write(path1, lines, StandardCharsets.UTF_8);
    }
}