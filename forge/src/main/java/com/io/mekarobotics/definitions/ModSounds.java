package com.io.mekarobotics.definitions;

import com.io.mekarobotics.Robotics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;

@SuppressWarnings("unused")
public class ModSounds {

    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(Registries.SOUND_EVENT, Robotics.MODID);

    public static final DeferredHolder<SoundEvent, SoundEvent> ROBOT_HURT = register("entity.robot.hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> ROBOT_DEATH = register("entity.robot.death");
    public static final DeferredHolder<SoundEvent, SoundEvent> ROBOT_DEATH_ACTIVE = register("entity.robot.death_active");
    public static final DeferredHolder<SoundEvent, SoundEvent> ROBOT_KILL_COMMAND = register("entity.robot.kill_command");
    public static final DeferredHolder<SoundEvent, SoundEvent> ASSEMBLER = register("machine.assembler");
    public static final DeferredHolder<SoundEvent, SoundEvent> WIRE_CUTTER = register("machine.wire_cutter");
    public static final DeferredHolder<SoundEvent, SoundEvent> SHIELD_UP = register("shield.start");
    public static final DeferredHolder<SoundEvent, SoundEvent> SHIELD_IMPACT = register("shield.impact");
    public static final DeferredHolder<SoundEvent, SoundEvent> SHIELD_IMPACT_FATAL = register("shield.fatal_impact");
    public static final DeferredHolder<SoundEvent, SoundEvent> INFOPAD_OPEN = register("book_open");
    public static final DeferredHolder<SoundEvent, SoundEvent> INFOPAD_FLIP = register("book_flip");

    private static DeferredHolder<SoundEvent, SoundEvent> register(String name) {
        return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(Robotics.MODID, name)));
    }
}
