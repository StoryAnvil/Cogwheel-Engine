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

package com.storyanvil.cogwheel.client.screen;

import com.storyanvil.cogwheel.client.saui.AbstractCogComponent;
import com.storyanvil.cogwheel.client.saui.SAUI;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class CustomScreen extends Screen {
    private final SAUI saui;
    private AbstractCogComponent selected = null;
    private boolean wasResized = false;

    protected CustomScreen(SAUI saui) {
        super(Component.empty());
        this.saui = saui;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return saui.isShouldCloseOnESC();
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        if (!wasResized)
            this.resize(Minecraft.getInstance(), this.width, this.height);
        if (saui.isRenderBackground())
            super.renderBackground(guiGraphics);
        for (int i = 0; i < saui.getComponents().size(); i++) {
            AbstractCogComponent cogComponent = saui.getComponents().get(i);
            if (cogComponent.render(guiGraphics, pMouseX, pMouseY, pPartialTick)) {
                selected = cogComponent;
            }
        }
    }

    @Override
    public void resize(@NotNull Minecraft minecraft, int width, int height) {
        wasResized = true;
        super.resize(minecraft, width, height);
        for (int i = 0; i < saui.getComponents().size(); i++) {
            AbstractCogComponent cogComponent = saui.getComponents().get(i);
            cogComponent.resize(minecraft, width, height);
        }
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        if (selected != null) {
            if (selected.mouseClicked(pMouseX, pMouseY, pButton)) {
                return true;
            }
        }
        return super.mouseClicked(pMouseX, pMouseY, pButton);
    }
    @Override
    public boolean mouseReleased(double pMouseX, double pMouseY, int pButton) {
        if (selected != null) {
            if (selected.mouseReleased(pMouseX, pMouseY, pButton)) {
                return true;
            }
        }
        return super.mouseReleased(pMouseX, pMouseY, pButton);
    }

    @Override
    public boolean mouseDragged(double pMouseX, double pMouseY, int pButton, double pDragX, double pDragY) {
        if (selected != null) {
            if (selected.mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY)) {
                return true;
            }
        }
        return super.mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY);
    }

    @Override
    public boolean mouseScrolled(double pMouseX, double pMouseY, double pDelta) {
        if (selected != null) {
            if (selected.mouseScrolled(pMouseX, pMouseY, pDelta)) {
                return true;
            }
        }
        return super.mouseScrolled(pMouseX, pMouseY, pDelta);
    }
}
