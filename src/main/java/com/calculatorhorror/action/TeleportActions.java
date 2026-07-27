package com.calculatorhorror.action;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.Set;

/**
 * Teleport a player, within their current dimension or to a different one.
 */
public final class TeleportActions {
    private TeleportActions() {
    }

    public static void teleport(ServerPlayer player, double x, double y, double z) {
        player.teleportTo(x, y, z);
    }

    public static void teleport(ServerPlayer player, ServerLevel destination, double x, double y, double z) {
        player.teleportTo(destination, x, y, z, Set.of(), player.getYRot(), player.getXRot());
    }
}
