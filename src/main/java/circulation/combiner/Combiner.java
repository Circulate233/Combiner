package circulation.combiner;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;

public class Combiner {

    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            Set<Decode.Pack> workPacks = Decode.packs.stream()
                    .filter(Decode.Pack::work)
                    .collect(Collectors.toSet());

            System.out.println("当前配置下将打包：");
            workPacks.forEach(p -> System.out.println(" - " + p.name()));
            System.out.println("\n即将执行以下操作：");
            System.out.println("1. 清理输出目录");
            System.out.println("2. 将设置开启的目录合并至目标目录");

            System.out.print("是否继续执行？(y/n) > ");
            String input = scanner.nextLine().trim().toLowerCase();

            if (!input.equals("y")) {
                System.out.println("操作已取消");
                return;
            }
            ClearHandler.cleanDirectory(Decode.targetFile.toPath());

            for (Decode.Pack pack : workPacks) {
                copyWithXCopyBehavior(pack.getPath());
                System.out.println("复制操作成功完成");
            }
            var log = new File(Decode.targetFile,"更新日志.txt");
            if (log.exists()) {
                ClearHandler.deleteWithRetry(log.toPath());
            }
        } catch (IOException e) {
            System.err.println("复制失败: " + e.getMessage());
        }
    }

    public static void copyWithXCopyBehavior(Path source) throws IOException {
        if (!Files.exists(source)) {
            throw new IOException("源目录不存在: " + source);
        }
        if (!Files.isDirectory(source)) {
            throw new IOException("源路径不是目录: " + source);
        }

        var target = Decode.targetFile.toPath();

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

}
