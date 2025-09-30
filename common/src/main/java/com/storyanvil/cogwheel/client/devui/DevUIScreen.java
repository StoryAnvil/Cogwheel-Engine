/*
 *
 * StoryAnvil Cogwheel Engine
 * Copyright (C) 2025 StoryAnvil
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.storyanvil.cogwheel.client.devui;

import com.storyanvil.cogwheel.CogwheelEngine;
import com.storyanvil.cogwheel.CogwheelHooks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

public class DevUIScreen extends Screen {
    public DevUIScreen() {
        super(Text.literal("Cogwheel Engine DevUI"));
        if (DevUI.instance == null) {
            DevUI.instance = new DevUI(MinecraftClient.getInstance().getWindow().getWidth(), MinecraftClient.getInstance().getWindow().getHeight());
            DevUI.instance.resizeLogic(MinecraftClient.getInstance(), DevUI.instance.screenWidth, DevUI.instance.screenHeight);
        }
        DevUI.instance.init();
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return true;
    }

    @Override
    public void render(@NotNull DrawContext g, int mouseX, int mouseY, float partialTick) {
//        if (DevUI.instance == null) {
//            DevUI.instance = new DevUI(this.width, this.height);
//            DevUI.instance.init();
//            this.resize(MinecraftClient.getInstance(), this.width, this.height);
//        }
        try {
            DevUI.instance.renderLogic(g, mouseX, mouseY, partialTick, width, height);
        } catch (Exception e) {
            CogwheelEngine.LOGGER.error("", e);
        }
    }

    @Override
    public void resize(@NotNull MinecraftClient minecraft, int width, int height) {
        super.resize(minecraft, width, height);
        DevUI.instance.resizeLogic(minecraft, width, height);
    }

    @Override
    public boolean keyPressed(int key, int scancode, int mods) {
        if (CogwheelHooks.getDevUIBind().matchesKey(key, scancode)) {
            DevUI.instance.openConsole();
            return true;
        }
        if (DevUI.instance.keyPressed(key, scancode, mods)) {
            return true;
        }
        return super.keyPressed(key, scancode, mods);
    }
    @Override
    public boolean keyReleased(int key, int scancode, int mods) {
        if (DevUI.instance.keyReleased(key, scancode, mods)) {
            return true;
        }
        return super.keyReleased(key, scancode, mods);
    }
    @Override
    public boolean charTyped(char pCodePoint, int pModifiers) {
        if (DevUI.instance.charTyped(pCodePoint, pModifiers)) {
            return true;
        }
        return super.charTyped(pCodePoint, pModifiers);
    }

    @Override
    public void mouseMoved(double pMouseX, double pMouseY) {
        super.mouseMoved(pMouseX, pMouseY);
        if (DevUI.instance == null) return;
        DevUI.instance.mouseMoved(pMouseX, pMouseY);
    }
    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        if (DevUI.instance == null) return false;
        return DevUI.instance.mouseClicked(pMouseX, pMouseY, pButton) || super.mouseClicked(pMouseX, pMouseY, pButton);
    }
    @Override
    public boolean mouseReleased(double pMouseX, double pMouseY, int pButton) {
        if (DevUI.instance == null) return false;
        return DevUI.instance.mouseReleased(pMouseX, pMouseY, pButton) || super.mouseReleased(pMouseX, pMouseY, pButton);
    }
    @Override
    public boolean mouseDragged(double pMouseX, double pMouseY, int pButton, double pDragX, double pDragY) {
        if (DevUI.instance == null) return false;
        return DevUI.instance.mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY) || super.mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY);
    }
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (DevUI.instance == null) return false;
        return DevUI.instance.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount) || super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }
}
