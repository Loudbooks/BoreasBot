package me.boreasbot.discord;

import net.hypixel.api.HypixelAPI;
import net.hypixel.api.apache.ApacheHttpClient;

import java.util.UUID;

public class HypixelUtil {
    public static final HypixelAPI API;

    static {
        String key = System.getProperty("apiKey", "33d30862-9581-498e-aad3-ef23efefba11");
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
