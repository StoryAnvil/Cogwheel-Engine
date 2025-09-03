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

package com.storyanvil.cogwheel.client.saui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

public abstract class AbstractCogComponent {
    public abstract boolean render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick);
    public abstract void resize(Minecraft minecraft, int width, int height);
    public boolean mouseClicked(double mouseX, double mouseY, int button) {return false;};
    public boolean mouseReleased(double mouseX, double mouseY, int button) {return false;};
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {return false;};
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {return false;};
}
