package com.io.mekarobotics.network.messages.server;

import com.io.mekarobotics.common.WorldData;
import com.io.mekarobotics.common.capabilities.ModCapabilities;
import com.io.mekarobotics.common.helpers.EntityFinder;
import com.io.mekarobotics.network.messages.IMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import com.io.mekarobotics.network.NetworkEvent;

import java.util.UUID;

public class PacketReleaseFromCommandGroup implements IMessage {

    protected int commandGroup;
    protected UUID entity;

    public PacketReleaseFromCommandGroup() {

    }

    public PacketReleaseFromCommandGroup(int commandGroup, UUID entity) {
        this.commandGroup = commandGroup;
        this.entity = entity;
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(commandGroup);
        buf.writeUUID(entity);
    }

    @Override
    public void decode(FriendlyByteBuf buf) {
        this.commandGroup = buf.readInt();
        this.entity = buf.readUUID();
    }

    @Override
    public void handle(NetworkEvent.Context cxt) {
        WorldData.get().releaseRobotFromCommandGroup(commandGroup, entity);
        Entity ent = EntityFinder.getEntity(cxt.getSender().level(), entity);
        if(ent != null) {
            ModCapabilities.get(ent, ModCapabilities.ROBOT).ifPresent(robotics -> robotics.setCommandGroup(0));
        }
    }
}
