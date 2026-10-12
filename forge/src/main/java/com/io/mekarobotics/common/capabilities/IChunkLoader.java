package com.io.mekarobotics.common.capabilities;

import net.minecraft.world.level.ChunkPos;

import java.util.UUID;

public interface IChunkLoader {
	
	UUID getUUID();
	
	void loadChunks(ChunkPos chunk);
	
	void unloadChunks(ChunkPos chunk);

}
