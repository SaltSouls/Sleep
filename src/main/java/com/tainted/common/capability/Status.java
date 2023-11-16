package com.tainted.common.capability;

import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.util.INBTSerializable;

public interface Status extends INBTSerializable<CompoundTag> {
    String sleepTime = "";
    String exhaustionState = "";

    String getSleepTime();

    void setSleepTime(String sleepTime);

    String getExhaustionState();

    void setExhaustionState(String exhaustionState);


}
