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
import com.storyanvil.cogwheel.network.mc.CogwheelPacketHandler;
import com.storyanvil.cogwheel.network.devui.DevBoundRequest;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
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
import java.util.function.Consumer;

import static com.storyanvil.cogwheel.util.StoryUtils.isHovering;

@OnlyIn(Dist.CLIENT) @Mod.EventBusSubscriber(modid = CogwheelEngine.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class DevUI {
    protected static DevUI instance;
    private static final ResourceLocation ATLAS = ResourceLocation.fromNamespaceAndPath(CogwheelEngine.MODID, "textures/gui/devui.png");
    protected Minecraft mc;
    protected Font mainFont;
    protected boolean fullscreen = false;
    protected int panelWidth;
    protected int panelLeft;
    protected int screenHeight;
    protected int screenWidth;
    protected Hover click = null;
    protected double tabScroll = 0f;
    protected ArrayList<DevTab> tabs = new ArrayList<>();
    protected DevTab selected = null;

    private GuiGraphics g;
    public void renderLogic(@NotNull GuiGraphics g, int mouseX, int mouseY, float partialTick, int width, int height) {
        if (width != screenWidth || height != screenHeight) {
            resizeLogic(Minecraft.getInstance(), width, height);
        }
        this.g = g;
        this.click = null;

        // Render
        g.fill(panelLeft, 0, screenWidth, screenHeight, -15987436);

        renderBtn(panelLeft, 0, 10, 10, 0, 0, 10, 10, this::fullscreenBtn, mouseX, mouseY);
        renderBtn(panelLeft + 10, 0, 10, 10, 10, 0, 10, 10, this::syncBtn, mouseX, mouseY);
        renderBtn(panelLeft + 20, 0, 10, 10, 20, 0, 10, 10, this::fileBtn, mouseX, mouseY);
        renderTabs(mouseX, mouseY, partialTick);
        // Render end

        this.g = null;
    }

    private void renderBtn(int x, int y, int w, int h, int ox, int oy, int sx, int sy, Consumer<Integer> click, int mX, int mY) {
        g.blit(ATLAS, x, y, w, h, ox, oy, sx, sy, 256, 256);
        if (isHovering(mX, mY, x, x + w, y, y + h)) {
            this.click = new Hover().withClick(click);
        }
    }
    private void renderTabs(int mX, int mY, float partialTick) {
        g.fill(panelLeft, 10, screenWidth, 22, -14736844);
        boolean h = false;
        if (isHovering(mX, mY, panelLeft, screenWidth, 12, 22)) {
            this.click = new Hover().withScroll(this::tabScroll);
            h = true;
        }
        g.enableScissor(panelLeft, 10, screenWidth, 22);
        int offestX = (int) tabScroll;
        int textY = 20 - mainFont.lineHeight;
        for (int i = 0; i < tabs.size(); i++) {
            DevTab tab = tabs.get(i);
            boolean isSelected = tab == instance.selected;
            int left = panelLeft + offestX;
            int w = mainFont.width(tab.getName()) + 4;
            int right = left + w;
            boolean isHovered = h && (mX >= left && mX <= right);
            g.fill(left, 10, right, 22, -15987436);
            if (isSelected) {
                g.fill(left, 21, right, 22, -14468940);
            }
            if (isHovered) {
                this.click.withClick(I -> {
                    if (I == 1) {
                        if (tab.closeRequest()) {
                            tabs.remove(tab);
                            if (tab == selected) {
                                selected = tabs.isEmpty() ? null : tabs.get(0);
                            }
                        }
                    } else if (I == 0) {
                        DevUI.this.selected = tab;
                    }
                });
            }
            //noinspection DataFlowIssue
            g.drawString(mainFont, tab.getName(), left + 2, textY, (isSelected || isHovered ? ChatFormatting.WHITE : ChatFormatting.GRAY).getColor(), false);
            offestX += right - left;
        }
        g.disableScissor();
        g.enableScissor(panelLeft, 23, screenWidth, screenHeight);
        if (selected != null)
            selected.render(g, mX, mY, partialTick, panelLeft, 23, screenWidth, screenHeight);
        g.disableScissor();
    }

    public void resizeLogic(@NotNull Minecraft minecraft, int width, int height) {
        mc = minecraft;
        mainFont = minecraft.font;
        screenHeight = height;
        screenWidth = width;
        if (fullscreen) {
            panelLeft = 0;
            panelWidth = screenWidth;
        } else {
            panelLeft = width / 2;
            panelWidth = panelLeft;
        }
    }

    private void fullscreenBtn(int btn) {
        this.fullscreen = !this.fullscreen;
        this.screenWidth = -1; // schedule resize
    }
    private void syncBtn(int btn) {
        CogwheelPacketHandler.DELTA_BRIDGE.sendToServer(new DevBoundRequest("full"));
    }
    private void fileBtn(int btn) {
        // TODO
    }
    private void tabScroll(double delta) {
        this.tabScroll += delta * 5;
    }

    public static final Lazy<KeyMapping> OPEN_DEVUI = Lazy.of(() ->
            new KeyMapping("ui.storyanvil_cogwheel.dev_ui", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_GRAVE_ACCENT, "ui.storyanvil_cogwheel"));

    @SubscribeEvent
    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(OPEN_DEVUI.get());
    }

    public boolean mouseClicked(int button) {
        if (click != null) {
            click.click.accept(button);
            return true;
        }
        return false;
    }

    public void reInit() {
        click = null;
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (click != null) {
            click.scroll.accept(delta);
            return true;
        }
        return false;
    }

    public boolean keyPressed(int key, int scancode, int mods) {
        return selected.keyPressed(key, scancode, mods);
    }

    public static class Hover {
        public Consumer<Integer> click = i -> {};
        public Consumer<Double> scroll = i -> {};

        public Hover withClick(Consumer<Integer> click) {
            this.click = click;
            return this;
        }

        public Hover withScroll(Consumer<Double> scroll) {
            this.scroll = scroll;
            return this;
        }
    }
}
