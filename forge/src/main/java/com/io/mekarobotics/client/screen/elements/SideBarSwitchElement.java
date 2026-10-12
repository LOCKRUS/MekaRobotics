package com.io.mekarobotics.client.screen.elements;

import com.io.mekarobotics.Robotics;
import com.io.mekarobotics.client.screen.base.GuiElement;
import com.io.mekarobotics.client.screen.base.IGuiTexturable;
import com.io.mekarobotics.common.access.AccessConfig;
import com.io.mekarobotics.common.handlers.RobotBehavior;
import com.io.mekarobotics.common.handlers.RoboticsMenus;
import com.io.mekarobotics.network.NetworkHandler;
import com.io.mekarobotics.network.messages.server.PacketOpenRobotMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.ArrayList;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class SideBarSwitchElement extends GuiElement {

    public static final ResourceLocation DEFAULT_TEXTURE = ResourceLocation.fromNamespaceAndPath(Robotics.MODID, "textures/gui/sidebar.png");
    private static final DeferredHolder<MenuType<?>, MenuType<?>>[] POSSIBLE_TYPES = RobotBehavior.ALL_ROBOT_MENUS;

    /** Index of current menu inside all possible menus */
    int currentGuiIndex = 0;
    /** Index of current menu inside the currently active menus */
    int subsetGuiIndex = 0;
    /** For each button, saves at which row the designated texture is */
    int[] textureRows;

    public SideBarSwitchElement(MenuType<?> currentMenu, List<MenuType<?>> possibleMenus, int x, int y, int width, int height, int entityId) {
        this(currentMenu, possibleMenus, AccessConfig.ALL_PERMISSIONS, x, y, width, height, entityId);
    }

    public SideBarSwitchElement(MenuType<?> currentMenu, List<MenuType<?>> possibleMenus, AccessConfig permissions, int x, int y, int width, int height, int entityId) {
        this(currentMenu, possibleMenus, x, y, width, height);

        for(int i = 0; i < possibleMenus.size(); i++) {
            if(possibleMenus.get(i).equals(currentMenu)) {
                subsetGuiIndex = i;
                continue; //Don't add a button for the currently active gui
            }
            ButtonElement button = new ButtonElement(x, y + i * height, width, height) {
                @Override
                public void onPress() {
                    NetworkHandler.sendToServer(new PacketOpenRobotMenu(possibleMenus.get(Math.floorDiv(getY() - y, height)), entityId));
                }
            };
            if(!RoboticsMenus.hasRequiredPermissions(permissions, Robotics.proxy.getPlayer(), possibleMenus.get(i))) button.setEnabled(false);

            addElement(button);
        }
    }

    public SideBarSwitchElement(MenuType<?> currentMenu, List<MenuType<?>> possibleMenus, int x, int y, int width, int height, BlockPos pos) {
        this(currentMenu, possibleMenus, x, y, width, height);

        for(int i = 0; i < possibleMenus.size(); i++) {
            if(possibleMenus.get(i).equals(currentMenu)) {
                subsetGuiIndex = i;
                continue; //Don't add a button for the currently active gui
            }
            ButtonElement button = new ButtonElement(x, y + i * height, width, height) {
                @Override
                public void onPress() {
                    NetworkHandler.sendToServer(new PacketOpenRobotMenu(possibleMenus.get(Math.floorDiv(getY() - y, height)), pos));
                }
            };

            addElement(button);
        }
    }

    private SideBarSwitchElement(MenuType<?> currentMenu, List<MenuType<?>> possibleMenus, int x, int y, int width, int height) {
        super(x, y, width, height);

        for(int i = 0; i < POSSIBLE_TYPES.length; i++) {
            if(currentMenu.equals(POSSIBLE_TYPES[i].get())) {
                currentGuiIndex = i;
            }
        }

        //Find out on which row the texture is and save it in textureRows
        List<MenuType<?>> buttonGuiIds = new ArrayList<>(possibleMenus);
        buttonGuiIds.remove(currentMenu);
        this.textureRows = new int[buttonGuiIds.size()];
        int i = 0, j = 0;
        while(i < POSSIBLE_TYPES.length && j < buttonGuiIds.size()) {
            if(buttonGuiIds.get(j).equals(POSSIBLE_TYPES[i].get())) {
                textureRows[j] = i;
                j++;
            }
            i++;
        }
    }

    @Override
    public void initTextureLocation(ResourceLocation texture, int textureX, int textureY) {
        //Assumes inactive icons are in the 4th column
        super.initTextureLocation(texture, textureX + width * 4, textureY + currentGuiIndex * height);
        int i = 0;
        for(var child : children()) {
            if(!(child instanceof IGuiTexturable texturable)) continue;
            texturable.initTextureLocation(texture, textureX, textureY + textureRows[i] * height);
            i++;
        }
    }

    @Override
    public int getY() {
        return super.getY() + subsetGuiIndex * height;
    }
}
