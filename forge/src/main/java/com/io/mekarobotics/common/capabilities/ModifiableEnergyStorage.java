package com.io.mekarobotics.common.capabilities;

import net.neoforged.neoforge.energy.IEnergyStorage;

public interface ModifiableEnergyStorage extends IEnergyStorage {

    void setEnergy(int value);

    void setMaxEnergyStored(int maxEnergyStored);
}
