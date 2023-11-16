package com.tainted.common.utils;

import com.tainted.Sleep;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.stream.Stream;

public class LevelUtils {

    @NotNull
    public static AABB newAABB(@NotNull Entity entity, double horizontal, double vertical) {
        double x = entity.getX();
        double y = entity.getY();
        double z = entity.getZ();
        return new AABB(x - horizontal, y - vertical, z - horizontal, x + horizontal, y + vertical, z + horizontal);
    }

    public static ArrayList<BlockPos> getBlocksForTick(Player player) {
        Level level = player.getLevel();
        AABB bounds = newAABB(player, 16, 16);
        Stream<BlockPos> temp = BlockPos.betweenClosedStream(bounds);
        ArrayList<BlockPos> list = new ArrayList<>();
        temp.forEach(listPos -> {
            BlockState state = level.getBlockState(listPos);
            if (state.isRandomlyTicking() && state.getBlock() instanceof CropBlock) list.add(listPos);
        });
        return list;
    }

    private static void randomlyTickBlocks(ServerLevel level, BlockPos pos, int amount, int ticks, int speed) {
        BlockState state = level.getBlockState(pos);
        for (int i = ((ticks) * speed) / (amount * speed); i > 0; i--) {
            state.randomTick(level, pos, level.random);
            Sleep.LOGGER.info("Remaining ticks: " + i);
        }
    }

    public static void tickBlocks(Player player, long timeAddition, int dayLength) {
        Level level = player.getLevel();
        int skippedTicks = (int) (timeAddition % dayLength);
        int speed = level.getGameRules().getInt(GameRules.RULE_RANDOMTICKING);
        int blocksToTick = getBlocksForTick(player).size();
        int remainingBlocks = blocksToTick;

        for (long i = skippedTicks; i > 0; i--) { level.tickBlockEntities(); }
        if (!(level instanceof ServerLevel)) return;
        for (BlockPos listPos : getBlocksForTick(player)) {
            String blockName = level.getBlockState(listPos).getBlock().getName().toString();
            Sleep.LOGGER.info("Remaining Blocks: " + remainingBlocks + " | Current Block: " + blockName);
            randomlyTickBlocks((ServerLevel) level, listPos, blocksToTick, skippedTicks, speed);
            remainingBlocks -= 1;
        }
    }

}
