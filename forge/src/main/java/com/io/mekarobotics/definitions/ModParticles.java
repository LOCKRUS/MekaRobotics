package com.io.mekarobotics.definitions;

import com.io.mekarobotics.Robotics;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;

@SuppressWarnings("unused")
public class ModParticles {

    public static final DeferredRegister<ParticleType<?>> PARTICLES = DeferredRegister.create(Registries.PARTICLE_TYPE, Robotics.MODID);

    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> SPARK = PARTICLES.register("spark",  () -> new SimpleParticleType(false));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> FLARE = PARTICLES.register("flare",  () -> new SimpleParticleType(false));
}
