/*
 *
 *  * StoryAnvil CogWheel Engine
 *  * Copyright (C) 2025 StoryAnvil
 *  *
 *  * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *  *
 *  * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 *  *
 *  * You should have received a copy of the GNU Lesser General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.storyanvil.cogwheel.client.devui;

import com.mojang.blaze3d.platform.InputConstants;
import com.storyanvil.cogwheel.CogwheelEngine;
import com.storyanvil.cogwheel.network.devui.DevEarlySyncPacket;
import com.storyanvil.cogwheel.network.devui.DevNetwork;
import com.storyanvil.cogwheel.network.devui.DevResyncRequest;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;

import static org.lwjgl.glfw.GLFW.*;

@OnlyIn(Dist.CLIENT) @Mod.EventBusSubscriber(modid = CogwheelEngine.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class DevUI implements GuiEventListener {
    public static final Lazy<KeyMapping> OPEN_DEVUI = Lazy.of(() -> new KeyMapping("ui.storyanvil_cogwheel.dev_ui", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_GRAVE_ACCENT, "ui.storyanvil_cogwheel"));
    public static final ResourceLocation ATLAS = ResourceLocation.fromNamespaceAndPath(CogwheelEngine.MODID, "textures/gui/devui.png");
    public static final int ATLAS_SIZE = 256;
    @SubscribeEvent
    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(OPEN_DEVUI.get());
    }
    protected static DevUI instance;
    public static boolean permitted = false;

    // ========================================================================================== \\

    protected DevWidget hovered = null;
    protected float hoverTime = 0f;
    private ArrayList<DevWidget> widgets = new ArrayList<>();
    protected int screenWidth = 0;
    protected int screenHeight = 0;
    protected Font font;

    boolean fullscreen = false;
    protected int panelLeft = 0;
    protected int panelTop = 0;

    protected boolean drawConsole = false;
    private DWConsole console;
    DWTabbedView tabs;

    public DevUI() {
        instance = this;
        font = Minecraft.getInstance().font;
        addWidget(new DWButton(0, 0, 11, 11, 0, 0, 11, 11, Component.translatable("ui.storyanvil_cogwheel.devui.fullscreen")){
            @Override
            public void press(int btn) {
                if (btn == GLFW_MOUSE_BUTTON_LEFT) {
                    fullscreen = !fullscreen;
                    scheduleResize();
                }
            }
        });
        addWidget(new DWButton(11, 0, 11, 11, 10, 0, 11, 11, Component.translatable("ui.storyanvil_cogwheel.devui.resync")){
            @Override
            public void press(int btn) {
                if (btn == GLFW_MOUSE_BUTTON_LEFT) {
                    DevNetwork.sendToServer(new DevEarlySyncPacket(permitted, false));
                    DevNetwork.sendToServer(new DevResyncRequest());
                }
            }
        });
        addWidget(new DWButton(22, 0, 11, 11, 20, 0, 11, 11, Component.translatable("ui.storyanvil_cogwheel.devui.console")){
            @Override
            public void press(int btn) {
                if (btn == GLFW_MOUSE_BUTTON_LEFT) {
                    DevUI.instance.openConsole();
                }
            }
        });
        tabs = addWidget(new DWTabbedView(0, 12, 0, 0){
            @Override
            public void resize(@NotNull Minecraft minecraft, int width, int height) {
                setWidth(ui().screenWidth - ui().panelLeft);
                setHeight(screenHeight - getRawTop());
                super.resize(minecraft, width, height);
            }
        });
        console = addWidget(new DWConsole());
    }

    public void openConsole() {
        drawConsole = true;
    }

    public void init() {
        hovered = null;
    }

    public void renderLogic(@NotNull GuiGraphics g, int mouseX, int mouseY, float partialTick, int width, int height) {
        if (screenWidth != width || screenHeight != height) {
            this.resizeLogic(Minecraft.getInstance(), width, height);
        }
        g.fill(panelLeft, panelTop, screenWidth, screenHeight, -15461087);
        DevWidget oldHovered = hovered;
        boolean allowHover = true;
        this.hovered = null;
        if (drawConsole) {
            this.hovered = console;
            allowHover = false;
        }
        for (int i = 0; i < widgets.size(); i++) {
            DevWidget widget = widgets.get(i);
            boolean hover = allowHover && widget.isHovered(mouseX, mouseY);
            if (hover) {
                hovered = widget;
                if (hovered != oldHovered) hoverTime = 0f;
                else hoverTime += partialTick;
            }
            widget.render(g, mouseX, mouseY, partialTick, hover, hoverTime);
        }
        for (int i = 0; i < widgets.size(); i++) {
            DevWidget widget = widgets.get(i);
            widget.renderLast(g, mouseX, mouseY, partialTick, hovered == widget, hoverTime);
        }
    }

    public void resizeLogic(@NotNull Minecraft minecraft, int width, int height) {
        this.screenWidth = width;
        this.screenHeight = height;

        this.panelTop = 0;
        if (fullscreen) {
            this.panelLeft = 0;
        } else {
            this.panelLeft = this.screenWidth / 2;
        }
        for (int i = 0; i < widgets.size(); i++) {
            DevWidget widget = widgets.get(i);
            widget.resize(minecraft, width, height);
        }
    }

    @Override
    public boolean keyPressed(int key, int scancode, int mods) {
        if (drawConsole) {
            return console.keyPressed(key, scancode, mods);
        }
        if (tabs.selected != null && tabs.selected.keyPressed(key, scancode, mods)) {
            return true;
        }
        return false;
    }

    @Override
    public void setFocused(boolean pFocused) {}

    @Override
    public boolean isFocused() {
        return true;
    }

    public boolean mouseClicked(double pMouseX, double pMouseY, int button) {
        return (hovered != null && hovered.mouseClicked(pMouseX, pMouseY, button));
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        return (hovered != null && hovered.mouseScrolled(mouseX, mouseY, delta));
    }

    public <T extends DevWidget> T addWidget(T widget) {
        widgets.add(widget);
        scheduleResize();
        return widget;
    }
    public void scheduleResize() {
        this.screenHeight = -1;
    }

    @Override
    public boolean keyReleased(int key, int scancode, int mods) {
        if (hovered != null && hovered.keyReleased(key, scancode, mods)) {
            return true;
        }
        if (tabs.selected != null && tabs.selected.keyReleased(key, scancode, mods)) {
            return true;
        }
        return false;
    }
    @Override
    public boolean charTyped(char pCodePoint, int pModifiers) {
        if (hovered != null && hovered.charTyped(pCodePoint, pModifiers)) {
            return true;
        }
        if (tabs.selected != null && tabs.selected.charTyped(pCodePoint, pModifiers)) {
            return true;
        }
        return false;
    }

    @Override
    public void mouseMoved(double pMouseX, double pMouseY) {
        if (hovered != null) hovered.mouseMoved(pMouseX, pMouseY);
    }
    @Override
    public boolean mouseReleased(double pMouseX, double pMouseY, int pButton) {
        return hovered != null && hovered.mouseReleased(pMouseX, pMouseY, pButton);
    }
    @Override
    public boolean mouseDragged(double pMouseX, double pMouseY, int pButton, double pDragX, double pDragY) {
        return hovered != null && hovered.mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY);
    }
}
