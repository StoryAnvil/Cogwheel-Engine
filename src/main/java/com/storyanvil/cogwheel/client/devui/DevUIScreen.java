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
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class DevUIScreen extends Screen {
    public DevUIScreen() {
        super(Component.literal("Cogwheel Engine DevUI"));
        if (DevUI.instance != null)
            DevUI.instance.reInit();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return true;
    }

    @Override
    public void render(@NotNull GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        if (DevUI.instance == null) {
            DevUI.instance = new DevUI();
            DevUI.instance.reInit();
            this.resize(Minecraft.getInstance(), this.width, this.height);
        }
        try {
            DevUI.instance.renderLogic(g, mouseX, mouseY, partialTick, width, height);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void resize(@NotNull Minecraft minecraft, int width, int height) {
        super.resize(minecraft, width, height);
        DevUI.instance.resizeLogic(minecraft, width, height);
    }

    @Override
    public boolean keyPressed(int key, int scancode, int mods) {
        if (DevUI.OPEN_DEVUI.get().isActiveAndMatches(InputConstants.getKey(key, scancode))) {
            Minecraft.getInstance().setScreen(null);
            return true;
        }
        if (DevUI.instance.keyPressed(key, scancode, mods)) {
            return true;
        }
        return super.keyPressed(key, scancode, mods);
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        if (DevUI.instance.mouseClicked(pButton)) {
            return true;
        }
        return super.mouseClicked(pMouseX, pMouseY, pButton);
    }

    @Override
    public boolean mouseScrolled(double pMouseX, double pMouseY, double pDelta) {
        if (DevUI.instance.mouseScrolled(pMouseX, pMouseY, pDelta)) {
            return true;
        }
        return super.mouseScrolled(pMouseX, pMouseY, pDelta);
    }
}
