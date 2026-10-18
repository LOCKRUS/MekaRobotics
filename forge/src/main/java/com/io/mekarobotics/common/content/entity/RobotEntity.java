package com.io.mekarobotics.common.content.entity;

import com.io.mekarobotics.common.capabilities.IPartBuilt;
import com.io.mekarobotics.common.capabilities.ModCapabilities;
import com.io.mekarobotics.common.capabilities.impl.CommandCapability;
import com.io.mekarobotics.common.content.entity.ai.RangedBowAttack;
import com.io.mekarobotics.common.content.entity.ai.RangedCrossbowAttack;
import com.io.mekarobotics.common.content.entity.ai.RangedGenericAttack;
import com.io.mekarobotics.common.content.entity.ai.ReachAcrossDimensionGoal;
import com.io.mekarobotics.common.handlers.RobotBehavior;
import com.io.mekarobotics.common.helpers.util.InventoryUtil;
import com.io.mekarobotics.common.robot.EnumModuleSlot;
import com.io.mekarobotics.common.robot.EnumRobotMaterial;
import com.io.mekarobotics.common.robot.RobotModule;
import com.io.mekarobotics.common.robot.RobotPart;
import com.io.mekarobotics.definitions.ModEntityTypes;
import com.io.mekarobotics.definitions.ModItems;
import com.io.mekarobotics.definitions.ModSounds;
import com.io.mekarobotics.definitions.robotics.ModModules;
import com.io.mekarobotics.integration.config.RoboticsConfig;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.NonNullList;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.control.BodyRotationControl;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.*;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Optional;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class RobotEntity extends PathfinderMob implements GeoEntity {

    public static final EntityDataAccessor<Integer> COLOR = SynchedEntityData.defineId(RobotEntity.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<Integer>[] BODY_PARTS = new EntityDataAccessor[] {
            SynchedEntityData.defineId(RobotEntity.class, EntityDataSerializers.INT),
            SynchedEntityData.defineId(RobotEntity.class, EntityDataSerializers.INT),
            SynchedEntityData.defineId(RobotEntity.class, EntityDataSerializers.INT),
            SynchedEntityData.defineId(RobotEntity.class, EntityDataSerializers.INT),
            SynchedEntityData.defineId(RobotEntity.class, EntityDataSerializers.INT),
            SynchedEntityData.defineId(RobotEntity.class, EntityDataSerializers.INT)
    };
    public static final EntityDataAccessor<Integer> RENDER_OVERLAYS = SynchedEntityData.defineId(RobotEntity.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<Boolean> ACTIVATED = SynchedEntityData.defineId(RobotEntity.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Boolean> MUTED = SynchedEntityData.defineId(RobotEntity.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Integer> LOAD_CHUNK = SynchedEntityData.defineId(RobotEntity.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<Integer> PICKUP_STATE = SynchedEntityData.defineId(RobotEntity.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<Integer> COMMAND_GROUP = SynchedEntityData.defineId(RobotEntity.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<Integer> SWELLING = SynchedEntityData.defineId(RobotEntity.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<Float> SHIELD_HEALTH = SynchedEntityData.defineId(RobotEntity.class, EntityDataSerializers.FLOAT);

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(COLOR, 0);
        for (EntityDataAccessor<Integer> bodyPart : BODY_PARTS) {
            builder.define(bodyPart, EnumRobotMaterial.NONE.getID());
        }
        builder.define(RENDER_OVERLAYS, 0);
        builder.define(ACTIVATED, false);
        builder.define(MUTED, false);
        builder.define(LOAD_CHUNK, 0);
        builder.define(PICKUP_STATE, 0);
        builder.define(COMMAND_GROUP, 0);
        builder.define(SWELLING, 0);
        builder.define(SHIELD_HEALTH, 0.0F);
    }

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private int swingTime = 0;

    public RobotEntity(Level level) {
        this(level, RoboticsConfig.general.startColor.get());
    }

    public RobotEntity(Level pLevel, DyeColor color) {
        super(ModEntityTypes.ROBOT.get(), pLevel);
        ModCapabilities.get(this, ModCapabilities.PARTS).ifPresent(part -> part.setColor(color));
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(4, new RangedGenericAttack(this, 2, 8, 2));
        this.goalSelector.addGoal(4, new RangedBowAttack(this, 2, 8, 4));
        this.goalSelector.addGoal(4, new RangedCrossbowAttack(this, 2, 8, 1));
        this.goalSelector.addGoal(5, new MeleeAttackGoal(this, 1, true));
        this.goalSelector.addGoal(6, new ReachAcrossDimensionGoal(this, 64));
        this.goalSelector.addGoal(CommandCapability.MAX_COMMANDS + CommandCapability.MAX_NON_COMMAND_GOALS + 2, new LookAtPlayerGoal(this, Player.class, 6));
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if(!ModCapabilities.get(this, ModCapabilities.PARTS).isPresent()) return InteractionResult.PASS;
        IPartBuilt parts = ModCapabilities.get(this, ModCapabilities.PARTS).get();
        EnumRobotMaterial repairMaterial = parts.materialForSlot(EnumModuleSlot.BODY);
        DeferredHolder<Item, Item> repairItem = ModItems.PLATES.get(repairMaterial);
        ItemStack stack = player.getItemInHand(hand);
        if(repairItem != null && stack.getItem().equals(repairItem.get()) && getHealth() < getMaxHealth()) {
            if(!player.isCreative()) stack.setCount(stack.getCount() - 1);
            setHealth(getHealth() + 5);
            playSound(SoundEvents.ANVIL_USE);
            return InteractionResult.sidedSuccess(level().isClientSide);
        }
        RobotPart part = RobotPart.getFromItem(stack.getItem());
        if(part != null && (!parts.hasBodyPart(part.getPart().toModuleSlot()) || player.isCreative())) {
            if(!player.isCreative()) stack.setCount(stack.getCount() - 1);
            if(!level().isClientSide()) parts.setBodyPart(part);
            playSound(SoundEvents.ANVIL_USE);
            return InteractionResult.sidedSuccess(level().isClientSide);
        }
        if(ModModules.isModule(stack) && player.isCreative()) {
            RobotModule module = ModModules.get(stack);
            for(EnumModuleSlot slot : module.getViableSlots()) {
                if(!level().isClientSide()) {
                    NonNullList<ItemStack> items = parts.getBodyParts(slot);
                    NonNullList<ItemStack> copy = NonNullList.withSize(items.size(), ItemStack.EMPTY);
                    int i = 0;
                    if(items.isEmpty()) return InteractionResult.PASS;
                    while(i < items.size() && !items.get(i).isEmpty()) {
                        copy.set(i++, items.get(i));
                    }
                    copy.set(i, stack);
                    parts.setBodyParts(slot, copy);
                }
                playSound(SoundEvents.ANVIL_USE);
                break;
            }
            return InteractionResult.sidedSuccess(level().isClientSide);
        }
        if(stack.getItem() instanceof DyeItem dye && player.isCreative()) {
            parts.setColor(dye.getDyeColor());
            return InteractionResult.sidedSuccess(level().isClientSide);
        }

        return InteractionResult.PASS;
    }

    @Override
    public boolean canBeAffected(MobEffectInstance instance) {
        if(instance.getEffect().equals(MobEffects.POISON) && RoboticsConfig.general.poisonImmunity.get()) return false;
        return super.canBeAffected(instance);
    }

    @Override
    public void heal(float p_21116_) {
        //Prevent any health regeneration
    }

    @Override
    public int getAirSupply() {
        return super.getMaxAirSupply();
    }

    @Override
    public ItemStack equipItemIfPossible(ItemStack stack) {
        //TODO Move this to a mixin/event
        ItemStack pickedUpStack = super.equipItemIfPossible(stack);
        if(!pickedUpStack.isEmpty()) return pickedUpStack;
        if(ModCapabilities.items(this).isPresent()) {
            Optional<IItemHandler> inventory = ModCapabilities.items(this);
            if(inventory.isPresent()) {
                stack.setCount(stack.getCount() - InventoryUtil.insert(inventory.get(), stack, false).getCount());
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    /////////////////
    // Animations
    /////////////////


    @Override
    public void swing(InteractionHand p_21012_, boolean p_21013_) {
        swingTime = RobotBehavior.swingSpeed(this) * 3;
        super.swing(p_21012_, p_21013_);
    }

    private <T extends GeoAnimatable> PlayState getAnimation(AnimationState<T> animationState) {
        // TODO Use native walkanimation attributes in living entity
        if(animationState.isMoving()) {
            animationState.getController().setAnimation(RawAnimation.begin().then("animation.robot.walk", Animation.LoopType.LOOP));
            return PlayState.CONTINUE;
        }
        if(swingTime-- > 0) {
            Animation.LoopType animationType = swingTime > RobotBehavior.swingSpeed(this) * 3 / 2 ? Animation.LoopType.LOOP : Animation.LoopType.PLAY_ONCE;
            switch(swingingArm) {
                case MAIN_HAND -> animationState.getController().setAnimation(RawAnimation.begin().then("animation.robot.interactRight", animationType));
                case OFF_HAND -> animationState.getController().setAnimation(RawAnimation.begin().then("animation.robot.interactLeft", animationType));
            }
            return PlayState.CONTINUE;
        }
        animationState.getController().setAnimation(RawAnimation.begin().then("animation.robot.idle", Animation.LoopType.LOOP));
        return PlayState.CONTINUE;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 0, this::getAnimation));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Override
    protected @NotNull BodyRotationControl createBodyControl() {
        return new RobotBodyControl(this);
    }

    ////////////////
    // Sounds
    ////////////////

    @Nullable
    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.ROBOT_DEATH.get();
    }

    @Nullable
    @Override
    protected SoundEvent getHurtSound(DamageSource pDamageSource) {
        return ModSounds.ROBOT_HURT.get();
    }
}
