package com.io.mekarobotics.client.rendering;

import com.io.mekarobotics.Robotics;
import com.io.mekarobotics.common.content.entity.RobotEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;

public class RobotModel extends DefaultedEntityGeoModel<RobotEntity> {
    public RobotModel() {
        super(ResourceLocation.fromNamespaceAndPath(Robotics.MODID, "robot"), true);
    }

    @Override
    protected String subtype() {
        return "robot";
    }
}
