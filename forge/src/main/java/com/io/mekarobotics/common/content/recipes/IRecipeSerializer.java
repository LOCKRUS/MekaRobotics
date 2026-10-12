package com.io.mekarobotics.common.content.recipes;

import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import java.util.List;

public interface IRecipeSerializer<R extends Recipe<?>> extends RecipeSerializer<R> {

    @Override
    default MapCodec<R> codec() {
        return MapCodec.unit(null);
    }

    @Override
    default StreamCodec<RegistryFriendlyByteBuf, R> streamCodec() {
        return StreamCodec.unit(null);
    }

    List<R> getRecipes();

}
