package com.io.mekarobotics.definitions;

import com.io.mekarobotics.Reference;
import com.io.mekarobotics.Robotics;
import com.io.mekarobotics.common.content.items.CommanderItem;
import com.io.mekarobotics.common.robot.EnumRobotMaterial;
import com.io.mekarobotics.common.robot.EnumRobotPart;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unused")
public class ModItems {

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, Robotics.MODID);

    public static final DeferredHolder<Item, Item> CIRCUIT = registerBasicItem("circuit");
    public static final DeferredHolder<Item, Item> ADVANCED_CIRCUIT = registerBasicItem("advanced_circuit");
    public static final DeferredHolder<Item, Item> COMPLEX_CIRCUIT = registerBasicItem("complex_circuit");

    public static final DeferredHolder<Item, Item> WIRING = registerBasicItem("wiring");
    public static final DeferredHolder<Item, Item> ADVANCED_WIRING = registerBasicItem("advanced_wiring");
    public static final DeferredHolder<Item, Item> COMPLEX_WIRING = registerBasicItem("complex_wiring");

    public static final DeferredHolder<Item, Item> IRON_ROD = registerBasicItem("iron_rod");
    public static final DeferredHolder<Item, Item> ELECTRO_MAGNET = registerBasicItem("electro_magnet");
    public static final DeferredHolder<Item, Item> SERVO_MOTOR = registerBasicItem("servo_motor");

    public static final DeferredHolder<Item, Item> CAMERA_UNIT = registerBasicItem("camera_unit");
    public static final DeferredHolder<Item, Item> PERCEPTRON_LAYER = registerBasicItem("perceptron_layer");
    public static final DeferredHolder<Item, Item> NEURAL_PROCESSING_UNIT = registerBasicItem("neural_processing_unit");

    public static final DeferredHolder<Item, Item> MODULE_BATTERY = registerBasicItem("battery");
    public static final DeferredHolder<Item, Item> MODULE_SOLAR_PANEL = registerBasicItem("solar_panel");
    public static final DeferredHolder<Item, Item> MODULE_FIST = registerBasicItem("fist");
    public static final DeferredHolder<Item, Item> MODULE_TELEPORT = registerBasicItem("ender_module");
    public static final DeferredHolder<Item, Item> MODULE_DASH = registerBasicItem("dash_module");
    public static final DeferredHolder<Item, Item> MODULE_CHARGE_LEGS = registerBasicItem("charge_legs");
    public static final DeferredHolder<Item, Item> MODULE_REINFORCE = registerBasicItem("reinforce_module");
    public static final DeferredHolder<Item, Item> MODULE_STEALTH = registerBasicItem("stealth_module");
    public static final DeferredHolder<Item, Item> MODULE_SHIELD = registerBasicItem("shield_module");

    public static final DeferredHolder<Item, Item> ROBOT_SPAWN_EGG = ITEMS.register("robot_spawn_egg", () -> new SpawnEggItem(ModEntityTypes.ROBOT.get(), 0x000000, 0xffa500, new Item.Properties()));

    public static final DeferredHolder<Item, Item> COMMANDER = ITEMS.register("commander", CommanderItem::new);

    public static final Map<EnumRobotMaterial, DeferredHolder<Item, Item>> WIRES = new HashMap<>();
    public static final Map<EnumRobotMaterial, DeferredHolder<Item, Item>> PLATES = new HashMap<>();
    public static final Map<EnumRobotMaterial, Map<EnumRobotPart, DeferredHolder<Item, Item>>> MATERIALS = new HashMap<>();

    static {
        int i = 0;
        for(EnumRobotMaterial material : Reference.WIRE_METALS) {
            WIRES.put(material, registerBasicItem(material.getName() + "_wire"));
        }
        for(EnumRobotMaterial material : EnumRobotMaterial.valuesWithoutEmpty()) {
            if(material.hasPlate()) {
                PLATES.put(material, registerBasicItem("plate_" + material.getName()));
            }
            MATERIALS.put(material, new HashMap<>());
            for(EnumRobotPart part : EnumRobotPart.values()) {
                MATERIALS.get(material).put(part, ITEMS.register(material.getName() + "_" + part.getName(), () -> new Item(new Item.Properties().stacksTo(1))));
            }
        }
    }

    private static DeferredHolder<Item, Item> registerBasicItem(String name) {
        return ITEMS.register(name, () -> new Item(new Item.Properties()));
    }
}
