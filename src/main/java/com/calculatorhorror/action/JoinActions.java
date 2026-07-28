package com.calculatorhorror.action;

import com.mojang.authlib.GameProfile;
import java.util.List;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.common.util.FakePlayerFactory;

/**
 * A fake "player joined" event: the real vanilla join chat line plus a real tab-list (player
 * list) entry, for a name that never actually connects. Built on a NeoForge {@link
 * net.neoforged.neoforge.common.util.FakePlayer} purely as a way to get a {@link ServerPlayer}
 * shape to hand to {@link ClientboundPlayerInfoUpdatePacket#createPlayerInitializing} - the
 * packet's public constructors only accept real {@code ServerPlayer} instances, and this is the
 * same trick {@code SelfTest}/the GameTest suite already lean on elsewhere in this toolkit.
 *
 * Two scopes, matching {@link MessageActions}: {@link #broadcast} is real, everyone sees it;
 * {@link #illusion} is a packet-only whisper to one target, same "invisible to everyone else"
 * pattern as {@link ChunkActions#ghost}.
 */
public final class JoinActions {
    private JoinActions() {
    }

    public static void broadcast(ServerLevel level, String name) {
        ServerPlayer fake = fakePlayer(level, name);
        level.getServer().getPlayerList().broadcastAll(ClientboundPlayerInfoUpdatePacket.createPlayerInitializing(List.of(fake)));
        MessageActions.sendToAll(level.getServer(), joinMessage(name));
    }

    public static void illusion(ServerPlayer target, String name) {
        ServerPlayer fake = fakePlayer(target.serverLevel(), name);
        target.connection.send(ClientboundPlayerInfoUpdatePacket.createPlayerInitializing(List.of(fake)));
        target.sendSystemMessage(joinMessage(name));
    }

    private static ServerPlayer fakePlayer(ServerLevel level, String name) {
        UUID uuid = UUID.nameUUIDFromBytes(("calculatorhorror:fakejoin:" + name).getBytes());
        return FakePlayerFactory.get(level, new GameProfile(uuid, name));
    }

    private static Component joinMessage(String name) {
        return Component.translatable("multiplayer.player.joined", Component.literal(name)).withStyle(ChatFormatting.YELLOW);
    }
}
