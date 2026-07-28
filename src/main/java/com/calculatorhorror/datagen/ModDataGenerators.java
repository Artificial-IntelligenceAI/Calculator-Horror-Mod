package com.calculatorhorror.datagen;

import java.util.List;
import java.util.Set;
import net.minecraft.data.DataProvider;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.neoforge.data.event.GatherDataEvent;

/**
 * Wires up this mod's data providers. Run with ./gradlew runData; output lands in
 * src/generated/resources (see the `data` run config in build.gradle), from where it should be
 * copied/merged into src/main/resources once reviewed - datagen output isn't picked up directly
 * at runtime.
 */
public final class ModDataGenerators {
    private ModDataGenerators() {
    }

    public static void gatherData(GatherDataEvent event) {
        event.getGenerator().addProvider(
            event.includeServer(),
            (DataProvider.Factory<LootTableProvider>) output -> new LootTableProvider(
                output,
                Set.of(),
                List.of(new LootTableProvider.SubProviderEntry(ModBlockLootTables::new, LootContextParamSets.BLOCK)),
                event.getLookupProvider()));
    }
}
