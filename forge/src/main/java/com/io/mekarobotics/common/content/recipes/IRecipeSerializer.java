package com.io.mekarobotics.common.content.recipes;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.io.mekarobotics.Robotics;
import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;

import java.util.List;
import java.util.function.Function;

public interface IRecipeSerializer<R extends Recipe<?>> extends RecipeSerializer<R> {

    MapCodec<JsonElement> JSON_CODEC = MapCodec.assumeMapUnsafe(ExtraCodecs.JSON);

    /**
     * Adapts the legacy recipe JSON parser to the 1.21 codec-based recipe API.
     * The recipe id is not part of the codec payload in vanilla, so parsers use
     * a stable synthetic id when the data pack does not provide one.
     */
    static <R> MapCodec<R> jsonCodec(Function<JsonObject, R> decoder, Function<R, JsonObject> encoder) {
        return JSON_CODEC.xmap(element -> {
            if (!element.isJsonObject()) {
                throw new IllegalArgumentException("Recipe payload must be a JSON object");
            }
            return decoder.apply(element.getAsJsonObject());
        }, recipe -> encoder.apply(recipe));
    }

    static String recipeId(JsonObject json) {
        return json.has("id") ? json.get("id").getAsString() : Robotics.MODID + ":runtime_recipe";
    }

    @Override
    MapCodec<R> codec();

    @Override
    default StreamCodec<RegistryFriendlyByteBuf, R> streamCodec() {
        return ByteBufCodecs.fromCodecWithRegistriesTrusted(codec().codec());
    }

    List<R> getRecipes();
}
