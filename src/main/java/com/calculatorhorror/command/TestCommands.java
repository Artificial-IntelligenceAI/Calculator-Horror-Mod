package com.calculatorhorror.command;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

import com.calculatorhorror.CalculatorHorror;
import com.calculatorhorror.action.BlockActions;
import com.calculatorhorror.action.ChunkActions;
import com.calculatorhorror.action.ContainerActions;
import com.calculatorhorror.action.EffectActions;
import com.calculatorhorror.action.EnderChestActions;
import com.calculatorhorror.action.GameModeActions;
import com.calculatorhorror.action.InventoryActions;
import com.calculatorhorror.action.ItemContainerActions;
import com.calculatorhorror.action.RespawnActions;
import com.calculatorhorror.action.SoundActions;
import com.calculatorhorror.action.TeleportActions;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.DimensionArgument;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.GameModeArgument;
import net.minecraft.commands.arguments.ResourceArgument;
import net.minecraft.commands.arguments.blocks.BlockInput;
import net.minecraft.commands.arguments.blocks.BlockStateArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.commands.arguments.item.ItemInput;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.util.List;
import java.util.function.IntFunction;

/**
 * Manual test harness for the action toolkit ({@code com.calculatorhorror.action}).
 * Op-only ({@code /calculatorhorror test ...}); lets each capability be exercised in-game
 * before any horror mechanic (entity, timer, etc.) is wired up to call into them.
 *
 * Each subcommand is added as its own statement (rather than one deep chained expression) so
 * new ones can be added/found without hand-counting nested parens.
 */
