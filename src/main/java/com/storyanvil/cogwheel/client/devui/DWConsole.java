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

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class DWConsole extends DevWidget {
    final EditBox editBox;
    private List<Component> tips = new ArrayList<>();

    public DWConsole() {
        DWConsoleAutoComplete.init();
        editBox = new EditBox(ui().font, 0, 0, 0, 0, Component.literal("")){
            @Override
            public void insertText(String pTextToWrite) {
                super.insertText(pTextToWrite);
                DWConsoleAutoComplete.update(getValue());
            }
        };
        editBox.setEditable(true);
        editBox.setFocused(true);
        editBox.setCanLoseFocus(false);
        DWConsoleAutoComplete.update(editBox.getValue());
    }

    @Override
    public void renderLastS(@NotNull GuiGraphics g, int mouseX, int mouseY, float partialTick, boolean isHovered, float timeHovered, int top, int left, int right, int bottom) {
        if (!ui().drawConsole) return;
//        fill(g, left, top, right, bottom, -7631989);
//        fill(g, left + 1, top + 1, right - 1, bottom - 1, -15461087);
        editBox.render(g, mouseX, mouseY, partialTick);

        synchronized (this) {
            if (!tips.isEmpty()) {
                int l = bottom + tips.size() * ui().font.lineHeight;
                fill(g, left - 1, bottom, right + 1, l + 1, -1);
                fill(g, left, bottom, right, l, -16777216);
                int h = bottom;
                for (Component tip : tips) {
                    draw(g, left + 1, h, tip, ui().font);
                    h += ui().font.lineHeight;
                }
            }
        }
    }

    @Override
    public void resize(@NotNull Minecraft minecraft, int width, int height) {
        int w = width / 2;
        int hw = width / 4;
        int h = ui().font.lineHeight + 1;
        int hh = h / 2;
        setLeft(w - hw);
        setTop(height / 2 - hh);
        setHeight(h);
        setWidth(w);
        editBox.setX(getLeft());
        editBox.setY(getTop());
        editBox.setWidth(getWidth());
        editBox.setHeight(getHeight());
    }

    @Override
    public boolean isHovered(int mouseX, int mouseY) {
        return ui().drawConsole && super.isHovered(mouseX, mouseY);
    }

    @Override
    protected int offX() {
        return 0;
    }

    @Override
    protected int offY() {
        return 0;
    }

    @Override
    public boolean keyReleased(int pKeyCode, int pScanCode, int pModifiers) {
        return editBox.keyReleased(pKeyCode, pScanCode, pModifiers);
    }

    @Override
    public boolean mouseScrolled(double pMouseX, double pMouseY, double pDelta) {
        return editBox.mouseScrolled(pMouseX, pMouseY, pDelta);
    }

    @Override
    public void mouseMoved(double pMouseX, double pMouseY) {
        editBox.mouseMoved(pMouseX, pMouseY);
    }

    @Override
    public boolean mouseDragged(double pMouseX, double pMouseY, int pButton, double pDragX, double pDragY) {
        return editBox.mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY);
    }

    @Override
    public boolean mouseReleased(double pMouseX, double pMouseY, int pButton) {
        return editBox.mouseReleased(pMouseX, pMouseY, pButton);
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        return editBox.mouseClicked(pMouseX, pMouseY, pButton);
    }

    @Override
    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
        if (pKeyCode == GLFW.GLFW_KEY_ENTER) {
            DWConsoleAutoComplete.execute(editBox.getValue());
            return true;
        }
        boolean c = editBox.keyPressed(pKeyCode, pScanCode, pModifiers);
        if (c)
            DWConsoleAutoComplete.update(editBox.getValue());
        return c;
    }

    @Override
    public boolean charTyped(char pCodePoint, int pModifiers) {
        if (pCodePoint == '`') {
            ui().drawConsole = false;
            return true;
        }
        return editBox.charTyped(pCodePoint, pModifiers);
    }
    private void compute(ArrayList<Component> tips, String q, String cmd, String arg, String desc) {
        if (cmd.startsWith(q)) {
            boolean full = q.length() <= cmd.length();
            tips.add(
                    Component.literal(full ? cmd.substring(0, q.length()) : cmd).withStyle(ChatFormatting.YELLOW).append(
                            Component.literal(full ? cmd.substring(q.length()) : "").withStyle(ChatFormatting.GRAY)).append(
                            Component.literal(arg).withStyle(ChatFormatting.DARK_GRAY)).append(
                            Component.literal(" - " + desc).withStyle(ChatFormatting.WHITE))
            );
        }
    }

    public List<Component> getTips() {
        return tips;
    }

    public void setEditable(boolean pEnabled) {
        if (editBox == null) return;
        editBox.setEditable(pEnabled);
    }
}
