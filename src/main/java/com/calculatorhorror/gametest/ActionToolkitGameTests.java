package com.calculatorhorror.gametest;

import com.calculatorhorror.CalculatorHorror;
import com.calculatorhorror.action.BlockActions;
import com.calculatorhorror.action.ChunkActions;
import com.calculatorhorror.action.ContainerActions;
import com.calculatorhorror.action.EffectActions;
import com.calculatorhorror.action.EnderChestActions;
import com.calculatorhorror.action.GameModeActions;
import com.calculatorhorror.action.InventoryActions;
import com.calculatorhorror.action.ItemContainerActions;
import com.calculatorhorror.action.MessageActions;
import com.calculatorhorror.action.RespawnActions;
import com.calculatorhorror.action.SoundActions;
import com.calculatorhorror.action.TeleportActions;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * Automated headless tests for the action toolkit ({@code com.calculatorhorror.action}), run via
 * NeoForge's GameTest framework instead of the manual RCON+real-client dance in
 * {@code com.calculatorhorror.command.SelfTest}. Registered from {@code RegisterGameTestsEvent}
 * in {@code CalculatorHorror}, not auto-discovered.
 *
 * All tests share one tiny reusable structure ("empty": a 5x4x5 room with a stone floor at y=0,
 * see src/main/resources/data/calculatorhorror/structure/empty.nbt) rather than each needing its
 * own hand-authored structure file.
 *
 * Uses GameTestHelper#makeMockServerPlayerInLevel() rather than NeoForge's FakePlayer - this one
 * goes through the real PlayerList#placeNewPlayer flow with an actual embedded network Connection,
 * so (unlike FakePlayer, whose connection field is null) teleportdim/respawn/sound - which need a
 * real connection - can actually be verified here instead of just SKIPping. It's marked
 * @Deprecated(forRemoval=true) in vanilla, so if a future MC/NeoForge version removes it, these
 * specific tests are the ones that will need revisiting.
 *
 * Run with: ./gradlew runGameTestServer
 */
@PrefixGameTestTemplate(false)
public final class ActionToolkitGameTests {
    private static final String NS = CalculatorHorror.MODID;
    private static final String TEMPLATE = "empty";

    private ActionToolkitGameTests() {
    }

    private static ServerPlayer spawnPlayer(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        BlockPos pos = helper.absolutePos(new BlockPos(2, 1, 2));
        player.teleportTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
        return player;
    }