@EventBusSubscriber(modid = CalculatorHorror.MODID)
public final class TestCommands {
    private TestCommands() {
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandBuildContext buildContext = event.getBuildContext();
        LiteralArgumentBuilder<CommandSourceStack> test = literal("test").requires(source -> source.hasPermission(2));

        test.then(literal("effect")
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
                        })))));

        test.then(literal("cleareffects")
            .executes(ctx -> {
                ServerPlayer player = ctx.getSource().getPlayerOrException();
                EffectActions.clearAll(player);
                ctx.getSource().sendSuccess(() -> Component.literal("Cleared all effects"), false);
                return 1;
            }));

        test.then(literal("give")
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
                    }))));

        test.then(literal("clearinventory")
            .executes(ctx -> {
                ServerPlayer player = ctx.getSource().getPlayerOrException();
                InventoryActions.clear(player);
                ctx.getSource().sendSuccess(() -> Component.literal("Cleared inventory"), false);
                return 1;
            }));

        test.then(literal("invpeek")
            .executes(ctx -> runInvPeek(ctx, ctx.getSource().getPlayerOrException()))
            .then(argument("player", EntityArgument.player())
                .executes(ctx -> runInvPeek(ctx, EntityArgument.getPlayer(ctx, "player")))));

        test.then(literal("selftest")
            .executes(ctx -> {
                ServerLevel level = ctx.getSource().getLevel();
                BlockPos origin = BlockPos.containing(ctx.getSource().getPosition());
                List<String> results = SelfTest.run(level, origin);
                results.forEach(line -> {
                    ctx.getSource().sendSuccess(() -> Component.literal(line), false);
                    CalculatorHorror.LOGGER.info("[selftest] {}", line);
                });
                return 1;
            }));

        test.then(literal("setblock")
            .then(argument("pos", BlockPosArgument.blockPos())
                .then(argument("block", BlockStateArgument.block(buildContext))
                    .executes(ctx -> {
                        Level level = ctx.getSource().getLevel();
                        BlockPos pos = BlockPosArgument.getLoadedBlockPos(ctx, "pos");
                        BlockInput blockInput = BlockStateArgument.getBlock(ctx, "block");
                        BlockActions.set(level, pos, blockInput.getState());
                        ctx.getSource().sendSuccess(() -> Component.literal("Set block at " + pos.toShortString()), false);
                        return 1;
                    }))));

        test.then(literal("teleport")
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
                        })))));

        test.then(literal("teleportdim")
            .then(argument("player", EntityArgument.player())
                .then(argument("dimension", DimensionArgument.dimension())
                    .then(argument("x", DoubleArgumentType.doubleArg())
                        .then(argument("y", DoubleArgumentType.doubleArg())
                            .then(argument("z", DoubleArgumentType.doubleArg())
                                .executes(ctx -> {
                                    ServerPlayer target = EntityArgument.getPlayer(ctx, "player");
                                    ServerLevel destination = DimensionArgument.getDimension(ctx, "dimension");
                                    double x = DoubleArgumentType.getDouble(ctx, "x");
                                    double y = DoubleArgumentType.getDouble(ctx, "y");
                                    double z = DoubleArgumentType.getDouble(ctx, "z");
                                    TeleportActions.teleport(target, destination, x, y, z);
                                    ctx.getSource().sendSuccess(() -> Component.literal(
                                        "Teleported " + target.getGameProfile().getName() + " to "
                                            + destination.dimension().location() + " " + x + ", " + y + ", " + z), false);
                                    return 1;
                                })))))));

        test.then(literal("gamemode")
            .then(argument("player", EntityArgument.player())
                .then(argument("mode", GameModeArgument.gameMode())
                    .executes(ctx -> {
                        ServerPlayer target = EntityArgument.getPlayer(ctx, "player");
                        var mode = GameModeArgument.getGameMode(ctx, "mode");
                        GameModeActions.set(target, mode);
                        ctx.getSource().sendSuccess(() -> Component.literal(
                            "Set " + target.getGameProfile().getName() + "'s game mode to " + mode.getName()), false);
                        return 1;
                    }))));

        test.then(literal("respawn")
            .then(argument("player", EntityArgument.player())
                .executes(ctx -> {
                    ServerPlayer target = EntityArgument.getPlayer(ctx, "player");
                    ServerPlayer respawned = RespawnActions.respawn(target);
                    ctx.getSource().sendSuccess(() -> Component.literal(
                        "Force-respawned " + respawned.getGameProfile().getName()), false);
                    return 1;
                })));

        test.then(literal("sound")
            .then(argument("player", EntityArgument.player())
                .then(argument("sound", ResourceArgument.resource(buildContext, Registries.SOUND_EVENT))
                    .then(argument("volume", FloatArgumentType.floatArg(0))
                        .then(argument("pitch", FloatArgumentType.floatArg(0))
                            .executes(ctx -> {
                                ServerPlayer target = EntityArgument.getPlayer(ctx, "player");
                                var sound = ResourceArgument.getResource(ctx, "sound", Registries.SOUND_EVENT);
                                float volume = FloatArgumentType.getFloat(ctx, "volume");
                                float pitch = FloatArgumentType.getFloat(ctx, "pitch");
                                SoundActions.playToPlayer(target, sound.value(), SoundSource.HOSTILE, volume, pitch);
                                ctx.getSource().sendSuccess(() -> Component.literal(
                                    "Played sound to " + target.getGameProfile().getName()), false);
                                return 1;
                            }))))));

        test.then(literal("ghostchunk")
            .then(argument("player", EntityArgument.player())
                .then(argument("pos", BlockPosArgument.blockPos())
                    .executes(ctx -> {
                        ServerPlayer target = EntityArgument.getPlayer(ctx, "player");
                        BlockPos pos = BlockPosArgument.getLoadedBlockPos(ctx, "pos");
                        ChunkActions.ghost(target, new ChunkPos(pos));
                        ctx.getSource().sendSuccess(() -> Component.literal(
                            "Ghosted chunk " + new ChunkPos(pos) + " for " + target.getGameProfile().getName()), false);
                        return 1;
                    }))));

        test.then(literal("chestpeek")
            .then(argument("pos", BlockPosArgument.blockPos())
                .executes(ctx -> {
                    Level level = ctx.getSource().getLevel();
                    BlockPos pos = BlockPosArgument.getLoadedBlockPos(ctx, "pos");
                    var container = ContainerActions.getAt(level, pos);
                    if (container == null) {
                        ctx.getSource().sendFailure(Component.literal("No container at " + pos.toShortString()));
                        return 0;
                    }
                    String summary = summarize(container.getContainerSize(), container::getItem);
                    ctx.getSource().sendSuccess(() -> Component.literal(summary), false);
                    return 1;
                })));

        test.then(literal("chestset")
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
                            }))))));

        test.then(literal("enderpeek")
            .executes(ctx -> runEnderPeek(ctx, ctx.getSource().getPlayerOrException()))
            .then(argument("player", EntityArgument.player())
                .executes(ctx -> runEnderPeek(ctx, EntityArgument.getPlayer(ctx, "player")))));

        test.then(literal("enderset")
            .then(argument("player", EntityArgument.player())
                .then(argument("slot", IntegerArgumentType.integer(0))
                    .then(argument("item", ItemArgument.item(buildContext))
                        .then(argument("count", IntegerArgumentType.integer(1))
                            .executes(ctx -> {
                                ServerPlayer target = EntityArgument.getPlayer(ctx, "player");
                                int slot = IntegerArgumentType.getInteger(ctx, "slot");
                                ItemInput itemInput = ItemArgument.getItem(ctx, "item");
                                int count = IntegerArgumentType.getInteger(ctx, "count");
                                EnderChestActions.setSlot(target, slot, itemInput.createItemStack(count, false));
                                ctx.getSource().sendSuccess(() -> Component.literal(
                                    "Set ender chest slot " + slot + " for " + target.getGameProfile().getName()), false);
                                return 1;
                            }))))));

        test.then(literal("shulkerinvpeek")
            .then(argument("player", EntityArgument.player())
                .then(argument("slot", IntegerArgumentType.integer(0))
                    .executes(ctx -> {
                        ServerPlayer target = EntityArgument.getPlayer(ctx, "player");
                        int slot = IntegerArgumentType.getInteger(ctx, "slot");
                        ItemStack holder = InventoryActions.getSlot(target, slot);
                        String summary = summarize(ItemContainerActions.size(holder), s -> ItemContainerActions.getSlot(holder, s));
                        ctx.getSource().sendSuccess(() -> Component.literal(
                            holder.getItem().getDescription().getString() + " in slot " + slot + " - " + summary), false);
                        return 1;
                    }))));

        test.then(literal("shulkerinvset")
            .then(argument("player", EntityArgument.player())
                .then(argument("slot", IntegerArgumentType.integer(0))
                    .then(argument("shulkerSlot", IntegerArgumentType.integer(0))
                        .then(argument("item", ItemArgument.item(buildContext))
                            .then(argument("count", IntegerArgumentType.integer(1))
                                .executes(ctx -> {
                                    ServerPlayer target = EntityArgument.getPlayer(ctx, "player");
                                    int slot = IntegerArgumentType.getInteger(ctx, "slot");
                                    int shulkerSlot = IntegerArgumentType.getInteger(ctx, "shulkerSlot");
                                    ItemInput itemInput = ItemArgument.getItem(ctx, "item");
                                    int count = IntegerArgumentType.getInteger(ctx, "count");
                                    ItemStack holder = InventoryActions.getSlot(target, slot);
                                    ItemContainerActions.setSlot(holder, shulkerSlot, ItemContainerActions.SHULKER_BOX_SLOTS,
                                        itemInput.createItemStack(count, false));
                                    ctx.getSource().sendSuccess(() -> Component.literal(
                                        "Set shulker slot " + shulkerSlot + " inside inventory slot " + slot), false);
                                    return 1;
                                })))))));

        event.getDispatcher().register(literal("calculatorhorror").then(test));
    }

    private static int runInvPeek(CommandContext<CommandSourceStack> ctx, ServerPlayer target) {
        String summary = summarize(InventoryActions.size(target), slot -> InventoryActions.getSlot(target, slot));
        ctx.getSource().sendSuccess(() -> Component.literal(
            target.getGameProfile().getName() + "'s inventory - " + summary), false);
        return 1;
    }

    private static int runEnderPeek(CommandContext<CommandSourceStack> ctx, ServerPlayer target) {
        String summary = summarize(EnderChestActions.size(target), slot -> EnderChestActions.getSlot(target, slot));
        ctx.getSource().sendSuccess(() -> Component.literal(
            target.getGameProfile().getName() + "'s ender chest - " + summary), false);
        return 1;
    }

    private static String summarize(int size, IntFunction<ItemStack> getter) {
        StringBuilder summary = new StringBuilder();
        for (int slot = 0; slot < size; slot++) {
            ItemStack stack = getter.apply(slot);
            if (!stack.isEmpty()) {
                summary.append(slot).append(": ").append(stack.getCount()).append("x ")
                    .append(stack.getItem().getDescription().getString()).append("; ");
            }
        }
        return summary.isEmpty() ? "empty" : summary.toString();
    }
}
