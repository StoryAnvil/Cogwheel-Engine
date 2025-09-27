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

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class QuestUI implements Element {
    protected static QuestUI instance;
    @Override public void setFocused(boolean pFocused) {}
    @Override public boolean isFocused() {return true;}

    public static final Identifier ATLAS = DevUI.ATLAS;
    public static final int ATLAS_SIZE = DevUI.ATLAS_SIZE;

    protected DevWidget hovered = null;
    protected float hoverTime = 0f;
    private ArrayList<DevWidget> widgets = new ArrayList<>();
    protected int screenWidth = 0;
    protected int screenHeight = 0;
    protected TextRenderer font;

    public QuestUI() {
        addWidget(new DWQuestView(0, 0, 10, 10){
            @Override
            public void resize(@NotNull MinecraftClient minecraft, int width, int height) {
                this.setLeft(0);
                this.setTop(0);
                this.setWidth(width);
                this.setHeight(height);
                super.resize(minecraft, width, height);
            }
        });
    }

    public void init() {
        hovered = null;
        hoverTime = 0f;
    }

    public void renderLogic(@NotNull DrawContext g, int mouseX, int mouseY, float partialTick, int width, int height) {
        if (screenWidth != width || screenHeight != height) {
            this.resizeLogic(MinecraftClient.getInstance(), width, height);
        }
        DevWidget oldHovered = hovered;
        boolean allowHover = true;
        this.hovered = null;
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

    public void resizeLogic(@NotNull MinecraftClient minecraft, int width, int height) {
        this.screenWidth = width;
        this.screenHeight = height;
        this.font = minecraft.textRenderer;
        for (int i = 0; i < widgets.size(); i++) {
            DevWidget widget = widgets.get(i);
            widget.resize(minecraft, width, height);
        }
    }

    public <T extends DevWidget> T addWidget(T widget) {
        widgets.add(widget);
        scheduleResize();
        return widget;
    }

    public void scheduleResize() {
        this.screenHeight = -1;
    }
}
