package com.io.mekarobotics.common.handlers;

import com.io.mekarobotics.Reference;
import com.io.mekarobotics.Robotics;
import com.io.mekarobotics.common.access.AccessConfig;
import com.io.mekarobotics.common.access.EnumPermission;
import com.io.mekarobotics.common.access.WorldAccessData;
import com.io.mekarobotics.common.capabilities.IPartBuilt;
import com.io.mekarobotics.common.capabilities.IRobot;
import com.io.mekarobotics.common.capabilities.ModCapabilities;
import com.io.mekarobotics.common.capabilities.impl.EnergyStorage;
import com.io.mekarobotics.common.capabilities.impl.inventory.RobotInventory;
import com.io.mekarobotics.common.content.entity.RobotEntity;
import com.io.mekarobotics.common.content.entity.ai.AbstractRangedAttackGoal;
import com.io.mekarobotics.common.content.entity.ai.RetrieveGoal;
import com.io.mekarobotics.definitions.ModAttributes;
import com.io.mekarobotics.definitions.ModMenuTypes;
import com.io.mekarobotics.definitions.ModSounds;
import com.io.mekarobotics.integration.config.RoboticsConfig;
import com.io.mekarobotics.network.NetworkHandler;
import com.io.mekarobotics.network.messages.server.PacketSetAccessConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.event.entity.EntityStruckByLightningEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.entity.living.*;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

@EventBusSubscriber(modid = Robotics.MODID, bus = EventBusSubscriber.Bus.GAME)
public class RobotBehavior {

    public static final DeferredHolder<MenuType<?>, MenuType<?>>[] ALL_ROBOT_MENUS = new DeferredHolder[]{
            ModMenuTypes.ROBOT,
            ModMenuTypes.ROBOT_INFO,
            ModMenuTypes.ROBOT_INVENTORY,
            ModMenuTypes.ROBOT_COMMANDS
    };

    @SubscribeEvent
    public static void onRobotSpawn(MobSpawnEvent.PositionCheck event) {
        if(!ModCapabilities.get(event.getEntity(), ModCapabilities.ROBOT).isPresent()) return;
        onRobotCreated(event.getEntity());
    }

    @SubscribeEvent
    public static void onRobotTick(EntityTickEvent.Post event) {
        if(!(event.getEntity() instanceof LivingEntity entity)) return;
        if(entity.level().isClientSide()) return;
        ModCapabilities.get(entity, ModCapabilities.ROBOT).ifPresent(robot -> {
            ModCapabilities.energy(entity).ifPresent(energy -> {
                    if(energy.getEnergyStored() <= 0) {
                        robot.setActivation(false);
                    }


                    if(!robot.isActive()) return;

                    //Consume/Receive Energy through passive sources
                    double consumption = entity.getAttributeValue(ModAttributes.ENERGY_CONSUMPTION);
                    if(consumption > 0) {
                        float configMultiplier = RoboticsConfig.general.robotBaseConsumption.get() / 100f;
                        energy.extractEnergy((int) (consumption * configMultiplier), false);
                    } else {
                        energy.receiveEnergy((int) -consumption, false);
                    }

                    if(robot.isSwelling()) {
                        robot.swell();
                    }
                });
            });
    }

