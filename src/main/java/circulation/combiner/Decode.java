package circulation.combiner;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class Decode {

    static final Path JAR_FILE = getJarFile().toPath();
    static final Path cleanroomFile = JAR_FILE.resolve("cleanroom");
    static final Path forgeFile = JAR_FILE.resolve("forge");
    static final Path sameFile = JAR_FILE.resolve("same");
    static final Path temporaryFile = JAR_FILE.resolve("temporary");
    static final Path buildFile = JAR_FILE.resolve("build");

    private static File getJarFile() {
        try {
            String encodedPath = Decode.class.getProtectionDomain()
                    .getCodeSource().getLocation().getFile();
            String decodedPath = URLDecoder.decode(encodedPath, StandardCharsets.UTF_8);

            File jarFile = new File(decodedPath);
            return jarFile.isFile() ? jarFile.getParentFile() : jarFile;
        } catch (Exception e) {
            throw new RuntimeException("路径解析失败", e);
        }
    }

    static {
        try {
            Files.createDirectories(temporaryFile);
            Files.createDirectories(buildFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}