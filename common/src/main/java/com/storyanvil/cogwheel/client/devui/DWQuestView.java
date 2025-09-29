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
import net.minecraft.client.gui.DrawContext;
import org.jetbrains.annotations.NotNull;

public class DWQuestView extends DWTabbedView.Tab {
    public DWQuestView(int left, int top, int width, int height) {
        super(left, top, width, height);
    }

    @SuppressWarnings("FieldCanBeLocal")
    private boolean tabsOpen = false;
    @SuppressWarnings("FieldCanBeLocal")
    private int tabsWidth = 0;
    @SuppressWarnings("FieldCanBeLocal")
    private boolean dev = false;

    @Override
    public void renderS(@NotNull DrawContext g, int mouseX, int mouseY, float partialTick, boolean isHovered, float timeHovered, int top, int left, int right, int bottom) {
        // Background
        fill(g, left, top, right, bottom, -770172376);

        // Tab selector
//        if (tabsOpen) {
//
//        } else {
//
//        }
    }

    @Override
    public void resize(@NotNull MinecraftClient minecraft, int width, int height) {
        tabsWidth = width / 3;
        super.resize(minecraft, width, height);
    }

    @Override
    public String getName() {
        return "Quests";
    }

    public void setDev(boolean dev) {
        this.dev = dev;
    }
}