    @SubscribeEvent
    public static void onRobotStruckByLightning(EntityStruckByLightningEvent event) {
        if(!(event.getEntity() instanceof LivingEntity living)) return;
        ModCapabilities.get(event.getEntity(), ModCapabilities.ROBOT).ifPresent(robot -> {
            ModCapabilities.energy(living).ifPresent(energy -> {
                energy.receiveEnergy(2000000, false);
                living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 300, 1));
            });
        });
    }

    /**
     * Damage Armor for entities that are robotic but don't do it themselves
     * NOTE: Code taken from {@link net.minecraft.world.entity.player.Inventory#hurtArmor(DamageSource, float, int[])}
     * @param event The event
     */
    @SubscribeEvent
    public static void afterRobotDamaged(LivingDamageEvent.Pre event) {
        if(!ModCapabilities.get(event.getEntity(), ModCapabilities.ROBOT).isPresent()) return;
        if(event.getSource().is(DamageTypeTags.BYPASSES_ARMOR)) return;
        if(event.getNewDamage() <= 0) return;
        if(event.getEntity() instanceof Player) return; //Players armor already gets damaged by default
        float amount = Math.max(1, event.getNewDamage());
        ModCapabilities.items(event.getEntity()).ifPresent(inventory -> {
            for(int i : new int[] {2, 3, 4, 5}) {
                ItemStack stack = inventory.getStackInSlot(i);
                if ((!event.getSource().is(DamageTypeTags.IS_FIRE) || !stack.has(net.minecraft.core.component.DataComponents.FIRE_RESISTANT)) && stack.getItem() instanceof ArmorItem) {
                    EquipmentSlot equipmentSlot = switch(i) { case 2 -> EquipmentSlot.FEET; case 3 -> EquipmentSlot.LEGS; case 4 -> EquipmentSlot.CHEST; default -> EquipmentSlot.HEAD; };
                    stack.hurtAndBreak((int) amount, event.getEntity(), equipmentSlot);
                }
            }
        });
    }

    @SubscribeEvent
    public static void onRobotDeath(LivingDeathEvent event) {
        if(event.getEntity().level().isClientSide()) return;
        Entity entity = event.getEntity();
        // Drop Inventory
        if(event.getEntity().level().getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
            ModCapabilities.items(entity).ifPresent(inventory -> {
                if(inventory instanceof RobotInventory robotInv) {
                    robotInv.dropItems();
                }
            });
        }
        // Close any open containers
        if(entity instanceof Mob mob && ModCapabilities.get(mob, ModCapabilities.COMMANDS).isPresent()) {
            for(WrappedGoal goal : mob.goalSelector.getAvailableGoals()) {
                if(goal.getGoal() instanceof RetrieveGoal retrieveGoal) {
                    retrieveGoal.closeContainer();
                }
            }
        }
    }

    @SubscribeEvent
    public static void onRobotRightClick(PlayerInteractEvent.EntityInteract event) {
        if(event.getSide().isClient()) return;
        event.setCancellationResult(InteractionResult.SUCCESS);
        event.setCanceled(true);
        Player player = event.getEntity();
        Entity target = event.getTarget();
        ItemStack stack = event.getItemStack();
        InteractionHand hand = event.getHand();
        if(event.getTarget().interact(player, hand).consumesAction()) return;
        if(target instanceof LivingEntity living && stack.getItem().interactLivingEntity(stack, player, living, hand).consumesAction()) return;

        if(target instanceof Mob mob && ModCapabilities.get(mob, ModCapabilities.ROBOT).isPresent()) {
            if(equipIfPossible(mob, player, hand, EquipmentSlot.HEAD, stack) ||
                    equipIfPossible(mob, player, hand, EquipmentSlot.CHEST, stack) ||
                    equipIfPossible(mob, player, hand, EquipmentSlot.LEGS, stack) ||
                    equipIfPossible(mob, player, hand, EquipmentSlot.FEET, stack)) {
                return;
            }
        }
        RoboticsMenus.openRobotMenu(player, ModMenuTypes.ROBOT.get(), target);
    }

    @SubscribeEvent
    public static void determineProjectile(LivingGetProjectileEvent event) {
        LivingEntity living = event.getEntity();
        if(!ModCapabilities.get(living, ModCapabilities.ROBOT).isPresent()) return;
        ItemStack weapon = event.getProjectileWeaponItemStack();
        ItemStack ammunition = AbstractRangedAttackGoal.retrieveAmmunitionFromInventory(living, weapon);
        if(!ammunition.isEmpty() && !event.getProjectileWeaponItemStack().isEmpty()) {
            event.setProjectileItemStack(ammunition);
        }
    }

    private static boolean equipIfPossible(Mob mob, Player player, InteractionHand hand, EquipmentSlot slot, ItemStack stack) {
        if(!stack.getAttributeModifiers().modifiers().isEmpty()) {
            if(!player.isCreative()) {
                player.setItemInHand(hand, mob.getItemBySlot(slot));
            }
            mob.setItemSlot(slot, stack.copy());
            return true;
        }
        return false;
    }

    public static void onRobotCreated(LivingEntity entity) {
        if(entity instanceof Mob mob) {
            mob.setPersistenceRequired();
            mob.setCanPickUpLoot(true);
            mob.setLeftHanded(false);
            for(EquipmentSlot slot : EquipmentSlot.values()) {
                mob.setDropChance(slot, 0); //We do this manually to not randomly damage anything!
            }
        }
        ModCapabilities.get(entity, ModCapabilities.PARTS).ifPresent(IPartBuilt::onCreation);
        ModCapabilities.energy(entity).ifPresent(energy -> {
                if(!(energy instanceof EnergyStorage storage)) return;
                storage.setEnergy(Integer.MAX_VALUE);
            });
        ModCapabilities.get(entity, ModCapabilities.ROBOT).ifPresent(robot -> robot.setActivation(true));
        entity.setHealth(entity.getMaxHealth());
    }

    public static List<MenuType<?>> possibleMenus(Entity entity) {
        List<MenuType<?>> menus = new ArrayList<>();
        menus.add(ModMenuTypes.ROBOT.get());
        menus.add(ModMenuTypes.ROBOT_INFO.get());
        menus.add(ModMenuTypes.ROBOT_COMMANDS.get());
        return menus;
    }

    public static boolean hasAccess(Player player, Entity entity, EnumPermission permission) {
        return hasAccess(player.getUUID(), entity, permission);
    }

    public static boolean hasAccess(UUID player, Entity entity, EnumPermission permission) {
        AtomicBoolean access = new AtomicBoolean(false);
        ModCapabilities.get(entity, ModCapabilities.ROBOT).ifPresent(robot -> access.set(robot.hasAccess(player, permission)));
        return access.get();
    }

    public static void setAccess(WorldAccessData.EnumAccessScope scope, Entity entity, AccessConfig access) {
        if(entity.level().isClientSide()) {
            NetworkHandler.sendToServer(new PacketSetAccessConfig(scope, entity, access));
        } else ModCapabilities.get(entity, ModCapabilities.ROBOT).ifPresent(robot -> robot.setAccess(access));
    }

    public static boolean canReach(LivingEntity entity, BlockPos pos) {
        double reach = 4.5;
        return entity.getEyePosition().distanceToSqr(Vec3.atCenterOf(pos)) < reach * reach;
    }

    public static boolean canReach(LivingEntity entity, GlobalPos pos) {
        if(!entity.level().dimension().equals(pos.dimension())) return false;
        return canReach(entity, pos.pos());
    }

    public static void playAggressionSound(LivingEntity entity) {
        IRobot robot = ModCapabilities.get(entity, ModCapabilities.ROBOT).orElse(null);
        if(robot != null && !robot.isActive()) return;
        if(entity instanceof RobotEntity) {
            entity.playSound(ModSounds.ROBOT_KILL_COMMAND.get());
        } else if(entity instanceof Mob mob) {
            mob.playAmbientSound();
        }
    }

    /**
     * Speed modifier for any entity attempting to break a block
     * NOTE: These calculations are copied from {@link Player#getDigSpeed(BlockState, BlockPos)}
     * @param entity the entity attempting to break a block
     * @param selected the item stack the entity is using
     * @param state the blocks state
     * @return the multiplier to the destroy speed
     */
    public static float destroySpeed(LivingEntity entity, ItemStack selected, BlockState state) {
        float f = selected.getDestroySpeed(state);
        if (f > 1.0F) {
            int i = selected.getEnchantments().getLevel(entity.level().registryAccess()
                    .registryOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT)
                    .getHolderOrThrow(net.minecraft.world.item.enchantment.Enchantments.EFFICIENCY));
            if (i > 0 && !selected.isEmpty()) {
                f += (float)(i * i + 1);
            }
        }

        if (MobEffectUtil.hasDigSpeed(entity)) {
            f += (float)(MobEffectUtil.getDigSpeedAmplification(entity) + 1) * 0.2F;
        }

        if (entity.hasEffect(MobEffects.DIG_SLOWDOWN)) {
            float f1 = switch (entity.getEffect(MobEffects.DIG_SLOWDOWN).getAmplifier()) {
                case 0 -> 0.3F;
                case 1 -> 0.09F;
                case 2 -> 0.0027F;
                default -> 8.1E-4F;
            };

            f *= f1;
        }
        //TODO: Post to event with attached fake player
        //f = net.neoforged.neoforge.event.EventHooks.getBreakSpeed(entity, state, f, pos);
        return f;
    }

    public static int swingSpeed(LivingEntity entity) {
        if (MobEffectUtil.hasDigSpeed(entity)) {
            return 6 - (1 + MobEffectUtil.getDigSpeedAmplification(entity));
        } else {
            return entity.hasEffect(MobEffects.DIG_SLOWDOWN) ? 6 + (1 + entity.getEffect(MobEffects.DIG_SLOWDOWN).getAmplifier()) * 2 : 6;
        }
    }
}
