package com.io.mekarobotics.datagen;

import com.io.mekarobotics.Robotics;
import com.io.mekarobotics.common.robot.EnumRobotMaterial;
import com.io.mekarobotics.common.robot.EnumRobotPart;
import com.io.mekarobotics.definitions.ModItems;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredHolder;

public class ItemModelGenerator extends ItemModelProvider {

    public static final ResourceLocation DEFAULT_PARENT = ResourceLocation.fromNamespaceAndPath("minecraft", "item/generated");

    public ItemModelGenerator(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, Robotics.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        for(DeferredHolder<Item, Item> item : ModItems.PLATES.values()) {
            String name = item.getId().getPath();
            texture(name, Robotics.rl("item/strong_platings/strong_" + name));
        }
        for(EnumRobotMaterial material : EnumRobotMaterial.valuesWithoutEmpty()) {
            for(EnumRobotPart part : EnumRobotPart.values()) {
                String name = material.getName() + "_" + part.getName();
                texture(name, Robotics.rl("item/robot/" + material.getName() + "/robot_" + part.getName()));
            }
        }
    }

    public void texture(String name, ResourceLocation texture) {
        singleTexture(name, DEFAULT_PARENT, "layer0", texture);
    }
}
