package com.io.mekarobotics.network.messages.server;

import com.io.mekarobotics.common.handlers.RoboticsMenus;
import com.io.mekarobotics.network.messages.BufferSerializers;
import com.io.mekarobotics.network.messages.IMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.inventory.MenuType;
import com.io.mekarobotics.network.NetworkEvent;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.BuiltInRegistries;

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
        buf.writeResourceLocation(BuiltInRegistries.MENU.getKey(type));
        BufferSerializers.writeObject(buf, extraData);
    }

    @Override
    public void decode(FriendlyByteBuf buf) {
        type = BuiltInRegistries.MENU.get(buf.readResourceLocation());
        extraData = BufferSerializers.readObject(buf);
    }

    @Override
    public void handle(NetworkEvent.Context cxt) {
        RoboticsMenus.openMenu(cxt.getSender(), type, extraData);
    }

}
