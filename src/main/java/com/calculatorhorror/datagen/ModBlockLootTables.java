package com.calculatorhorror.datagen;

import com.calculatorhorror.CalculatorHorror;
import java.util.List;
import java.util.Set;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;

/**
 * Generates this mod's block loot tables (data/calculatorhorror/loot_table/blocks/*.json)
 * instead of hand-writing them - run via ./gradlew runData.
 */
public class ModBlockLootTables extends BlockLootSubProvider {
    public ModBlockLootTables(HolderLookup.Provider registries) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), registries);
    }

    @Override
    protected void generate() {
        dropSelf(CalculatorHorror.END_TOUCH_BLOCK.get());
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return List.of(CalculatorHorror.END_TOUCH_BLOCK.get());
    }
}
