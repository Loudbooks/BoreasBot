package me.boreasbot.discord;

import com.github.steveice10.mc.protocol.packet.ingame.clientbound.ClientboundLoginPacket;
import com.github.steveice10.mc.protocol.packet.ingame.serverbound.ServerboundChatPacket;
import com.github.steveice10.packetlib.Session;
import com.github.steveice10.packetlib.event.session.SessionAdapter;
import com.github.steveice10.packetlib.packet.Packet;
import com.google.gson.JsonObject;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import static me.boreasbot.discord.Main.*;


public class DiscordChat extends ListenerAdapter {
    @Override
    public void onMessageReceived(MessageReceivedEvent e) {
        if (e.getAuthor().isBot()) return;
        if (e.getChannelType() == ChannelType.TEXT && e.getTextChannel().getId().equals("872232416771735592")) {
            Member member = e.getGuild().getMember(e.getAuthor());
            JsonObject jsonObject = MongoDBUtil.readData("_id", member.getId());
            String author = e.getAuthor().getName();
            if (jsonObject.get("donator") != null || jsonObject.get("role") != null){
                if (member.getNickname() != null){
                    author = member.getNickname();
                } else {
                    author = e.getAuthor().getName();
                }
            }
            String message = e.getMessage().getContentDisplay();
            if (!(message.chars().count() > 255)){
                client.send(new ServerboundChatPacket("/gc " + author + ": " + message));
                messages.add(e.getTextChannel().getId());
                messagesID.add(e.getMessage().getId());
            } else {
                e.getMessage().reply("Message is too long, canceling send.").queue();
            }
        } else if (e.getChannelType() == ChannelType.TEXT && e.getTextChannel().getId().equals("872232566424481812")){
            String author = e.getAuthor().getName();
            String message = e.getMessage().getContentDisplay();
            if (!(message.chars().count() > 255)){
                client.send(new ServerboundChatPacket("/go " + author + ": " + message));
                messages.add(e.getTextChannel().getId());
                messagesID.add(e.getMessage().getId());
            } else {
                e.getMessage().reply("Message is too long, canceling send.").queue();
            }
        }
    }
}
