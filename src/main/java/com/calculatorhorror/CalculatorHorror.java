package com.calculatorhorror;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import com.calculatorhorror.block.EndTouchBlock;
import com.calculatorhorror.datagen.ModDataGenerators;
import com.calculatorhorror.effect.CalculatorHorrorEffects;
import com.calculatorhorror.gametest.ActionToolkitGameTests;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(CalculatorHorror.MODID)
public class CalculatorHorror {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "calculatorhorror";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();

    // Deferred registers, ready for content (blocks, items, entities, sounds, etc.)
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    public static final DeferredBlock<EndTouchBlock> END_TOUCH_BLOCK = BLOCKS.registerBlock(
        "end_touch_block", EndTouchBlock::new,
        BlockBehaviour.Properties.of().mapColor(MapColor.SNOW).strength(1.5F).noCollission().noOcclusion());
    public static final DeferredItem<BlockItem> END_TOUCH_BLOCK_ITEM =
        ITEMS.registerSimpleBlockItem("end_touch_block", END_TOUCH_BLOCK);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> CREATIVE_TAB = CREATIVE_MODE_TABS.register(
        "calculatorhorror_tab", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.calculatorhorror"))
            .withTabsBefore(CreativeModeTabs.COMBAT)
            .icon(() -> END_TOUCH_BLOCK_ITEM.get().getDefaultInstance())
            .displayItems((parameters, output) -> output.accept(END_TOUCH_BLOCK_ITEM.get()))
            .build());

    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public CalculatorHorror(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::registerGameTests);
        modEventBus.addListener(ModDataGenerators::gatherData);

        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);
        CalculatorHorrorEffects.MOB_EFFECTS.register(modEventBus);

        NeoForge.EVENT_BUS.register(this);

        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        LOGGER.info("{} common setup complete", MODID);
    }

    private void registerGameTests(RegisterGameTestsEvent event) {
        event.register(ActionToolkitGameTests.class);
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("{} server starting", MODID);
    }
}
