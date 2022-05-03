package me.boreasbot.discord;

import com.google.common.base.Splitter;
import com.google.gson.JsonObject;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bson.Document;

import java.text.SimpleDateFormat;
import java.util.*;

import static me.boreasbot.discord.Main.*;

public class SlashCommandInteraction extends ListenerAdapter {
    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent e){

        e.deferReply();
        SimpleDateFormat formatter= new SimpleDateFormat("MM-dd-yyyy 'at' HH:mm:ss z");
        Date date = new Date(System.currentTimeMillis());
        if (e.getName().equals("verify")){
            String pl = e.getOption("username").getAsString();
            if (MongoDBUtil.readData("_id", e.getUser().getId()) != null){
                mongoCollection.updateOne(Filters.eq("_id", e.getUser().getId()), Updates.set("discordname", e.getUser().getName()));
                mongoCollection.updateOne(Filters.eq("_id", e.getUser().getId()), Updates.set("hypixelusername", pl));
                EmbedBuilder eb = new EmbedBuilder();
                eb.setTitle("You have successfully updated your information!");
                eb.setDescription("**Discord ID: **" + e.getUser().getId() + "\n**Date Verified: **" + formatter.format(date) + "\n**Discord Username: **" + e.getUser().getName() + "\n**Hypixel Username: **" + pl);
                eb.setFooter("Make sure that you re-run this command if you change your Hypixel username.");
                eb.setColor(0x6ac0dc);
                e.replyEmbeds(eb.build()).queue();
            } else {
                Document document = new Document();
                document.append("_id", e.getUser().getId());
                document.append("verified", formatter.format(date));
                document.append("discordname", e.getUser().getName());
                document.append("hypixelusername", pl);
                mongoCollection.insertOne(document);
                EmbedBuilder eb = new EmbedBuilder();
                eb.setTitle("You have successfully verified!");
                eb.setDescription("**Discord ID: **" + e.getUser().getId() + "\n**Date Verified: **" + formatter.format(date) + "\n**Discord Username: **" + e.getUser().getName() + "\n**Hypixel Username: **" + pl);
                eb.setFooter("Make sure that you re-run this command if you change your Hypixel username.");
                eb.setColor(0x6ac0dc);
                e.replyEmbeds(eb.build()).queue();
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
            hashMap.add(command);
            e.reply("Done, please note this will **not** send an error if the command was not executed successfully.").setEphemeral(true).queue();
        }

    }
    public static List<String> usingSplitMethod(String text, int n) {
        String[] results = text.split("(?<=\\G.{" + n + "})");

        return Arrays.asList(results);
    }
}