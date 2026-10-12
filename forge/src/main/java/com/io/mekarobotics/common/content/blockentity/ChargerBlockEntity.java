package com.io.mekarobotics.common.content.blockentity;

import com.io.mekarobotics.common.capabilities.ModCapabilities;
import com.io.mekarobotics.common.capabilities.impl.EnergyStorage;
import com.io.mekarobotics.common.content.blocks.ChargerBlock;
import com.io.mekarobotics.common.helpers.EntityFinder;
import com.io.mekarobotics.definitions.ModMachines;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@ParametersAreNonnullByDefault
public class ChargerBlockEntity extends BlockEntity {

    protected EnergyStorage storage;

    public ChargerBlockEntity(BlockPos p_155229_, BlockState p_155230_) {
        super(ModMachines.CHARGER.get(), p_155229_, p_155230_);
        storage = new EnergyStorage(1000000, 2000);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, ChargerBlockEntity charger) {
        ModCapabilities.energy(charger).ifPresent(chargerEnergy -> {
            List<LivingEntity> entitiesToCharge = level.getEntities(EntityFinder.LIVING, new AABB(pos), ent -> ModCapabilities.energy(ent).isPresent());
            if(state.getValue(ChargerBlock.ACTIVE) == entitiesToCharge.isEmpty()) {
                BlockState newState = state.setValue(ChargerBlock.ACTIVE, !entitiesToCharge.isEmpty());
                level.setBlock(pos, newState, 3);
                setChanged(level, pos, newState);
            }

            for(Entity entity : entitiesToCharge) {
                ModCapabilities.energy(entity).ifPresent(energyStorage -> {
                    int extractedEnergy = chargerEnergy.extractEnergy(Integer.MAX_VALUE, false);
                    energyStorage.receiveEnergy(extractedEnergy, false);
                });
            }
        });

    }

    @Override
    protected void saveAdditional(CompoundTag compound, HolderLookup.Provider provider) {
        super.saveAdditional(compound, provider);
        compound.put("energy", storage.serializeNBT(provider));
    }

    @Override
    protected void loadAdditional(CompoundTag compound, HolderLookup.Provider provider) {
        super.loadAdditional(compound, provider);
        storage.deserializeNBT(provider, compound.get("energy"));
    }

    public EnergyStorage energyStorage() {
        return storage;
    }

}
