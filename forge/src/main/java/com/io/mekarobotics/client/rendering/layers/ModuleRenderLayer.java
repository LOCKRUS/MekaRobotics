package com.io.mekarobotics.client.rendering.layers;

import com.io.mekarobotics.common.capabilities.IPartBuilt;
import com.io.mekarobotics.common.capabilities.ModCapabilities;
import com.io.mekarobotics.common.content.entity.RobotEntity;
import com.io.mekarobotics.common.robot.RobotModule;
import com.io.mekarobotics.definitions.robotics.ModModules;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

import java.util.Optional;

public class ModuleRenderLayer extends GeoRenderLayer<RobotEntity> {

    private final int layerId;

    public ModuleRenderLayer(GeoRenderer<RobotEntity> entityRendererIn, int layerId) {
        super(entityRendererIn);
        this.layerId = layerId;
    }

    @Override
    public void render(PoseStack poseStack, RobotEntity robot, BakedGeoModel bakedModel, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
        if(robot.isInvisible()) return;
        Optional<IPartBuilt> partsOptional = ModCapabilities.get(robot, ModCapabilities.PARTS);
        if(partsOptional.isEmpty()) return;
        IPartBuilt parts = partsOptional.get();

        if(!parts.hasRenderLayer(layerId)) return;
        RobotModule module = ModModules.byOverlayID(layerId);
        RenderType armorRenderType = RenderType.entityCutoutNoCull(module.getOverlay());

        getRenderer().reRender(getDefaultBakedModel(robot), poseStack, bufferSource, robot, armorRenderType,
                bufferSource.getBuffer(armorRenderType), partialTick, packedLight, packedOverlay, 1);
    }
}
