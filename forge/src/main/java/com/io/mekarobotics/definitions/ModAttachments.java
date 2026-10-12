package com.io.mekarobotics.definitions;

import com.io.mekarobotics.Robotics;
import com.io.mekarobotics.common.capabilities.impl.EnergyStorage;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/**
 * Persistent state owned by MekaRobotics entities and machines.
 *
 * <p>NeoForge 1.21.1 no longer exposes Forge's old entity capability
 * container. Attachments are the replacement for state which belongs to an
 * entity, block entity, chunk or level. The first attachment is the robot's
 * energy storage; other robot state will move here incrementally.</p>
 */
public final class ModAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENTS =
            DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, Robotics.MODID);

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<EnergyStorage>> ROBOT_ENERGY =
            ATTACHMENTS.register("robot_energy", () -> AttachmentType
                    .serializable(() -> new EnergyStorage(1_000_000))
                    .build());

    private ModAttachments() {
    }
}
