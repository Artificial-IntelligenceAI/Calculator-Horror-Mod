package com.calculatorhorror.action;

import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

/**
 * Give/clear potion effects on a living entity (typically a player).
 */
public final class EffectActions {
    private EffectActions() {
    }

    public static void give(LivingEntity target, Holder<MobEffect> effect, int durationTicks, int amplifier) {
        target.addEffect(new MobEffectInstance(effect, durationTicks, amplifier));
    }

    public static void clear(LivingEntity target, Holder<MobEffect> effect) {
        target.removeEffect(effect);
    }

    public static void clearAll(LivingEntity target) {
        target.removeAllEffects();
    }
}
