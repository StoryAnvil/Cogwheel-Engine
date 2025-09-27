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

package com.storyanvil.cogwheel.client.screen;

import com.storyanvil.cogwheel.CogwheelHooks;
import com.storyanvil.cogwheel.network.mc.DialogChoiceBound;
import com.storyanvil.cogwheel.network.mc.DialogResponseBound;
import com.storyanvil.cogwheel.util.StoryUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.NotNull;

import static com.storyanvil.cogwheel.CogwheelEngine.MODID;

@Environment(EnvType.CLIENT)
public class DialogChoiceScreen extends Screen {
    private DialogChoiceBound bound;
    private int selectedButton = -1;
    private Identifier image = null;

    public DialogChoiceScreen(DialogChoiceBound bound) {
        super(Text.translatable("ui.storyanvil_cogwheel.dialog_choice"));
        this.bound = bound;
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    private float time = 0f;
    @Override
    public void render(@NotNull DrawContext DrawContext, int pMouseX, int pMouseY, float pPartialTick) {
        if (boxLeft == 0)
            resize(MinecraftClient.getInstance(), this.width, this.height);
        selectedButton = -1;
        TextRenderer font = MinecraftClient.getInstance().textRenderer;
        DrawContext.fill(boxLeft, boxTop, boxRight, boxBottom, -939524096);
        DrawContext.drawGuiTexture(t -> RenderLayer.getGui(), image, boxLeft, boxBottom - imageWidth, imageWidth, imageWidth, 0, 0, 100, 100);
        DrawContext.drawText(font, bound.npcName(), textX, textY, 5635925, false);
        time += pPartialTick;
        DrawContext.drawWrappedText(font, Text.of(bound.request().substring(0, Math.min((int) (time), bound.request().length()))), textX, textY + font.fontHeight + 5, textEnd, 16777215, false);

        for (int i = 0; i < bound.options().size(); i++) {
            String o = bound.options().get(i);
            int left = responseX - 1;
            int top = responseY[i] - 1;
            int bottom = responseY[i] + responseH[i];
            boolean isHovered = StoryUtils.isHovering(pMouseX, pMouseY, left, responseEnd, top, bottom);
            if (isHovered) selectedButton = i;
            DrawContext.fill(left, top, responseEnd, bottom, isHovered ? -936892376 : -939524096);
            DrawContext.drawWrappedText(font, Text.of(o), responseX, responseY[i], responseWidth, 16777215, false);
        }
    }

    private int boxLeft = 0;
    private int boxRight = 0;
    private int boxTop = 0;
    private int boxBottom = 0;
    private int boxWidth = 0;
    private int boxHeight = 0;
    private int imageWidth = 0;
    private int textX = 0;
    private int textY = 0;
    private int textEnd = 0;

    private int responseX = 0;
    private int responseWidth = 0;
    private int responseEnd = 0;
    private int[] responseY = new int[0];
    private int[] responseH = new int[0];

    @Override
    public void resize(@NotNull MinecraftClient minecraft, int width, int height) {
        super.resize(minecraft, width, height);
        TextRenderer font = minecraft.textRenderer;
        boxWidth = MathHelper.clamp((int) ((width / 100f) * 70), 100, 350);
        boxHeight = MathHelper.clamp((int) ((height / 100f) * 30), 50, 150);
        boxTop = (height / 4 * 3) - (boxHeight / 2);
        boxLeft = (width / 2) - (boxWidth / 2);
        boxRight = boxLeft + boxWidth;
        boxBottom = boxTop + boxHeight;
        imageWidth = Math.max(boxWidth / 3, boxHeight);
        textX = boxLeft + imageWidth + 15;
        textY = boxTop + 5;
        textEnd = boxWidth - imageWidth - 30;
        image = Identifier.of(MODID, "textures/dialog/" + bound.texture() + ".png");

        responseY = new int[bound.options().size()];
        responseH = new int[bound.options().size()];
        responseX = boxRight - 25;
        responseWidth = boxWidth / 3;
        int baseY = boxTop - 30;
        responseEnd = responseX + responseWidth;
        for (int i = 0; i < bound.options().size(); i++) {
            String o = bound.options().get(i);
            int lines = font.getWrappedLinesHeight(Text.of(o), responseWidth) / 9;
            int h = lines * font.fontHeight;
            responseY[i] = baseY - h;
            responseH[i] = h;
            baseY = responseY[i] - 10;
        }
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        if (selectedButton == -1) {
            return super.mouseClicked(pMouseX, pMouseY, pButton);
        }
        CogwheelHooks.sendPacketToServer(new DialogResponseBound(bound.dialogId(), selectedButton));
        return true;
    }
}
