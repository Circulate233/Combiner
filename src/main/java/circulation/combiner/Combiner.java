package circulation.combiner;

import com.google.gson.*;

import java.io.*;
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

            var forge = "NovaEngineering-World-" + input;
            var cleanroom = "NovaEngineering-World-" + input + "-cleanroom";

            VersionWriting(input,false);
            compressDirectoryToZip(Decode.temporaryFile,Decode.buildFile.resolve(forge + ".zip"));

            copyWithXCopyBehavior(Decode.cleanroomFile);
            VersionWriting(input,true);
            compressDirectoryToZip(Decode.temporaryFile,Decode.buildFile.resolve(cleanroom + ".zip"));

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

    public static void VersionWriting(String version,boolean isCrl){
        var source = Decode.temporaryFile.resolve("manifest.json");
        try (FileReader reader = new FileReader(source.toFile())) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            var name = "NovaEngineering-World-" + version;
            if (isCrl){
                name += "-cleanroom";
            }
            root.addProperty("name",name);
            root.addProperty("version",version);
            try (FileWriter writer = new FileWriter(source.toFile())) {
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                gson.toJson(root, writer);
            } catch (IOException ignored) {

            }
        } catch (IOException ignored) {

        }
    }

}
