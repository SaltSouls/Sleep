package com.tainted.common.utils;

import com.tainted.Sleep;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

public class LevelUtils {

    @NotNull
    public static AABB newAABB(@NotNull Entity entity, double horizontal, double vertical) {
        double x = entity.getX();
        double y = entity.getY();
        double z = entity.getZ();
        return new AABB(x - horizontal, y - vertical, z - horizontal, x + horizontal, y + vertical, z + horizontal);
    }

    public static Collection<BlockPos> getBlocksForTick(Player player) {
        Level level = player.getLevel();
        AABB bounds = newAABB(player, 128, 128);
        Iterable<BlockPos> temp = BlockPos.betweenClosed((int) bounds.minX, (int) bounds.minY, (int) bounds.minZ, (int) bounds.maxX, (int) bounds.maxY, (int) bounds.maxZ);
        Collection<BlockPos> list = new ArrayList<>();
        for (BlockPos listPos : temp) {
            BlockState state = level.getBlockState(listPos);
            if (state.getBlock() instanceof CropBlock) list.add(listPos);
        }
        return list;
    }

    private static void randomlyTickBlock(Level level, BlockPos pos) {
        level.getBlockState(pos).randomTick((ServerLevel) level, pos, level.random);
    }

    public static void tickBlocks(Player player, long timeAddition, int dayLength) {
        Level level = player.getLevel();
        int skippedTicks = (int) (timeAddition % dayLength);
        int speed = level.getGameRules().getInt(GameRules.RULE_RANDOMTICKING);
        Sleep.LOGGER.info(skippedTicks);
        int blocksToTick = getBlocksForTick(player).size();
        int blocksLeft = blocksToTick;
        Sleep.LOGGER.info("Blocks to tick = " + blocksToTick);

        for (long i = skippedTicks; i > 0; i--) { level.tickBlockEntities(); }
        for (BlockPos listPos : getBlocksForTick(player)) {
            Sleep.LOGGER.info("Block left: " + blocksLeft);
            for (int i = ((skippedTicks) * speed) / (blocksToTick * 2); i > 0; i--) {
                randomlyTickBlock(level, listPos);
                Sleep.LOGGER.info("ticks left: " + i);
            }
            blocksLeft -= 1;
        }

    }
}
