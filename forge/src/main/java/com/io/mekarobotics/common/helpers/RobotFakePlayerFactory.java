package com.io.mekarobotics.common.helpers;

import com.google.common.collect.Maps;
import com.io.mekarobotics.Robotics;
import com.io.mekarobotics.common.helpers.types.RobotFakePlayer;
import com.mojang.authlib.GameProfile;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.common.EventBusSubscriber;

import java.util.Map;

@EventBusSubscriber(modid = Robotics.MODID, bus = EventBusSubscriber.Bus.GAME)
public class RobotFakePlayerFactory {

    private static final Map<GameProfile, FakePlayer> fakePlayers = Maps.newHashMap();

    public static FakePlayer get(Mob mob, GameProfile username) {
        if(!(mob.level() instanceof ServerLevel)) return null;
        if (!fakePlayers.containsKey(username)) {
            FakePlayer fakePlayer = new RobotFakePlayer(mob, username);
            fakePlayers.put(username, fakePlayer);
        }

        return fakePlayers.get(username);
    }

    @SubscribeEvent
    public static void unloadLevel(LevelEvent.Unload event) {
        fakePlayers.entrySet().removeIf(entry -> entry.getValue().level() == event.getLevel());
    }
}
