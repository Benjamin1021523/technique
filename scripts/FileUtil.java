import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Stream;

public class FileUtil {

    public static void main(String[] args) throws IOException {
        String content = readContent(Path.of("docs/java/.order"));
        System.out.println(content);

        LinkedHashMap<String, List<String>> nameMap = new LinkedHashMap<>();
        for (String next : Stream.of(content.split("\n")).map(String::trim).toList()) {
             String nextContent = readContent(Path.of("docs/java/" + next.trim() + "/.order"));
             System.out.println(nextContent);
             nameMap.put(next.trim(), Stream.of(nextContent.split("\n")).map(String::trim).toList());
        }
        System.out.println(nameMap);
    }

    public static String readContent(Path path) throws IOException {
        if (!Files.exists(path)) {
            return "";
        }
        return Files.readString(path, java.nio.charset.StandardCharsets.UTF_8).trim();
    }
}
