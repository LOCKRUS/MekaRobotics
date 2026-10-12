package com.io.mekarobotics.network.messages.server;

import com.io.mekarobotics.common.capabilities.ModCapabilities;
import com.io.mekarobotics.common.robot.RobotCommand;
import com.io.mekarobotics.network.messages.IMessage;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import com.io.mekarobotics.network.NetworkEvent;

public class PacketAddCommand implements IMessage {

    protected int entityId;
    protected RobotCommand command;

    public PacketAddCommand() {

    }

    public PacketAddCommand(int entityId, RobotCommand command) {
        this.entityId = entityId;
        this.command = command;
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(entityId);
        CompoundTag nbt = new CompoundTag();
        RobotCommand.writeToNBT(nbt, command); //NOTE: NBT is suboptimal here, but sufficient as this packet is not created as often
        buf.writeNbt(nbt);
    }

    @Override
    public void decode(FriendlyByteBuf buf) {
        entityId = buf.readInt();
        command = RobotCommand.readSingleCommandFromNBT(buf.readNbt());
    }

    @Override
    public void handle(NetworkEvent.Context cxt) {
        Entity entity = cxt.getSender().level().getEntity(entityId);
        if(entity == null) return;
        ModCapabilities.get(entity, ModCapabilities.COMMANDS).ifPresent(robot -> {
            robot.addCommand(command);
        });
    }
}
