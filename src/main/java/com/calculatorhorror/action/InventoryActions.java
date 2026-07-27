package com.calculatorhorror.action;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Read/modify a player's own inventory.
 */
public final class InventoryActions {
    private InventoryActions() {
    }

    public static int size(Player player) {
        return player.getInventory().getContainerSize();
    }

    public static ItemStack getSlot(Player player, int slot) {
        return player.getInventory().getItem(slot);
    }

    public static void setSlot(Player player, int slot, ItemStack stack) {
        player.getInventory().setItem(slot, stack);
    }

    /** Adds to the first available slot, dropping the stack at the player's feet if the inventory is full. */
    public static void give(Player player, ItemStack stack) {
        if (!player.getInventory().add(stack)) {
            player.drop(stack, false);
        }
    }

    public static void clear(Player player) {
        player.getInventory().clearContent();
    }
}
