package com.calculatorhorror.action;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;

/**
 * Force-load/unload chunks the same way vanilla's /forceload command does — via
 * ServerLevel#setChunkForced, which both registers the ticket and persists it in the level's
 * ForcedChunksSavedData (what ServerLevel#getForcedChunks() actually reads from; the lower-level
 * ServerChunkCache#updateChunkForced alone only touches the live ticket, not that persisted set).
 * "Unload" releases our forced hold on a chunk (letting it unload once nothing else, e.g. a
 * nearby player, keeps it loaded) rather than an instant unconditional eviction — there is no
 * such thing as forcing a chunk to unload out from under a player standing in it.
 */
public final class ChunkActions {
    private ChunkActions() {
    }

    public static void forceLoad(ServerLevel level, ChunkPos pos) {
        level.setChunkForced(pos.x, pos.z, true);
    }

    public static void unload(ServerLevel level, ChunkPos pos) {
        level.setChunkForced(pos.x, pos.z, false);
    }
}
