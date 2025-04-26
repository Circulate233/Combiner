package circulation.combiner;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Scanner;
import java.util.zip.*;

public class Combiner {

    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {

            System.out.print("输入版本名称: ");
            String input = scanner.nextLine().trim().toLowerCase();

            if (input.isEmpty()) {
                return;
            }

            ClearHandler.cleanDirectory(Decode.temporaryFile);

            copyWithXCopyBehavior(Decode.sameFile);
            copyWithXCopyBehavior(Decode.forgeFile);

            compressDirectoryToZip(Decode.temporaryFile,Decode.buildFile.resolve("NovaEngineering-World-" + input + ".zip"));

            copyWithXCopyBehavior(Decode.cleanroomFile);
            compressDirectoryToZip(Decode.temporaryFile,Decode.buildFile.resolve("NovaEngineering-World-" + input + "-cleanroom.zip"));

            ClearHandler.cleanDirectory(Decode.temporaryFile);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void copyWithXCopyBehavior(Path source) throws IOException {
        if (!Files.exists(source)) {
            throw new IOException("源目录不存在: " + source);
        }
        if (!Files.isDirectory(source)) {
            throw new IOException("源路径不是目录: " + source);
        }

        var target = Decode.temporaryFile;

        Files.createDirectories(target);

        Files.walkFileTree(source, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                Path relativePath = source.relativize(dir);
                Path targetDir = target.resolve(relativePath);

                if (!Files.exists(targetDir)) {
                    Files.createDirectories(targetDir);
                    System.out.println("创建目录: " + targetDir);
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Path relativePath = source.relativize(file);
                Path targetFile = target.resolve(relativePath);

                Files.copy(file, targetFile, StandardCopyOption.REPLACE_EXISTING);
                System.out.println("复制文件: " + targetFile);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) {
                System.err.println("无法访问文件: " + file + " (" + exc.getMessage() + ")");
                return FileVisitResult.CONTINUE;
            }
        });
    }

    public static void compressDirectoryToZip(Path sourceDir, Path zipOutputPath) throws IOException {
        try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(zipOutputPath))) {
            Files.walkFileTree(sourceDir, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    // 跳过根目录本身
                    if (!dir.equals(sourceDir)) {
                        Path relativePath = sourceDir.relativize(dir);
                        String entryName = relativePath.toString().replace(File.separator, "/") + "/";
                        zos.putNextEntry(new ZipEntry(entryName));
                        zos.closeEntry();
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Path relativePath = sourceDir.relativize(file);
                    String entryName = relativePath.toString().replace(File.separator, "/");

                    zos.putNextEntry(new ZipEntry(entryName));
                    Files.copy(file, zos);
                    zos.closeEntry();
                    return FileVisitResult.CONTINUE;
                }
            });
        }
    }

}
