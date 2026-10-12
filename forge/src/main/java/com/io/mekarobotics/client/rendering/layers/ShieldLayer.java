package com.io.mekarobotics.client.rendering.layers;

import com.google.common.collect.ImmutableMap;
import com.io.mekarobotics.Robotics;
import com.io.mekarobotics.client.ClientSetup;
import com.io.mekarobotics.client.RoboticsRenderTypes;
import com.io.mekarobotics.common.capabilities.IShielded;
import com.io.mekarobotics.common.capabilities.ModCapabilities;
import com.io.mekarobotics.common.content.entity.RobotEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.model.geometry.StandaloneGeometryBakingContext;
import net.neoforged.neoforge.client.model.obj.ObjLoader;
import net.neoforged.neoforge.client.model.obj.ObjModel;
import net.neoforged.neoforge.client.model.renderable.CompositeRenderable;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public class ShieldLayer extends GeoRenderLayer<RobotEntity> {

    public static final ResourceLocation MODEL = ResourceLocation.fromNamespaceAndPath(Robotics.MODID, "models/uvsphere.obj");
    private static final float WIDTHMOD = 3 / 2f;
    private static final float HEIGHTMOD = 2 / 3f;

    private final CompositeRenderable renderable;

    public ShieldLayer(GeoRenderer<RobotEntity> renderer) {
        super(renderer);
        ObjModel model = new ObjLoader().loadModel(new ObjModel.ModelSettings(MODEL, false, false, false, true, null));
        renderable = model.bakeRenderable(StandaloneGeometryBakingContext.create(MODEL));
    }

    @Override
    public void render(PoseStack poseStack, RobotEntity animatable, BakedGeoModel bakedModel, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
        if(!ModCapabilities.get(animatable, ModCapabilities.SHIELDED).isPresent()) return;
        IShielded shield = ModCapabilities.get(animatable, ModCapabilities.SHIELDED).get();
        if(!shield.isShielded()) return;

        AABB aabb = animatable.getBoundingBox();
        float widthX = (float) (aabb.maxX - aabb.minX) * WIDTHMOD;
        float height = (float) (aabb.maxY - aabb.minY) * HEIGHTMOD;
        float widthZ = (float) (aabb.maxZ - aabb.minZ) * WIDTHMOD;
        Vec3 centerOffset = aabb.getCenter().subtract(animatable.position());

        int diffuseColor = ModCapabilities.get(animatable, ModCapabilities.PARTS).orElse(ModCapabilities.NO_PARTS).getColor().getTextureDiffuseColor();
        float[] color = new float[] {(diffuseColor >> 16 & 255) / 255.0F, (diffuseColor >> 8 & 255) / 255.0F, (diffuseColor & 255) / 255.0F};
        Vector3f cameraPos = Robotics.proxy.getPlayer().getEyePosition().toVector3f();
        float strength = shield.getHealth() / shield.getMaxHealth();

        poseStack.pushPose();
        renderable.render(poseStack, bufferSource, name -> {
            ClientSetup.SHADER_SHIELD.get().safeGetUniform("Color").set(color);
            ClientSetup.SHADER_SHIELD.get().safeGetUniform("CameraPos").set(cameraPos);
            ClientSetup.SHADER_SHIELD.get().safeGetUniform("Hit").set(animatable.invulnerableTime);
            ClientSetup.SHADER_SHIELD.get().safeGetUniform("Strength").set(strength);
            return RoboticsRenderTypes.RENDER_TYPE_SHIELD.apply(name);
        }, packedLight, packedOverlay, partialTick, transformOf(translateAndScale(centerOffset, widthX, height, widthZ)));
        poseStack.popPose();
    }

    private CompositeRenderable.Transforms transformOf(Matrix4f operation) {
        return  CompositeRenderable.Transforms.of(ImmutableMap.<String, Matrix4f>builder().put("Sphere", operation).build());
    }

    private Matrix4f translateAndScale(Vec3 translation, float xScale, float yScale, float zScale) {
        return new Matrix4f(xScale, 0, 0, 0, 0, yScale, 0, 0, 0, 0, zScale, 0, (float) translation.x, (float) translation.y, (float) translation.z, 1);
    }
}
