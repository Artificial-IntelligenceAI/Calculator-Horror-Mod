package com.calculatorhorror.action;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;

/**
 * Play a sound that only one specific player hears (e.g. a whisper or sting), rather than
 * broadcasting to everyone nearby. Wraps ServerPlayer#playNotifySound, which sends the sound
 * packet directly to that player's own connection.
 */
public final class SoundActions {
    private SoundActions() {
    }

    public static void playToPlayer(ServerPlayer player, SoundEvent sound, SoundSource source, float volume, float pitch) {
        player.playNotifySound(sound, source, volume, pitch);
    }
}
