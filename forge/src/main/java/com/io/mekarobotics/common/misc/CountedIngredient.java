package com.io.mekarobotics.common.misc;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Minecraft 1.21 makes Ingredient final. The amount is therefore kept as
 * recipe metadata while the vanilla Ingredient remains responsible for item
 * matching and network serialization.
 */
public final class CountedIngredient {
    private static final Map<Ingredient, Integer> COUNTS = Collections.synchronizedMap(new WeakHashMap<>());

    private CountedIngredient() {
    }

    public static Ingredient fromJson(JsonElement element) {
        if (!element.isJsonObject()) return Ingredient.EMPTY;
        JsonObject json = element.getAsJsonObject();
        JsonElement ingredientJson = json.has("items") ? json.get("items") : json;
        Ingredient ingredient = Ingredient.fromJson(ingredientJson);
        int amount = json.has("amount") ? Math.max(1, json.get("amount").getAsInt()) : 1;
        COUNTS.put(ingredient, amount);
        return ingredient;
    }

    public static void setCount(Ingredient ingredient, int amount) {
        COUNTS.put(ingredient, Math.max(1, amount));
    }

    public static int getCount(Ingredient ingredient) {
        if (ingredient == null || ingredient.isEmpty()) return 0;
        Integer count = COUNTS.get(ingredient);
        if (count != null) return count;
        return ingredient.getItems().length == 0 ? 1 : Math.max(1, ingredient.getItems()[0].getCount());
    }
}
