package com.tainted.common.config;

import java.util.List;

import static com.tainted.common.config.Config.COMMON;

public interface ConfigGetter {

    static int getSleepTimer() {
        return COMMON.SLEEP_TIMER.get();
    }

    static boolean getEnablePlayerCheck() {
        return COMMON.PLAYER_CHECK.get();
    }

    static boolean getEnableList() {
        return COMMON.ENABLE_LIST.get();
    }

    static List<? extends String> getMobList() {
        return COMMON.MOB_LIST.get();
    }

    static boolean getEnableScaling() {
        return COMMON.ENABLE_SCALING.get();
    }

    static int getVerticalRange() {
        return COMMON.VERTICAL_RANGE.get();
    }

    static int getHorizontalRange() {
        return COMMON.HORIZONTAL_RANGE.get();
    }

}
