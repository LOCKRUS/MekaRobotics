package com.io.mekarobotics.common.content.perks;

import com.io.mekarobotics.common.capabilities.impl.perk.Perk;
import com.io.mekarobotics.common.helpers.types.SimpleDataManager;
import com.io.mekarobotics.common.helpers.util.Lang;
import com.io.mekarobotics.integration.config.RoboticsConfig;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.UUID;

public class PerkAcceleration extends Perk {

	public static final ResourceLocation MODIFIER_ID = ResourceLocation.fromNamespaceAndPath("mekarobotics", "perk_acceleration");
	public static final String ACCELERATION = "perk_acceleration";
	public int MAX_ACC_TICKS;
	public float MAX_ACCELERATION;
	public float SPEED_SLOWDOWN;

	public PerkAcceleration(String name) {
		super(name, 2);
	}

	@Override
	public void onEntityUpdate(int level, Mob entity, SimpleDataManager values) {
		float currentAcc = values.get(ACCELERATION);
		MAX_ACC_TICKS = RoboticsConfig.general.accelerationChargeUp.get();
		MAX_ACCELERATION = RoboticsConfig.general.accelerationMax.get().floatValue();
		SPEED_SLOWDOWN = RoboticsConfig.general.accelerationSlowDown.get().floatValue();
		if(entity.moveDist > 0.15) {
			values.set(ACCELERATION, Math.min(values.get(ACCELERATION) + 1, MAX_ACC_TICKS));
		} else {
			values.set(ACCELERATION, Math.max(values.get(ACCELERATION) - 20, 0));
		}
		float acceleration = values.get(ACCELERATION);
		if(acceleration == currentAcc) return;
		float scalar = (MAX_ACCELERATION + SPEED_SLOWDOWN) * (acceleration / MAX_ACC_TICKS) - SPEED_SLOWDOWN;
		AttributeInstance attributeInstance = entity.getAttribute(Attributes.MOVEMENT_SPEED);
		if(attributeInstance == null) return;
			if(attributeInstance.getModifier(MODIFIER_ID) == null || attributeInstance.getModifier(MODIFIER_ID).amount() != scalar) {
				attributeInstance.removeModifier(MODIFIER_ID);
				attributeInstance.addTransientModifier(new AttributeModifier(MODIFIER_ID, scalar, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
		}
	}

	@Override
	public Component getDescriptionText() {
		return Lang.localise("perk.acceleration.desc", String.format("%.1f%%", SPEED_SLOWDOWN * 100), MAX_ACC_TICKS / 20, String.format("%.1f%%", MAX_ACCELERATION * 100));
	}
}
