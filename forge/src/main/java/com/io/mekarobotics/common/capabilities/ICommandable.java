package com.io.mekarobotics.common.capabilities;

import com.io.mekarobotics.common.robot.RobotCommand;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.common.util.INBTSerializable;

import java.util.List;

public interface ICommandable extends INBTSerializable<CompoundTag> {

    void setCommands(List<RobotCommand> commands);

    default void addCommand(RobotCommand command) {
        List<RobotCommand> currentCommands = getCommands();
        for(RobotCommand c : currentCommands) {
            if(command.equals(c)) return;
        }
        currentCommands.add(command);
        setCommands(currentCommands);
    }

    List<RobotCommand> getCommands();

    void removeCommand(RobotCommand command);

    /**
     * Clear tasks applied by commands, as well as vanilla tasks
     */
    void removeAllTasks();

    /**
     * Reapply all tasks that have been cleared by {@link #removeAllTasks()}
     */
    void reapplyAllTasks();
}
