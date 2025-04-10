package circulation.combiner;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

public class ClearHandler {

    public static void cleanDirectory(Path targetDir) throws IOException {

        if (!Files.exists(targetDir)) {
            Files.createDirectories(targetDir);
            return;
        }

        Files.walkFileTree(targetDir, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                deleteWithRetry(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
                if (!dir.equals(targetDir)) {
                    deleteWithRetry(dir);
                }
                return FileVisitResult.CONTINUE;
            }
        });
    }

    private static void deleteWithRetry(Path path) {
        try {
            Files.delete(path);
            System.out.println("已删除: " + path);
        } catch (IOException ignored) {
        }
    }
}
