package com.github.stealthykamereon.discordlinker;

import com.github.stealthykamereon.discordlinker.discord.DiscordBot;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppingEvent;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.util.UUID;

@Mod("discordlinker")
public class DiscordLinker {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final String D2MC_MESSAGE_FORMAT = "<Discord/%s> %s";
    private static final String MC2D_MESSAGE_FORMAT = "%s";
    private MinecraftServer server;
    protected DiscordBot bot;

    public DiscordLinker() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onServerStarting(FMLServerStartingEvent event) {
        this.server = event.getServer();
        try {
            this.bot = new DiscordBot(this, "discord.token");
        } catch (IOException e) {
            LOGGER.error("Couldn't read token for the discord bot ! ", e);
        } catch (LoginException e) {
            LOGGER.error("Couldn't connect to discord : ", e);
        }
    }

    @SubscribeEvent
    public void onServerStopping(FMLServerStoppingEvent event) {
        this.bot.stop();
    }

    @SubscribeEvent
    public void onServerChat(ServerChatEvent event) {
        this.bot.sendMessage(event.getPlayer(), event.getMessage());
    }

    @net.dv8tion.jda.api.hooks.SubscribeEvent
    public void onBotEvent(GenericEvent event) {
        if (event instanceof ReadyEvent) {
            LOGGER.info("Discord bot successfully connected !");
        }
        else if (event instanceof MessageReceivedEvent) {
            this.handleMessage((MessageReceivedEvent) event);
        }
    }

    private void handleMessage(MessageReceivedEvent event) {
        if (event.isFromType(ChannelType.TEXT) && this.bot.isSyncedChannel(event.getTextChannel())) {
            if (event.getAuthor().equals(this.bot.getUser())) {
                return;
            }
            String message = this.formatMessage(event);
            this.server.sendMessage(new StringTextComponent(message), new UUID(0, 0));
            for (ServerPlayerEntity player : this.server.getPlayerList().getPlayers()) {
                player.sendMessage(new StringTextComponent(message), new UUID(0, 0));
            }
        }
    }

    private String formatMessage(MessageReceivedEvent event) {
        return String.format(D2MC_MESSAGE_FORMAT, event.getAuthor().getName(), event.getMessage().getContentDisplay());
    }

    private String formatMessage(ServerChatEvent event) {
        return String.format(MC2D_MESSAGE_FORMAT, event.getMessage());
    }
}
