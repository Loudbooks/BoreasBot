package me.boreasbot.discord;

import com.google.gson.JsonObject;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.hypixel.api.HypixelAPI;
import net.hypixel.api.reply.PlayerReply;
import org.shanerx.mojang.Mojang;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static me.boreasbot.discord.Main.jda;
import static me.boreasbot.discord.Main.mongoCollection;

public class Apply extends ListenerAdapter {
    @Override
    public void onMessageReceived(MessageReceivedEvent e) {
        if (e.getTextChannel().getName().contains("-application")){
            JsonObject jsonObject = MongoDBUtil.readData("_id", e.getAuthor().getId());
            assert jsonObject != null;
            if (jsonObject.get("step").getAsInt() == 2){
                try {
                    Apply.apply(e.getAuthor(), e.getTextChannel(), 2, e.getMessage().getContentDisplay());
                } catch (ExecutionException | InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    public static void apply(User user, TextChannel channel, int step, String otherInfo) throws ExecutionException, InterruptedException {
        Guild guild = jda.getGuildById("860667007632277524");
        JsonObject jsonObject = MongoDBUtil.readData("_id", user.getId());
        if (!(channel.getName().contains("-application")) && jsonObject.get("application").getAsBoolean()){
            channel.sendMessage("You already have an application open...").queue();
        } else {
            List<TextChannel> applyChannelList = guild.getTextChannelsByName(user.getName() + "-application", true);
            TextChannel applyChannel = applyChannelList.get(0);
            switch (step){
                case 0:
                    applyChannel.sendMessage("Welcome to Boreas <@" + user.getId() + ">!" + " I'l be your guide for applying to the guild. \n\n First, please verify using our bot. Do `/verify` and make sure you select **BoreasBot**.").queue();
                    jda.getGuildById("860667007632277524").addRoleToMember(user.getId(), jda.getRoleById("975088508110774293")).queue();
                case 1:
                    applyChannel.sendMessage("");
                    HypixelAPI api = HypixelUtil.API;
                    Mojang apis = new Mojang().connect();
                    JsonObject username = MongoDBUtil.readData("_id", user.getId());
                    String playerUuid = apis.getUUIDOfUsername(username.get("hypixelusername").getAsString());
                    PlayerReply reply = api.getPlayerByUuid(playerUuid).get();
//                    mongoCollection.updateOne(Filters.eq("_id", user.getId()), Updates.set("step", 2));
                case 2:
                case 3:
            }

        }
    }
}
