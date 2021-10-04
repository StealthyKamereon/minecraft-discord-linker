package com.github.stealthykamereon.discordlinker;

import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.MinecraftForge;

import java.lang.reflect.Method;
import java.util.TimerTask;
import java.util.function.Consumer;
import java.util.function.Function;

public class TPSUpdateThread extends TimerTask {

    private MinecraftServer server;
    private Consumer<Float> updateFunction;

    public TPSUpdateThread(MinecraftServer server, Consumer<Float> updateFunction) {
        this.server = server;
        this.updateFunction = updateFunction;
    }

    @Override
    public void run() {
        updateFunction.accept(server.getAverageTickTime());
    }
}
