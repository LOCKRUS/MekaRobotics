package com.io.mekarobotics.network.messages;

import com.io.mekarobotics.Robotics;
import com.mojang.authlib.GameProfile;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.SkullBlockEntity;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.UUID;

public class EntityByteBufUtil implements IBufferSerializable {
	
	public static final String PLAYER = "EntityPlayer";
	
	private CompoundTag nbt;
	private int entityId;  //While it is usually not required, temporary entityIds are used to communicate robots to guis, so it has to be saved & transported here
	
	public EntityByteBufUtil(Entity entity) {
		entityId = entity.getId();
		if(entity instanceof Player player) {
            CompoundTag nbtProfile = new CompoundTag();
            GameProfile profile = player.getGameProfile();
            if(profile.getId() != null) nbtProfile.putUUID("id", profile.getId());
            nbtProfile.putString("name", profile.getName());

            nbt = player.serializeNBT(player.level().registryAccess());
			nbt.putString("id", PLAYER);
			nbt.put("profile", nbtProfile);
		} else {
            nbt = entity.serializeNBT(entity.level().registryAccess());
		}
	}
	
	public EntityByteBufUtil(FriendlyByteBuf buf) {
		read(buf);
	}

	@Override
	public void write(FriendlyByteBuf buf) {
		buf.writeInt(entityId);
		buf.writeNbt(nbt);
	}
	
	@Override
	public void read(FriendlyByteBuf buf) {
		entityId = buf.readInt();
		nbt = buf.readNbt();
	}

	@Nullable
	public LivingEntity constructEntity(Level level) {
		if(nbt == null) return null;
		
		String entity_type = nbt.getString("id");
		Optional<EntityType<?>> entityType = EntityType.byString(entity_type);
		
		//Handle Entities
		if(entityType.isPresent()) {
			try {
				Entity ent = entityType.get().create(level);
				if(!(ent instanceof LivingEntity)) return null;
                    ent.deserializeNBT(level.registryAccess(), nbt);
				ent.setId(entityId);
				return (LivingEntity) ent;
			} catch (IllegalArgumentException e) {
				e.printStackTrace(); //Invocation seems to have failed for whatever reason
			}
		} else
		//Handle Players
		if(entity_type.equals(PLAYER)) {
                CompoundTag profileTag = nbt.getCompound("profile");
                UUID profileId = profileTag.hasUUID("id") ? profileTag.getUUID("id") : UUID.nameUUIDFromBytes(("robot:" + entityId).getBytes(java.nio.charset.StandardCharsets.UTF_8));
                GameProfile profile = new GameProfile(profileId, profileTag.getString("name"));
                Player other = Robotics.proxy.createFakePlayer(level, profile).get();
                other.deserializeNBT(level.registryAccess(), nbt);
			other.setId(entityId);
			return other;
		}
		return null;
	}

	public static void writeEntity(Entity entity, FriendlyByteBuf buf) {
		new EntityByteBufUtil(entity).write(buf);
	}

	/**
	 * Assumes overworld as dimension
	 * @param buf byte buffer
	 * @return a constructed entity on the overworld. (does not spawn the entity)
	 */
	@Nullable
	public static LivingEntity readEntity(FriendlyByteBuf buf) {
		return new EntityByteBufUtil(buf).constructEntity(Robotics.proxy.getLevel());
	}

	public static void writeEntities(Collection<LivingEntity> entities, FriendlyByteBuf buf) {
		buf.writeShort(entities.size() & 65535); //Write an unsigned short
		for(LivingEntity living : entities) {
			writeEntity(living, buf);
		}
	}

	public static Collection<LivingEntity> readEntities(FriendlyByteBuf buf) {
		int size = buf.readShort() & 65535;
		Collection<LivingEntity> entities = new HashSet<>(size);
		for(int i = 0; i < size; i++) {
			LivingEntity living = readEntity(buf);
			if(living == null) continue;
			entities.add(living);
		}
		return entities;
	}

}
