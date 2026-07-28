package com.calculatorhorror.action;

import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

/**
 * Real (not client-only-illusion) system messages, at two different scopes: everyone on the
 * server, or just one targeted player. Both go through the normal vanilla system-message
 * pipeline — they show up in chat like any other system message, unlike {@link ChunkActions}
 * or {@link SoundActions}, which are packet-only illusions invisible to server state.
 */
public final class MessageActions {
    private MessageActions() {
    }

    public static void sendToAll(MinecraftServer server, Component message) {
        server.getPlayerList().broadcastSystemMessage(message, false);
    }

    public static void sendToPlayer(ServerPlayer player, Component message) {
        player.sendSystemMessage(message);
    }
}
