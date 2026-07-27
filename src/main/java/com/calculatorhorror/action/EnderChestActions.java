package com.calculatorhorror.action;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Read/modify a player's ender chest inventory. Unlike a placed chest, this is per-player
 * storage (Player#getEnderChestInventory()), not tied to a block position.
 */
public final class EnderChestActions {
    private EnderChestActions() {
    }

    public static int size(Player player) {
        return player.getEnderChestInventory().getContainerSize();
    }

    public static ItemStack getSlot(Player player, int slot) {
        return player.getEnderChestInventory().getItem(slot);
    }

    public static void setSlot(Player player, int slot, ItemStack stack) {
        player.getEnderChestInventory().setItem(slot, stack);
    }

    public static void clear(Player player) {
        player.getEnderChestInventory().clearContent();
    }
}
