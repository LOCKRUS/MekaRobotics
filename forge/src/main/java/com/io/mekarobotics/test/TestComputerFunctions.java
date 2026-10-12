package com.io.mekarobotics.test;

import com.io.mekarobotics.Robotics;
import net.minecraft.gametest.framework.GameTest;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(Robotics.MODID)
public class TestComputerFunctions {

    @PrefixGameTestTemplate(false)
    @GameTest(template = "duel", batch = "commandAPI")
    public static void testAddCommand() {

    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "duel", batch = "commandAPI")
    public static void testRemoveCommand() {
        //Test edge cases (beginning, end & middle of list)
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "default", batch = "commandAPI")
    public static void testGetCommands() {
        //Test edge cases (beginning, end & middle of list)
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "default", batch = "sensorAPI")
    public static void testBrightness() {
        //Once in total darkness, once in medium light, once in sunlight; use sky & blocks
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "default", batch = "sensorAPI")
    public static void testSeenEntities() {

    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "default", batch = "robotAPI")
    public static void testDeactivate() {

    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "default", batch = "robotAPI")
    public static void testHasAccess() {
        // Change around access rights randomly?, for specific player as well as general
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "default", batch = "robotAPI")
    public static void testGetEnergy() {
        // Test Edge Cases
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "default", batch = "robotAPI")
    public static void testGetMaxEnergy() {

    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "default", batch = "robotAPI")
    public static void testGetHealth() {

    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "default", batch = "robotAPI")
    public static void testGetEffects() {

    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "default", batch = "robotAPI")
    public static void testGetAttribute() {

    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "default", batch = "robotAPI")
    public static void testActivate() {

    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "default", batch = "robotAPI")
    public static void testSetOptions() {

    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "default", batch = "robotAPI")
    public static void testGetOptions() {

    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "default", batch = "robotAPI")
    public static void testGetOwner() {

    }

    public static void testCobbleStoneFarm() {

    }
}
