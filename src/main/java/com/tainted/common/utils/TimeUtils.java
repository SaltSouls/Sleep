package com.tainted.common.utils;

import com.tainted.Sleep;
import com.tainted.common.capability.StatusCapProvider;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

import java.util.concurrent.atomic.AtomicReference;

public class TimeUtils {
    
    private static final int dayLength = Level.TICKS_PER_DAY;


    private static boolean getTime(Level level, Time time) {
        if (level == null || time == null) return false; // this should never happen
        long start = time.getStart();
        long end = time.getEnd();
        /* apparently, this is how the game gets the time of day. don't know
         why it doesn't reset to 0 on waking or hitting 24000, but whatever */
        long dayTime = level.getDayTime() % dayLength;
        return dayTime >= start && dayTime < end;
    }

    private static boolean isTimeChunk(Level level, Time slice1, Time slice2, Time slice3) {
        return getTime(level, slice1) || getTime(level, slice2) || getTime(level, slice3);
    }

    private static Time getTimeChunk(Level level) {
        if (isTimeChunk(level, Time.MORNING_E, Time.MORNING, Time.MORNING_L)) return Time.MORNING;
        else if (isTimeChunk(level, Time.NOON_E, Time.NOON, Time.NOON_L)) return Time.NOON;
        else if (isTimeChunk(level, Time.EVENING_E, Time.EVENING, Time.EVENING_L)) return Time.EVENING;
        else if (isTimeChunk(level, Time.NIGHT_E, Time.NIGHT, Time.NIGHT_L)) return Time.NIGHT;
        else return null;
    }

    private static Time determineTimeSlice(Level level, Time slice1, Time slice2, Time slice3) {
        if (getTime(level, slice1)) return slice1;
        else if (getTime(level, slice2)) return slice2;
        else return slice3;
    }

    public static Time getTimeSlice(Level level) {
        Time timeChunk = getTimeChunk(level);
        if (timeChunk == null) return null; // this should never happen
        switch (timeChunk) {
            case MORNING -> { return determineTimeSlice(level, Time.MORNING_E, Time.MORNING, Time.MORNING_L); }
            case NOON -> { return determineTimeSlice(level, Time.NOON_E, Time.NOON, Time.NOON_L); }
            case EVENING -> { return determineTimeSlice(level, Time.EVENING_E, Time.EVENING, Time.EVENING_L); }
            case NIGHT -> { return determineTimeSlice(level, Time.NIGHT_E, Time.NIGHT, Time.NIGHT_L); }
            default -> {
                // this should never happen
                Sleep.LOGGER.info("""
                Time = null
                Some how you managed to make time null. This shouldn't be possible.
                Please let me know what you were doing when this occurred.""");
                return null;
            }
        }
    }

    public static Time getPlayerTimeSlice(ServerPlayer player) {
        final AtomicReference<Time> time = new AtomicReference<>();
        player.getCapability(StatusCapProvider.INSTANCE).ifPresent(playerCap -> {
            String sleepTime = playerCap.getSleepTime();
            switch (sleepTime) {
                case "early_morning" -> time.set(Time.MORNING_E);
                case "morning" -> time.set(Time.MORNING);
                case "late_morning" -> time.set(Time.MORNING_L);
                case "early_afternoon" -> time.set(Time.NOON_E);
                case "afternoon" -> time.set(Time.NOON);
                case "late_afternoon" -> time.set(Time.NOON_L);
                case "early_evening" -> time.set(Time.EVENING_E);
                case "evening" -> time.set(Time.EVENING);
                case "late_evening" -> time.set(Time.EVENING_L);
                case "early_night" -> time.set(Time.NIGHT_E);
                case "night" -> time.set(Time.NIGHT);
                case "late_night" -> time.set(Time.NIGHT_L);
                default -> time.set(null); // this should never happen
            }
        });
        return time.get();
    }

    private static boolean isPlayerTimeChunk(Time time, Time time1, Time time2, Time time3) {
        return time.equals(time1) || time.equals(time2) || time.equals(time3);
    }

