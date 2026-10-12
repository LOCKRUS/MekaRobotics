package com.io.mekarobotics.common.content.events;

import com.io.mekarobotics.common.capabilities.IPerkMap;
import com.io.mekarobotics.common.capabilities.impl.PartsCapability;
import com.io.mekarobotics.common.robot.EnumModuleSlot;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.Event;

/**
 *  PerkChangeEvent is fired whenever the perks of a robot change <br>
 *  The event is fired whenever a part about the robot changes in {@link PartsCapability#setBodyParts(EnumModuleSlot, NonNullList)}}
 *  <br>
 *  This event is not {@link Cancelable}.<br>
 *  <br>
 *  This event does not have a result. {@link HasResult}<br>
 *  <br>
 *  This event is fired on the {@link net.neoforged.neoforge.common.NeoForge#EVENT_BUS}.
 */
public class PerkChangeEvent extends Event {

    private final LivingEntity entity;
    private final IPerkMap perks;

    public PerkChangeEvent(LivingEntity entity, IPerkMap perks) {
        this.entity = entity;
        this.perks = perks;
    }

    public LivingEntity getEntity() {
        return entity;
    }

    public IPerkMap getPerks() {
        return perks;
    }
}
