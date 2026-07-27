package com.calculatorhorror.action;

import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;

/**
 * Read/modify the contents stored inside a container item (e.g. a shulker box sitting in a
 * player's inventory) via its DataComponents.CONTAINER component, rather than a placed block.
 *
 * ItemContainerContents is immutable and only remembers up to its last non-empty slot, not the
 * item's true capacity — callers must pass the real slot count (see SHULKER_BOX_SLOTS) so a
 * write to a currently-empty container doesn't get truncated.
 */
public final class ItemContainerActions {
    public static final int SHULKER_BOX_SLOTS = ShulkerBoxBlockEntity.CONTAINER_SIZE;

    private ItemContainerActions() {
    }

    /** Slots actually worth reading (up to the last non-empty one) — not the item's true capacity, see setSlot. */
    public static int size(ItemStack containerStack) {
        ItemContainerContents contents = containerStack.get(DataComponents.CONTAINER);
        return contents == null ? 0 : contents.getSlots();
    }

    public static ItemStack getSlot(ItemStack containerStack, int slot) {
        ItemContainerContents contents = containerStack.get(DataComponents.CONTAINER);
        if (contents == null || slot >= contents.getSlots()) {
            return ItemStack.EMPTY;
        }
        return contents.getStackInSlot(slot);
    }

    public static void setSlot(ItemStack containerStack, int slot, int capacity, ItemStack newStack) {
        NonNullList<ItemStack> items = NonNullList.withSize(capacity, ItemStack.EMPTY);
        ItemContainerContents contents = containerStack.get(DataComponents.CONTAINER);
        if (contents != null) {
            contents.copyInto(items);
        }
        items.set(slot, newStack);
        containerStack.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(items));
    }

    public static void clear(ItemStack containerStack) {
        containerStack.set(DataComponents.CONTAINER, ItemContainerContents.EMPTY);
    }
}
