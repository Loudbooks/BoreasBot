package me.boreasbot.discord;

import com.github.steveice10.mc.auth.exception.request.RequestException;
import com.github.steveice10.mc.auth.service.AuthenticationService;
import com.github.steveice10.mc.auth.service.MsaAuthenticationService;
import com.github.steveice10.mc.auth.service.SessionService;
import com.github.steveice10.mc.protocol.MinecraftConstants;
import com.github.steveice10.mc.protocol.MinecraftProtocol;
import com.github.steveice10.mc.protocol.packet.ingame.clientbound.ClientboundChatPacket;
import com.github.steveice10.mc.protocol.packet.ingame.clientbound.ClientboundLoginPacket;
import com.github.steveice10.mc.protocol.packet.ingame.serverbound.ServerboundChatPacket;
import com.github.steveice10.packetlib.ProxyInfo;
import com.github.steveice10.packetlib.Session;
import com.github.steveice10.packetlib.event.session.DisconnectedEvent;
import com.github.steveice10.packetlib.event.session.SessionAdapter;
import com.github.steveice10.packetlib.packet.Packet;
import com.github.steveice10.packetlib.tcp.TcpClientSession;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mongodb.Block;
import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import lombok.Getter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.bson.Document;

import javax.security.auth.login.LoginException;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Proxy;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import static me.boreasbot.discord.Passwords.PASSWORD;
import static me.boreasbot.discord.Passwords.USERNAME;
import static net.dv8tion.jda.api.interactions.commands.OptionType.*;

public class Main extends ListenerAdapter {
    private static final String HOST = "hypixel.net";
    private static final int PORT = 25565;
    private static final ProxyInfo PROXY = null;
    private static final Proxy AUTH_PROXY = Proxy.NO_PROXY;
    public static Session client = null;
    public static HashMap<String, Boolean> hashMap = new HashMap<>();
    public static ArrayList<String> messages = new ArrayList<>();
    public static ArrayList<String> messagesID = new ArrayList<>();
    public static boolean ready = false;
    public static MongoCollection<Document> mongoCollection;
    public static int indexCount = 0;
    @Getter
    public static JDA jda;
    private TextChannel textChannel;

