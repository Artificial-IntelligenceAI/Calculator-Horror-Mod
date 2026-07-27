package com.calculatorhorror.action;

import net.minecraft.server.level.ServerPlayer;

/**
 * Teleport a player within their current dimension.
 */
public final class TeleportActions {
    private TeleportActions() {
    }

    public static void teleport(ServerPlayer player, double x, double y, double z) {
        player.teleportTo(x, y, z);
    }
}
