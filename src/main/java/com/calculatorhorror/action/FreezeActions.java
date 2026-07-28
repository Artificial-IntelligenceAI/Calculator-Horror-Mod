package com.calculatorhorror.action;

import com.calculatorhorror.CalculatorHorror;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * Pin a player to their exact position and look direction for a short duration, fighting back
 * against their own client input every tick - there's no vanilla "disable input" hook, so this
 * is done by repeatedly force-teleporting the player to the pose captured at
 * {@link #freeze}-time, the same absolute (non-relative) teleport {@link TeleportActions} uses
 * elsewhere, just repeated every tick instead of once.
 *
 * Must run on {@code ServerTickEvent.Pre}, not {@code Post}: while a teleport is unacknowledged,
 * {@code ServerGamePacketListenerImpl#handleMovePlayer} ignores the client's reported position
 * entirely - but only if that teleport was already sent before this tick's queued movement
 * packets get processed. Correcting on Post (confirmed by testing against a real client: the
 * player walked at full, uncorrected speed) sends the correction *after* that tick's packets
 * already moved the player, so by the next tick the previous correction has usually already been
 * acknowledged over the network and the same gap repeats - a steady one-tick-per-tick leak that
 * added up to full walking speed over a few hundred milliseconds. Pre fires before packet
 * processing, so the correction is already pending when that tick's movement packet arrives.
 */
@EventBusSubscriber(modid = CalculatorHorror.MODID)
public final class FreezeActions {
    private static final Map<UUID, Frozen> FROZEN = new HashMap<>();

    private FreezeActions() {
    }

    public static void freeze(ServerPlayer player, int ticks) {
        FROZEN.put(player.getUUID(), new Frozen(player, player.getX(), player.getY(), player.getZ(), player.getYRot(), player.getXRot(), ticks));
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Pre event) {
        if (FROZEN.isEmpty()) {
            return;
        }
        Iterator<Frozen> iterator = FROZEN.values().iterator();
        while (iterator.hasNext()) {
            Frozen frozen = iterator.next();
            if (frozen.player.isRemoved() || frozen.remainingTicks <= 0) {
                iterator.remove();
                continue;
            }
            frozen.player.connection.teleport(frozen.x, frozen.y, frozen.z, frozen.yaw, frozen.pitch, Set.of());
            frozen.remainingTicks--;
        }
    }

    private static final class Frozen {
        final ServerPlayer player;
        final double x;
        final double y;
        final double z;
        final float yaw;
        final float pitch;
        int remainingTicks;

        Frozen(ServerPlayer player, double x, double y, double z, float yaw, float pitch, int ticks) {
            this.player = player;
            this.x = x;
            this.y = y;
            this.z = z;
            this.yaw = yaw;
            this.pitch = pitch;
            this.remainingTicks = ticks;
        }
    }
}
