package com.calculatorhorror.action;

import com.calculatorhorror.CalculatorHorror;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * Takes over a player's movement and combat entirely: each tick, retargets the nearest living
 * entity, walks the player toward it, and attacks once in range - until that target dies, the
 * possession's own safety timeout runs out, or {@link #stop} is called.
 *
 * There's no vanilla pathfinding available for a non-{@code Mob} entity - {@code PathNavigation}/
 * {@code PathFinder}/{@code WalkNodeEvaluator} are typed to {@code Mob} at every layer (not just
 * the constructor - the node evaluator calls {@code Mob}-specific methods like
 * {@code getPathfindingMalus}), so reusing them for a {@code ServerPlayer} would mean forking that
 * whole stack rather than just casting around it. This instead does simple straight-line homing:
 * step toward the target each tick, try stepping up one block if the level step is blocked, and
 * give up on that tick's movement (but keep facing the target) if both are blocked. Same
 * "force it every tick via an absolute teleport, on {@code ServerTickEvent.Pre}" technique
 * {@link FreezeActions} uses to reliably override the player's own client input.
 */
@EventBusSubscriber(modid = CalculatorHorror.MODID)
public final class PossessionActions {
    private static final double SEARCH_RADIUS = 64.0;
    private static final double ATTACK_RANGE = 3.0;
    private static final double STEP_SIZE = 0.2;
    private static final int ATTACK_COOLDOWN_TICKS = 10;
    // Tried in order for each horizontal step: stay level, step up one block, or step/fall down
    // a short distance - whichever is the first with solid footing beneath it.
    private static final double[] STEP_Y_OFFSETS = {0.0, 1.0, -1.0, -2.0, -3.0};

    private static final Map<UUID, Possessed> POSSESSED = new HashMap<>();

    private PossessionActions() {
    }

    public static void start(ServerPlayer player, int maxTicks) {
        POSSESSED.put(player.getUUID(), new Possessed(player, maxTicks));
    }

    public static void stop(ServerPlayer player) {
        POSSESSED.remove(player.getUUID());
    }

    public static boolean isPossessed(ServerPlayer player) {
        return POSSESSED.containsKey(player.getUUID());
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Pre event) {
        if (POSSESSED.isEmpty()) {
            return;
        }
        Iterator<Possessed> iterator = POSSESSED.values().iterator();
        while (iterator.hasNext()) {
            Possessed possessed = iterator.next();
            if (possessed.player.isRemoved() || possessed.ticksElapsed++ >= possessed.maxTicks) {
                iterator.remove();
                continue;
            }
            if (!tick(possessed)) {
                iterator.remove();
            }
        }
    }

    /**
     * @return false once this possession is done (its target died) and should be removed
     */
    private static boolean tick(Possessed possessed) {
        ServerPlayer player = possessed.player;
        if (possessed.target == null || !possessed.target.isAlive() || possessed.target.isRemoved()) {
            possessed.target = findNearestLivingEntity(player);
        }
        LivingEntity target = possessed.target;
        if (target == null) {
            return true;
        }

        Vec3 playerPos = player.position();
        Vec3 lookTarget = target.position().add(0, target.getEyeHeight() * 0.5, 0);
        player.lookAt(EntityAnchorArgument.Anchor.EYES, lookTarget);
        float yaw = player.getYRot();
        float pitch = player.getXRot();

        double distance = playerPos.distanceTo(target.position());
        if (distance <= ATTACK_RANGE) {
            player.connection.teleport(playerPos.x, playerPos.y, playerPos.z, yaw, pitch, Set.of());
            if (possessed.attackCooldown > 0) {
                possessed.attackCooldown--;
            } else {
                player.attack(target);
                possessed.attackCooldown = ATTACK_COOLDOWN_TICKS;
                if (!target.isAlive()) {
                    return false;
                }
            }
            return true;
        }

        Vec3 direction = target.position().subtract(playerPos);
        Vec3 step = new Vec3(direction.x, 0, direction.z).normalize().scale(STEP_SIZE);
        Vec3 nextPos = clearStepTowards(player, playerPos, step);
        player.connection.teleport(nextPos.x, nextPos.y, nextPos.z, yaw, pitch, Set.of());
        return true;
    }

    /**
     * Tries the horizontal step at a few vertical offsets (level, then a step up, then a few
     * steps down) and takes the first one that both has room for the player's body and has solid
     * ground right beneath it - "has room" alone isn't enough, since open air always passes that
     * check, which is exactly how a naive version of this got the player stuck floating: nudged
     * up once by a borderline collision, it then "moved" freely through open air forever with no
     * ground-check to either stop it or bring it back down. If nothing has footing, stays put
     * this tick rather than clipping through something or wandering into open air.
     */
    private static Vec3 clearStepTowards(ServerPlayer player, Vec3 from, Vec3 step) {
        double x = from.x + step.x;
        double z = from.z + step.z;
        for (double dy : STEP_Y_OFFSETS) {
            Vec3 candidate = new Vec3(x, from.y + dy, z);
            if (isClear(player, candidate) && hasFooting(player, candidate)) {
                return candidate;
            }
        }
        return from;
    }

    private static boolean isClear(ServerPlayer player, Vec3 pos) {
        AABB box = player.getBoundingBox().move(pos.subtract(player.position()));
        return player.level().noCollision(player, box);
    }

    private static boolean hasFooting(ServerPlayer player, Vec3 pos) {
        AABB justBelowFeet = player.getBoundingBox().move(pos.subtract(player.position())).move(0.0, -0.1, 0.0);
        return !player.level().noCollision(player, justBelowFeet);
    }

    private static LivingEntity findNearestLivingEntity(ServerPlayer player) {
        Level level = player.level();
        AABB searchArea = player.getBoundingBox().inflate(SEARCH_RADIUS);
        List<LivingEntity> nearby = level.getEntitiesOfClass(
            LivingEntity.class, searchArea, entity -> entity != player && entity.isAlive());

        LivingEntity nearest = null;
        double nearestDistanceSq = Double.MAX_VALUE;
        for (LivingEntity candidate : nearby) {
            double distanceSq = candidate.distanceToSqr(player);
            if (distanceSq < nearestDistanceSq) {
                nearestDistanceSq = distanceSq;
                nearest = candidate;
            }
        }
        return nearest;
    }

    private static final class Possessed {
        final ServerPlayer player;
        final int maxTicks;
        int ticksElapsed;
        int attackCooldown;
        LivingEntity target;

        Possessed(ServerPlayer player, int maxTicks) {
            this.player = player;
            this.maxTicks = maxTicks;
        }
    }
}
