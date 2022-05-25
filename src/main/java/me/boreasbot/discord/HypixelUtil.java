package me.boreasbot.discord;

import net.hypixel.api.HypixelAPI;
import net.hypixel.api.apache.ApacheHttpClient;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import java.util.UUID;

public class HypixelUtil {
    public static final HypixelAPI API;

    static {
        String configFilePath = "src/main/resources/config.properties";
        FileInputStream propsInput = null;
        try {
            propsInput = new FileInputStream(configFilePath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        Properties prop = new Properties();
        try {
            prop.load(propsInput);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String key = prop.getProperty("api-key");
        API = new HypixelAPI(new ApacheHttpClient(UUID.fromString(key)));
    }

    public static void await() {
        while (!Thread.interrupted()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
