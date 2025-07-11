package net.londonderri.autorestart.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import net.londonderri.autorestart.AutoRestart;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ConfigManager {
    public void createConfig() throws IOException {
        FileOutputStream outputStream = new FileOutputStream("./config/autorestart.yml");
        AutoRestart.class.getResourceAsStream("/autorestart.yml").transferTo(outputStream);
        outputStream.close();
    }

    public Config loadConfig() throws IOException {
        return new ObjectMapper(new YAMLFactory()).readValue(new File("./config/autorestart.yml"), Config.class);
    }
}
