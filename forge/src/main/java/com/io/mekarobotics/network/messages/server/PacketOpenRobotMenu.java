package com.io.mekarobotics.network.messages.server;

import com.io.mekarobotics.common.handlers.RoboticsMenus;
import com.io.mekarobotics.network.messages.BufferSerializers;
import com.io.mekarobotics.network.messages.IMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.inventory.MenuType;
import com.io.mekarobotics.network.NetworkEvent;
import net.minecraft.core.registries.Registries;

public class PacketOpenRobotMenu implements IMessage {

    private MenuType<?> type;
    private Object extraData;

    public PacketOpenRobotMenu() {}

    public PacketOpenRobotMenu(MenuType<?> type, Object extraData) {
        this.type = type;
        this.extraData = extraData;
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeRegistryId(Registries.MENU, type);
        BufferSerializers.writeObject(buf, extraData);
    }

    @Override
    public void decode(FriendlyByteBuf buf) {
        type = buf.readRegistryId();
        extraData = BufferSerializers.readObject(buf);
    }

    @Override
    public void handle(NetworkEvent.Context cxt) {
        RoboticsMenus.openMenu(cxt.getSender(), type, extraData);
    }

}
