package com.io.mekarobotics.network.messages.server;

import com.io.mekarobotics.client.screen.base.IElement;
import com.io.mekarobotics.common.helpers.types.EntitySearch;
import com.io.mekarobotics.network.NetworkHandler;
import com.io.mekarobotics.network.messages.IMessage;
import com.io.mekarobotics.network.messages.client.PacketGuiData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import com.io.mekarobotics.network.NetworkEvent;

public class PacketRequestEntitySearch implements IMessage {

	EntitySearch search;
	int[] parentGuiPath;
	
	public PacketRequestEntitySearch() {}

	@OnlyIn(Dist.CLIENT)
	public PacketRequestEntitySearch(IElement[] parentGuiPath, EntitySearch search) {
		this.parentGuiPath = new int[parentGuiPath.length];
		for(int i = 0; i < parentGuiPath.length; i++) {
			this.parentGuiPath[i] = parentGuiPath[i].hashCode();
		}
		this.search = search;
	}

	@Override
	public void encode(FriendlyByteBuf buf) {
		buf.writeByte(parentGuiPath.length);
		for(int j : parentGuiPath) {
			buf.writeInt(j);
		}
		search.write(buf);
	}

	@Override
	public void decode(FriendlyByteBuf buf) {
		int size = buf.readByte();
		parentGuiPath = new int[size];
		for(int i = 0; i < size; i++) {
			parentGuiPath[i] = buf.readInt();
		}
		search = EntitySearch.from(buf);
	}

	@Override
	public void handle(NetworkEvent.Context cxt) {
		ServerPlayer player = cxt.getSender();
		if(player == null) return;
		if(!(player.level() instanceof ServerLevel server)) return;
		Entity result = search.commence(server, player.position());
		// Let the client know, even if the search did not find anything
		NetworkHandler.sendToPlayer(new PacketGuiData(parentGuiPath, result), player);
	}
}
