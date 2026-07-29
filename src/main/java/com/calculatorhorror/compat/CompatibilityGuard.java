package com.calculatorhorror.compat;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.ModLoadingException;
import net.neoforged.fml.ModLoadingIssue;
import net.neoforged.neoforgespi.language.IModInfo;

/**
 * This mod is meant to be played only alongside its own declared required dependencies (see the
 * {@code [[dependencies.calculatorhorror]]} entries in {@code neoforge.mods.toml}) - nothing else.
 * This isn't a real technical incompatibility (nothing here would actually break with other mods
 * present); it's an intentional, hard, "we don't support/allow this combination" block, since the
 * user's call is that this mod's horror mechanics are only meant to be experienced in this exact
 * configuration.
 *
 * Checked once at mod construction time, when {@link ModList} is already fully populated with
 * every other discovered mod but before any of them (including this one) have finished
 * constructing - so this can still abort loading cleanly.
 */
public final class CompatibilityGuard {
    private CompatibilityGuard() {
    }

    public static void check(ModContainer modContainer) {
        Set<String> allowed = new HashSet<>();
        allowed.add(modContainer.getModId());
        for (IModInfo.ModVersion dependency : modContainer.getModInfo().getDependencies()) {
            if (dependency.getType() == IModInfo.DependencyType.REQUIRED) {
                allowed.add(dependency.getModId());
            }
        }

        List<String> disallowed = ModList.get().getMods().stream()
            .map(IModInfo::getModId)
            .filter(modId -> !allowed.contains(modId))
            .sorted()
            .toList();

        if (!disallowed.isEmpty()) {
            throw new ModLoadingException(ModLoadingIssue.error(
                "Calculator (Horror) is only meant to be played standalone, alongside its own required "
                    + "dependencies (" + String.join(", ", allowed) + "), and no other mods. Detected "
                    + "additional mod(s) not on that list: " + String.join(", ", disallowed) + ". Remove "
                    + "the extra mod(s), or run Calculator (Horror) without them, to continue.")
                .withAffectedMod(modContainer.getModInfo()));
        }
    }
}
