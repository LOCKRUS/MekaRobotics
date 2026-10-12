package com.io.mekarobotics.network.messages.client;

import com.io.mekarobotics.Robotics;
import com.io.mekarobotics.common.content.menu.BaseMenu;
import com.io.mekarobotics.network.container.PropertyData;
import com.io.mekarobotics.network.messages.IMessage;
import net.minecraft.network.FriendlyByteBuf;
import com.io.mekarobotics.network.NetworkEvent;

import java.util.List;

public class PacketContainerData implements IMessage {

    private short windowId;
    private List<PropertyData> data;

    public PacketContainerData() {}

    public PacketContainerData(short windowId, List<PropertyData> data) {
        this.windowId = windowId;
        this.data = data;
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeShort(windowId);
        buf.writeCollection(data, PropertyData::toBuffer);
    }

    @Override
    public void decode(FriendlyByteBuf buf) {
        windowId = buf.readShort();
        data = buf.readList(PropertyData::fromBuffer);
    }

    @Override
    public void handle(NetworkEvent.Context cxt) {
        if(!(Robotics.proxy.getPlayer().containerMenu instanceof BaseMenu menu)) return;
        if(menu.containerId != windowId) return;
        data.forEach(data -> data.handleWindowProperty(menu));
    }
}
