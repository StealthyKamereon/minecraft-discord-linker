package com.github.stealthykamereon.discordlinker.discord;

import com.github.stealthykamereon.discordlinker.DiscordLinker;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.hooks.AnnotatedEventManager;
import net.dv8tion.jda.internal.entities.RichPresenceImpl;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import javax.security.auth.login.LoginException;
import java.awt.*;
import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class DiscordBot {

    private final String CHANNEL_FILE_PATH = "config/discord_channel.id";
    private final String HEAD_URL = "https://www.mc-heads.net/avatar/%s/32.png";
    private final String TPS = "Tick Time : %.2fms";
    private final Logger LOGGER = LogManager.getLogger();

    private String syncedChannelId = "";
    private Map<String, Color> dimensionColors;
    private JDA jda;

    public DiscordBot(DiscordLinker mod, String tokenPath) throws IOException, LoginException {
        dimensionColors = new HashMap<>();
        dimensionColors.put("overworld", new Color(47, 206, 89));
        dimensionColors.put("the_nether", new Color(102, 38, 25));
        dimensionColors.put("the_end", new Color(59, 33, 107));
        dimensionColors.put("twilight_forest", new Color(26, 71, 18));
        dimensionColors.put("Deep Dark", new Color(30, 30, 30));
        BufferedReader reader = new BufferedReader(new FileReader(tokenPath));
        String token = reader.readLine();
        reader.close();
        this.loadSyncedChannel();
        this.jda = JDABuilder.createDefault(token).setEventManager(new AnnotatedEventManager()).addEventListeners(mod).build();
    }

    private void loadSyncedChannel() {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(this.CHANNEL_FILE_PATH));
            this.syncedChannelId = reader.readLine();
            reader.close();
        } catch (IOException ignored) {
        }
        LOGGER.info(String.format("Linked channel %s", this.syncedChannelId));
    }

    public void syncWith(TextChannel channel) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(this.CHANNEL_FILE_PATH));
        writer.write(channel.getId());
        writer.close();
    }

    public boolean isSyncedChannel(TextChannel channel) {
        if (!this.syncedChannelId.equals("")) {
            return this.syncedChannelId.equals(channel.getId());
        }
        try {
            BufferedReader reader = new BufferedReader(new FileReader(this.CHANNEL_FILE_PATH));
            String channelId = reader.readLine();
            reader.close();
            return channelId.equals(this.syncedChannelId);
        } catch (IOException e) {
            return false;
        }
    }

    public void sendMessage(ServerPlayerEntity author, String content) {
        this.sendMessage(author.getDisplayName().getString(), author.getStringUUID(), author.getLevel().dimension().location().getPath(), content);
    }

    public void sendMessage(PlayerEntity entity, String content) {
        this.sendMessage(entity.getDisplayName().getString(), entity.getStringUUID(), entity.level.dimension().location().getPath(), content);
    }

    public void sendMessage(String authorName, String authorUUID, String authorDimension, String content) {
        if (!this.syncedChannelId.equals("")) {
            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder.setAuthor(authorName);
            embedBuilder.setThumbnail(String.format(HEAD_URL, authorUUID));
            embedBuilder.setDescription(content);
            if (this.dimensionColors.containsKey(authorDimension)) {
                embedBuilder.setColor(this.dimensionColors.get(authorDimension));
            }
            MessageBuilder messageBuilder = new MessageBuilder();
            messageBuilder.setEmbeds(embedBuilder.build());
            this.jda.getTextChannelById(this.syncedChannelId).sendMessage(messageBuilder.build()).queue();
        }
    }

    public void updateRichPresence(float tps) {
        this.jda.getPresence().setPresence(Activity.of(Activity.ActivityType.WATCHING, String.format(TPS, tps)), false);
    }

    public void stop() {
        this.jda.shutdownNow();
    }

    public User getUser() {
        return this.jda.getSelfUser();
    }

}