    public static Time getPlayerTimeChunk(Time time) {
        if (isPlayerTimeChunk(time, Time.MORNING_E, Time.MORNING, Time.MORNING_L)) return Time.MORNING;
        else if (isPlayerTimeChunk(time, Time.NOON_E, Time.NOON, Time.NOON_L)) return Time.NOON;
        else if (isPlayerTimeChunk(time, Time.EVENING_E, Time.EVENING, Time.EVENING_L)) return Time.EVENING;
        else if (isPlayerTimeChunk(time, Time.NIGHT_E, Time.NIGHT, Time.NIGHT_L)) return Time.NIGHT;
        else return null;
    }

    public static boolean isOpposite(Level level, ServerPlayer player) {
        Time wakeTime = getTimeSlice(level);
        Time sleepTime = getPlayerTimeSlice(player);
        if (wakeTime == null || sleepTime == null) return false; // this should never happen
        switch (wakeTime) {
            case MORNING_E -> { return sleepTime.equals(Time.EVENING_E); }
            case MORNING -> { return sleepTime.equals(Time.EVENING); }
            case MORNING_L -> { return sleepTime.equals(Time.EVENING_L); }
            case NOON_E -> { return sleepTime.equals(Time.NIGHT_E); }
            case NOON -> { return sleepTime.equals(Time.NIGHT); }
            case NOON_L -> { return sleepTime.equals(Time.NIGHT_L); }
            case EVENING_E -> { return sleepTime.equals(Time.MORNING_E); }
            case EVENING -> { return sleepTime.equals(Time.MORNING); }
            case EVENING_L -> { return sleepTime.equals(Time.MORNING_L); }
            case NIGHT_E -> { return sleepTime.equals(Time.NOON_E); }
            case NIGHT -> { return sleepTime.equals(Time.NOON); }
            case NIGHT_L -> { return sleepTime.equals(Time.NOON_L); }
        }
        return false;
    }

    public static boolean isNextTimeChunk(Level level, ServerPlayer player) {
        Time wakeTime = getTimeChunk(level);
        Time sleepTime = getPlayerTimeChunk(getPlayerTimeSlice(player));
        if (wakeTime == null || sleepTime == null) return false; // this should never happen
        switch (wakeTime) {
            case MORNING -> { return sleepTime.equals(Time.NIGHT); }
            case NOON -> { return sleepTime.equals(Time.MORNING); }
            case EVENING -> { return sleepTime.equals(Time.NOON); }
            case NIGHT -> { return sleepTime.equals(Time.EVENING); }
        }
        return false;
    }


    public enum Time {
        /* These calculations are based around a 24000 tick day
        and may not result in the same level of accuracy if
        the total ticks in a day are changed. */
        MORNING_E(0, dayLength/12),                   // start:0      | end: 2,000
        MORNING(MORNING_E.end, dayLength / 6),             // start:2,000  | end: 4,000
        MORNING_L(MORNING.end, dayLength / 4),             // start:4,000  | end: 6,000
        NOON_E(MORNING_L.end, dayLength / 3),              // start:6,000  | end: 8,000
        NOON(NOON_E.end, (int)(dayLength / 2.4)),               // start:8,000  | end: 10,000
        NOON_L(NOON.end, dayLength / 2),                   // start:10,000 | end: 12,000
        EVENING_E(NOON_L.end, (int)(NOON_E.end * 1.75)),        // start:12,000 | end: 14,000
        EVENING(EVENING_E.end, NOON_E.end * 2),            // start:14,000 | end: 16,000
        EVENING_L(EVENING.end, (int)(NOON_L.end * 1.5)),        // start:16,000 | end: 18,000
        NIGHT_E(EVENING_L.end, NOON.end * 2),              // start:18,000 | end: 20,000
        NIGHT(NIGHT_E.end, (int)(EVENING.end * 1.375)),         // start:20,000 | end: 22,000
        NIGHT_L(NIGHT.end, dayLength);                          // start:22,000 | end: 24,000

        private final int start;
        private final int end;

        Time(int start, int end) {
            this.start = start;
            this.end = end;
        }

        int getStart() { return start; }

        int getEnd() { return end; }
    }

}