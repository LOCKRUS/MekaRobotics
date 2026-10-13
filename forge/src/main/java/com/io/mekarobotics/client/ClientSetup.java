package com.io.mekarobotics.client;

import com.io.mekarobotics.Robotics;
import com.io.mekarobotics.client.rendering.*;
import com.io.mekarobotics.client.rendering.particles.FlareParticle;
import com.io.mekarobotics.client.rendering.particles.SparkParticles;
import com.io.mekarobotics.client.screen.*;
import com.io.mekarobotics.client.tooltips.ClientItemTooltip;
import com.io.mekarobotics.client.tooltips.ClientModuleTooltip;
import com.io.mekarobotics.client.tooltips.ItemTooltip;
import com.io.mekarobotics.client.tooltips.ModuleTooltip;
import com.io.mekarobotics.definitions.ModEntityTypes;
import com.io.mekarobotics.definitions.ModMachines;
import com.io.mekarobotics.definitions.ModMenuTypes;
import com.io.mekarobotics.definitions.ModParticles;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.RecipeBookCategories;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.common.EventBusSubscriber;

import javax.annotation.ParametersAreNonnullByDefault;
import java.io.IOException;
import java.util.function.Supplier;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
@EventBusSubscriber(modid = Robotics.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public class ClientSetup {

    public static final ShaderTracker SHADER_SHIELD = new ShaderTracker();

    @SubscribeEvent
    public static void registerMenuScreens(RegisterMenuScreensEvent event) {
            event.register(ModMenuTypes.WIRE_CUTTER.get(), WireCutterScreen::new);
            event.register(ModMenuTypes.ASSEMBLER.get(), AssemblerScreen::new);
            event.register(ModMenuTypes.FACTORY.get(), FactoryScreen::new);
            event.register(ModMenuTypes.STORAGE.get(), StorageScreen::new);
            event.register(ModMenuTypes.ROBOT.get(), RobotScreen::new);
            event.register(ModMenuTypes.ROBOT_INFO.get(), RobotInfoScreen::new);
            event.register(ModMenuTypes.ROBOT_COMMANDS.get(), RobotCommandScreen::new);
            event.register(ModMenuTypes.COMMANDER.get(), CommanderScreen::new);
    }

    @SubscribeEvent
    public static void registerClientToolTipTypes(RegisterClientTooltipComponentFactoriesEvent event) {
        event.register(ModuleTooltip.class, ClientModuleTooltip::new);
        event.register(ItemTooltip.class, ClientItemTooltip::new);
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        EntityRenderers.register(ModEntityTypes.ROBOT.get(), RobotRenderer::new);
        EntityRenderers.register(ModEntityTypes.STOMPED_BLOCK.get(), StompedBlockRenderer::new);
        event.registerBlockEntityRenderer(ModMachines.ROBOT_STORAGE.getBlockEntityType(), RobotStorageRenderer::new);
        event.registerBlockEntityRenderer(ModMachines.ROBOT_FACTORY.getBlockEntityType(), RobotFactoryRenderer::new);
        event.registerBlockEntityRenderer(ModMachines.MACHINE_ARM.get(), MachineArmRenderer::new);
    }

    @SubscribeEvent
    public static void registerRecipeCategories(RegisterRecipeBookCategoriesEvent event) {
        // Avoids log dumps
        event.registerRecipeCategoryFinder(ModMachines.ASSEMBLER.getRecipeType(), r -> RecipeBookCategories.UNKNOWN);
        event.registerRecipeCategoryFinder(ModMachines.WIRE_CUTTER.getRecipeType(), r -> RecipeBookCategories.UNKNOWN);
    }

    @SubscribeEvent
    public static void registerModels(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(MachineArmModel.LAYER_LOCATION, MachineArmModel::createBodyLayer);
    }

    @SubscribeEvent
    public static void registerParticles(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ModParticles.SPARK.get(), SparkParticles.SparkProvider::new);
        event.registerSpriteSet(ModParticles.FLARE.get(), FlareParticle.FlareProvider::new);
    }

    @SubscribeEvent
    public static void registerShaders(RegisterShadersEvent event) throws IOException {
        event.registerShader(new ShaderInstance(event.getResourceProvider(), ResourceLocation.fromNamespaceAndPath(Robotics.MODID, "shield"), DefaultVertexFormat.POSITION_COLOR_NORMAL), SHADER_SHIELD::setInstance);
    }

    public static class ShaderTracker implements Supplier<ShaderInstance> {

        private ShaderInstance instance;
        final RenderStateShard.ShaderStateShard shard = new RenderStateShard.ShaderStateShard(this);

        private ShaderTracker() {
        }

        private void setInstance(ShaderInstance instance) {
            this.instance = instance;
        }

        @Override
        public ShaderInstance get() {
            return instance;
        }
    }
}
