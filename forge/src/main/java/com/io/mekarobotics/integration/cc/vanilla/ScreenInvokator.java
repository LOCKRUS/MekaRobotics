package com.io.mekarobotics.integration.cc.vanilla;

import com.io.mekarobotics.Reference;
import com.io.mekarobotics.common.access.EnumPermission;
import com.io.mekarobotics.integration.cc.IComputerized;
import com.io.mekarobotics.integration.cc.ProgrammingMenu;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.inventory.MenuConstructor;
import net.neoforged.fml.ModList;

import static com.io.mekarobotics.common.handlers.RobotBehavior.hasAccess;

public class ScreenInvokator {

    public static MenuConstructor invokeProgrammingMenu(Entity target, IComputerized computer) {
        if(ModList.get().isLoaded(Reference.CC_MOD_ID)) return (id, playerInv, f3) -> new ProgrammingMenu(id, playerInv, target, p -> hasAccess(p, target, EnumPermission.COMMANDS), computer.getComputer());
        return (id, playerInv, f3) -> new VProgrammingMenu(id, playerInv, null);
    }
}
