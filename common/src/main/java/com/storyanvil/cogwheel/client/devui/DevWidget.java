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

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

@Environment(EnvType.CLIENT)
public abstract class DevWidget implements Element {
    private int left;
    private int top;
    private int width;
    private int height;
    private int right;
    private int bottom;

    public DevWidget() {}

    public DevWidget(int left, int top, int width, int height) {
        this.left = left;
        this.top = top;
        this.width = width;
        this.height = height;
    }

    public void render(@NotNull DrawContext g, int mouseX, int mouseY, float partialTick, boolean isHovered, float timeHovered) {
        renderS(g, mouseX, mouseY, partialTick, isHovered, timeHovered, getTop(), getLeft(), getRight(), getBottom());
    }
    public void renderLast(@NotNull DrawContext g, int mouseX, int mouseY, float partialTick, boolean isHovered, float timeHovered) {
        renderLastS(g, mouseX, mouseY, partialTick, isHovered, timeHovered, getTop(), getLeft(), getRight(), getBottom());
    }
    public void resize(@NotNull MinecraftClient minecraft, int width, int height) {
        right = left + this.getWidth();
        bottom = top + this.getHeight();
    }
//    public boolean keyPressed(int key, int scancode, int mods) {return false;}
//    public boolean mouseClicked(double pMouseX, double pMouseY, int button) {return false;}
//    public boolean mouseReleased(double pMouseX, double pMouseY, int button) {return false;}
//    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {return false;}

    public void renderS(@NotNull DrawContext g, int mouseX, int mouseY, float partialTick, boolean isHovered, float timeHovered, int top, int left, int right, int bottom) {}
    public void renderLastS(@NotNull DrawContext g, int mouseX, int mouseY, float partialTick, boolean isHovered, float timeHovered, int top, int left, int right, int bottom) {}

    public boolean isHovered(int mouseX, int mouseY) {
        mouseX -= offX(); mouseY -= offY();
        return mouseX >= left && mouseY >= top && mouseX <= right && mouseY <= bottom;
    }

    protected void blitImage(DrawContext g, int x, int y, int w, int h, int ox, int oy, int ow, int oh) {
        g.drawTexture(t -> RenderLayer.getGui(), DevUI.ATLAS, x, y, ox, oy, w, h, ow, oh, DevUI.ATLAS_SIZE, DevUI.ATLAS_SIZE);
    }
    protected void fill(DrawContext g, int left, int top, int right, int bottom, int color) {
        g.fill(left, top, right, bottom, color);
    }
    protected void draw(DrawContext g, int x, int y, String str, TextRenderer font) {
        g.drawText(font, str, x, y, 16777215, false);
    }
    protected void draw(DrawContext g, int x, int y, String str, TextRenderer font, int color) {
        g.drawText(font, str, x, y, color, false);
    }
    protected void draw(DrawContext g, int x, int y, Text str, TextRenderer font) {
        g.drawText(font, str, x, y, 16777215, false);
    }
    protected void draw(DrawContext g, int x, int y, Text str, TextRenderer font, int color) {
        g.drawText(font, str, x, y, color, false);
    }
    protected DevUI ui() {
        return DevUI.instance;
    }
    protected int offX() {
        return ui().panelLeft;
    }
    protected int offY() {
        return ui().panelTop;
    }

    public int getLeft() {
        return offX() + left;
    }

    public int getTop() {
        return offY() + top;
    }
    public int getRawLeft() {
        return left;
    }

    public int getRawTop() {
        return top;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getRight() {
        return offX() + right;
    }

    public int getBottom() {
        return offY() + bottom;
    }

    public void setHeight(int height) {
        this.height = height;
        this.bottom = top + height;
    }

    public void setWidth(int width) {
        this.width = width;
        this.right = left + width;
    }

    public void setTop(int top) {
        this.top = top;
    }

    public void setLeft(int left) {
        this.left = left;
    }

    @Override
    public String toString() {
        return "DevWidget{" +
                "left=" + left +
                ", top=" + top +
                ", width=" + getWidth() +
                ", height=" + getHeight() +
                ", right=" + right +
                ", bottom=" + bottom +
                '}';
    }

    @Override
    public void setFocused(boolean pFocused) {

    }

    @Override
    public boolean isFocused() {
        return true;
    }
}
