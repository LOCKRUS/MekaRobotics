package com.io.mekarobotics.definitions;

import com.io.mekarobotics.Robotics;
import com.io.mekarobotics.common.misc.CopyContentsFunction;
import com.mojang.serialization.MapCodec;
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

    public static final DeferredRegister<LootItemFunctionType<?>> LOOT_FUNCTIONS = DeferredRegister.create(Registries.LOOT_FUNCTION_TYPE, Robotics.MODID);

    public static final DeferredHolder<LootItemFunctionType<?>, LootItemFunctionType<CopyContentsFunction>> COPY_CONTENTS = register("copy_contents", CopyContentsFunction.INSTANCE);

    private static <T extends LootItemFunction> DeferredHolder<LootItemFunctionType<?>, LootItemFunctionType<T>> register(String name, T lootItemFunction) {
        MapCodec<T> codec = MapCodec.unit(lootItemFunction);
        return LOOT_FUNCTIONS.register(name, () -> new LootItemFunctionType<>(codec));
    }
}
