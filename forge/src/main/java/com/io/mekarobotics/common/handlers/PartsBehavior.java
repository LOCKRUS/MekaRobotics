package com.io.mekarobotics.common.handlers;

import com.io.mekarobotics.Robotics;
import com.io.mekarobotics.common.capabilities.ModCapabilities;
import com.io.mekarobotics.common.helpers.util.InventoryUtil;
import com.io.mekarobotics.common.robot.EnumModuleSlot;
import com.io.mekarobotics.integration.config.RoboticsConfig;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = Robotics.MODID, bus = EventBusSubscriber.Bus.GAME)
public class PartsBehavior {

    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if(entity.level().isClientSide() || !entity.level().getGameRules().getBoolean(GameRules.RULE_DOMOBLOOT)) return;
        ModCapabilities.get(entity, ModCapabilities.PARTS).ifPresent(parts -> {
            for(EnumModuleSlot slot : EnumModuleSlot.values()) {
                for(ItemStack stack : parts.getBodyParts(slot)) {
                    if(Robotics.RANDOM.nextDouble() < RoboticsConfig.general.limbDropChance.get()) {
                        InventoryUtil.dropItem(entity, stack);
                    }
                }
            }
        });
    }
}
