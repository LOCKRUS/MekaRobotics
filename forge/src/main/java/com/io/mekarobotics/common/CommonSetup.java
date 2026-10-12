package com.io.mekarobotics.common;

import com.io.mekarobotics.Robotics;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.common.EventBusSubscriber;
import net.minecraft.core.registries.Registries;

import java.util.HashMap;

@EventBusSubscriber(modid = Robotics.MODID, bus = EventBusSubscriber.Bus.GAME)
public class CommonSetup {

    /**
     * List of basic instances of most living entities, excluding special ones like lightning. Only contains elements once a world is loaded
     */
    public static final HashMap<EntityType<?>, LivingEntity> allLivingEntities = new HashMap<>();

    @SubscribeEvent
    public static void onLevelLoad(LevelEvent.Load event) {
        if(!allLivingEntities.isEmpty() && event.getLevel().equals(allLivingEntities.get(EntityType.CREEPER).level())) return;
        if(!(event.getLevel() instanceof Level level)) return;
        for(EntityType<?> type : Registries.ENTITY_TYPE.getValues()) {
            try {
                Entity entity = type.create(level);
                if(entity instanceof LivingEntity living) {
                    allLivingEntities.put(type, living);
                }
            } catch(Exception e) {
                Robotics.LOGGER.info("Unable to initialize entity {}. It will not show up in type selection", type.getDescription());
            }
        }
    }

    @SuppressWarnings("unchecked")
    public static <T extends Entity> Class<T> getClassOf(EntityType<T> type) {
        return (Class<T>) allLivingEntities.get(type).getClass();
    }
}
