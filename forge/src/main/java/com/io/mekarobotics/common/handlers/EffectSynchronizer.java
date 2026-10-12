package com.io.mekarobotics.common.handlers;

import com.google.common.collect.HashMultimap;
import com.io.mekarobotics.Robotics;
import com.io.mekarobotics.network.NetworkHandler;
import com.io.mekarobotics.network.messages.client.PacketSetEntityEffects;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.common.EventBusSubscriber;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

@EventBusSubscriber(modid = Robotics.MODID, bus = EventBusSubscriber.Bus.GAME)
public class EffectSynchronizer {

    private static final HashMultimap<Integer, UUID> entitiesToSynchronize = HashMultimap.create();

    public static void onEffectChanged(LivingEntity entity, MobEffectInstance instance, boolean toAdd) {
        if(entity.level().isClientSide()) return;
        if(!entitiesToSynchronize.containsKey(entity.getId())) return;
        Collection<MobEffectInstance> effectInstances = new ArrayList<>(entity.getActiveEffects());
        if(toAdd) {
            effectInstances.add(instance);
        } else effectInstances.remove(instance);

        for(UUID uuid : entitiesToSynchronize.get(entity.getId())) {
            ServerPlayer player = (ServerPlayer) entity.level().getPlayerByUUID(uuid);
            NetworkHandler.sendToPlayer(new PacketSetEntityEffects(entity.getId(), effectInstances), player);
        }
    }

    @SubscribeEvent
    public static void onEffectAdded(MobEffectEvent.Added event) {
        onEffectChanged(event.getEntity(), event.getEffectInstance(), true);
    }

    @SubscribeEvent
    public static void onEffectRemoved(MobEffectEvent.Remove event) {
        onEffectChanged(event.getEntity(), event.getEffectInstance(), false);
    }

    @SubscribeEvent
    public static void onEffectExpired(MobEffectEvent.Expired event) {
        onEffectChanged(event.getEntity(), event.getEffectInstance(), false);
    }

    public static void addWatcher(Entity entity, ServerPlayer watcher) {
        entitiesToSynchronize.put(entity.getId(), watcher.getUUID());
    }

    public static void removeWatcher(Entity entity, ServerPlayer watcher) {
        entitiesToSynchronize.remove(entity.getId(), watcher.getUUID());
    }

    public static boolean isWatchedBy(Entity entity, ServerPlayer watcher) {
        return entitiesToSynchronize.containsEntry(entity.getId(), watcher.getUUID());
    }
}