    @GameTest(templateNamespace = NS, template = TEMPLATE)
    public static void setblock(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 1, 1);
        BlockActions.set(helper.getLevel(), helper.absolutePos(pos), Blocks.GLASS.defaultBlockState());
        helper.assertBlockPresent(Blocks.GLASS, pos);
        helper.succeed();
    }

    @GameTest(templateNamespace = NS, template = TEMPLATE)
    public static void chestActions(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 1, 1);
        ServerLevel level = helper.getLevel();
        BlockPos absPos = helper.absolutePos(pos);
        BlockActions.set(level, absPos, Blocks.CHEST.defaultBlockState());
        ContainerActions.setSlot(level, absPos, 0, new ItemStack(Items.APPLE, 5));

        ItemStack peeked = ContainerActions.getSlot(level, absPos, 0);
        helper.assertTrue(peeked.is(Items.APPLE), "expected apples in the chest, found " + peeked.getItem());
        helper.assertTrue(peeked.getCount() == 5, "expected 5 apples, got " + peeked.getCount());

        ContainerActions.clear(level, absPos);
        helper.assertTrue(ContainerActions.getSlot(level, absPos, 0).isEmpty(), "container should be empty after clear");
        helper.succeed();
    }

    @GameTest(templateNamespace = NS, template = TEMPLATE)
    public static void effectActions(GameTestHelper helper) {
        ServerPlayer player = spawnPlayer(helper);
        EffectActions.give(player, net.minecraft.world.effect.MobEffects.GLOWING, 100, 0);
        helper.assertTrue(player.hasEffect(net.minecraft.world.effect.MobEffects.GLOWING), "effect should be applied");

        EffectActions.clearAll(player);
        helper.assertTrue(!player.hasEffect(net.minecraft.world.effect.MobEffects.GLOWING), "effect should be cleared");
        helper.succeed();
    }

    @GameTest(templateNamespace = NS, template = TEMPLATE)
    public static void inventoryActions(GameTestHelper helper) {
        ServerPlayer player = spawnPlayer(helper);
        InventoryActions.give(player, new ItemStack(Items.DIAMOND, 3));
        boolean found = false;
        for (int slot = 0; slot < InventoryActions.size(player); slot++) {
            ItemStack stack = InventoryActions.getSlot(player, slot);
            if (stack.is(Items.DIAMOND) && stack.getCount() == 3) {
                found = true;
                break;
            }
        }
        helper.assertTrue(found, "expected 3 diamonds in inventory");

        InventoryActions.clear(player);
        for (int slot = 0; slot < InventoryActions.size(player); slot++) {
            helper.assertTrue(InventoryActions.getSlot(player, slot).isEmpty(), "inventory should be empty after clear");
        }
        helper.succeed();
    }

    @GameTest(templateNamespace = NS, template = TEMPLATE)
    public static void enderChestActions(GameTestHelper helper) {
        ServerPlayer player = spawnPlayer(helper);
        EnderChestActions.setSlot(player, 0, new ItemStack(Items.NETHERITE_INGOT, 2));
        ItemStack peeked = EnderChestActions.getSlot(player, 0);
        helper.assertTrue(peeked.is(Items.NETHERITE_INGOT) && peeked.getCount() == 2, "ender chest slot 0 should hold 2 netherite ingots");

        EnderChestActions.clear(player);
        helper.assertTrue(EnderChestActions.getSlot(player, 0).isEmpty(), "ender chest should be empty after clear");
        helper.succeed();
    }

    @GameTest(templateNamespace = NS, template = TEMPLATE)
    public static void shulkerInInventoryActions(GameTestHelper helper) {
        ServerPlayer player = spawnPlayer(helper);
        InventoryActions.setSlot(player, 0, new ItemStack(Items.SHULKER_BOX));
        ItemStack holder = InventoryActions.getSlot(player, 0);
        ItemContainerActions.setSlot(holder, 0, ItemContainerActions.SHULKER_BOX_SLOTS, new ItemStack(Items.GOLD_INGOT, 4));
        InventoryActions.setSlot(player, 0, holder);

        ItemStack shulkerContents = ItemContainerActions.getSlot(InventoryActions.getSlot(player, 0), 0);
        helper.assertTrue(
            shulkerContents.is(Items.GOLD_INGOT) && shulkerContents.getCount() == 4,
            "shulker box slot 0 should hold 4 gold ingots");
        helper.succeed();
    }

    @GameTest(templateNamespace = NS, template = TEMPLATE)
    public static void gameModeActions(GameTestHelper helper) {
        ServerPlayer player = spawnPlayer(helper);
        GameModeActions.set(player, GameType.CREATIVE);
        helper.assertTrue(GameModeActions.get(player) == GameType.CREATIVE, "game mode should be creative");
        helper.succeed();
    }

    @GameTest(templateNamespace = NS, template = TEMPLATE)
    public static void teleportActions(GameTestHelper helper) {
        ServerPlayer player = spawnPlayer(helper);
        BlockPos target = helper.absolutePos(new BlockPos(3, 1, 3));
        TeleportActions.teleport(player, target.getX() + 0.5, target.getY(), target.getZ() + 0.5);
        helper.assertTrue(
            Math.abs(player.getX() - (target.getX() + 0.5)) < 0.01 && Math.abs(player.getZ() - (target.getZ() + 0.5)) < 0.01,
            "player should have moved to the target position");
        helper.succeed();
    }

    @GameTest(templateNamespace = NS, template = TEMPLATE)
    public static void soundActions(GameTestHelper helper) {
        ServerPlayer player = spawnPlayer(helper);
        // No exception is the bar here - actual audibility can't be asserted headlessly.
        SoundActions.playToPlayer(player, SoundEvents.AMBIENT_CAVE.value(), SoundSource.MASTER, 1.0F, 1.0F);
        helper.succeed();
    }

    @GameTest(templateNamespace = NS, template = TEMPLATE)
    public static void ghostChunkActions(GameTestHelper helper) {
        ServerPlayer player = spawnPlayer(helper);
        ChunkActions.ghost(player, new net.minecraft.world.level.ChunkPos(helper.absolutePos(BlockPos.ZERO)));
        helper.succeed();
    }

    @GameTest(templateNamespace = NS, template = TEMPLATE)
    public static void revealChunkActions(GameTestHelper helper) {
        ServerPlayer player = spawnPlayer(helper);
        var pos = new net.minecraft.world.level.ChunkPos(helper.absolutePos(BlockPos.ZERO));
        ChunkActions.ghost(player, pos);
        ChunkActions.reveal(player, pos);
        helper.succeed();
    }

    @GameTest(templateNamespace = NS, template = TEMPLATE)
    public static void messageActions(GameTestHelper helper) {
        ServerPlayer player = spawnPlayer(helper);
        // No exception is the bar here, same as soundActions - a real client would show the text.
        MessageActions.sendToPlayer(player, Component.literal("gametest direct message"));
        MessageActions.sendToAll(helper.getLevel().getServer(), Component.literal("gametest broadcast message"));
        helper.succeed();
    }

    @GameTest(templateNamespace = NS, template = TEMPLATE)
    public static void teleportDimActions(GameTestHelper helper) {
        ServerPlayer player = spawnPlayer(helper);
        ServerLevel nether = helper.getLevel().getServer().getLevel(Level.NETHER);
        if (nether == null) {
            helper.fail("nether level not loaded");
            return;
        }
        TeleportActions.teleport(player, nether, 8.5, 64.0, 8.5);
        helper.succeedWhen(() -> helper.assertTrue(player.level().dimension() == Level.NETHER, "player should have moved to the nether"));
    }

    @GameTest(templateNamespace = NS, template = TEMPLATE)
    public static void respawnActions(GameTestHelper helper) {
        ServerPlayer player = spawnPlayer(helper);
        ServerPlayer respawned = RespawnActions.respawn(player);
        helper.assertTrue(respawned != null, "respawn should return the new player entity");
        helper.assertTrue(respawned.getHealth() == respawned.getMaxHealth(), "respawned player should be at full health");
        helper.succeed();
    }
}
