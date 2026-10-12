package com.io.mekarobotics.definitions;

import com.io.mekarobotics.Robotics;
import com.io.mekarobotics.common.content.entity.RobotEntity;
import com.io.mekarobotics.common.content.entity.StompedUpBlockEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;

public class ModEntityTypes {

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(Registries.ENTITY_TYPE, Robotics.MODID);

    public static final DeferredHolder<EntityType<?>, EntityType<RobotEntity>> ROBOT = ENTITY_TYPES.register("robot",
            () -> EntityType.Builder.of((EntityType.EntityFactory<RobotEntity>) (type, level) -> new RobotEntity(level), MobCategory.MISC)
                    .sized(0.4f, 2)
                    .build(ResourceLocation.fromNamespaceAndPath(Robotics.MODID, "robot").toString()));


    public static final DeferredHolder<EntityType<?>, EntityType<StompedUpBlockEntity>> STOMPED_BLOCK = ENTITY_TYPES.register("stomped_block",
            () -> EntityType.Builder.of((EntityType.EntityFactory<StompedUpBlockEntity>) (type, level) -> new StompedUpBlockEntity(level), MobCategory.MISC)
                    .sized(1, 1).build(ResourceLocation.fromNamespaceAndPath(Robotics.MODID, "stomped_block").toString()));

}
