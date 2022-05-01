package me.boreasbot.discord;

import com.google.gson.JsonObject;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.checkerframework.checker.units.qual.A;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static me.boreasbot.discord.Main.jda;
import static me.boreasbot.discord.Main.mongoCollection;

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
            ArrayList<String> arrayList = new ArrayList<>();
            for (Member user : jda.getGuildById("860667007632277524").getMembers()) {
                if (MongoDBUtil.readData("_id", user.getId()) != null){
                    JsonObject object = MongoDBUtil.readData("_id", user.getId());
                    arrayList.add("<@" + object.get("_id") + ">" + " > " + object.get("hypixelusername"));
                }
            }
            EmbedBuilder eb = new EmbedBuilder();
            eb.setColor(0x6ac0dc);
            eb.setTitle("Verified List");
            eb.setFooter("Boreas", "https://cdn.discordapp.com/icons/860667007632277524/f842ec5c1feb1dc84e419cc64f37da73.webp?size=96");
            arrayList.forEach(name ->{
                eb.setDescription(name);
            });
            e.replyEmbeds(eb.build()).queue();
        } else if (e.getName().equals("unverifiedlist")){
            ArrayList<String> arrayList = new ArrayList<>();
//            List list = (List) jda.getGuildById("860667007632277524").loadMembers();
            ArrayList<User> list = new ArrayList<>();
            jda.getGuildById("860667007632277524").retrieveMembers(list);
            System.out.println(list);
            for (Member user : jda.getGuildById("860667007632277524").getMembers()) {
                if (MongoDBUtil.readData("_id", user.getId()) == null){
                    arrayList.add("<@" + user.getId() + ">");
                }
            }
            EmbedBuilder eb = new EmbedBuilder();
            eb.setColor(0x6ac0dc);
            eb.setTitle("Unverified List");
            eb.setFooter("Boreas", "https://cdn.discordapp.com/icons/860667007632277524/f842ec5c1feb1dc84e419cc64f37da73.webp?size=96");
            arrayList.forEach(name ->{
                eb.setDescription(name);
            });
            e.replyEmbeds(eb.build()).queue();
        } else if (e.getName().equals("fetch")) {
            try {
                String pl = e.getOption("username").getAsString();

                JsonObject object = MongoDBUtil.readData("_id", String.valueOf(e.getOption("username")));
                EmbedBuilder eb = new EmbedBuilder();
                eb.setTitle(pl + "'s data:");
                eb.setColor(0x6ac0dc);
                String name = String.valueOf(object.get("_id"));
                String joined = String.valueOf(object.get("verified"));
                eb.setDescription("**Discord ID:**" + name + "\n**Verified:**" + joined + "\n**Discord Name:** " + object.get("discordname") + "\n**Hypixel Username:** " + object.get("hypixelusername"));
                eb.setThumbnail("https://minotar.net/avatar/" + String.valueOf(e.getOption("username")));
                e.replyEmbeds(eb.build()).queue();
            } catch (Exception ex){
                e.reply("**Could not find player in the database!**");
            }
        }
    }
}