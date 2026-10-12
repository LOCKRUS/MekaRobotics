package com.io.mekarobotics.network;

import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.minecraft.server.level.ServerPlayer;

/** Compatibility context used while legacy message classes are migrated to payloads. */
public final class NetworkEvent {
    private NetworkEvent() {
    }

    public static final class Context {
        private final IPayloadContext payloadContext;

        public Context(IPayloadContext payloadContext) {
            this.payloadContext = payloadContext;
        }

        public ServerPlayer getSender() {
            return payloadContext.player() instanceof ServerPlayer player ? player : null;
        }

        public void enqueueWork(Runnable work) {
            payloadContext.enqueueWork(work);
        }

        public void setPacketHandled(boolean handled) {
            // NeoForge payload handlers are considered handled when the callback returns.
        }
    }
}
