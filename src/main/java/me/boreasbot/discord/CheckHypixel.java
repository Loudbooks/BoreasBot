package me.boreasbot.discord;

import com.google.gson.JsonObject;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.hypixel.api.HypixelAPI;
import net.hypixel.api.apache.ApacheHttpClient;
import net.hypixel.api.reply.PlayerReply;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class CheckHypixel {
    public static String checkUser(String name, SlashCommandInteractionEvent hook, User user) throws IOException, ExecutionException, InterruptedException {
        String configFilePath = "src/main/resources/config.properties";
        FileInputStream propsInput = new FileInputStream(configFilePath);
        Properties prop = new Properties();
        prop.load(propsInput);
        String key = System.getProperty("apiKey", prop.getProperty("api-key"));
        HypixelAPI api  = new HypixelAPI(new ApacheHttpClient(UUID.fromString(key)));
        api.getPlayerByName(name);
        PlayerReply apiReply = api.getPlayerByName(name).get();
        JsonObject jsonObject = apiReply.getPlayer().getRaw();
        if (jsonObject.get("socialMedia").getAsJsonObject().has("DISCORD")){
            JsonObject socialMedia = jsonObject.get("socialMedia").getAsJsonObject();
            System.out.println(socialMedia.get("DISCORD").getAsString().equals(user.getName() + user.getDiscriminator()));
            if (socialMedia.get("DISCORD").getAsString().equals(user.getName() + user.getDiscriminator())){
                return jsonObject.get("socialMedia").getAsJsonObject().get("DISCORD").getAsString();
            } else {
                hook.reply("Your Discord didn't match up with Hypixel.\n\n For more help, please visit this video: https://youtu.be/MiC72ZHL3cs\n\n*It may take up to ten minutes to update this value.*").queue();
            }
        } else {
            hook.reply("Your Discord didn't match up with Hypixel.\n\n For more help, please visit this video: https://youtu.be/MiC72ZHL3cs\n\n*It may take up to ten minutes to update this value.*").queue();
        }
        return null;
    }
}
