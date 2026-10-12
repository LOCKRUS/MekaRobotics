package com.io.mekarobotics.common.capabilities;

import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.common.util.INBTSerializable;

public interface IShielded extends INBTSerializable<CompoundTag> {

    void damage(float damage);

    void tick();

    void recharge(float health);

    boolean isShielded();

    float getHealth();

    float getMaxHealth();

    boolean setActive(boolean activation);
}
