package com.tainted.common.capability;

import net.minecraft.nbt.CompoundTag;

public class StatusStore implements Status {

    private String sleepTime = "";
    private String exhaustionState = "";

    @Override
    public String getSleepTime() {
        return this.sleepTime;
    }

    @Override
    public void setSleepTime(String sleepTime) {
        this.sleepTime = sleepTime;
    }

    @Override
    public String getExhaustionState() {
        return this.exhaustionState;
    }

    @Override
    public void setExhaustionState(String exhaustionState) {
        this.exhaustionState = exhaustionState;
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putString("sleepTime", this.sleepTime);
        tag.putString("exhaustionState", this.exhaustionState);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        this.sleepTime = nbt.getString("sleepTime");
        this.exhaustionState = nbt.getString("exhaustionState");
    }
}
