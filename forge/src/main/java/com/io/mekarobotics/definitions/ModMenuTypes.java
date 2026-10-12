package com.io.mekarobotics.definitions;

import com.io.mekarobotics.Robotics;
import com.io.mekarobotics.common.content.menu.*;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.network.IContainerFactory;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.jetbrains.annotations.Nullable;

public class ModMenuTypes {

    public static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(Registries.MENU, Robotics.MODID);

    public static final DeferredHolder<MenuType<?>, MenuType<AssemblerMenu>> ASSEMBLER = registerMenuType(AssemblerMenu::new, "assembler_menu");
    public static final DeferredHolder<MenuType<?>, MenuType<WireCutterMenu>> WIRE_CUTTER = registerMenuType(WireCutterMenu::new, "wire_cutter_menu");
    public static final DeferredHolder<MenuType<?>, MenuType<FactoryMenu>> FACTORY = registerMenuType(FactoryMenu::new, "factory_menu");
    public static final DeferredHolder<MenuType<?>, MenuType<StorageMenu>> STORAGE = registerMenuType(StorageMenu::new, "storage_menu");
    public static final DeferredHolder<MenuType<?>, MenuType<RobotMenu>> ROBOT = registerMenuType(RobotMenu::new, "robot_menu");
    public static final DeferredHolder<MenuType<?>, MenuType<RobotInfoMenu>> ROBOT_INFO = registerMenuType(RobotInfoMenu::new, "robot_info_menu");
    public static final DeferredHolder<MenuType<?>, MenuType<AbstractContainerMenu>> ROBOT_INVENTORY = registerMenuType((id, inv, buf) -> null, "robot_inventory_menu");
    public static final DeferredHolder<MenuType<?>, MenuType<RobotCommandMenu>> ROBOT_COMMANDS = registerMenuType(RobotCommandMenu::new, "robot_command_menu");
    public static final DeferredHolder<MenuType<?>, MenuType<CommanderMenu>> COMMANDER = registerMenuType(CommanderMenu::new , "commander_menu");

    private static <T extends AbstractContainerMenu> DeferredHolder<MenuType<?>, MenuType<T>> registerMenuType(IContainerFactory<T> factory, String name) {
        return MENU_TYPES.register(name, () -> IMenuTypeExtension.create(factory));
    }


    @Nullable
    public static DeferredHolder<MenuType<?>, MenuType<?>> of(MenuType<?> menuType) {
        return MENU_TYPES.getEntries().stream().filter(m -> m.get().equals(menuType)).findFirst().get();
    }
}
