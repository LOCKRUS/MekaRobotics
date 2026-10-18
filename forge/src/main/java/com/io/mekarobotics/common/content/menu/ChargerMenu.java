package com.io.mekarobotics.common.content.menu;

import com.io.mekarobotics.Reference;
import com.io.mekarobotics.common.content.blockentity.ChargerBlockEntity;
import com.io.mekarobotics.definitions.ModBlocks;
import com.io.mekarobotics.definitions.ModMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;

public class ChargerMenu extends BaseMenu {
    public final ChargerBlockEntity blockEntity;
    private final Level level;

    public ChargerMenu(int id, Inventory inventory, FriendlyByteBuf extraData) {
        this(id, inventory, inventory.player.level().getBlockEntity(extraData.readBlockPos()));
    }

    public ChargerMenu(int id, Inventory inventory, BlockEntity blockEntity) {
        super(ModMenuTypes.CHARGER.get(), inventory, id);
        this.blockEntity = (ChargerBlockEntity) blockEntity;
        this.blockEntity.addTrackingData(this);
        this.level = inventory.player.level();
        addPlayerInv(Reference.GUI_ASSEMBLER_DIMENSIONS);
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int slotIndex) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return stillValid(ContainerLevelAccess.create(level, blockEntity.getBlockPos()), player, ModBlocks.CHARGER.get());
    }
}
