package me.boreasbot.discord;

import com.github.steveice10.mc.auth.exception.request.RequestException;
import com.github.steveice10.mc.protocol.MinecraftProtocol;
import com.github.steveice10.mc.protocol.packet.ingame.clientbound.ClientboundLoginPacket;
import com.github.steveice10.mc.protocol.packet.ingame.serverbound.ServerboundChatPacket;
import com.github.steveice10.packetlib.ProxyInfo;
import com.github.steveice10.packetlib.Session;
import com.github.steveice10.packetlib.packet.Packet;
import com.github.steveice10.packetlib.tcp.TcpClientSession;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.*;

//import static me.boreasbot.discord.Main.client;
import static me.boreasbot.discord.Main.*;


public class DiscordChat extends ListenerAdapter{

    @Override
    public void onMessageReceived(MessageReceivedEvent e) {

        if (e.getAuthor().isBot()) return;
        if (e.getChannelType() == ChannelType.TEXT && e.getTextChannel().getId().equals("872232416771735592")) {
            String author = e.getAuthor().getName();
            String message = e.getMessage().getContentDisplay();
            if (!(message.chars().count() > 255)){
                hashMap.add("/gc " + author + ": " + message);
            } else {
                e.getMessage().reply("Message is too long, canceling send.").queue();
            }
        } else if (e.getChannelType() == ChannelType.TEXT && e.getTextChannel().getId().equals("872232566424481812")){
            String author = e.getAuthor().getName();
            String message = e.getMessage().getContentDisplay();
            if (!(message.chars().count() > 255)){
                hashMap.add("/go " + author + ": " + message);
            } else {
                e.getMessage().reply("Message is too long, canceling send.").queue();
            }
        }
    }
}
