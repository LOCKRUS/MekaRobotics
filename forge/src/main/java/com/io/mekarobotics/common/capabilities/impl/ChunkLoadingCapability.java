package com.io.mekarobotics.common.capabilities.impl;

import com.io.mekarobotics.common.capabilities.IChunkLoader;
import com.io.mekarobotics.common.capabilities.IRobot;
import com.io.mekarobotics.common.capabilities.ModCapabilities;
import com.io.mekarobotics.common.handlers.ChunkLoadingHandler;
import com.io.mekarobotics.integration.config.RoboticsConfig;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;

import java.util.Optional;
import java.util.UUID;

public class ChunkLoadingCapability implements IChunkLoader {

    private final Entity entity;

    public ChunkLoadingCapability(Entity entity) {
        this.entity = entity;
    }

    @Override
    public UUID getUUID() {
        return entity.getUUID();
    }

    @Override
    public void loadChunks(ChunkPos chunk) {
        if(entity.level().isClientSide()) return;
        Optional<ChunkLoadingHandler.ChunkTracker> opt = ModCapabilities.get(entity.level(), ModCapabilities.CHUNK_TRACKER).get();
        if(opt.isEmpty()) return;
        Optional<IRobot> robot = ModCapabilities.get(entity, ModCapabilities.ROBOT).get();
        if(robot.isEmpty()) return;
        if(RoboticsConfig.general.chunkLoadShutdown.get() && !robot.get().isActive()) return;

        ChunkLoadingHandler.ChunkTracker tracker = opt.get();
        switch(robot.get().getChunkLoadingState()) {
            case 0: break; //NO-OP
            case 1: tracker.add(chunk, this); break;
            case 2:
                for(int i = -1; i <= 1; i++) {
                    for(int j = -1; j <= 1; j++) {
                        tracker.add(new ChunkPos(chunk.x + i, chunk.z + j), this);
                    }
                }
                break;
        }
    }

    @Override
    public void unloadChunks(ChunkPos chunk) {
        if(entity.level().isClientSide()) return;
        ModCapabilities.get(entity.level(), ModCapabilities.CHUNK_TRACKER).ifPresent(tracker -> {
            for(int i = -1; i <= 1; i++) {
                for(int j = -1; j <= 1; j++) {
                    tracker.remove(new ChunkPos(chunk.x + i, chunk.z + j), this);
                }
            }
        });
    }
}
