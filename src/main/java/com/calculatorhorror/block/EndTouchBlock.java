package com.calculatorhorror.block;

import com.calculatorhorror.action.TeleportActions;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;

/**
 * A block that teleports a player to the End the moment they touch it, from any side — not just
 * standing on top. Has no collision (see the noCollission() property in CalculatorHorror.java) so
 * a player can actually overlap it from any direction, same as how vanilla's own EndPortalBlock
 * works; entityInside (not stepOn, which only fires when standing on a solid block's top surface)
 * is what fires on that overlap.
 *
 * Breaking it and picking up the dropped item is unaffected — this only fires on physical contact
 * with a placed instance of the block, never on the item form.
 */
public class EndTouchBlock extends Block {
    public EndTouchBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (!level.isClientSide
            && entity instanceof ServerPlayer player
            && Shapes.joinIsNotEmpty(
                Shapes.create(entity.getBoundingBox().move(-pos.getX(), -pos.getY(), -pos.getZ())),
                state.getShape(level, pos),
                BooleanOp.AND)) {
            ServerLevel end = player.getServer().getLevel(Level.END);
            if (end != null) {
                BlockPos spawn = ServerLevel.END_SPAWN_POINT;
                TeleportActions.teleport(player, end, spawn.getX() + 0.5, spawn.getY(), spawn.getZ() + 0.5);
            }
        }
    }
}
