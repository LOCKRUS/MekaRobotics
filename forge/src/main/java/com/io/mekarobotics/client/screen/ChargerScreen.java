package com.io.mekarobotics.client.screen;

import com.io.mekarobotics.Reference;
import com.io.mekarobotics.client.screen.base.BaseContainerScreen;
import com.io.mekarobotics.client.screen.elements.EnergyBarElement;
import com.io.mekarobotics.common.capabilities.ModCapabilities;
import com.io.mekarobotics.common.content.menu.ChargerMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class ChargerScreen extends BaseContainerScreen<ChargerMenu> {
    public ChargerScreen(ChargerMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        imageWidth = Reference.GUI_DEFAULT_DIMENSIONS.width;
        imageHeight = Reference.GUI_ASSEMBLER_DIMENSIONS.height;
    }

    @Override
    protected void init() {
        super.init();
        ModCapabilities.energy(menu.blockEntity).ifPresent(energy ->
                addElement(new EnergyBarElement(energy, leftPos + 14, topPos + 13, 109)));
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, 0xFF373737);
        graphics.fill(leftPos + 1, topPos + 1, leftPos + imageWidth - 1, topPos + imageHeight - 1, 0xFFC6C6C6);
        graphics.fill(leftPos + 5, topPos + 20, leftPos + imageWidth - 5, topPos + 126, 0xFFAAAAAA);
        graphics.drawCenteredString(font, Component.translatable("gui.mekarobotics.charger.description"),
                leftPos + imageWidth / 2, topPos + 72, 0x404040);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, 0, 0, 0);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }
}
