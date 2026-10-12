package com.io.mekarobotics.common.content.recipes;

import com.io.mekarobotics.definitions.ModItems;
import com.io.mekarobotics.definitions.ModMachines;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Predicate;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CommanderCopyRecipe extends CustomRecipe {

    public static final Predicate<ItemStack> PREDICATE_COPY_FROM = stack ->
            !stack.isEmpty() && stack.is(ModItems.COMMANDER.get()) && stack.has(DataComponents.CUSTOM_DATA);

    public CommanderCopyRecipe(CraftingBookCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingInput inv, Level level) {
        int count = 0;
        boolean dataPresent = false;
        for (int i = 0; i < inv.size(); i++) {
            ItemStack stack = inv.getItem(i);
            if (!stack.isEmpty() && stack.is(ModItems.COMMANDER.get())) count++;
            dataPresent |= stack.has(DataComponents.CUSTOM_DATA);
        }
        return count == 2 && dataPresent;
    }

    @Override
    public ItemStack assemble(CraftingInput inv, HolderLookup.Provider registries) {
        ItemStack copyFrom = ItemStack.EMPTY;
        for (int i = 0; i < inv.size(); i++) {
            ItemStack stack = inv.getItem(i);
            if (PREDICATE_COPY_FROM.test(stack)) {
                copyFrom = stack;
                break;
            }
        }
        if (copyFrom.isEmpty()) return ItemStack.EMPTY;

        ItemStack copy = new ItemStack(ModItems.COMMANDER.get());
        CustomData customData = copyFrom.get(DataComponents.CUSTOM_DATA);
        if (customData != null) copy.set(DataComponents.CUSTOM_DATA, CustomData.of(customData.copyTag()));
        if (copyFrom.has(DataComponents.CUSTOM_NAME)) {
            copy.set(DataComponents.CUSTOM_NAME, copyFrom.get(DataComponents.CUSTOM_NAME));
        }
        return copy;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInput inv) {
        NonNullList<ItemStack> remaining = NonNullList.withSize(inv.size(), ItemStack.EMPTY);
        for (int i = 0; i < inv.size(); i++) {
            ItemStack stack = inv.getItem(i);
            if (PREDICATE_COPY_FROM.test(stack)) {
                ItemStack copied = stack.copy();
                copied.setCount(1);
                remaining.set(i, copied);
                break;
            }
        }
        return remaining;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width + height > 1;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModMachines.COMMANDERCOPY_RECIPE.get();
    }
}