    public static void main(String[] args) throws InterruptedException, RequestException, IOException {
        String configFilePath = "src/main/resources/config.properties";
        FileInputStream propsInput = new FileInputStream(configFilePath);
        Properties prop = new Properties();
        prop.load(propsInput);
        MongoClient mongoClient = MongoClients.create(prop.getProperty("mongo_token"));
        MongoDatabase mongoDatabase = mongoClient.getDatabase("BoreasBot");
        mongoCollection = mongoDatabase.getCollection("userdata");
        System.out.println("Connected to Database");
        try {
            jda = JDABuilder.createDefault(prop.getProperty("discord_token"))
                    .enableIntents(GatewayIntent.GUILD_MEMBERS)
                    .setChunkingFilter(ChunkingFilter.ALL)
                    .setMemberCachePolicy(MemberCachePolicy.ALL)
                    .addEventListeners(new DiscordChat())
                    .addEventListeners(new SlashCommandInteraction())
                    .setActivity(Activity.listening("Boreas"))
                    .setStatus(OnlineStatus.DO_NOT_DISTURB)
                    .build();
            jda.awaitReady();
        } catch (LoginException | InterruptedException e) {
            e.printStackTrace();
            return;
        }
        if (jda == null) {
            System.out.println("Unable to connect to Discord!");
        }
        Guild guild = jda.getGuildById("860667007632277524");

        CommandListUpdateAction commands = guild.updateCommands();
        commands.addCommands(
                Commands.slash("verifiedlist", "Get all the verified users."));
        commands.addCommands(
                Commands.slash("verify", "Link your Discord to your IGN.")
                        .addOptions(new OptionData(STRING, "username", "Hypixel username.").setRequired(true)));
        commands.addCommands(
                Commands.slash("unverifiedlist", "Get all the unverified user.")
                        .addOptions(new OptionData(INTEGER, "page", "Page of unverified users, 3 pages total.").setRequired(true)));
        commands.addCommands(
                Commands.slash("fetch", "Fetch a person's data.")
                        .addOptions(new OptionData(USER, "username", "Discord username").setRequired(true)));
        commands.addCommands(
                Commands.slash("execute", "Execute a command in-game")
                        .addOptions(new OptionData(STRING, "command", "Command to execute, use / at the start.").setRequired(true)));
        commands.queue();
        for(Command c : guild.retrieveCommands().complete()){
            System.out.println(c);
        }
        VoiceChannel channel = guild.getVoiceChannelById("921425697870843924");
        channel.getManager().setName("Bot Status: Online").queue();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            VoiceChannel channel1 = guild.getVoiceChannelById("921425697870843924");
            channel1.getManager().setName("Bot Status: Offline").queue();
            System.out.println("Index fail count: " + indexCount);
        }));
        Thread.sleep(3000);
        login();


    }

    static void login() throws RequestException, InterruptedException, IOException {
        Guild guild = jda.getGuildById("860667007632277524");
        jda.getPresence().setPresence(OnlineStatus.IDLE, true);
        TextChannel textChannel = jda.getGuildById("860667007632277524").getTextChannelById("872232416771735592");
        textChannel.sendMessage("**Boreas Bot is starting...**").queue();
        String configFilePath = "src/main/resources/config.properties";
        FileInputStream propsInput = new FileInputStream(configFilePath);
        Properties prop = new Properties();
        prop.load(propsInput);
        ready = false;
        AuthenticationService authService = new MsaAuthenticationService(prop.getProperty("msa_token"));
        authService.setUsername(USERNAME);
        authService.setPassword(PASSWORD);
        authService.setProxy(AUTH_PROXY);
        authService.login();
        MinecraftProtocol protocol;
        protocol = new MinecraftProtocol(authService.getSelectedProfile(), authService.getAccessToken());
        SessionService sessionService = new SessionService();
        sessionService.setProxy(AUTH_PROXY);
        client = new TcpClientSession(HOST, PORT, protocol, PROXY);
        client.setFlag(MinecraftConstants.SESSION_SERVICE_KEY, sessionService);
        client.connect();

        //jda.getShardManager().setStatus(OnlineStatus.IDLE);
        for (int i = 0; i < 16; i++) {
            client.send(new ServerboundChatPacket("/"));
        }
        textChannel.sendMessage("__**Boreas Bot has started!**__\n\n__Monthly Sponsors:__ Djeff#0001\n\n__Donators:__ Dax1e#4765\n**Thanks for all your support!**").queue();
        ready = true;
        client.addListener(new SessionAdapter() {
            @Override
            public void packetReceived(Session session, Packet packet) {
                if (packet instanceof ClientboundLoginPacket) {
                    for (int i = 0; i < 16; i++) {
                        session.send(new ServerboundChatPacket("/"));
                    }
                } else if (packet instanceof ClientboundChatPacket) {
                    Component message = ((ClientboundChatPacket) packet).getMessage().asComponent();
                    TextChannel textChannel = jda.getGuildById("860667007632277524").getTextChannelById("872232416771735592");
                    assert textChannel != null;
                    String gson = GsonComponentSerializer.gson().serialize(message);
                    JsonObject jsonObject = (JsonObject) new JsonParser().parse(gson);
                    if (jsonObject.get("text").getAsString().equals("You cannot say the same message twice!")) {
                        TextChannel failedMessageChannel = guild.getTextChannelById(messages.get(0));
                        failedMessageChannel.retrieveMessageById(messagesID.get(0)).queue((messageFail) -> messageFail.addReaction("❌").queue());
                        messagesID.remove(0);
                        messages.remove(0);

                    } else {
                        try{
                            messagesID.remove(0); messages.remove(0);
                        } catch (IndexOutOfBoundsException ex){
                            indexCount++;
                        }
                    }
                    try {
                        String str = null;
                        String str1 = null;
                        try {
                            str = jsonObject.getAsJsonArray("extra").get(1).getAsJsonObject().get("text").getAsString().replace("*", "\\*");
                            str1 = jsonObject.getAsJsonArray("extra").get(1).getAsJsonObject().get("text").getAsString().replace("*", "\\*");

                        } catch (Exception ex) {
                        }
                        EmbedBuilder eb = new EmbedBuilder();
                        if (str == null) {
                            return;
                        }
                        if (str1 == null) {
                            return;
                        }
                        String author = null;
                        String authorSub = null;
                        try {
                            authorSub = jsonObject.getAsJsonArray("extra").get(0).getAsJsonObject().get("text").getAsString();
                            author = authorSub.substring(10);
                        } catch (Exception ex) {
                        }
                        if (author == null) {
                            author = jsonObject.getAsJsonArray("extra").get(0).getAsJsonObject().get("text").getAsString();
                        }
                        String strNew = author
                                .replace("§3", "")
                                .replace(":", "")
                                .replaceFirst("\\[[^\\]]+\\]", "")
                                .trim().replace("§f", "")
                                .replace("§f", "")
                                .replace("§0", "")
                                .replace("§1", "")
                                .replace("§2", "")
                                .replace("§3", "")
                                .replace("§5", "")
                                .replace("§6", "")
                                .replace("§7", "")
                                .replace("§9", "")
                                .replace("§a", "")
                                .replace("§b", "")
                                .replace("§c", "")
                                .replace("§d", "")
                                .replace("§e", "")

                                .replace("§f", "").trim();

                        if (ready) {
                            DateFormat dateFormat2 = new SimpleDateFormat("MM/dd/yyyy hh.mm aa");
                            String dateString2 = dateFormat2.format(new Date());
                            if (!strNew.contains("BoreasBot")) {
                                if (!str1.contains("joined.")) {
                                    if (!str1.contains("left.")) {
                                        if (authorSub.contains("Guild >")) {
                                            String authorMessage = strNew.replaceFirst("\\[[^\\]]+\\]", "").trim();
                                            eb.setDescription(str);
                                            if (authorMessage.equals("ItsMeDjeff") || authorMessage.equals("Loudbook") || authorMessage.equals("Dax1e")){
                                                eb.setColor(0xE69D3E);
                                            } else {
                                                eb.setColor(0x6ac0dc);
                                            }
                                            eb.setFooter(dateString2);
                                            eb.setAuthor(strNew + " [Click]", "https://sky.shiiyu.moe/stats/" + authorMessage, "https://minotar.net/helm/" + authorMessage);
                                            textChannel.sendMessageEmbeds(eb.build()).queue();
                                        } else if (authorSub.contains("+") && !(authorSub.contains("§3Officer >"))){
                                            String subject = jsonObject.getAsJsonArray("extra").get(1).getAsJsonObject().get("text").getAsString().replace("]", "").trim();
                                            String predicate = jsonObject.getAsJsonArray("extra").get(2).getAsJsonObject().get("text").getAsString();
                                            if (predicate.contains("was kicked")){
                                                EmbedBuilder eb2 = new EmbedBuilder();
                                                eb2.setTitle(subject + " was kicked from the guild!");
                                                eb2.setColor(0xFF0000);
                                                eb2.setFooter(dateString2);
                                                textChannel.sendMessageEmbeds(eb2.build()).queue();
                                            } else if (predicate.contains("left the guild!")){
                                                EmbedBuilder eb2 = new EmbedBuilder();
                                                eb2.setTitle(subject + " left the guild!");
                                                eb2.setColor(0xFF0000);
                                                eb2.setFooter(dateString2);
                                                textChannel.sendMessageEmbeds(eb2.build()).queue();
                                                FindIterable<Document> iterable = mongoCollection.find();
                                                iterable.forEach(new Block<>() {
                                                    @Override
                                                    public void apply(final Document document) {
                                                        String json = document.toJson();
                                                        JsonParser parser = new JsonParser();
                                                        JsonObject jsonObject1 = (JsonObject) parser.parse(json);
                                                        if (subject.equals(String.valueOf(jsonObject1.get("hypixelusername")).replace("\"", ""))) {
                                                            String user = null;
                                                            try {
                                                                user = String.valueOf(jsonObject1.get("_id"));
                                                            } catch (Exception ignored) {
                                                            }
                                                            if (user == null) {
                                                                return;
                                                            }
                                                            User discordUser = jda.getUserById(user.replace("\"", ""));
                                                            Member discordMember = jda.getGuildById("860667007632277524").getMember(discordUser);
                                                            Guild guild = jda.getGuildById("860667007632277524");
                                                            guild.addRoleToMember(discordMember,guild.getRoleById("920329177406799872")).queue();
                                                        }
                                                    }
                                                });
                                            } else if (predicate.contains("joined the guild!")){
                                                EmbedBuilder eb2 = new EmbedBuilder();
                                                eb2.setTitle(subject + " joined the guild!");
                                                eb2.setColor(0x00FF00);
                                                eb2.setFooter(dateString2);
                                                textChannel.sendMessageEmbeds(eb2.build()).queue();
                                                client.send(new ServerboundChatPacket("Welcome to the guild " + subject + "! Make sure that you join the Discord and run /verify."));
                                            }
                                            if (predicate.contains("demoted")){
                                                String[] split = predicate.split(" ");
                                                EmbedBuilder eb1 = new EmbedBuilder();
                                                eb1.setColor(0xFF0000);
                                                eb1.setTitle(subject + " was demoted from " + split[3]+ " to " + split[5]);
                                                eb1.setFooter(dateString2);
                                                textChannel.sendMessageEmbeds(eb1.build()).queue();
                                                FindIterable<Document> iterable = mongoCollection.find();
                                                iterable.forEach(new Block<Document>() {
                                                    @Override
                                                    public void apply(final Document document) {
                                                        String json = document.toJson();
                                                        JsonParser parser = new JsonParser();
                                                        JsonObject jsonObject1 = (JsonObject) parser.parse(json);
                                                        if (subject.equals(String.valueOf(jsonObject1.get("hypixelusername")).replace("\"", ""))) {
                                                            String user = null;
                                                            try {user = String.valueOf(jsonObject1.get("_id"));} catch (Exception ignored){}
                                                            if (user == null){
                                                                return;
                                                            }
                                                            User discordUser = jda.getUserById(user.replace("\"", ""));
                                                            Member discordMember = jda.getGuildById("860667007632277524").getMember(discordUser);
                                                            Guild guild = jda.getGuildById("860667007632277524");
                                                            assert guild != null;
                                                            assert discordMember != null;
                                                            if (split[5].contains("Member")){
                                                                guild.addRoleToMember(discordMember, Objects.requireNonNull(guild.getRoleById("877188839255453766"))).queue();
                                                                guild.removeRoleFromMember(discordMember, Objects.requireNonNull(guild.getRoleById("917903499671507014"))).queue();
                                                            } else if (split[5].contains("Elite")){
                                                                guild.addRoleToMember(discordMember, Objects.requireNonNull(guild.getRoleById("917903499671507014"))).queue();
                                                                guild.removeRoleFromMember(discordMember, Objects.requireNonNull(guild.getRoleById("917898479563579432"))).queue();
                                                            } else if (split[5].contains("Warden")) {
                                                                guild.addRoleToMember(discordMember, Objects.requireNonNull(guild.getRoleById("917898479563579432"))).queue();
                                                                guild.removeRoleFromMember(discordMember, Objects.requireNonNull(guild.getRoleById("861410060034506762"))).queue();
                                                            }
                                                        }
                                                    }
                                                });

                                            } else if (predicate.contains("promoted")) {
                                                String[] split = predicate.split(" ");
                                                EmbedBuilder eb1 = new EmbedBuilder();
                                                eb1.setColor(0x00FF00);
                                                eb1.setTitle(subject + " was promoted from " + split[3]+ " to " + split[5]);
                                                eb1.setFooter(dateString2);
                                                textChannel.sendMessageEmbeds(eb1.build()).queue();
                                                FindIterable<Document> iterable = mongoCollection.find();
                                                final int[] i = {0};
                                                iterable.forEach(new Block<Document>() {
                                                    @Override
                                                    public void apply(final Document document) {
                                                        i[0]++;
                                                        String json = document.toJson();
                                                        JsonParser parser = new JsonParser();
                                                        JsonObject jsonObject1 = (JsonObject) parser.parse(json);
                                                        if (subject.equals(String.valueOf(jsonObject1.get("hypixelusername").getAsString()).replace("\"", ""))) {
                                                            System.out.println(subject);
                                                            String user = null;
                                                            try {
                                                                user = String.valueOf(jsonObject1.get("_id"));
                                                            } catch (Exception ex) {
                                                                ex.printStackTrace();
                                                            }
                                                            if (user == null) {
                                                                return;
                                                            }
                                                            User discordUser = jda.getUserById(user.replace("\"", ""));
                                                            assert discordUser != null;
                                                            Member discordMember = Objects.requireNonNull(jda.getGuildById("860667007632277524")).getMember(discordUser);
                                                            Guild guild = jda.getGuildById("860667007632277524");
                                                            assert guild != null;
                                                            assert discordMember != null;
                                                            if (split[5].contains("Elite")) {
                                                                guild.addRoleToMember(discordMember, Objects.requireNonNull(guild.getRoleById("917903499671507014"))).queue();
                                                            } else if (split[5].contains("Warden")) {
                                                                guild.addRoleToMember(discordMember, Objects.requireNonNull(guild.getRoleById("917898479563579432"))).queue();
                                                                guild.removeRoleFromMember(discordMember, Objects.requireNonNull(guild.getRoleById("917903499671507014"))).queue();
                                                            } else if (split[5].contains("Staff")) {
                                                                guild.addRoleToMember(discordMember, Objects.requireNonNull(guild.getRoleById("861410060034506762"))).queue();
                                                                guild.removeRoleFromMember(discordMember, Objects.requireNonNull(guild.getRoleById("917898479563579432"))).queue();
                                                            }
                                                        }
                                                    }
                                                });

                                                if (i[0] > (int) mongoCollection.countDocuments()){
                                                    textChannel.sendMessage("**Unable to update role: User not in database.**").queue();

                                                }
                                            }
                                        } else if (authorSub.contains("§3Officer >")){
                                            TextChannel officerChannel = jda.getTextChannelById("872232566424481812");
                                            officerChannel.sendMessage("**" + strNew.replace(">", "").trim() + "**: " + str.replace(">", "").trim()).queue();
                                        }

                                    }else if (str1.contentEquals("left.")) {
                                        String authorMessage = jsonObject.getAsJsonArray("extra").get(0).getAsJsonObject().get("text").getAsString();
                                        eb.setAuthor(authorMessage + "left.", "https://sky.shiiyu.moe/stats/" + authorMessage, "https://minotar.net/helm/" + authorMessage);
                                        eb.setColor(0xFF0000);
                                        eb.setFooter(dateString2);
                                        textChannel.sendMessageEmbeds(eb.build()).queue();
                                    }
                                    } else {
                                    String authorMessage = jsonObject.getAsJsonArray("extra").get(0).getAsJsonObject().get("text").getAsString();
                                    eb.setAuthor(authorMessage + "joined.", "https://sky.shiiyu.moe/stats/" + authorMessage, "https://minotar.net/helm/" + authorMessage);
                                    eb.setColor(0x00FF00);
                                    eb.setFooter(dateString2);
                                    if (authorMessage.contains("ItsMeDjeff")) {
                                        client.send(new ServerboundChatPacket("/gc Everyone welcome ItsMeDjeff! They are one of our generous sponsors."));
                                    } else if (authorMessage.contains("Dax1e")){
                                        client.send(new ServerboundChatPacket("/gc Everyone welcome Dax1e! They are one of our generous donators."));
                                    }
                                    textChannel.sendMessageEmbeds(eb.build()).queue();
                                }
                            }
                        }
                    } catch(Exception ex){
                        ex.printStackTrace();
                    }
                }
            }

            @Override
            public void disconnected(DisconnectedEvent event) {
                System.out.println("Disconnected: " + event.getReason());
                if (event.getCause() != null) {
                    event.getCause().printStackTrace();
                    VoiceChannel channel = jda.getGuildById("860667007632277524").getVoiceChannelById("921425697870843924");
                    channel.getManager().setName("Bot Status: Offline").queue();
                }
                try {
                    login();
                    VoiceChannel channel = jda.getGuildById("860667007632277524").getVoiceChannelById("921425697870843924");
                    channel.getManager().setName("Bot Status: Online").queue();
                } catch (RequestException | InterruptedException | IOException e) {
                    e.printStackTrace();
                }
            }

        });
    }
    @Override
    public void onMessageReceived(MessageReceivedEvent e) {
        textChannel.sendMessage(e.getMessage()).queue();
        if (client == null){return;}
        if (e.getAuthor().isBot()) return;
        if (e.getChannelType() == ChannelType.TEXT && e.getTextChannel().getId().equals("872232416771735592")) {
            Member member = e.getGuild().getMember(e.getAuthor());
            JsonObject jsonObject = MongoDBUtil.readData("_id", member.getId());
            String author = e.getAuthor().getName();
            if (jsonObject.get("donator") != null || jsonObject.get("role") != null){
                author = member.getNickname();
            }
            String message = e.getMessage().getContentDisplay();
            if (!(message.chars().count() > 255)){
                client.send(new ServerboundChatPacket("/gc " + author + ": " + message));
            } else {
                e.getMessage().reply("Message is too long, canceling send.").queue();
            }
        } else if (e.getChannelType() == ChannelType.TEXT && e.getTextChannel().getId().equals("872232566424481812")){
            String author = e.getAuthor().getName();
            String message = e.getMessage().getContentDisplay();
            if (!(message.chars().count() > 255)){
                client.send(new ServerboundChatPacket("/go " + author + ": " + message));
            } else {
                e.getMessage().reply("Message is too long, canceling send.").queue();
            }
        }
    }
}