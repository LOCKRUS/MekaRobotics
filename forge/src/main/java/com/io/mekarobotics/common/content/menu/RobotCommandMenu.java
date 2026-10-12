package com.io.mekarobotics.common.content.menu;

import com.io.mekarobotics.common.access.AccessConfig;
import com.io.mekarobotics.common.capabilities.ModCapabilities;
import com.io.mekarobotics.common.robot.RobotCommand;
import com.io.mekarobotics.definitions.ModMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class RobotCommandMenu extends AbstractContainerMenu {

    public final LivingEntity robot;
    public final AccessConfig access = new AccessConfig();

    public RobotCommandMenu(int id, Inventory inv, FriendlyByteBuf extraData) {
        this(id, inv.player.level().getEntity(extraData.readInt()));
        ModCapabilities.get(robot, ModCapabilities.COMMANDS).ifPresent(commands -> {
            commands.setCommands(RobotCommand.readFromNBT(extraData.readNbt()));
        });
        access.read(extraData);
    }

    public RobotCommandMenu(int id, Entity entity) {
        super(ModMenuTypes.ROBOT_COMMANDS.get(), id);
        this.robot = (LivingEntity) entity;
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player pPlayer, int pIndex) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(@NotNull Player pPlayer) {
        return !robot.isDeadOrDying();
    }
}
