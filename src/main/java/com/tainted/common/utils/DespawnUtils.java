package com.tainted.common.utils;

import com.tainted.common.config.ConfigGetter;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static com.tainted.common.utils.LevelUtils.newAABB;

public class DespawnUtils {

    // TODO: find a better way to do this
    // a list of entities that should not be despawned
    private static final ArrayList<EntityType<?>> blackList = new ArrayList<>(List.of(
            // bosses/dungeon enemies
            EntityType.ENDER_DRAGON,
            EntityType.WITHER,
            EntityType.GUARDIAN,
            EntityType.ELDER_GUARDIAN,
            /* this should prevent raids/roaming parties from being
              affected, though there might be a better way to do this */
            EntityType.PILLAGER,
            EntityType.EVOKER,
            EntityType.ILLUSIONER,
            EntityType.RAVAGER,
            // this shouldn't happen, but better safe than sorry
            EntityType.PLAYER
    ));

    private static boolean shouldDespawn(@NotNull Entity entity) {
        EntityType<?> type = entity.getType();
        // see if the mob is in the list
        if (ConfigGetter.getEnableList()) {
            String mobKey = EntityType.getKey(type).toString();
            return ConfigGetter.getMobList().contains(mobKey);
        }
        // see if the mob is in the category, minus blacklisted ones
        if (!blackList.contains(type)) return type.getCategory().equals(MobCategory.MONSTER);
        return false;
    }

    private static void despawn(@NotNull Entity entity) {
        Level level = entity.getLevel();
        if (shouldDespawn(entity) && !entity.hasCustomName()) {
            // get entity's position for particles
            double x = entity.getX();
            double y = entity.getY() + 1.0D;
            double z = entity.getZ();
            // despawn the entity and spawn particles
            entity.discard();
            if (!(level instanceof ServerLevel)) return;
            ((ServerLevel) level).sendParticles(ParticleTypes.POOF, x, y, z, 15, 0.05D, 0.50D, 0.05D, 0.001D);
        }
    }

    @Nullable
    private static Player getNearbyPlayer(@NotNull Player player, double distance) {
        Level level = player.getLevel();
        Player nearbyPlayer = level.getNearestPlayer(TargetingConditions.forNonCombat(), player);
        if (!(nearbyPlayer == null || nearbyPlayer == player) && nearbyPlayer.distanceTo(player) <= distance)
            return nearbyPlayer;
        else return null;
    }

    private static boolean isWithinArea(Entity entity, AABB area) {
        if (entity == null) return false;
        AABB entityBounds = entity.getBoundingBox();
        return entityBounds.intersects(area);
    }

    private static void despawnSelected(@NotNull Player player, Player player2, AABB area) {
        Level level = player.getLevel();
        Difficulty difficulty = level.getDifficulty();

        AABB area1 = newAABB(player2, 8.0D * (SleepUtils.scaling(difficulty) / 2.0D), 6.0D);
        AABB exclusion = area1.intersect(area);
        for (Entity entity : level.getEntities(null, area)) {
            if (!isWithinArea(entity, exclusion)) { despawn(entity); }
        }
    }

    private static void despawnSelected(@NotNull Player player, AABB area) {
        Level level = player.getLevel();
        for (Entity entity : level.getEntities(null, area)) { despawn(entity); }
    }

    private static boolean isPlayerCheckEnabled(Player player, AABB area) {
        if (ConfigGetter.getEnablePlayerCheck()) return true;
        despawnSelected(player, area);
        return false;
    }

    private static boolean isPlayerNearby(Player player, double h, AABB area) {
        Player nearby = getNearbyPlayer(player, h * 1.25D);
        if (SleepUtils.notCheater(nearby) && isWithinArea(player, area)) return true;
        despawnSelected(player, area);
        return false;
    }

    private static boolean isOtherPlayerValid(Player player, Player player2, AABB area) {
        int st = SleepUtils.timeReq();
        if (!player2.equals(player) && player2.getSleepTimer() < st && SleepUtils.notCheater(player2)) return true;
        despawnSelected(player, area);
        return false;
    }

    public static void despawnEntities(Level level, ServerPlayer player) {
        Difficulty difficulty = level.getDifficulty();

        if (difficulty == Difficulty.PEACEFUL) return;  // do nothing if peaceful
        double scaling = SleepUtils.scaling(difficulty);
        double h = Math.round(ConfigGetter.getHorizontalRange() / scaling);
        double v = Math.round(ConfigGetter.getVerticalRange() / scaling);
        AABB area = newAABB(player, h, v);
        // check to see if player check is enabled in config
        if (!isPlayerCheckEnabled(player, area)) return;
        // sees if another player is within the range of the main player
        if (!isPlayerNearby(player, h, area)) return;

        for (Player others : level.getNearbyPlayers(TargetingConditions.forNonCombat(), player, area)) {
            // makes sure the other player isn't the main player and isn't cheating
            if (!isOtherPlayerValid(player, others, area)) return;
            despawnSelected(player, others, area);
        }
    }

}
