package com.io.mekarobotics.common.content.recipes;

import com.google.gson.JsonObject;
import com.io.mekarobotics.common.misc.CountedIngredient;
import com.io.mekarobotics.definitions.ModItems;
import com.io.mekarobotics.definitions.ModMachines;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.crafting.CraftingHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

@ParametersAreNonnullByDefault
public class WireCutterRecipes implements IRecipeSerializer<MachineRecipe<?>> {

    public static final int INPUT_SIZE = 2;
    public static final int OUTPUT_SIZE = 1;
    public static List<MachineRecipe<?>> recipes = new ArrayList<>();

    @Override
    public List<MachineRecipe<?>> getRecipes() {
        return recipes;
    }

    public @NotNull MachineRecipe<?> fromJson(ResourceLocation loc, JsonObject json) {
        int processing_time = json.get("processing_time").getAsInt();
        int energy = json.get("energy").getAsInt();
        Ingredient ingredient = CountedIngredient.fromJson(json.get("ingredient"));
        ItemStack result = ItemStack.SIMPLE_ITEM_CODEC.parse(com.mojang.serialization.JsonOps.INSTANCE, json.getAsJsonObject("result")).result().orElse(ItemStack.EMPTY);

        MachineRecipe<?> recipe = new MachineRecipe.Builder(ModMachines.WIRE_CUTTER, loc)
                .setProcessingTime(processing_time)
                .setEnergyRequirement(energy)
                .setInputs(new Ingredient[] {Ingredient.of(ModItems.IRON_ROD.get()), ingredient})
                .setOutput(result).build();
        recipes.add(recipe);
        return recipe;
    }

}
