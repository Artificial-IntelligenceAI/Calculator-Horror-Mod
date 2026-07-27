package com.calculatorhorror.action;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Read/change blocks in the world.
 */
public final class BlockActions {
    private BlockActions() {
    }

    public static BlockState get(Level level, BlockPos pos) {
        return level.getBlockState(pos);
    }

    public static void set(Level level, BlockPos pos, BlockState state) {
        level.setBlock(pos, state, Block.UPDATE_ALL);
    }
}
