package com.io.mekarobotics;

import com.io.mekarobotics.common.capabilities.impl.perk.Perk;
import com.io.mekarobotics.common.capabilities.ModCapabilities;
import com.io.mekarobotics.common.content.blockentity.MachineBlockEntity;
import com.io.mekarobotics.common.content.blockentity.StorageBlockEntity;
import com.io.mekarobotics.common.content.blockentity.ChargerBlockEntity;
import com.io.mekarobotics.common.misc.CountedIngredient;
import com.io.mekarobotics.common.robot.RobotModule;
import com.io.mekarobotics.definitions.*;
import com.io.mekarobotics.definitions.robotics.*;
import com.io.mekarobotics.integration.config.RoboticsConfig;
import com.io.mekarobotics.network.NetworkHandler;
import com.io.mekarobotics.network.proxy.ClientProxy;
import com.io.mekarobotics.network.proxy.IProxy;
import com.io.mekarobotics.network.proxy.ServerProxy;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;
import net.neoforged.neoforge.registries.RegisterEvent;
import net.minecraft.core.registries.Registries;
import org.slf4j.Logger;

import java.util.Random;

@Mod(Robotics.MODID)
public class Robotics {

    public static final String MODID = "mekarobotics";
    public static final String MODNAME = "Robotics";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final Random RANDOM = new Random();

    public static IProxy proxy = FMLEnvironment.dist == Dist.CLIENT ? new ClientProxy() : new ServerProxy();
    public static IEventBus EVENT_BUS;

    static {
        if (FMLEnvironment.dist == Dist.CLIENT) ModSelectionTypes.loadGuis();
    }

    public Robotics(ModContainer modContainer, IEventBus modEventBus) {
        EVENT_BUS = modEventBus;

        ModItems.ITEMS.register(EVENT_BUS);
        ModBlocks.BLOCKS.register(EVENT_BUS);
        ModMachines.BLOCK_ENTITIES.register(EVENT_BUS);
        ModMachines.RECIPE_TYPES.register(EVENT_BUS);
        ModMachines.RECIPE_SERIALIZERS.register(EVENT_BUS);
        ModMenuTypes.MENU_TYPES.register(EVENT_BUS);
        ModEntityTypes.ENTITY_TYPES.register(EVENT_BUS);
        ModAttributes.ATTRIBUTES.register(EVENT_BUS);
        ModSounds.SOUNDS.register(EVENT_BUS);
        ModParticles.PARTICLES.register(EVENT_BUS);
        ModMobEffects.EFFECTS.register(EVENT_BUS);
        ModCreativeTabs.CREATIVE_TABS.register(EVENT_BUS);
        ModActions.ACTION_SERIALIZERS.register(EVENT_BUS);
        ModPerks.PERKS.register(EVENT_BUS);
        ModCommands.COMMANDS.register(EVENT_BUS);
        ModLootItemFunctions.LOOT_FUNCTIONS.register(EVENT_BUS);
        ModAttachments.ATTACHMENTS.register(EVENT_BUS);

        RoboticsConfig.registerConfigs(modContainer);


        EVENT_BUS.addListener(this::commonSetup);
        EVENT_BUS.addListener(NetworkHandler::registerPayloads);
        EVENT_BUS.addListener(this::registerCapabilities);
        EVENT_BUS.addListener(this::registerDataPack);
        EVENT_BUS.addListener(this::registerRecipeSerializers);
    }

    @SubscribeEvent
    public void commonSetup(final FMLCommonSetupEvent event) {
        NetworkHandler.registerMessages();
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public void registerCapabilities(RegisterCapabilitiesEvent event) {
        registerMachineCapabilities(event, ModMachines.ASSEMBLER.getBlockEntityType());
        registerMachineCapabilities(event, ModMachines.WIRE_CUTTER.getBlockEntityType());
        registerMachineCapabilities(event, ModMachines.ROBOT_FACTORY.getBlockEntityType());
        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK,
                (net.minecraft.world.level.block.entity.BlockEntityType) ModMachines.ROBOT_STORAGE.getBlockEntityType(),
                (be, side) -> ((StorageBlockEntity) be).energyStorage());
        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, ModMachines.CHARGER.get(),
                (be, side) -> ((ChargerBlockEntity) be).energyStorage());
        event.registerEntity(Capabilities.EnergyStorage.ENTITY, ModEntityTypes.ROBOT.get(),
                (entity, side) -> ModCapabilities.get(entity, ModCapabilities.ENERGY).orElse(null));
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void registerMachineCapabilities(RegisterCapabilitiesEvent event,
                                                     net.minecraft.world.level.block.entity.BlockEntityType type) {
        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, type,
                (be, side) -> ((MachineBlockEntity) be).energyStorage());
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, type,
                (be, side) -> ((MachineBlockEntity) be).itemHandler());
    }

    @SubscribeEvent
    public void registerDataPack(DataPackRegistryEvent.NewRegistry event) {
        event.dataPackRegistry(ModPerks.KEY, Perk.CODEC, Perk.CODEC);
        event.dataPackRegistry(ModModules.KEY, RobotModule.CODEC, RobotModule.NETWORK_CODEC);
    }

    @SubscribeEvent
    public void registerRecipeSerializers(RegisterEvent event) {
        if (event.getRegistryKey().equals(Registries.RECIPE_SERIALIZER)) {
            // Counted ingredient serializer is registered through the modern datapack codec path.
        }
    }

    public static ResourceLocation rl(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }

}
