package com.io.mekarobotics.definitions;

import com.io.mekarobotics.Reference;
import com.io.mekarobotics.Robotics;
import com.io.mekarobotics.common.capabilities.ModCapabilities;
import com.io.mekarobotics.common.capabilities.ModifiableEnergyStorage;
import com.io.mekarobotics.common.helpers.util.InventoryUtil;
import com.io.mekarobotics.common.robot.EnumModuleSlot;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;
import net.neoforged.neoforge.event.entity.living.LivingEquipmentChangeEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

@EventBusSubscriber(modid = Robotics.MODID, bus = EventBusSubscriber.Bus.MOD)
public final class ModAttributes {
    public static final DeferredRegister<Attribute> ATTRIBUTES = DeferredRegister.create(net.minecraft.core.registries.Registries.ATTRIBUTE, Robotics.MODID);

    public static final DeferredHolder<Attribute, Attribute> ENERGY_CAPACITY = register("robot.energy_capacity", 1000000, 0, Double.MAX_VALUE, true);
    public static final DeferredHolder<Attribute, Attribute> ENERGY_CONSUMPTION = register("robot.energy_consumption", 30, -Double.MAX_VALUE, Double.MAX_VALUE, false);
    public static final Map<EnumModuleSlot, DeferredHolder<Attribute, Attribute>> MODIFIER_SLOTS = new HashMap<>(EnumModuleSlot.values().length);
    public static final DeferredHolder<Attribute, Attribute> STACK_SIZE = register("robot.stack_size", 1, 0, 6, true);
    public static final DeferredHolder<Attribute, Attribute> LOGISTICS_TIME = register("robot.logistics_time", 1, 0.2, 10, false);
    public static final DeferredHolder<Attribute, Attribute> MODULE_COST = register("robot.module_cost", 1, 0, 10, false);
    public static final DeferredHolder<Attribute, Attribute> MODULE_COOLDOWN = register("robot.module_cooldown", 1, 0.2, 10, false);
    public static final DeferredHolder<Attribute, Attribute> MODULE_DURATION = register("robot.module_duration", 1, 0, 10, false);
    public static final DeferredHolder<Attribute, Attribute> INVENTORY_SLOTS = register("robot.inventory_slots", 12, 0, Reference.MAX_INVENTORY_SIZE, true);

    static {
        for (EnumModuleSlot slotType : EnumModuleSlot.values()) {
            MODIFIER_SLOTS.put(slotType, register("robot.slots.modules." + slotType.name().toLowerCase(), slotType.isPrimary() ? 1 : 0, 0, Reference.MAX_MODULES, true));
        }
    }

    private static DeferredHolder<Attribute, Attribute> register(String name, double defaultValue, double min, double max, boolean syncable) {
        return ATTRIBUTES.register(name, () -> new RangedAttribute("attribute.name." + name, defaultValue, min, max).setSyncable(syncable));
    }

    @SubscribeEvent
    public static void robotAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntityTypes.ROBOT.get(), createRobotAttributes());
    }

    @SubscribeEvent
    public static void registerAttributes(EntityAttributeModificationEvent event) {
        event.add(ModEntityTypes.ROBOT.get(), ENERGY_CAPACITY);
        event.add(ModEntityTypes.ROBOT.get(), ENERGY_CONSUMPTION);
        event.add(ModEntityTypes.ROBOT.get(), STACK_SIZE);
        event.add(ModEntityTypes.ROBOT.get(), LOGISTICS_TIME);
        event.add(ModEntityTypes.ROBOT.get(), MODULE_COST);
        event.add(ModEntityTypes.ROBOT.get(), MODULE_COOLDOWN);
        event.add(ModEntityTypes.ROBOT.get(), MODULE_DURATION);
        for (Holder<Attribute> attribute : MODIFIER_SLOTS.values()) event.add(ModEntityTypes.ROBOT.get(), attribute);
    }

    public static AttributeSupplier createRobotAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 4.0D).add(Attributes.ATTACK_DAMAGE, 0.0f)
                .add(Attributes.ATTACK_SPEED, 1.0f).add(Attributes.MOVEMENT_SPEED, 0.05f).build();
    }

    @EventBusSubscriber(modid = Robotics.MODID, bus = EventBusSubscriber.Bus.GAME)
    static class ModAttributeChanges {
        @SubscribeEvent
        public static void onEquipmentChanged(LivingEquipmentChangeEvent event) {
            event.getTo().getAttributeModifiers().forEach(event.getSlot(), (attribute, modifier) -> {
                onAttributeChanged(event.getEntity(), event.getEntity().getAttribute(attribute));
            });
        }
    }

    public static void onAttributeChanged(LivingEntity entity, @Nullable AttributeInstance instance) {
        if (instance == null) return;
        if (instance.getAttribute().equals(ENERGY_CAPACITY)) {
            ModCapabilities.energy(entity).ifPresent(storage -> {
                if (storage instanceof ModifiableEnergyStorage energy) energy.setMaxEnergyStored((int) instance.getValue());
            });
        }
        if (entity.level().isClientSide()) return;
        if (instance.getAttribute().equals(STACK_SIZE)) {
            ModCapabilities.items(entity).ifPresent(inventory -> {
                for (int i = 0; i < inventory.getSlots(); i++) {
                    ItemStack stack = inventory.getStackInSlot(i);
                    if (stack.getCount() <= inventory.getSlotLimit(i)) continue;
                    InventoryUtil.dropItem(entity, inventory.extractItem(i, stack.getCount() - inventory.getSlotLimit(i), false));
                }
            });
        }
    }
}
