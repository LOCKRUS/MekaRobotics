package com.io.mekarobotics.common.capabilities;

import com.io.mekarobotics.Robotics;
import com.io.mekarobotics.common.capabilities.impl.ChunkLoadingCapability;
import com.io.mekarobotics.common.capabilities.impl.CommandCapability;
import com.io.mekarobotics.common.capabilities.impl.EnergyStorage;
import com.io.mekarobotics.common.capabilities.impl.PartsCapability;
import com.io.mekarobotics.common.capabilities.impl.RobotCapability;
import com.io.mekarobotics.common.capabilities.impl.ShieldCapability;
import com.io.mekarobotics.common.capabilities.impl.inventory.BaseInventory;
import com.io.mekarobotics.common.capabilities.impl.inventory.RobotInventory;
import com.io.mekarobotics.common.capabilities.impl.perk.AdvancedPerkMap;
import com.io.mekarobotics.common.capabilities.impl.perk.Perk;
import com.io.mekarobotics.common.content.entity.RobotEntity;
import com.io.mekarobotics.common.handlers.ChunkLoadingHandler;
import com.io.mekarobotics.common.helpers.types.SimpleDataManager;
import com.io.mekarobotics.common.helpers.types.Tuple;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;

/**
 * NeoForge 1.21.1 access point for state formerly exposed through Forge's
 * Capability/LazyOptional API.
 *
 * <p>The old capability container was removed from NeoForge. This class is a
 * deliberately small compatibility boundary: callers keep asking for the
 * domain interfaces while storage is moved to registered attachments one
 * capability family at a time. The robot energy attachment is the first
 * family being migrated; the remaining families use this boundary until
 * their attachment serializers are ported.</p>
 */
public final class ModCapabilities {
    public static final LegacyCapability<IRobot> ROBOT = new LegacyCapability<>("robot");
    public static final LegacyCapability<IPerkMap> PERKS = new LegacyCapability<>("perks");
    public static final LegacyCapability<IPartBuilt> PARTS = new LegacyCapability<>("parts");
    public static final LegacyCapability<ICommandable> COMMANDS = new LegacyCapability<>("commands");
    public static final LegacyCapability<IChunkLoader> CHUNK_LOADER = new LegacyCapability<>("chunk_loader");
    public static final LegacyCapability<ChunkLoadingHandler.ChunkTracker> CHUNK_TRACKER = new LegacyCapability<>("chunk_tracker");
    public static final LegacyCapability<IShielded> SHIELDED = new LegacyCapability<>("shielded");
    public static final LegacyCapability<EnergyStorage> ENERGY = new LegacyCapability<>("energy");
    public static final LegacyCapability<RobotInventory> INVENTORY = new LegacyCapability<>("inventory");

    public static final ResourceLocation LOC_ROBOT = Robotics.rl("robot");
    public static final ResourceLocation LOC_INVENTORY = Robotics.rl("inventory");
    public static final ResourceLocation LOC_ENERGY = Robotics.rl("energy");
    public static final ResourceLocation LOC_PERKS = Robotics.rl("perks");
    public static final ResourceLocation LOC_PARTS = Robotics.rl("parts");
    public static final ResourceLocation LOC_COMMANDS = Robotics.rl("commands");
    public static final ResourceLocation LOC_LOADER = Robotics.rl("chunk_loader");
    public static final ResourceLocation LOC_TRACKER = Robotics.rl("chunk_tracker");
    public static final ResourceLocation LOC_SHIELDED = Robotics.rl("shielded");

    public static final BaseInventory EMPTY_INVENTORY = new BaseInventory(() -> BlockPos.ZERO, 0);
    public static final EnergyStorage NO_ENERGY = new EnergyStorage(0);
    public static final IPartBuilt NO_PARTS = new PartsCapability();
    public static final IPerkMap NO_PERKS = new IPerkMap() {
        @Override public SimpleDataManager values() { return new SimpleDataManager(); }
        @Override public void add(Perk perk, int level) {}
        @Override public void remove(Perk perk, int level) {}
        @Override public void merge(IPerkMap other) {}
        @Override public void diff(IPerkMap toRemove) {}
        @Override public void clear() {}
        @Override public boolean contains(Perk perk) { return false; }
        @Override public int getLevel(Perk perk) { return 0; }
        @NotNull @Override public Iterator<Tuple<Perk, Integer>> iterator() { return Collections.emptyIterator(); }
        @Override public Iterable<Tuple<ResourceLocation, Integer>> baseIterator() { return Collections::emptyIterator; }
    };

    private static final Map<Object, Map<LegacyCapability<?>, Object>> FALLBACK_STATE =
            Collections.synchronizedMap(new java.util.WeakHashMap<>());

    private ModCapabilities() {
    }

    /** Typed replacement for old Entity/Level#getCapability calls. */
    public static <T> Optional<T> get(Object holder, LegacyCapability<T> capability) {
        if (holder == null) return Optional.empty();
        Map<LegacyCapability<?>, Object> values = FALLBACK_STATE.computeIfAbsent(
                holder, ignored -> new IdentityHashMap<>());
        Object value = values.get(capability);
        if (value == null) {
            value = create(holder, capability);
            if (value != null) values.put(capability, value);
        }
        return Optional.ofNullable(capability.type().cast(value));
    }

    public static Optional<IEnergyStorage> energy(Object holder) {
        if (holder instanceof Entity entity) {
            return Optional.ofNullable(Capabilities.EnergyStorage.ENTITY.getCapability(entity, null));
        }
        if (holder instanceof BlockEntity blockEntity && blockEntity.getLevel() != null) {
            return Optional.ofNullable(Capabilities.EnergyStorage.BLOCK.getCapability(
                    blockEntity.getLevel(), blockEntity.getBlockPos(), blockEntity.getBlockState(), blockEntity, null));
        }
        return Optional.empty();
    }

    public static Optional<IItemHandler> items(Object holder) {
        if (holder instanceof Entity entity) {
            return Optional.ofNullable(Capabilities.ItemHandler.ENTITY.getCapability(entity, null));
        }
        if (holder instanceof BlockEntity blockEntity && blockEntity.getLevel() != null) {
            return Optional.ofNullable(Capabilities.ItemHandler.BLOCK.getCapability(
                    blockEntity.getLevel(), blockEntity.getBlockPos(), blockEntity.getBlockState(), blockEntity, null));
        }
        return Optional.empty();
    }

    @SuppressWarnings("unchecked")
    private static <T> T create(Object holder, LegacyCapability<T> capability) {
        if (capability == ENERGY && holder instanceof RobotEntity) {
            return (T) new EnergyStorage(1_000_000);
        }
        if (holder instanceof RobotEntity robot) {
            if (capability == ROBOT) return (T) new RobotCapability(robot);
            if (capability == INVENTORY) return (T) new RobotInventory(robot);
            if (capability == PERKS) return (T) new AdvancedPerkMap();
            if (capability == CHUNK_LOADER) return (T) new ChunkLoadingCapability(robot);
            if (capability == PARTS) return (T) new PartsCapability(robot);
            if (capability == COMMANDS) return (T) new CommandCapability(robot);
            if (capability == SHIELDED) return (T) new ShieldCapability(robot);
        }
        if (capability == CHUNK_TRACKER && holder instanceof ServerLevel level) {
            return (T) new ChunkLoadingHandler.ChunkTracker(level);
        }
        return null;
    }

    public record LegacyCapability<T>(String name) {
        @SuppressWarnings("unchecked")
        public Class<T> type() {
            return (Class<T>) Object.class;
        }
    }
}
