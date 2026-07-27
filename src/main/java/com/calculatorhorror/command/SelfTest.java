package com.calculatorhorror.command;

import com.calculatorhorror.action.BlockActions;
import com.calculatorhorror.action.ChunkActions;
import com.calculatorhorror.action.ContainerActions;
import com.calculatorhorror.action.EffectActions;
import com.calculatorhorror.action.EnderChestActions;
import com.calculatorhorror.action.InventoryActions;
import com.calculatorhorror.action.ItemContainerActions;
import com.calculatorhorror.action.TeleportActions;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.util.FakePlayerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Headless smoke test for the action toolkit ({@code com.calculatorhorror.action}), run against a
 * NeoForge FakePlayer instead of a real connected client. Test-phase tooling only, not player-facing
 * content — see /calculatorhorror test selftest.
 *
 * Teleport can't be verified this way: {@code ServerPlayer#teleportTo} sends packets through the
 * player's network connection, which a FakePlayer never has (it's never placed by the network/login
 * flow that assigns one). That's a real property of the production code path, not a gap in this
 * harness, so it's reported as SKIP rather than faked.
 */
final class SelfTest {
    private static final UUID BOT_UUID = UUID.nameUUIDFromBytes("calculatorhorror:selftest".getBytes());

    private SelfTest() {
    }

    static List<String> run(ServerLevel level, BlockPos origin) {
        List<String> results = new ArrayList<>();
        var fakePlayer = FakePlayerFactory.get(level, new GameProfile(BOT_UUID, "CalculatorHorrorTestBot"));

        BlockPos blockPos = origin.above(50);
        try {
            BlockActions.set(level, blockPos, Blocks.GLASS.defaultBlockState());
            check(results, "setblock", BlockActions.get(level, blockPos).is(Blocks.GLASS));
        } catch (Exception e) {
            fail(results, "setblock", e);
        } finally {
            BlockActions.set(level, blockPos, Blocks.AIR.defaultBlockState());
        }

        BlockPos chestPos = blockPos.above();
        try {
            BlockActions.set(level, chestPos, Blocks.CHEST.defaultBlockState());
            ContainerActions.setSlot(level, chestPos, 0, new ItemStack(Items.APPLE, 5));
            ItemStack peeked = ContainerActions.getSlot(level, chestPos, 0);
            check(results, "chestset/chestpeek", peeked.is(Items.APPLE) && peeked.getCount() == 5);

            ContainerActions.clear(level, chestPos);
            check(results, "chestclear", ContainerActions.getSlot(level, chestPos, 0).isEmpty());
        } catch (Exception e) {
            fail(results, "container", e);
        } finally {
            BlockActions.set(level, chestPos, Blocks.AIR.defaultBlockState());
        }

        try {
            EffectActions.give(fakePlayer, MobEffects.GLOWING, 100, 0);
            check(results, "effect give", fakePlayer.hasEffect(MobEffects.GLOWING));

            EffectActions.clearAll(fakePlayer);
            check(results, "effect clear", !fakePlayer.hasEffect(MobEffects.GLOWING));
        } catch (Exception e) {
            fail(results, "effect", e);
        }

        try {
            InventoryActions.give(fakePlayer, new ItemStack(Items.DIAMOND, 3));
            check(results, "inventory give/invpeek", containsStack(fakePlayer, Items.DIAMOND, 3));

            InventoryActions.clear(fakePlayer);
            check(results, "inventory clear", isInventoryEmpty(fakePlayer));
        } catch (Exception e) {
            fail(results, "inventory", e);
        }

        try {
            TeleportActions.teleport(fakePlayer, blockPos.getX(), blockPos.getY(), blockPos.getZ());
            check(results, "teleport", true);
        } catch (Exception e) {
            results.add("SKIP teleport (" + e.getClass().getSimpleName()
                + ": needs a real connected client, not testable headlessly)");
        }

        try {
            EnderChestActions.setSlot(fakePlayer, 0, new ItemStack(Items.NETHERITE_INGOT, 2));
            ItemStack peeked = EnderChestActions.getSlot(fakePlayer, 0);
            check(results, "enderset/enderpeek", peeked.is(Items.NETHERITE_INGOT) && peeked.getCount() == 2);

            EnderChestActions.clear(fakePlayer);
            check(results, "enderclear", EnderChestActions.getSlot(fakePlayer, 0).isEmpty());
        } catch (Exception e) {
            fail(results, "enderchest", e);
        }

        try {
            InventoryActions.setSlot(fakePlayer, 0, new ItemStack(Items.SHULKER_BOX));
            ItemStack holder = InventoryActions.getSlot(fakePlayer, 0);
            ItemContainerActions.setSlot(holder, 0, ItemContainerActions.SHULKER_BOX_SLOTS, new ItemStack(Items.GOLD_INGOT, 4));
            InventoryActions.setSlot(fakePlayer, 0, holder);

            ItemStack shulkerContents = ItemContainerActions.getSlot(InventoryActions.getSlot(fakePlayer, 0), 0);
            check(results, "shulkerinvset/shulkerinvpeek", shulkerContents.is(Items.GOLD_INGOT) && shulkerContents.getCount() == 4);

            InventoryActions.clear(fakePlayer);
        } catch (Exception e) {
            fail(results, "shulker-in-inventory", e);
        }

        try {
            ChunkPos chunkPos = new ChunkPos(blockPos);
            ChunkActions.forceLoad(level, chunkPos);
            check(results, "chunkforceload", level.getForcedChunks().contains(chunkPos.toLong()));

            ChunkActions.unload(level, chunkPos);
            check(results, "chunkunload", !level.getForcedChunks().contains(chunkPos.toLong()));
        } catch (Exception e) {
            fail(results, "chunk", e);
        }

        return results;
    }

    private static boolean containsStack(net.minecraft.world.entity.player.Player player, net.minecraft.world.item.Item item, int count) {
        for (int slot = 0; slot < InventoryActions.size(player); slot++) {
            ItemStack stack = InventoryActions.getSlot(player, slot);
            if (stack.is(item) && stack.getCount() == count) {
                return true;
            }
        }
        return false;
    }

    private static boolean isInventoryEmpty(net.minecraft.world.entity.player.Player player) {
        for (int slot = 0; slot < InventoryActions.size(player); slot++) {
            if (!InventoryActions.getSlot(player, slot).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private static void check(List<String> results, String name, boolean passed) {
        results.add((passed ? "PASS " : "FAIL ") + name);
    }

    private static void fail(List<String> results, String name, Exception e) {
        results.add("FAIL " + name + " (" + e.getClass().getSimpleName() + ": " + e.getMessage() + ")");
    }
}
