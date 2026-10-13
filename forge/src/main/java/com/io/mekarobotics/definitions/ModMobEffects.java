package com.io.mekarobotics.definitions;

import com.io.mekarobotics.Robotics;
import com.io.mekarobotics.common.content.GenericMobEffect;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.DyeColor;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.function.Supplier;

public class ModMobEffects {

    public static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(Registries.MOB_EFFECT, Robotics.MODID);

    public static final DeferredHolder<MobEffect, MobEffect> KNOCKBACK_RESISTANCE = register("knockback_resistance", () ->
            new GenericMobEffect(MobEffectCategory.BENEFICIAL, 0).addAttributeModifier(Attributes.KNOCKBACK_RESISTANCE, ResourceLocation.fromNamespaceAndPath(Robotics.MODID, "knockback_resistance"), 1, AttributeModifier.Operation.ADD_VALUE));
    public static final DeferredHolder<MobEffect, MobEffect> IMPACTFUL = register("impactful", () ->
            new GenericMobEffect(MobEffectCategory.BENEFICIAL, 0));
    public static final DeferredHolder<MobEffect, MobEffect> ARMOR_SHRED = register("armor_shred", () ->
            new GenericMobEffect(MobEffectCategory.HARMFUL, DyeColor.CYAN.getFireworkColor()).addAttributeModifier(Attributes.ARMOR, ResourceLocation.fromNamespaceAndPath(Robotics.MODID, "armor_shred"), -1, AttributeModifier.Operation.ADD_VALUE));

    private static DeferredHolder<MobEffect, MobEffect> register(String name, Supplier<MobEffect> effect) {
        return EFFECTS.register(name, effect);
    }
}
