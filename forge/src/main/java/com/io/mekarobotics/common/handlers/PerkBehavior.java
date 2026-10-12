package com.io.mekarobotics.common.handlers;

import com.io.mekarobotics.Robotics;
import com.io.mekarobotics.common.capabilities.ModCapabilities;
import com.io.mekarobotics.common.capabilities.impl.perk.Perk;
import com.io.mekarobotics.common.content.events.ModuleActivationEvent;
import com.io.mekarobotics.common.content.events.PerkChangeEvent;
import com.io.mekarobotics.common.helpers.types.TempAttrMap;
import com.io.mekarobotics.common.helpers.types.Tuple;
import com.io.mekarobotics.common.robot.EnumModuleSlot;
import com.io.mekarobotics.common.robot.EnumRobotMaterial;
import com.io.mekarobotics.common.robot.EnumRobotPart;
import com.io.mekarobotics.definitions.ModAttributes;
import com.io.mekarobotics.definitions.robotics.ModModules;
import com.io.mekarobotics.definitions.robotics.ModPerks;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.DefaultAttributes;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = Robotics.MODID, bus = EventBusSubscriber.Bus.GAME)
public class PerkBehavior {

    public static final int PERK_TICK_RATE = 10;
    public static final float CORROSION_CHANCE_PER_DAY = 0.3f;

    @SubscribeEvent
    public static void onPerkChange(PerkChangeEvent event) {
        LivingEntity entity = event.getEntity();
        if(entity.level().isClientSide) return;
        @SuppressWarnings("unchecked")
        AttributeSupplier defaults = DefaultAttributes.getSupplier((EntityType<? extends LivingEntity>) entity.getType());
        TempAttrMap tempAttributeMap = new TempAttrMap(defaults);
        for(Tuple<Perk, Integer> tup : event.getPerks()) {
            tempAttributeMap.addTransientAttributeModifiers(tup.getFirst().getAttributeModifiers(tup.getSecond()));
        }
        //Copy the values to the actual attribute map
        for(Attribute attribute : tempAttributeMap.getAttributes()) {
            AttributeInstance instance = entity.getAttributes().getSyncableAttributes().stream()
                    .filter(candidate -> candidate.getAttribute().value() == attribute)
                    .findFirst().orElse(null);
            if(instance != null) {
                instance.setBaseValue(tempAttributeMap.getValue(attribute));
                ModAttributes.onAttributeChanged(entity, instance);
            }
        }
    }

    @SubscribeEvent
    public static void onDayChange(ServerTickEvent.Post event) {
        if(0 != event.getServer().overworld().dayTime()) return;
        for(ServerLevel level : event.getServer().getAllLevels()) {
            for(Entity entity : level.getAllEntities()) {
                ModCapabilities.get(entity, ModCapabilities.PARTS).ifPresent(parts -> {
                    for(EnumModuleSlot slot : EnumModuleSlot.primaries()) {
                        if(Math.random() > CORROSION_CHANCE_PER_DAY) continue;
                        EnumRobotMaterial material = parts.materialForSlot(slot);
                        for(ItemStack stack : parts.getBodyParts(slot)) {
                            if(ModModules.get(stack).getPerks().contains(ModPerks.PERK_CORRODABLE.get())) {
                                parts.setBodyPart(EnumRobotPart.valueOf(slot), material.getWeatheredMaterial());
                            }
                        }
                    }
                });
            }
        }
    }

    @SubscribeEvent
    public static void onEntityUpdate(EntityTickEvent.Post event) {
        if(!(event.getEntity() instanceof Mob mob)) return;
        if(mob.tickCount % PERK_TICK_RATE == 0) {
            ModCapabilities.get(mob, ModCapabilities.PERKS).ifPresent(perks -> {
                for(Tuple<Perk, Integer> tup : perks) {
                    tup.getFirst().onEntityUpdate(tup.getSecond(), mob, perks.values());
                }
            });
        }
    }

    @SubscribeEvent
    public static void jump(LivingEvent.LivingJumpEvent event) {
        if(!(event.getEntity() instanceof Mob mob)) return;
        ModCapabilities.get(event.getEntity(), ModCapabilities.PERKS).ifPresent(perks -> {
            for(Tuple<Perk, Integer> tup : perks) {
                tup.getFirst().onEntityJump(tup.getSecond(), mob, perks.values());
            }
        });
    }

    @SubscribeEvent
    public static void onModuleActivated(ModuleActivationEvent event) {
        if(!(event.getCaster() instanceof Mob mob)) return;
        ModCapabilities.get(event.getCaster(), ModCapabilities.PERKS).ifPresent(perks -> {
            for(Tuple<Perk, Integer> tup : perks) {
                tup.getFirst().onModuleActivated(tup.getSecond(), mob, perks.values());
            }
        });
    }

    @SubscribeEvent
    public static void onDamage(LivingIncomingDamageEvent event) {
        Entity causingEntity = event.getSource().getEntity();
        Entity targetEntity = event.getEntity();
        float originalAmount = event.getAmount();
        // First, apply the perks of the attacker
        if(causingEntity != null && targetEntity != null) {
            if(!(causingEntity instanceof Mob mob)) return;
            ModCapabilities.get(causingEntity, ModCapabilities.PERKS).ifPresent(perks -> {
                float damageChange = 0;
                for(Tuple<Perk, Integer> tup : perks) {
                    damageChange += tup.getFirst().onAttack(tup.getSecond(), mob, targetEntity, perks.values());
                }
                event.setAmount(event.getAmount() + damageChange);
            });
        }
        // Secondly, apply the perks of the hurt entity
        if(targetEntity instanceof Mob mob) {
            ModCapabilities.get(targetEntity, ModCapabilities.PERKS).ifPresent(perks -> {
                for(Tuple<Perk, Integer> tup : perks) {
                    event.setAmount(tup.getFirst().onDamage(tup.getSecond(), mob, event.getSource(), event.getAmount(), perks.values()));
                }
            });
        }
        if(event.getAmount() <= 0 && originalAmount != event.getAmount()) {
            event.setCanceled(true);
        }
    }
}
