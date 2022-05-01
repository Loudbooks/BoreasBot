package me.boreasbot.discord;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.UUID;

import static me.boreasbot.discord.Main.mongoCollection;


public final class MongoDBUtil {
    public static JsonObject readData(String id, String uuid) {
        Document uuids = mongoCollection.find(new Document(id, uuid)).first();
        JsonObject jsonObject;
        try {
            jsonObject = new JsonParser().parse(uuids.toJson()).getAsJsonObject();
            return jsonObject;
        } catch (Exception e) {
            return null;
        }
    }
    public static void writeData(String s, String s1){
        Document document = new Document();
        document.append(s, s1);
        mongoCollection.insertOne(document);
    }
    public static void updateData(String fieldName, Player p, String areatochange, Object change){
        mongoCollection.updateOne(Filters.eq(fieldName, p.getUniqueId().toString()), Updates.set(areatochange, change));
    }
}
