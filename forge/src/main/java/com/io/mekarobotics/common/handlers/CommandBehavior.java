package com.io.mekarobotics.common.handlers;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.io.mekarobotics.Robotics;
import com.io.mekarobotics.common.capabilities.ModCapabilities;
import com.io.mekarobotics.common.content.entity.ai.FollowGoal;
import com.io.mekarobotics.common.helpers.types.EntitySearch;
import com.io.mekarobotics.common.helpers.types.Selection;
import com.io.mekarobotics.common.robot.RobotCommand;
import com.io.mekarobotics.definitions.robotics.ModSelectionTypes;
import net.minecraft.BlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.portal.PortalShape;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.entity.EntityTravelToDimensionEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.common.EventBusSubscriber;

import javax.annotation.Nullable;

@EventBusSubscriber(modid = Robotics.MODID, bus = EventBusSubscriber.Bus.GAME)
public class CommandBehavior {

    /**
     * List of Entity Searches (Predicates), usually from {@link RobotCommand RobotCommands}
     * Because commands might refer to entities that do not exist (yet), we need to keep a list of these references/searches.
     * In case a new entity gets added/an entity gets changed to match the search, we notify the associated commands.
     * This covers cases like a robot guarding a portal.
     * <br>
     * NOTE: A EntitySearch is added to the level of the robot which tries to conduct this search.
     * This does not need to mean that the search only searches this level, it might search across all dimensions.
     */
    public static final Multimap<Level, EntitySearch> SEARCHES = HashMultimap.create();

    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if(event.getLevel().isClientSide) return;
        if(event.getEntity() instanceof ItemEntity) return; // Save some runtime here by ignoring items
        Entity entity = event.getEntity();
        for(EntitySearch search : SEARCHES.values()) {
            search.testAndNotify(entity);
        }
    }

    @SubscribeEvent
    public static void onEntityLeaveLevel(EntityLeaveLevelEvent event) {
        if(event.getLevel().isClientSide) return;
        // The search is not relevant to the level anymore -> Remove it
        ModCapabilities.get(event.getEntity(), ModCapabilities.COMMANDS).ifPresent(commands -> {
            for(RobotCommand command : commands.getCommands()) {
                for(Selection<?> selection : command.getSelectors()) {
                    if(selection.getType().equals(ModSelectionTypes.ENTITY_PREDICATE)) {
                        EntitySearch search = (EntitySearch) selection.get();
                        SEARCHES.remove(event.getLevel(), search);
                    }
                }
            }
        });
    }

    @SubscribeEvent
    public static void onLevelUnload(LevelEvent.Unload event) {
        if(event.getLevel().isClientSide()) return;
        SEARCHES.removeAll(event.getLevel());
    }

    @SubscribeEvent
    public static void onEntitySwitchedDimension(EntityTravelToDimensionEvent event) {
        Entity entity = event.getEntity();
        if(entity.level().isClientSide) return;
        if(entity instanceof ItemEntity) return; // Save some runtime here by ignoring items
        ServerLevel level = (ServerLevel) entity.level();
        ServerLevel otherDimension = level.getServer().getLevel(event.getDimension());
        if(otherDimension == null) return;
        AABB area = AABB.ofSize(entity.position(), 16, 16, 16);

        // Make followers follow through dimensions
        for(Entity otherEntity : level.getEntities(entity, area, (ent) -> true)) {
            if(!(otherEntity instanceof Mob mob)) continue;
            if(!mob.canChangeDimensions(level, otherDimension)) continue;
            if(mob.goalSelector.getAvailableGoals().stream().anyMatch(wrappedGoal -> wrappedGoal.getGoal() instanceof FollowGoal followGoal && entity.equals(followGoal.following()))) {
                followThroughDimension(mob, entity, otherDimension);
            }
        }
    }

    private static void followThroughDimension(Entity toTransport, Entity leader, ServerLevel dimension) {
        // DimensionTransition is ported separately; do not use the removed Forge ITeleporter API here.
    }
    //NOTE: Largely copied from Entity#findDimensionEntryPoint. Keep up to date with vanilla!

    /**
     * Calculate where to place an entity changing dimensions
     * @param entity the entity changing dimensions. It needs to be currently inside a portal block
     * @param from dimension the entity is currently in
     * @param to dimension the entity is travelling to
     * @return the position the entity should be placed
     */
    @Nullable
    protected static Vec3 findDimensionEntryPoint(Entity entity, Level from, ServerLevel to) {
        if (to.dimension() == Level.END) {
            return Vec3.atBottomCenterOf(ServerLevel.END_SPAWN_POINT);
        }
        if(from.dimension() == Level.END && to.dimension() == Level.OVERWORLD) {
            BlockPos blockpos = to.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, to.getSharedSpawnPos());
            return Vec3.atBottomCenterOf(blockpos);
        }
        if(from.dimension() != Level.NETHER && to.dimension() != Level.NETHER) {
            return null;
        }

        WorldBorder worldborder = to.getWorldBorder();
        double d0 = DimensionType.getTeleportationScale(from.dimensionType(), to.dimensionType());
        BlockPos portalEntrancePos = entity.blockPosition();
        BlockPos dimensionEntrancePos = worldborder.clampToBounds(entity.getX() * d0, entity.getY(), entity.getZ() * d0);
        return to.getPortalForcer().findClosestPortalPosition(dimensionEntrancePos, to.dimension() == Level.NETHER, worldborder)
                .map(Vec3::atBottomCenterOf).orElse(null);
    }

}
