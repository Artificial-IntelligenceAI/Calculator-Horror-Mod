package com.calculatorhorror.action;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;

/**
 * Change a player's game mode.
 */
public final class GameModeActions {
    private GameModeActions() {
    }

    public static void set(ServerPlayer player, GameType gameMode) {
        player.setGameMode(gameMode);
    }

    public static GameType get(ServerPlayer player) {
        return player.gameMode.getGameModeForPlayer();
    }
}
