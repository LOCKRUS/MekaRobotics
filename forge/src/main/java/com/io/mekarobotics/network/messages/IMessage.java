package com.io.mekarobotics.network.messages;

import net.minecraft.network.FriendlyByteBuf;
import com.io.mekarobotics.network.NetworkEvent;

public interface IMessage {

    void encode(FriendlyByteBuf buf);

    void decode(FriendlyByteBuf buf);

    void handle(NetworkEvent.Context cxt);
}
