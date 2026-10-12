package com.io.mekarobotics.network;

import com.io.mekarobotics.Robotics;
import com.io.mekarobotics.network.messages.IMessage;
import com.io.mekarobotics.network.messages.client.PacketContainerData;
import com.io.mekarobotics.network.messages.client.PacketGuiData;
import com.io.mekarobotics.network.messages.client.PacketSetEntityEffects;
import com.io.mekarobotics.network.messages.server.*;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.util.ArrayList;
import java.util.List;

public final class NetworkHandler {
    private static final List<Class<? extends IMessage>> MESSAGE_TYPES = new ArrayList<>();
    private static boolean initialized;

    private NetworkHandler() {
    }

    public static void registerMessages() {
        if (initialized) return;
        initialized = true;
        register(PacketContainerData.class);
        register(PacketSetEntityEffects.class);
        register(PacketGuiData.class);
        register(PacketSetWatched.class);
        register(PacketOpenRobotMenu.class);
        register(PacketComponentAction.class);
        register(PacketConstructRobot.class);
        register(PacketSetEntityName.class);
        register(PacketRequestEntitySearch.class);
        register(PacketSetCommands.class);
        register(PacketSetAccessConfig.class);
        register(PacketReleaseFromCommandGroup.class);
        register(PacketAddCommand.class);
    }

    private static void register(Class<? extends IMessage> type) {
        MESSAGE_TYPES.add(type);
    }

    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        registerMessages();
        PayloadRegistrar registrar = event.registrar(Robotics.MODID).versioned("1.0");
        registrar.playBidirectional(LegacyPayload.TYPE, LegacyPayload.STREAM_CODEC,
                NetworkHandler::decodeAndHandle);
    }

    private static void decodeAndHandle(LegacyPayload payload, net.neoforged.neoforge.network.handling.IPayloadContext payloadContext) {
        if (payload.messageId() < 0 || payload.messageId() >= MESSAGE_TYPES.size()) return;
        try {
            IMessage message = MESSAGE_TYPES.get(payload.messageId()).getConstructor().newInstance();
            message.decode(new FriendlyByteBuf(Unpooled.wrappedBuffer(payload.data())));
            NetworkEvent.Context context = new NetworkEvent.Context(payloadContext);
            context.enqueueWork(() -> message.handle(context));
        } catch (ReflectiveOperationException exception) {
            Robotics.LOGGER.error("Unable to decode MekaRobotics payload {}", payload.messageId(), exception);
        }
    }

    public static <MSG extends IMessage> void sendToServer(MSG message) {
        PacketDistributor.sendToServer(toPayload(message));
    }

    public static <MSG extends IMessage> void sendToPlayer(MSG message, ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, toPayload(message));
    }

    public static <MSG extends IMessage> void sendToAllPlayersWithChunk(MSG message, LevelChunk chunk) {
        if (chunk.getLevel() instanceof ServerLevel level) {
            PacketDistributor.sendToPlayersTrackingChunk(level, chunk.getPos(), toPayload(message));
        }
    }

    public static <MSG extends IMessage> void sendToAllPlayers(MSG message) {
        PacketDistributor.sendToAllPlayers(toPayload(message));
    }

    private static LegacyPayload toPayload(IMessage message) {
        registerMessages();
        int id = MESSAGE_TYPES.indexOf(message.getClass());
        if (id < 0) throw new IllegalArgumentException("Unregistered MekaRobotics message: " + message.getClass());
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        message.encode(buffer);
        byte[] data = new byte[buffer.readableBytes()];
        buffer.getBytes(buffer.readerIndex(), data);
        return new LegacyPayload(id, data);
    }
}
