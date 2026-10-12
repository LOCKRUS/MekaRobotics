package com.io.mekarobotics.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record LegacyPayload(int messageId, byte[] data) implements CustomPacketPayload {
    public static final Type<LegacyPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("mekarobotics", "legacy_message"));
    public static final StreamCodec<ByteBuf, LegacyPayload> STREAM_CODEC = StreamCodec.of(
            (buf, payload) -> {
                buf.writeVarInt(payload.messageId());
                buf.writeVarInt(payload.data().length);
                buf.writeBytes(payload.data());
            },
            buf -> {
                int id = buf.readVarInt();
                byte[] data = new byte[buf.readVarInt()];
                buf.readBytes(data);
                return new LegacyPayload(id, data);
            });

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
