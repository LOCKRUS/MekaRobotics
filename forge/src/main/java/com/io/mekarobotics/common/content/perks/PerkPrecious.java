package com.io.mekarobotics.common.content.perks;

import com.google.common.base.Predicates;
import com.io.mekarobotics.common.capabilities.IRobot;
import com.io.mekarobotics.common.capabilities.ModCapabilities;
import com.io.mekarobotics.common.capabilities.impl.perk.Perk;
import com.io.mekarobotics.common.helpers.types.EntitySearch;
import com.io.mekarobotics.common.helpers.types.Selection;
import com.io.mekarobotics.common.helpers.types.SimpleDataManager;
import com.io.mekarobotics.common.robot.RobotCommand;
import com.io.mekarobotics.definitions.robotics.ModCommands;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;

import java.util.List;

public class PerkPrecious extends Perk {
    public PerkPrecious(String name) {
        super(name);
    }

    @Override
    public float onDamage(int level, Mob robot, DamageSource dmgSource, float damage, SimpleDataManager values) {
        //TODO FORCE allies to support this robot
        IRobot iRobot = ModCapabilities.get(robot, ModCapabilities.ROBOT).get();
        if(iRobot.hasOwner() && dmgSource.getEntity() != null) {
            for(Entity ent : alliesInArea(robot, 16, iRobot.getOwner(), Predicates.alwaysTrue())) {
                ModCapabilities.get(ent, ModCapabilities.COMMANDS).ifPresent(commands -> {
                    commands.addCommand(new RobotCommand(ModCommands.ATTACK.get(), List.of(Selection.of(new EntitySearch(dmgSource.getEntity().getUUID())))));
                });
            }
        }
        return super.onDamage(level, robot, dmgSource, damage, values);
    }
}
