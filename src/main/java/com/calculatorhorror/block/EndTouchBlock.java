package com.calculatorhorror.block;

import com.calculatorhorror.action.TeleportActions;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * A solid block that teleports a player to the End the moment they step on it. Breaking it and
 * picking up the dropped item is unaffected — this only fires on physical contact with a placed
 * instance of the block, never on the item form.
 */
public class EndTouchBlock extends Block {
    public EndTouchBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        super.stepOn(level, pos, state, entity);
        if (!level.isClientSide && entity instanceof ServerPlayer player) {
            ServerLevel end = player.getServer().getLevel(Level.END);
            if (end != null) {
                BlockPos spawn = ServerLevel.END_SPAWN_POINT;
                TeleportActions.teleport(player, end, spawn.getX() + 0.5, spawn.getY(), spawn.getZ() + 0.5);
            }
        }
    }
}
