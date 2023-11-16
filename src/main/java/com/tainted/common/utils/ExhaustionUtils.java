package com.tainted.common.utils;

import com.tainted.Sleep;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.Level;

public class ExhaustionUtils {

    public static int getMultiplier(Level level) {
        Difficulty difficulty = level.getDifficulty();
        return switch (difficulty) {
            case EASY -> 5;
            case NORMAL -> 3;
            default -> 1;
        };
    }

    private static boolean isExhaustionState(Level level, int time, Exhaustion state) {
        return time >= state.getMinDays() && time < (state.getMaxDays() * getMultiplier(level));
    }

    public static Exhaustion getExhaustionState(Level level, int time) {
        if(isExhaustionState(level, time, Exhaustion.RESTED)) { return Exhaustion.RESTED; }
        else if(isExhaustionState(level, time, Exhaustion.TIRED)) { return Exhaustion.TIRED; }
        else if(isExhaustionState(level, time, Exhaustion.VERY_TIRED)) { return Exhaustion.VERY_TIRED; }
        else if(isExhaustionState(level, time, Exhaustion.EXHAUSTED)) { return Exhaustion.EXHAUSTED; }
        else if(isExhaustionState(level, time, Exhaustion.PASSED_OUT)) { return Exhaustion.PASSED_OUT; }
        else {
            // this should never happen
            Sleep.LOGGER.info("""
                Exhaustion = null
                Some how you managed to make your exhaustion null. This shouldn't be possible.
                Please let me know what you were doing when this occurred.""");
            return null;
        }
    }

    public enum Exhaustion {
        RESTED(0, 1),
        TIRED(RESTED.maxDays, RESTED.maxDays + 1),
        VERY_TIRED(TIRED.maxDays, TIRED.maxDays + 1),
        EXHAUSTED(VERY_TIRED.maxDays, VERY_TIRED.maxDays + 1),
        PASSED_OUT(EXHAUSTED.maxDays, EXHAUSTED.maxDays + 1);

        private final int minDays;
        private final int maxDays;


        Exhaustion(int minDays, int maxDays) {
            this.minDays = minDays;
            this.maxDays = maxDays;
        }

        public int getMinDays() { return minDays; }
        public int getMaxDays() { return maxDays; }
    }
}
