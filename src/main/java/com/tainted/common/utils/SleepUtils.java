package com.tainted.common.utils;

import com.tainted.common.config.ConfigGetter;
import com.tainted.common.utils.TimeUtils.Time;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class SleepUtils {

    private static final int dayLength = Level.TICKS_PER_DAY;

    public static boolean notCheater(Player player) {
        return player != null && !(player.isCreative() || player.isSpectator());
    }

    public static double scaling(Difficulty difficulty) {
        return switch (difficulty) {
            case NORMAL -> 2.0D;
            case HARD -> 4.0D;
            default -> 1.0D;
        };
    }

    public static int timeReq() {
        return ConfigGetter.getSleepTimer();
    }

    public static Time getNextTimeChunk(ServerPlayer player) {
        if (player == null) return null; // this should never happen
        Time sleepTime = TimeUtils.getPlayerTimeChunk(TimeUtils.getPlayerTimeSlice(player));
        if (sleepTime == null) return null; // this should also never happen
        switch (sleepTime) {
            case MORNING -> { return Time.NOON; }
            case NOON -> { return Time.EVENING; }
            case EVENING -> { return Time.NIGHT; }
            case NIGHT -> { return Time.MORNING; }
        }
        return null;
    }

    public static Time getOppositeTime(ServerPlayer player) {
        if (player == null) return null; // this should never happen
        Time sleepTime = TimeUtils.getPlayerTimeSlice(player);
        if (sleepTime == null) return null; // this should also never happen
        switch (sleepTime) {
            case MORNING_E -> { return Time.EVENING_E; }
            case MORNING -> { return Time.EVENING; }
            case MORNING_L -> { return Time.EVENING_L; }
            case NOON_E -> { return Time.NIGHT_E; }
            case NOON -> { return Time.NIGHT; }
            case NOON_L -> { return Time.NIGHT_L; }
            case EVENING_E -> { return Time.MORNING_E; }
            case EVENING -> { return Time.MORNING; }
            case EVENING_L -> { return Time.MORNING_L; }
            case NIGHT_E -> { return Time.NOON_E; }
            case NIGHT -> { return Time.NOON; }
            case NIGHT_L -> { return Time.NOON_L; }
        }
        return null;
    }

    public static boolean isLongRest(Level level, ServerPlayer player) {
        return TimeUtils.isOpposite(level, player);
    }

    public static boolean isShortRest(Level level, ServerPlayer player) {
        return TimeUtils.isNextTimeChunk(level, player);
    }

    public static long getWakeTime(Level level, ServerPlayer player) {
//        if (isLongRest(level, player)) {
//            return getOppositeTime(player).getStart();
//        } else if (isShortRest(level, player)) {
//            return getNextTimeChunk(player).getStart() - 2000;
//        }
        // just for testing purposes
        return getOppositeTime(player).getStart();
    }

    public static Long getTimeAddition(Level level, ServerPlayer player) {
        long dayTime = level.getDayTime();
        long dayRemainder = dayTime % dayLength;
        long wantedWakeTime = SleepUtils.getWakeTime(level, player);
        long wakeTime = Math.abs(wantedWakeTime - ((dayRemainder + dayLength) % dayLength));
        return wakeTime + dayTime;
    }

}
