package circulation.combiner;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Decode {

    static final File JAR_FILE = getJarFile();
    static final File configFile = new File(JAR_FILE,"config.json");
    static final File sourceFile = new File(JAR_FILE,"source");
    static final File targetFile = new File(JAR_FILE,"target");

    public static List<Pack> packs = new ArrayList<>();

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
        if (configFile.exists()) {
            try {
                Files.createDirectories(targetFile.toPath());
                Path configPath = configFile.toPath();
                Gson packGson = (new GsonBuilder()).disableHtmlEscaping().setPrettyPrinting().create();
                packs.addAll(
                        packGson.fromJson(
                                new String(Files.readAllBytes(configPath)),
                                new TypeToken<List<Pack>>(){}
                                        .getType()
                        )
                );
            } catch (IOException e) {
                throw new RuntimeException("配置文件读取失败: " + configFile, e);
            }
        } else {
            throw new RuntimeException("配置文件不存在: " + configFile.getAbsolutePath());
        }
    }

    public record Pack(String name, boolean work) {

        public Path getPath() {
            return sourceFile.toPath().resolve(name);
        }

    }
}