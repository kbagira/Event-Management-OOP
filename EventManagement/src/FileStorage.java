import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;


final class FileStorage {
    static final Path DATA_DIR = Paths.get("data");

    private FileStorage() { }

    static List<String> readLines(Path path) throws IOException {
        List<String> result = new ArrayList<>();
        if (!Files.exists(path)) return result;
        for (String line : Files.readAllLines(path)) {
            line = line.replace("\uFEFF", "").trim(); // BOM из Блокнота Windows
            if (!line.isEmpty()) result.add(line);
        }
        return result;
    }

    static void writeLines(Path path, List<String> lines) throws IOException {
        Files.createDirectories(DATA_DIR);
        Files.write(path, lines);
    }
}
