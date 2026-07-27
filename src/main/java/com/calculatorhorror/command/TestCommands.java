package com.calculatorhorror.command;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

import com.calculatorhorror.CalculatorHorror;
import com.calculatorhorror.action.BlockActions;
import com.calculatorhorror.action.ContainerActions;
import com.calculatorhorror.action.EffectActions;
import com.calculatorhorror.action.InventoryActions;
import com.calculatorhorror.action.TeleportActions;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceArgument;
import net.minecraft.commands.arguments.blocks.BlockInput;
import net.minecraft.commands.arguments.blocks.BlockStateArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.commands.arguments.item.ItemInput;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/**
 * Manual test harness for the action toolkit ({@code com.calculatorhorror.action}).
 * Op-only ({@code /calculatorhorror test ...}); lets each capability be exercised in-game
 * before any horror mechanic (entity, timer, etc.) is wired up to call into them.
 */
@EventBusSubscriber(modid = CalculatorHorror.MODID)
public final class TestCommands {
    private TestCommands() {
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandBuildContext buildContext = event.getBuildContext();

        event.getDispatcher().register(
            literal("calculatorhorror")
                .then(literal("test")
                    .requires(source -> source.hasPermission(2))
                    .then(literal("effect")
                        .then(argument("effect", ResourceArgument.resource(buildContext, Registries.MOB_EFFECT))
                            .then(argument("seconds", IntegerArgumentType.integer(1))
                                .then(argument("amplifier", IntegerArgumentType.integer(0))
                                    .executes(ctx -> {
                                        ServerPlayer player = ctx.getSource().getPlayerOrException();
                                        var effect = ResourceArgument.getMobEffect(ctx, "effect");
                                        int seconds = IntegerArgumentType.getInteger(ctx, "seconds");
                                        int amplifier = IntegerArgumentType.getInteger(ctx, "amplifier");
                                        EffectActions.give(player, effect, seconds * 20, amplifier);
                                        ctx.getSource().sendSuccess(() -> Component.literal(
                                            "Applied effect for " + seconds + "s (amplifier " + amplifier + ")"), false);
                                        return 1;
                                    })))))
                    .then(literal("cleareffects")
                        .executes(ctx -> {
                            ServerPlayer player = ctx.getSource().getPlayerOrException();
                            EffectActions.clearAll(player);
                            ctx.getSource().sendSuccess(() -> Component.literal("Cleared all effects"), false);
                            return 1;
                        }))
                    .then(literal("give")
                        .then(argument("item", ItemArgument.item(buildContext))
                            .then(argument("count", IntegerArgumentType.integer(1))
                                .executes(ctx -> {
                                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                                    ItemInput itemInput = ItemArgument.getItem(ctx, "item");
                                    int count = IntegerArgumentType.getInteger(ctx, "count");
                                    ItemStack stack = itemInput.createItemStack(count, false);
                                    InventoryActions.give(player, stack);
                                    ctx.getSource().sendSuccess(() -> Component.literal(
                                        "Gave " + count + "x " + itemInput.getItem().getDescription().getString()), false);
                                    return 1;
                                }))))
                    .then(literal("clearinventory")
                        .executes(ctx -> {
                            ServerPlayer player = ctx.getSource().getPlayerOrException();
                            InventoryActions.clear(player);
                            ctx.getSource().sendSuccess(() -> Component.literal("Cleared inventory"), false);
                            return 1;
                        }))
                    .then(literal("invpeek")
                        .executes(ctx -> runInvPeek(ctx, ctx.getSource().getPlayerOrException()))
                        .then(argument("player", EntityArgument.player())
                            .executes(ctx -> runInvPeek(ctx, EntityArgument.getPlayer(ctx, "player")))))
                    .then(literal("setblock")
                        .then(argument("pos", BlockPosArgument.blockPos())
                            .then(argument("block", BlockStateArgument.block(buildContext))
                                .executes(ctx -> {
                                    Level level = ctx.getSource().getLevel();
                                    BlockPos pos = BlockPosArgument.getLoadedBlockPos(ctx, "pos");
                                    BlockInput blockInput = BlockStateArgument.getBlock(ctx, "block");
                                    BlockActions.set(level, pos, blockInput.getState());
                                    ctx.getSource().sendSuccess(() -> Component.literal("Set block at " + pos.toShortString()), false);
                                    return 1;
                                }))))
                    .then(literal("teleport")
                        .then(argument("x", DoubleArgumentType.doubleArg())
                            .then(argument("y", DoubleArgumentType.doubleArg())
                                .then(argument("z", DoubleArgumentType.doubleArg())
                                    .executes(ctx -> {
                                        ServerPlayer player = ctx.getSource().getPlayerOrException();
                                        double x = DoubleArgumentType.getDouble(ctx, "x");
                                        double y = DoubleArgumentType.getDouble(ctx, "y");
                                        double z = DoubleArgumentType.getDouble(ctx, "z");
                                        TeleportActions.teleport(player, x, y, z);
                                        ctx.getSource().sendSuccess(() -> Component.literal(
                                            "Teleported to " + x + ", " + y + ", " + z), false);
                                        return 1;
                                    })))))
                    .then(literal("chestpeek")
                        .then(argument("pos", BlockPosArgument.blockPos())
                            .executes(ctx -> {
                                Level level = ctx.getSource().getLevel();
                                BlockPos pos = BlockPosArgument.getLoadedBlockPos(ctx, "pos");
                                var container = ContainerActions.getAt(level, pos);
                                if (container == null) {
                                    ctx.getSource().sendFailure(Component.literal("No container at " + pos.toShortString()));
                                    return 0;
                                }
                                StringBuilder summary = new StringBuilder();
                                for (int slot = 0; slot < container.getContainerSize(); slot++) {
                                    ItemStack stack = container.getItem(slot);
                                    if (!stack.isEmpty()) {
                                        summary.append(slot).append(": ").append(stack.getCount()).append("x ")
                                            .append(stack.getItem().getDescription().getString()).append("; ");
                                    }
                                }
                                ctx.getSource().sendSuccess(() -> Component.literal(
                                    summary.isEmpty() ? "Container is empty" : summary.toString()), false);
                                return 1;
                            })))
                    .then(literal("chestset")
                        .then(argument("pos", BlockPosArgument.blockPos())
                            .then(argument("slot", IntegerArgumentType.integer(0))
                                .then(argument("item", ItemArgument.item(buildContext))
                                    .then(argument("count", IntegerArgumentType.integer(1))
                                        .executes(ctx -> {
                                            Level level = ctx.getSource().getLevel();
                                            BlockPos pos = BlockPosArgument.getLoadedBlockPos(ctx, "pos");
                                            int slot = IntegerArgumentType.getInteger(ctx, "slot");
                                            ItemInput itemInput = ItemArgument.getItem(ctx, "item");
                                            int count = IntegerArgumentType.getInteger(ctx, "count");
                                            ContainerActions.setSlot(level, pos, slot, itemInput.createItemStack(count, false));
                                            ctx.getSource().sendSuccess(() -> Component.literal(
                                                "Set slot " + slot + " at " + pos.toShortString()), false);
                                            return 1;
                                        }))))))));
    }

    private static int runInvPeek(CommandContext<CommandSourceStack> ctx, ServerPlayer target) {
        StringBuilder summary = new StringBuilder();
        int size = InventoryActions.size(target);
        for (int slot = 0; slot < size; slot++) {
            ItemStack stack = InventoryActions.getSlot(target, slot);
            if (!stack.isEmpty()) {
                summary.append(slot).append(": ").append(stack.getCount()).append("x ")
                    .append(stack.getItem().getDescription().getString()).append("; ");
            }
        }
        ctx.getSource().sendSuccess(() -> Component.literal(
            target.getGameProfile().getName() + "'s inventory - "
                + (summary.isEmpty() ? "empty" : summary.toString())), false);
        return 1;
    }
}
