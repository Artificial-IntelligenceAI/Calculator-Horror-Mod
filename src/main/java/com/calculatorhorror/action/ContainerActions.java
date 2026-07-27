package com.calculatorhorror.action;

import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

/**
 * Read/modify the contents of a container block (chest, barrel, shulker box, hopper, furnace, etc.) at a position.
 */
public final class ContainerActions {
    private ContainerActions() {
    }

    @Nullable
    public static Container getAt(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.getBlock() instanceof ChestBlock chestBlock) {
            // Merges double chests into a single 54-slot container, same as vanilla's chest UI.
            return ChestBlock.getContainer(chestBlock, state, level, pos, false);
        }
        BlockEntity blockEntity = level.getBlockEntity(pos);
        return blockEntity instanceof Container container ? container : null;
    }

    public static ItemStack getSlot(Level level, BlockPos pos, int slot) {
        Container container = getAt(level, pos);
        return container == null ? ItemStack.EMPTY : container.getItem(slot);
    }

    public static void setSlot(Level level, BlockPos pos, int slot, ItemStack stack) {
        Container container = getAt(level, pos);
        if (container != null) {
            container.setItem(slot, stack);
        }
    }

    public static void clear(Level level, BlockPos pos) {
        Container container = getAt(level, pos);
        if (container != null) {
            container.clearContent();
        }
    }
}
