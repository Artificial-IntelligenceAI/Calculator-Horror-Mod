package com.calculatorhorror.effect;

import com.calculatorhorror.CalculatorHorror;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Horror-flavored {@link MobEffect}s. These carry no gameplay logic of their own (no periodic
 * ticking, no stat changes) - they're purely a marker a client-side renderer checks for
 * (see {@code com.calculatorhorror.client.ShortSightRenderEvents}), applied/cleared through the
 * existing generic {@code EffectActions} the same as any vanilla effect.
 */
public final class CalculatorHorrorEffects {
    public static final DeferredRegister<MobEffect> MOB_EFFECTS = DeferredRegister.create(Registries.MOB_EFFECT, CalculatorHorror.MODID);

    public static final DeferredHolder<MobEffect, MobEffect> SHORT_SIGHT =
        MOB_EFFECTS.register("short_sight", () -> new MobEffect(MobEffectCategory.HARMFUL, 0x37474F) {
        });

    private CalculatorHorrorEffects() {
    }
}
