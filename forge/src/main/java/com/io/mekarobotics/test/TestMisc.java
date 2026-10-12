package com.io.mekarobotics.test;

import com.io.mekarobotics.Reference;
import com.io.mekarobotics.Robotics;
import com.io.mekarobotics.common.capabilities.ModCapabilities;
import com.io.mekarobotics.common.content.blockentity.FactoryBlockEntity;
import com.io.mekarobotics.common.content.entity.RobotEntity;
import com.io.mekarobotics.common.robot.EnumRobotMaterial;
import com.io.mekarobotics.common.robot.EnumRobotPart;
import com.io.mekarobotics.definitions.ModEntityTypes;
import com.io.mekarobotics.definitions.ModItems;
import com.io.mekarobotics.definitions.robotics.ModPerks;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.List;

@GameTestHolder(Robotics.MODID)
public class TestMisc {

    @PrefixGameTestTemplate(false)
    @GameTest(template = "factory", batch = "misc", timeoutTicks = 2000)
    public static void robotConstruction(GameTestHelper helper) {
        helper.spawnItem(ModItems.MATERIALS.get(EnumRobotMaterial.STEEL).get(EnumRobotPart.HEAD).get(), 3, 3, 1);
        helper.spawnItem(ModItems.MATERIALS.get(EnumRobotMaterial.GOLD).get(EnumRobotPart.BODY).get(), 3, 3, 1);
        helper.spawnItem(ModItems.MATERIALS.get(EnumRobotMaterial.STEEL).get(EnumRobotPart.LEFT_ARM).get(), 3, 3, 1);
        helper.spawnItem(ModItems.MATERIALS.get(EnumRobotMaterial.STEEL).get(EnumRobotPart.LEFT_LEG).get(), 3, 3, 1);
        helper.spawnItem(ModItems.MATERIALS.get(EnumRobotMaterial.STEEL).get(EnumRobotPart.RIGHT_LEG).get(), 5, 3, 1);
        helper.spawnItem(ModItems.MODULE_STEALTH.get(), 3, 3, 1);
        BlockEntity be = helper.getBlockEntity(new BlockPos(3, 2, 1));
        if(!(be instanceof FactoryBlockEntity factory)) throw new GameTestAssertException("Did not find factory");
        helper.runAfterDelay(10, () -> factory.startMachine(0));
        helper.runAfterDelay(1850, () -> factory.createNewRobot(Reference.DEFAULT_UUID));
        helper.runAfterDelay(1900, () -> {
            List<RobotEntity> list = helper.getEntities(ModEntityTypes.ROBOT.get(), new BlockPos(3, 1, 0), 1);
            if(list.isEmpty()) throw new GameTestAssertException("Factory did not produce robot");
            TestHelpers.activateAction(helper, list.get(0), "stealth");
            helper.assertEntityProperty(list.get(0), r -> ModCapabilities.get(r, ModCapabilities.PERKS).orElse(ModCapabilities.NO_PERKS).contains(ModPerks.PERK_MASS_PRODUCED.get()), "has mass produced perk");
            helper.assertEntityProperty(list.get(0), r -> ModCapabilities.get(r, ModCapabilities.PERKS).orElse(ModCapabilities.NO_PERKS).contains(ModPerks.PERK_PRECIOUS.get()), "has precious perk");
        });
        helper.runAfterDelay(1910, () -> {
            List<RobotEntity> list = helper.getEntities(ModEntityTypes.ROBOT.get(), new BlockPos(3, 1, 0), 1);
            if(list.isEmpty()) throw new GameTestAssertException("Factory did not produce robot");
            helper.assertEntityProperty(list.get(0), r -> r.hasEffect(MobEffects.INVISIBILITY), "invisibility");
            helper.succeed();
        });

    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "default", batch = "misc", attempts = 10, timeoutTicks = 40)
    public static void robotDrops(GameTestHelper helper) {
        TestHelpers.setupDefaultRobot(helper);
        BlockPos pos = new BlockPos(1, 2, 1);
        helper.killAllEntities();
        helper.succeedWhen(() -> {
            helper.assertItemEntityPresent(ModItems.MATERIALS.get(EnumRobotMaterial.IRON).get(EnumRobotPart.HEAD).get(), pos, 2);
        });
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "default", batch = "misc", attempts = 10, timeoutTicks = 40)
    public static void robotDropsModules(GameTestHelper helper) {
        RobotEntity robot = TestHelpers.setupDefaultRobot(helper);
        TestHelpers.addActionToRobot(robot, "shield");
        BlockPos pos = new BlockPos(1, 2, 1);
        helper.killAllEntities();
        helper.succeedWhen(() -> {
            helper.assertItemEntityPresent(ModItems.MODULE_SHIELD.get(), pos, 2);
        });
    }

    public static void testMachines() {

    }
}
