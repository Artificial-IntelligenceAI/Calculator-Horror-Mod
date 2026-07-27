package com.calculatorhorror.action;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

/**
 * Force a player through the respawn sequence (same mechanic as the respawn button after death:
 * repositioned to their bed/anchor or world spawn, fully healed) without requiring them to
 * actually be dead first. Vanilla's own packet handler refuses to respawn a living player
 * ({@code if (player.getHealth() > 0.0F) return;}), but that check lives in the packet handler,
 * not in {@code PlayerList#respawn} itself, so calling it directly bypasses that restriction.
 *
 * Always keeps inventory: without a real death, nothing has dropped the player's items in the
 * world first, so respawning with keepInventory=false here would just delete them.
 *
 * Respawning replaces the player's entity object entirely (same as vanilla respawn/dimension
 * change) — the ServerPlayer passed in becomes stale after this call; use the returned one.
 */
public final class RespawnActions {
    private RespawnActions() {
    }

    public static ServerPlayer respawn(ServerPlayer player) {
        return player.getServer().getPlayerList().respawn(player, true, Entity.RemovalReason.KILLED);
    }
}
