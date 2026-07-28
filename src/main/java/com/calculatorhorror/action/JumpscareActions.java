package com.calculatorhorror.action;

import com.calculatorhorror.effect.CalculatorHorrorEffects;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;

/**
 * A jumpscare: a full-screen white flash plus a loud sound, both directed at just one player,
 * with their position/look direction pinned for the same duration so they can't immediately
 * look away or walk off. Composes three existing toolkit pieces rather than inventing new
 * plumbing - {@link EffectActions} (the flash, via {@link CalculatorHorrorEffects#JUMPSCARE_FLASH}
 * and the client-side renderer that checks for it), {@link SoundActions}, and {@link FreezeActions}.
 */
public final class JumpscareActions {
    private JumpscareActions() {
    }

    public static void trigger(ServerPlayer player, SoundEvent sound, int durationTicks) {
        EffectActions.give(player, CalculatorHorrorEffects.JUMPSCARE_FLASH, durationTicks, 0);
        SoundActions.playToPlayer(player, sound, SoundSource.MASTER, 1.0F, 1.0F);
        FreezeActions.freeze(player, durationTicks);
    }
}
