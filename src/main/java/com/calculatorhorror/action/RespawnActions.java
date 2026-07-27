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
 * Vanilla's own restoreFrom(that, keepEverything=true) resets health to max and then immediately
 * overwrites it with the old player's health again — so a player who died at 0 health stays at
 * 0 after this call unless we explicitly heal them back up ourselves, which is what a "respawn"
 * should mean. Confirmed by actually testing this against a player who fell into the void: they
 * were correctly repositioned but stayed at 0 health until this fix.
 *
 * Respawning replaces the player's entity object entirely (same as vanilla respawn/dimension
 * change) — the ServerPlayer passed in becomes stale after this call; use the returned one.
 */
public final class RespawnActions {
    private RespawnActions() {
    }

    public static ServerPlayer respawn(ServerPlayer player) {
        ServerPlayer respawned = player.getServer().getPlayerList().respawn(player, true, Entity.RemovalReason.KILLED);
        respawned.setHealth(respawned.getMaxHealth());
        return respawned;
    }
}
