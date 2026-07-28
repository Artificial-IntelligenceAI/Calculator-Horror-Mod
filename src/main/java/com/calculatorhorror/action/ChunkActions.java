package com.calculatorhorror.action;

import net.minecraft.network.protocol.game.ClientboundForgetLevelChunkPacket;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;

/**
 * Make a chunk visually vanish for one specific player, without touching real server/world
 * state — sends that player's client a "forget this chunk" packet, purely a client-side
 * rendering illusion. The server keeps simulating that chunk normally the whole time, and
 * normal chunk tracking will naturally resend it to the player (e.g. next time they move),
 * making the illusion temporary on its own.
 *
 * A true forced *server-side* unload isn't a coherent operation when a player is standing in
 * or near the chunk — the server's own PLAYER ticket keeps it loaded regardless, and vanilla
 * has no such command either. This client-only illusion is the actual horror-effect primitive.
 */
public final class ChunkActions {
    private ChunkActions() {
    }

    public static void ghost(ServerPlayer player, ChunkPos pos) {
        player.connection.send(new ClientboundForgetLevelChunkPacket(pos));
    }

    /**
     * Undo {@link #ghost}: resend the chunk as it actually is right now, the same way vanilla's
     * own {@code PlayerChunkSender} sends a chunk to a newly-tracking player (see
     * {@code sendChunk} there) — so the illusion can be lifted on cue instead of only fading
     * away naturally next time the player moves. No-op if the chunk isn't currently loaded.
     */
    public static void reveal(ServerPlayer player, ChunkPos pos) {
        ServerLevel level = player.serverLevel();
        LevelChunk chunk = level.getChunkSource().getChunk(pos.x, pos.z, false);
        if (chunk == null) {
            return;
        }
        player.connection.send(
            chunk.getAuxLightManager(pos)
                .sendLightDataTo(new ClientboundLevelChunkWithLightPacket(chunk, level.getLightEngine(), null, null)));
    }
}
