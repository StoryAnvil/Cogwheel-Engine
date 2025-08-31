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

import com.storyanvil.cogwheel.CogwheelEngine;
import com.storyanvil.cogwheel.network.mc.CogwheelPacketHandler;
import com.storyanvil.cogwheel.network.mc.DialogBound;
import com.storyanvil.cogwheel.network.mc.DialogResponseBound;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

@OnlyIn(Dist.CLIENT)
public class DialogScreen extends Screen {
    private DialogBound bound;

    private int selectedButton = -1;

    public DialogScreen(DialogBound bound) {
        super(Component.translatable("ui.storyanvil_cogwheel.dialog_choice"));
        this.bound = bound;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
//        guiGraphics.drawString(Minecraft.getInstance().font, bound.getRequest(), pMouseX, pMouseY, ChatFormatting.RED.getColor());
        int offsetY = 5;
        selectedButton = -1;
        for (int i = 0; i < bound.getOptions().length; i++) {
            offsetY += renderOption(guiGraphics, bound.getOptions()[i], i, 5, offsetY, pMouseX, pMouseY) + 5;
        }

        Font font = Minecraft.getInstance().font;
        int textWidth = font.width(bound.getRequest());
        int fullWidth = textWidth + 10;
        int left = (this.width - fullWidth) / 2;
        int top = height / 6 * 4;

        guiGraphics.fill(left, top, left + fullWidth, top + font.lineHeight + 10, -1772920468);
        guiGraphics.drawString(font, bound.getRequest(), left + 5, top + 5, ChatFormatting.WHITE.getColor());
    }

    private static final int LINE_LENGTH = 25;
    private int renderOption(GuiGraphics guiGraphics, String option, int _i, int x, int y, int mouseX, int mouseY) {
        Font font = Minecraft.getInstance().font;
        int lines = option.length() / LINE_LENGTH + (option.length() % LINE_LENGTH != 0 ? 1 : 0);
        int height = /* top + bottom offset */ 10 + font.lineHeight * lines;
        int width = -1;

        String[] list = new String[lines];
        for (int i = 0; i < lines; i++) {
            String line = option.substring(i * LINE_LENGTH, Math.min(i * LINE_LENGTH + LINE_LENGTH, option.length()));
            list[i] = line;
            width = Math.max(width, font.width(line));
        }
        boolean hovered = mouseX >= x && mouseX <= x + width + 10 && mouseY >= y && mouseY <= y + height;
        if (hovered) selectedButton = _i;
        guiGraphics.fill(x + 3, y, x + width + 7, y + height, hovered ? -1772920468 : -1773973672);
        guiGraphics.fill(x, y, x + 3, y + height, hovered ? -11312788 : -12365992);
        for (int i = 0; i < lines; i++) {
            String line = list[i];
            guiGraphics.drawString(font, line, x + 5, 5 + font.lineHeight * i + y, ChatFormatting.WHITE.getColor());
        }
        return height;
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        if (selectedButton == -1) {
            return super.mouseClicked(pMouseX, pMouseY, pButton);
        }
//        System.out.println(bound.getOptions()[selectedButton]);
        CogwheelPacketHandler.DELTA_BRIDGE.sendToServer(new DialogResponseBound(bound.getDialogId(), selectedButton));
        return true;
    }
}
