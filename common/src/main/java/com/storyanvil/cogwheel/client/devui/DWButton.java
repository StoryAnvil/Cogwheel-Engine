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

import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DWButton extends DevWidget {
    private int ox;
    private int oy;
    private int ow;
    private int oh;
    private @Nullable Text tooltip;

    public DWButton(int x, int y, int width, int height, int ox, int oy, int ow, int oh, @Nullable Text tooltip) {
        super(x, y, width, height);
        this.ox = ox;
        this.oy = oy;
        this.ow = ow;
        this.oh = oh;
        this.tooltip = tooltip;
    }

    @Override
    public void renderS(@NotNull DrawContext g, int mouseX, int mouseY, float partialTick, boolean isHovered, float timeHovered, int top, int left, int right, int bottom) {
        blitImage(g, left, top, getWidth(), getHeight(), ox, oy, ow, oh);

        // Hover white overlay
        if (isHovered) {
            fill(g, left, top, right, bottom, 855638015);
        }
    }

    @Override
    public void renderLastS(@NotNull DrawContext g, int mouseX, int mouseY, float partialTick, boolean isHovered, float timeHovered, int top, int left, int right, int bottom) {
        if (tooltip != null && isHovered && timeHovered > 40f) {
            int tooltipWidth = ui().font.getWidth(tooltip) + 10;
            int tooltipHeight = ui().font.fontHeight + 10;
            fill(g, mouseX, mouseY, mouseX + tooltipWidth, mouseY + tooltipHeight, -14736844);
            draw(g, mouseX + 5, mouseY + 5, tooltip, ui().font);
        }
    }

    public void press(int btn) {}

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int button) {
        press(button);
        return true;
    }
}
