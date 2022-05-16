package me.boreasbot.discord;

import com.google.gson.JsonObject;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import lombok.SneakyThrows;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.hypixel.api.HypixelAPI;
import net.hypixel.api.reply.PlayerReply;
import org.apache.commons.io.IOUtils;
import org.bson.Document;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;
import org.shanerx.mojang.Mojang;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutionException;

import static me.boreasbot.discord.Main.*;
public class SlashCommandInteraction extends ListenerAdapter {
    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent e){
        SimpleDateFormat formatter= new SimpleDateFormat("MM-dd-yyyy 'at' HH:mm:ss z");
        Date date = new Date(System.currentTimeMillis());
        if (e.getName().equals("verify")){
            e.deferReply().queue();
            Guild guild = jda.getGuildById("860667007632277524");
            String pl = e.getOption("username").getAsString();
            boolean verifying = false;
            if (e.getMember().getRoles().contains(jda.getGuildById("860667007632277524").getRoleById("975088508110774293"))){
                verifying = true;
            }
            if (MongoDBUtil.readData("_id", e.getUser().getId()) != null){
                HypixelAPI api = HypixelUtil.API;
                try {
                    String playerUuid = null;
                    boolean valid = false;
                    try {
                        String username = Objects.requireNonNull(e.getOption("username")).getAsString();
                        Mojang apis = new Mojang().connect();
                        playerUuid = apis.getUUIDOfUsername(username);
                        valid = true;
                    } catch (Exception ex) {
                        e.getHook().sendMessage("Uh oh... something went wrong when contacting the Mojang API...\n\nPlease make sure you entered a valid Minecraft username.").queue();
                        ex.printStackTrace();
                    }
                    if (valid) {
                        PlayerReply reply = api.getPlayerByUuid(playerUuid).get();
                        if (reply.getPlayer().getRaw().get("socialMedia").getAsJsonObject().get("links").getAsJsonObject().get("DISCORD").toString() == null){
                            e.getHook().sendMessage(("It looks like your Discord name is not properly set. Please follow this video and try this command again. (It may take time for your Discord tag to update.) \n\nhttps://youtu.be/MiC72ZHL3cs")).queue();
                        } else {
                            String discord = reply.getPlayer().getRaw().get("socialMedia").getAsJsonObject().get("links").getAsJsonObject().get("DISCORD").toString().replace("\"", "");
                            if (discord.replace("\"", "").equals(e.getUser().getName() + "#" + e.getUser().getDiscriminator())){
                                mongoCollection.updateOne(Filters.eq("_id", e.getUser().getId()), Updates.set("discordname", discord));
                                mongoCollection.updateOne(Filters.eq("_id", e.getUser().getId()), Updates.set("hypixelusername", pl));
                                mongoCollection.updateOne(Filters.eq("_id", e.getUser().getId()), Updates.set("verified", formatter.format(date)));
                                EmbedBuilder eb = new EmbedBuilder();
                                eb.setTitle("You have successfully updated your information!");
                                eb.setDescription("**Discord ID: **" + e.getUser().getId() + "\n**Date Verified: **" + formatter.format(date) + "\n**Discord Username: **" + discord.replace("\"", "") + "\n**Hypixel Username: **" + pl);
                                eb.setFooter("Make sure that you re-run this command if you change your Hypixel username.");
                                eb.setColor(0x6ac0dc);
                                Objects.requireNonNull(e.getGuild()).addRoleToMember(e.getUser().getId(), Objects.requireNonNull(e.getGuild().getRoleById("970859257983557693"))).queue();
                                e.getHook().sendMessageEmbeds(eb.build()).queue();
                                if (verifying){
                                    if (e.getTextChannel().getName().equals(e.getName() + "-application")){
                                        List<TextChannel> textList = guild.getTextChannelsByName(e.getName() + "-application", true);
                                        TextChannel text = textList.get(0);
                                        text.sendMessage("Great! What profile would you like us to look at?").queue();
                                        mongoCollection.updateOne(Filters.eq("_id", e.getUser().getId()), Updates.set("step", 1));
                                        jda.getGuildById("860667007632277524").removeRoleFromMember(e.getMember().getId(), jda.getRoleById("975088508110774293")).queue();
                                    } else {
                                        e.getTextChannel().sendMessage("Please do this in your verification channel.").queue();
                                    }
                                }
                            } else {
                                e.getHook().sendMessage("It looks like your Discord name is not properly set. Please follow this video and try this command again. (It may take time for your Discord tag to update.) \n\nhttps://youtu.be/MiC72ZHL3cs").queue();
                            }
                        }
                    }

                } catch (ExecutionException | InterruptedException ex) {
                    ex.getCause().printStackTrace();
                }

            } else {
                HypixelAPI api = HypixelUtil.API;
                try {
                    String playerUuid = null;
                    boolean valid = false;
                    try {
                        String username = Objects.requireNonNull(e.getOption("username")).getAsString();
                        Mojang apis = new Mojang().connect();
                        playerUuid = apis.getUUIDOfUsername(username);
                        valid = true;
                    } catch (Exception ex) {
                        e.getHook().sendMessage("Uh oh... something went wrong when contacting the Mojang API...\n\nPlease make sure you entered a valid Minecraft username.").queue();
                        ex.printStackTrace();
                    }
                    if (valid) {
                        PlayerReply reply = api.getPlayerByUuid(playerUuid).get();
                        if (reply.getPlayer().getRaw().get("socialMedia").getAsJsonObject().get("links").getAsJsonObject().get("DISCORD").toString() == null){
                            e.getHook().sendMessage("It looks like your Discord name is not properly set. Please follow this video and try this command again. (It may take time for your Discord tag to update.) \n\nhttps://youtu.be/MiC72ZHL3cs").queue();
                        } else {
                            String discord = reply.getPlayer().getRaw().get("socialMedia").getAsJsonObject().get("links").getAsJsonObject().get("DISCORD").toString().replace("\"", "");
                            if (discord.equals(e.getUser().getName() + "#" + e.getUser().getDiscriminator())){
                                Document document = new Document();
                                document.append("_id", e.getUser().getId());
                                document.append("verified", formatter.format(date));
                                document.append("discordname", discord);
                                document.append("hypixelusername", pl);
                                document.append("application", false);
                                mongoCollection.insertOne(document);
                                EmbedBuilder eb = new EmbedBuilder();
                                eb.setTitle("You have successfully verified!");
                                eb.setDescription("**Discord ID: **" + e.getUser().getId() + "\n**Date Verified: **" + formatter.format(date) + "\n**Discord Username: **" + discord + "\n**Hypixel Username: **" + pl);
                                eb.setFooter("Make sure that you re-run this command if you change your Hypixel username.");
                                eb.setColor(0x6ac0dc);
                                Objects.requireNonNull(e.getGuild()).addRoleToMember(e.getUser().getId(), Objects.requireNonNull(e.getGuild().getRoleById("970859257983557693"))).queue();
                                e.getHook().sendMessageEmbeds(eb.build()).queue();
                                if (verifying){
                                    if (e.getTextChannel().getName().equals(e.getName() + "-application")){
                                        List<TextChannel> textList = guild.getTextChannelsByName(e.getName() + "-application", true);
                                        TextChannel text = textList.get(0);
                                        document.append("step", 1);
                                        mongoCollection.insertOne(document);
                                        jda.getGuildById("860667007632277524").removeRoleFromMember(e.getMember().getId(), jda.getRoleById("975088508110774293")).queue();
                                        Apply.apply(e.getUser(), e.getTextChannel(), 1, null);
                                    } else {
                                        e.getTextChannel().sendMessage("Please do this in your verification channel.").queue();
                                    }
                                }
                            } else {
                                e.getHook().sendMessage("It looks like your Discord name is not properly set. Please follow this video and try this command again. (It may take time for your Discord tag to update.) \n\nhttps://youtu.be/MiC72ZHL3cs").queue();
                            }
                        }
                    }

                } catch (ExecutionException | InterruptedException ex) {
                    ex.getCause().printStackTrace();
                }
            }
        } else if (e.getName().equals("verifiedlist")){
            e.deferReply(false).queue();
            e.getHook().sendMessage("This will take a moment to execute, please be patient.").queue();
            ArrayList<String> arrayList = new ArrayList<>();
            double i = 0;
            for (Member user : jda.getGuildById("860667007632277524").getMembers()) {
                if (MongoDBUtil.readData("_id", user.getId()) != null){
                    arrayList.add("<@" + user.getId() + ">");
                    i++;
                }
            }
            EmbedBuilder eb = new EmbedBuilder();
            eb.setColor(0x00FF00);
            int percent = (int) Math.round(i / e.getGuild().getMemberCount() * 100);
            eb.setTitle("Verified List: " + (int) i + "/" + e.getGuild().getMemberCount() + " Users (" + percent + "%)");
            eb.setFooter("Boreas", "https://cdn.discordapp.com/icons/860667007632277524/f842ec5c1feb1dc84e419cc64f37da73.webp?size=96");
            List<String> results = SlashCommandInteraction.usingSplitMethod(arrayList.toString(), 1978);
            List<String> array1 = List.of(Arrays.toString(results.get(0).split(", ")).replace("[", "").replace("]", ""));
            arrayList.addAll(array1);
            arrayList.forEach(eb::setDescription);
            e.getHook().editOriginalEmbeds(eb.build()).queue();
        } else if (e.getName().equals("unverifiedlist")){
            e.deferReply(false).queue();
            e.getHook().sendMessage("This will take a moment to execute, please be patient.").queue();
            ArrayList<String> arrayList = new ArrayList<>();
            double i = 0;
            for (Member user : jda.getGuildById("860667007632277524").getMembers()) {
                if (MongoDBUtil.readData("_id", user.getId()) == null){
                    arrayList.add("<@" + user.getId() + ">");
                    i++;
                }
            }
            EmbedBuilder eb = new EmbedBuilder();
            eb.setColor(0xFF0000);
            int percent = (int) Math.round(i / e.getGuild().getMemberCount() * 100);
            eb.setTitle("Unverified List: " + (int) i + "/" + e.getGuild().getMemberCount() + " Users (" + percent + "%)");
            eb.setFooter("Boreas", "https://cdn.discordapp.com/icons/860667007632277524/f842ec5c1feb1dc84e419cc64f37da73.webp?size=96");
            List<String> results = SlashCommandInteraction.usingSplitMethod(arrayList.toString(), 1978);
            List<String> array1 = List.of(Arrays.toString(results.get(e.getOption("page").getAsInt() - 1).split(", ")).replace("[", "").replace("]", ""));
            arrayList.addAll(array1);
            arrayList.forEach(eb::setDescription);
            e.getHook().editOriginalEmbeds(eb.build()).queue();
        } else if (e.getName().equals("fetch")) {
            try {
                String pl = Objects.requireNonNull(e.getOption("username")).getAsUser().getName();
                JsonObject object = MongoDBUtil.readData("_id", e.getOption("username").getAsUser().getId());
                EmbedBuilder eb = new EmbedBuilder();
                eb.setTitle(pl + "'s data:");
                eb.setColor(0x6ac0dc);
                String name = String.valueOf(object.get("_id")).replace("\"", "");
                String joined = String.valueOf(object.get("verified")).replace("\n", "");
                eb.setDescription("**Discord ID:** " + name.replace("\"", "") + "\n**Verified:** " + joined.replace("\"", "") + "\n**Discord Name:** " + object.get("discordname").getAsString().replace("\n", "") + "\n**Hypixel Username:** " + object.get("hypixelusername").getAsString().replace("\"", ""));
                eb.setThumbnail("https://minotar.net/avatar/" + String.valueOf(e.getOption("username")));
                e.replyEmbeds(eb.build()).queue();
            } catch (Exception ex) {
                String pl = e.getOption("username").getAsUser().getName();
                e.reply("**Could not find " + pl + " in the database!**").queue();
            }
        } else if (e.getName().equals("execute")){
            String command = Objects.requireNonNull(e.getOption("command")).getAsString();
            e.reply("Done, please note this will **not** send an error if the command was not executed successfully.").setEphemeral(true).queue();
        } else if (e.getName().equals("apply")){
            e.getGuild().createTextChannel(e.getUser().getName() + "-application").queue();
            try {
                Apply.apply(e.getUser(), e.getTextChannel(), 0, null);
            } catch (ExecutionException | InterruptedException ex) {
                ex.printStackTrace();
            }
        }

    }
    public static List<String> usingSplitMethod(String text, int n) {
        String[] results = text.split("(?<=\\G.{" + n + "})");

        return Arrays.asList(results);
    }
    @SneakyThrows
    static String getResponse(String _url) {
        URL url = new URL(_url);
        URLConnection con = url.openConnection();

        InputStream in = con.getInputStream();
        String encoding = con.getContentEncoding();
        encoding = encoding == null ? "UTF-8" : encoding;
        String body = IOUtils.toString(in, encoding);

        return body;
    }
}