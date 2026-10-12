package com.io.mekarobotics.definitions;

import com.io.mekarobotics.Robotics;
import com.io.mekarobotics.common.misc.CopyContentsFunction;
import com.mojang.serialization.Codec;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ModLootItemFunctions {

    public static final DeferredRegister<LootItemFunctionType> LOOT_FUNCTIONS = DeferredRegister.create(Registries.LOOT_FUNCTION_TYPE, Robotics.MODID);

    public static final DeferredHolder<LootItemFunctionType, LootItemFunctionType> COPY_CONTENTS = register("copy_contents", CopyContentsFunction.INSTANCE);

    private static DeferredHolder<LootItemFunctionType, LootItemFunctionType> register(String name, LootItemFunction lootItemFunction) {
        Codec<LootItemFunction> codec = Codec.unit(lootItemFunction);
        return LOOT_FUNCTIONS.register(name, () -> new LootItemFunctionType(codec));
    }
}
