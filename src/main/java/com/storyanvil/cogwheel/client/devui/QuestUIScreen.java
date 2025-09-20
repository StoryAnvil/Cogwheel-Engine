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

public class QuestUIScreen extends Screen {
    public QuestUIScreen() {
        super(Component.literal("Cogwheel Engine QuestUI"));
        if (QuestUI.instance != null)
            QuestUI.instance.init();
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
        if (QuestUI.instance == null) {
            QuestUI.instance = new QuestUI();
            QuestUI.instance.init();
            this.resize(Minecraft.getInstance(), this.width, this.height);
        }
        try {
            QuestUI.instance.renderLogic(g, mouseX, mouseY, partialTick, width, height);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void resize(@NotNull Minecraft minecraft, int width, int height) {
        super.resize(minecraft, width, height);
        QuestUI.instance.resizeLogic(minecraft, width, height);
    }

    @Override
    public boolean keyPressed(int key, int scancode, int mods) {
        if (DevUI.OPEN_QUESTS.get().isActiveAndMatches(InputConstants.getKey(key, scancode))) {
            Minecraft.getInstance().setScreen(null);
            return true;
        }
        if (QuestUI.instance.keyPressed(key, scancode, mods)) {
            return true;
        }
        return super.keyPressed(key, scancode, mods);
    }
    @Override
    public boolean keyReleased(int key, int scancode, int mods) {
        if (QuestUI.instance.keyReleased(key, scancode, mods)) {
            return true;
        }
        return super.keyReleased(key, scancode, mods);
    }
    @Override
    public boolean charTyped(char pCodePoint, int pModifiers) {
        if (QuestUI.instance.charTyped(pCodePoint, pModifiers)) {
            return true;
        }
        return super.charTyped(pCodePoint, pModifiers);
    }

    @Override
    public void mouseMoved(double pMouseX, double pMouseY) {
        super.mouseMoved(pMouseX, pMouseY);
        QuestUI.instance.mouseMoved(pMouseX, pMouseY);
    }
    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        return QuestUI.instance.mouseClicked(pMouseX, pMouseY, pButton) || super.mouseClicked(pMouseX, pMouseY, pButton);
    }
    @Override
    public boolean mouseReleased(double pMouseX, double pMouseY, int pButton) {
        return QuestUI.instance.mouseReleased(pMouseX, pMouseY, pButton) || super.mouseReleased(pMouseX, pMouseY, pButton);
    }
    @Override
    public boolean mouseDragged(double pMouseX, double pMouseY, int pButton, double pDragX, double pDragY) {
        return QuestUI.instance.mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY) || super.mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY);
    }
    @Override
    public boolean mouseScrolled(double pMouseX, double pMouseY, double pDelta) {
        return QuestUI.instance.mouseScrolled(pMouseX, pMouseY, pDelta) || super.mouseScrolled(pMouseX, pMouseY, pDelta);
    }
}
