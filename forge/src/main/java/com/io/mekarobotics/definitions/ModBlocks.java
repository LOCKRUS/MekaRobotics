package com.io.mekarobotics.definitions;

import com.io.mekarobotics.Robotics;
import com.io.mekarobotics.common.content.blocks.*;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.function.Supplier;

public class ModBlocks {

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(Registries.BLOCK, Robotics.MODID);

    public static final DeferredHolder<Block, Block> ASSEMBLER = registerBlock("assembler", AssemblerBlock::new);
    public static final DeferredHolder<Block, Block> WIRE_CUTTER = registerBlock("wire_cutter", WireCutterBlock::new);
    public static final DeferredHolder<Block, Block> ROBOT_STORAGE = registerBlock("robot_storage", StorageBlock::new);
    public static final DeferredHolder<Block, Block> ROBOT_FACTORY = registerBlock("robot_factory", FactoryBlock::new);
    public static final DeferredHolder<Block, Block> CHARGER = registerBlock("charger", ChargerBlock::new);
    public static final DeferredHolder<Block, Block> MACHINE_ARM = registerBlock("machine_arm", MachineArmBlock::new);

    private static <T extends Block> DeferredHolder<?, T> registerBlock(String name, Supplier<T> block) {
        DeferredHolder<?, T> toReturn = BLOCKS.register(name, block);
        ModItems.ITEMS.register(name, () -> new BlockItem(toReturn.get(), new Item.Properties()));
        return toReturn;
    }
}
