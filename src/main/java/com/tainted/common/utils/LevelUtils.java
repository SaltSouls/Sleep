package com.tainted.common.utils;

import com.tainted.Sleep;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Stream;

public class LevelUtils {

    @NotNull
    public static AABB newAABB(@NotNull Entity entity, double horizontal, double vertical) {
        double x = entity.getX();
        double y = entity.getY();
        double z = entity.getZ();
        return new AABB(x - horizontal, y - vertical, z - horizontal, x + horizontal, y + vertical, z + horizontal);
    }

    private static int chunks(int amount) { return amount * 16; }

    private static Stream<BlockPos> getBlocksForTick(Player player) {
        Level level = player.getLevel();
        AABB bounds = newAABB(player, chunks(4), chunks(2));
        return BlockPos.betweenClosedStream(bounds).filter(blockPos -> {
            BlockState state = level.getBlockState(blockPos);
            return (state.getBlock() instanceof CropBlock && state.isRandomlyTicking());
        });
    }

    private static void randomlyTickBlock(ServerLevel level, BlockPos pos, int ticks, int speed) {
        BlockState state = level.getBlockState(pos);
        for (int i = ((ticks * speed) / 4096); i > 0; i--) {
            state.randomTick(level, pos, level.random);
        }
    }


    public static void tickBlocks(Player player, long time) {
        Level level = player.getLevel();
        int skippedTicks = (int) (time - level.getDayTime());
        int speed = level.getGameRules().getInt(GameRules.RULE_RANDOMTICKING);

        Sleep.LOGGER.info("Time Skipped: " + skippedTicks + " | Total Blocks to Tick: " + getBlocksForTick(player).toList().size());
        for (long i = skippedTicks; i > 0; i--) { level.tickBlockEntities(); }
        getBlocksForTick(player).forEach(blockPos -> randomlyTickBlock((ServerLevel) level, blockPos, skippedTicks, speed));
    }

}
