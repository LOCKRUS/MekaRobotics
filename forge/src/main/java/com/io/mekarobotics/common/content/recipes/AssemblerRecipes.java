package com.io.mekarobotics.common.content.recipes;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.io.mekarobotics.definitions.ModMachines;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import net.neoforged.neoforge.common.crafting.CraftingHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@ParametersAreNonnullByDefault
public class AssemblerRecipes implements IRecipeSerializer<MachineRecipe<?>> {

    public static final int INPUT_SIZE = 4;
    public static final int OUTPUT_SIZE = 1;
    public static List<MachineRecipe<?>> recipes = new ArrayList<>();

    @Override
    public MapCodec<MachineRecipe<?>> codec() {
        return IRecipeSerializer.jsonCodec(json -> fromJson(ResourceLocation.parse(IRecipeSerializer.recipeId(json)), json), this::toJson);
    }

    private JsonObject toJson(MachineRecipe<?> recipe) {
        JsonObject json = new JsonObject();
        json.addProperty("id", recipe.id().toString());
        json.addProperty("processing_time", recipe.processingTime);
        json.addProperty("energy", recipe.energy);
        StringBuilder pattern = new StringBuilder();
        JsonObject key = new JsonObject();
        char symbol = 'a';
        for (Ingredient ingredient : recipe.inputs) {
            if (ingredient == null || ingredient.isEmpty()) {
                pattern.append(' ');
                continue;
            }
            char current = symbol++;
            pattern.append(current);
            key.add(String.valueOf(current), Ingredient.CODEC.encodeStart(JsonOps.INSTANCE, ingredient).result().orElseThrow());
        }
        json.addProperty("pattern", pattern.toString());
        json.add("key", key);
        json.add("result", ItemStack.SIMPLE_ITEM_CODEC.encodeStart(JsonOps.INSTANCE, recipe.outputs[0]).result().orElseThrow());
        return json;
    }

    public @NotNull MachineRecipe<?> fromJson(ResourceLocation loc, JsonObject json) {
        int processing_time = json.get("processing_time").getAsInt();
        int energy = json.get("energy").getAsInt();
        ItemStack result = ItemStack.SIMPLE_ITEM_CODEC.parse(com.mojang.serialization.JsonOps.INSTANCE, json.getAsJsonObject("result")).result().orElse(ItemStack.EMPTY);

        // Pattern
        String pattern = json.get("pattern").getAsString();

        if(pattern.isEmpty())
            throw new JsonSyntaxException("Invalid pattern: empty pattern not allowed");
        if(pattern.length() > INPUT_SIZE)
            throw new JsonSyntaxException("Invalid pattern: should not exceed " + INPUT_SIZE + " items.");

        // Put Ingredients in map
        Map<Character, Ingredient> ingMap = Maps.newHashMap();
        for (Map.Entry<String, JsonElement> entry : json.getAsJsonObject("key").entrySet()) {
            if (entry.getKey().length() != 1)
                throw new JsonSyntaxException("Invalid key entry: '" + entry.getKey() + "' is an invalid symbol (must be 1 character only).");
            if (" ".equals(entry.getKey()))
                throw new JsonSyntaxException("Invalid key entry: ' ' is a reserved symbol.");

            ingMap.put(entry.getKey().toCharArray()[0], Ingredient.CODEC.parse(com.mojang.serialization.JsonOps.INSTANCE, entry.getValue()).result().orElse(Ingredient.EMPTY));
        }

        Set<Character> keys = Sets.newHashSet(ingMap.keySet());

        ingMap.put(' ', Ingredient.EMPTY);

        // Ingredients
        int i = 0;
        Ingredient[] ingredients = new Ingredient[INPUT_SIZE];
        for (char chr : pattern.toCharArray()) {
            Ingredient ing = ingMap.get(chr);
            if (ing == null)
                throw new JsonSyntaxException("Pattern references symbol '" + chr + "' but it's not defined in the key");
            ingredients[i] = ing;
            keys.remove(chr);
            i++;
        }

        if (!keys.isEmpty())
            throw new JsonSyntaxException("Key defines symbols that aren't used in pattern: " + keys);

        MachineRecipe<?> recipe = new MachineRecipe.Builder(ModMachines.ASSEMBLER, loc)
                .setInputs(ingredients)
                .setOutputs(new ItemStack[] {result})
                .setEnergyRequirement(energy)
                .setProcessingTime(processing_time).build();
        recipes.add(recipe);
        return recipe;
    }

    @Override
    public List<MachineRecipe<?>> getRecipes() {
        return recipes;
    }
}
