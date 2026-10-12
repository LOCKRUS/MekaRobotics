package com.io.mekarobotics.common.handlers;

import com.io.mekarobotics.Robotics;
import com.io.mekarobotics.common.capabilities.IChunkLoader;
import com.io.mekarobotics.common.capabilities.ModCapabilities;
import com.io.mekarobotics.common.helpers.types.Tuple;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.common.world.ForgeChunkManager;
import net.neoforged.neoforge.event.entity.EntityEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.common.EventBusSubscriber;

import java.util.*;

@EventBusSubscriber(modid = Robotics.MODID, bus = EventBusSubscriber.Bus.GAME)
public class ChunkLoadingHandler {

    @SubscribeEvent
    public static void onEntityEntersChunk(EntityEvent.EnteringSection event) {
        if(!event.didChunkChange()) return;
        if(event.getEntity().level().isClientSide()) return;
        ModCapabilities.get(event.getEntity(), ModCapabilities.CHUNK_LOADER).ifPresent(loader -> {
            loader.unloadChunks(event.getOldPos().chunk());
            loader.loadChunks(event.getNewPos().chunk());
        });
    }

    @SubscribeEvent
    public static void onEntityDeath(LivingDeathEvent event) {
        if(event.getEntity().level().isClientSide()) return;
        ModCapabilities.get(event.getEntity(), ModCapabilities.CHUNK_LOADER).ifPresent(loader -> {
            loader.unloadChunks(event.getEntity().chunkPosition());
        });
    }

    @SubscribeEvent
    public static void onLevelLoad(LevelEvent.Load event) {
        if(!(event.getLevel() instanceof ServerLevel serverLevel)) return;
        ModCapabilities.get(serverLevel, ModCapabilities.CHUNK_TRACKER).ifPresent(tracker -> {
            for(Tuple<ChunkPos,UUID> pair : tracker.pending) {
                Entity entity = serverLevel.getEntity(pair.getSecond());
                if(entity == null) continue;
                ModCapabilities.get(entity, ModCapabilities.CHUNK_LOADER).ifPresent(loader -> tracker.add(pair.getFirst(), loader));
            }
            tracker.pending.clear();
        });
    }

    /**
     * Keeps track of which Entities(UUIDs) are attempting to load which chunks, so Chunks are not removed to eagerly if multiple Entities are currently loading it
     */
        public static class ChunkTracker implements INBTSerializable<CompoundTag> {

        private final ServerLevel world;
        private final Map<ChunkPos, List<UUID>> chunks = new HashMap<>();
        final Deque<Tuple<ChunkPos, UUID>> pending = new LinkedList<>();

        public ChunkTracker(ServerLevel world){
            this.world = world;
        }

        public ChunkTracker() {
            this(null);
        }

        public void add(ChunkPos chunk, IChunkLoader loader) {
            if(this.chunks.containsKey(chunk) && this.chunks.get(chunk).contains(loader.getUUID()))
                return;

            //The world might not have loaded the entity yet. ForgeChunkManager usually ensures that entities with tickets are loaded
            if(world.getEntity(loader.getUUID()) == null) return;

            if(!this.chunks.containsKey(chunk)) {
                this.chunks.put(chunk, new LinkedList<>());
                ForgeChunkManager.forceChunk(world, Robotics.MODID, loader.getUUID(), chunk.x, chunk.z, true, false);
            }

            this.chunks.get(chunk).add(loader.getUUID());
        }

        public void remove(ChunkPos chunk, IChunkLoader loader) {
            if(!this.chunks.containsKey(chunk) || !this.chunks.get(chunk).contains(loader.getUUID()))
                return;

            if(this.chunks.get(chunk).size() == 1){
                ForgeChunkManager.forceChunk(world, Robotics.MODID, loader.getUUID(), chunk.x, chunk.z, false, false);
                this.chunks.remove(chunk);
            } else this.chunks.get(chunk).remove(loader.getUUID());
        }

        @Override
        public CompoundTag serializeNBT() {
            CompoundTag nbt = new CompoundTag();
            for(Map.Entry<ChunkPos,List<UUID>> entry : this.chunks.entrySet()) {
                CompoundTag chunkTag = new CompoundTag();
                chunkTag.putInt("chunkX", entry.getKey().x);
                chunkTag.putInt("chunkY", entry.getKey().z);


                ListTag uuids = new ListTag();
                entry.getValue().forEach(uuid -> uuids.add(StringTag.valueOf(uuid.toString())));
                chunkTag.put("uuids", uuids);

                nbt.put(entry.getKey().x + ";" + entry.getKey().z, chunkTag);
            }
            return nbt;
        }

        @Override
        public void deserializeNBT(CompoundTag nbt) {
            for(String key : nbt.getAllKeys()) {
                CompoundTag chunkTag = nbt.getCompound(key);
                ChunkPos chunk = new ChunkPos(chunkTag.getInt("chunkX"), chunkTag.getInt("chunkY"));

                ListTag uuids = chunkTag.getList("uuids", Tag.TAG_STRING);
                for (Tag value : uuids) {
                    StringTag tag = (StringTag) value;
                    UUID uuid = UUID.fromString(tag.getAsString());
                    pending.add(new Tuple<>(chunk, uuid));
                }
            }
        }
    }
}
