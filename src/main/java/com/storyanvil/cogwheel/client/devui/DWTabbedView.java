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

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class DWTabbedView extends DevWidget {
    private ArrayList<Tab> tabs = new ArrayList<>();
    Tab selected = null;
    private Tab hovered = null;

    public DWTabbedView(int left, int top, int width, int height) {
        super(left, top, width, height);
    }

    public void openAndSelect(Tab tab) {
        tabs.add(0, tab);
        tab.compute(this);
        tab.resize(Minecraft.getInstance(), ui().screenWidth, ui().screenHeight);
        selected = tab;
    }
    public void close(Tab tab) {
        if (tab.closingRequest()) {
            tabs.remove(tab);
            if (selected == tab) {
                selected = tabs.isEmpty() ? null : tabs.get(0);
            }
        }
    }

    private double scroll = 0;
    private int tabScrollerBottom = 0;
    private int textY = 0;

    @Override
    public void renderS(@NotNull GuiGraphics g, int mouseX, int mouseY, float partialTick, boolean isHovered, float timeHovered, int top, int left, int right, int bottom) {
        g.enableScissor(left, top, right, tabScrollerBottom);
        g.fill(left, top, right, tabScrollerBottom, -15197131);
        int x = left + ((int) scroll);
        Tab nh = null;
        boolean isBarHovered = mouseY >= top && mouseY <= tabScrollerBottom;
        for (int i = 0; i < tabs.size(); i++) {
            Tab tab = tabs.get(i);
            int r = x + tab.tabWidth;
            boolean isTabHovered = isBarHovered && mouseX >= x && mouseX <= r;
            fill(g, x, top, r, tabScrollerBottom, -14999487);
            draw(g, x + 5, textY, tab.getName(), ui().font);
            if (selected == tab) {
                fill(g, x, top + 11, r, tabScrollerBottom, -13021737);
            }
            if (isTabHovered) nh = tab;
            x = r + 1;
        }
        g.disableScissor();
        hovered = nh;
        if (selected == null) return;
        selected.render(g, mouseX, mouseY, partialTick, ui().hovered == selected, timeHovered);
    }

    @Override
    public void resize(@NotNull Minecraft minecraft, int width, int height) {
        super.resize(minecraft, width, height);
        tabScrollerBottom = getTop() + 12;
        textY = tabScrollerBottom - 2 - ui().font.lineHeight;
        for (int i = (int) scroll; i < tabs.size(); i++) {
            tabs.get(i).resize(minecraft, width, height);
            tabs.get(i).compute(this);
        }
    }

    @Override
    public boolean mouseScrolled(double pMouseX, double pMouseY, double pDelta) {
        if (pMouseY - offY() < 22) {
            scroll += pDelta * 4;
            return true;
        }
        if (selected == null) return false;
        return selected.mouseScrolled(pMouseX, pMouseY, pDelta);
    }

    @Override
    public void renderLastS(@NotNull GuiGraphics g, int mouseX, int mouseY, float partialTick, boolean isHovered, float timeHovered, int top, int left, int right, int bottom) {
        if (selected == null) return;
        selected.renderLastS(g, mouseX, mouseY, partialTick, isHovered, timeHovered, top, left, right, bottom);
    }

    @Override
    public boolean charTyped(char pCodePoint, int pModifiers) {
        if (selected == null) return false;
        return selected.charTyped(pCodePoint, pModifiers);
    }

    @Override
    public boolean keyReleased(int pKeyCode, int pScanCode, int pModifiers) {
        if (selected == null) return false;
        return selected.keyReleased(pKeyCode, pScanCode, pModifiers);
    }

    @Override
    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
        if (selected == null) return false;
        return selected.keyPressed(pKeyCode, pScanCode, pModifiers);
    }

    @Override
    public boolean mouseDragged(double pMouseX, double pMouseY, int pButton, double pDragX, double pDragY) {
        if (selected == null) return false;
        return selected.mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY);
    }

    @Override
    public boolean mouseReleased(double pMouseX, double pMouseY, int pButton) {
        if (selected == null) return false;
        return selected.mouseReleased(pMouseX, pMouseY, pButton);
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        if (hovered != null) {
            if (pButton == 0) {
                selected = hovered;
                return true;
            } else if (pButton == 1) {
                if (hovered.closingRequest()) {
                    tabs.remove(hovered);
                }
                if (hovered == selected) {
                    selected = tabs.isEmpty() ? null : tabs.get(0);
                }
            }
        }
        if (selected == null) return false;
        return selected.mouseClicked(pMouseX, pMouseY, pButton);
    }

    @Override
    public void mouseMoved(double pMouseX, double pMouseY) {
        if (selected == null) return;
        selected.mouseMoved(pMouseX, pMouseY);
    }

    public void closeAll() {
        for (int i = 0; i < tabs.size(); i++) {
            close(tabs.get(i));
        }
    }

    public abstract static class Tab extends DevWidget {
        public Tab() {}

        public Tab(int left, int top, int width, int height) {
            super(left, top, width, height);
        }

        private int tabWidth = 0;

        public void compute(@NotNull DWTabbedView view) {
            tabWidth = view.ui().font.width(getName()) + 10;
            Tab.this.setLeft(view.getRawLeft());
            Tab.this.setTop(view.tabScrollerBottom - offY());
            Tab.this.setHeight(view.getBottom() - view.tabScrollerBottom);
            Tab.this.setWidth(view.getWidth());
        }

        public abstract String getName();
        public boolean closingRequest() {
            return true;
        }
    }
}
