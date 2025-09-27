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

import com.storyanvil.cogwheel.CogwheelExecutor;
import com.storyanvil.cogwheel.util.Bi;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;

public class DWCodeViewer extends DWTabbedView.Tab {
    private String name;

    public DWCodeViewer(String name) {
        this.name = name;
        highlighter = DWCodeEditor.getHighlighterByFileName(name);
    }

    private ArrayList<Bi<String, MutableText>> code = new ArrayList<>();
    private int scroll = 0;
    private int scrollHor = 0;
    private boolean shift = false;
    private int codeLeft = 0;
    private DWCodeEditor.Highlighter highlighter;

    @Override
    public void renderS(@NotNull DrawContext g, int mouseX, int mouseY, float partialTick, boolean isHovered, float timeHovered, int top, int left, int right, int bottom) {
        int y = top;
        g.enableScissor(left, top, right, bottom);
        int codeLeftt = codeLeft - scrollHor;
        for (int i = scroll; i < code.size(); i++) {
            MutableText b = code.get(i).getB();
            draw(g, codeLeftt, y, b == null ? EMPTY : b, ui().font);
            y += ui().font.fontHeight;
        }
        g.disableScissor();
    }

    @Override
    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
        shift = (pModifiers & GLFW.GLFW_MOD_SHIFT) == GLFW.GLFW_MOD_SHIFT;
        return false;
    }

    @Override
    public boolean keyReleased(int pKeyCode, int pScanCode, int pModifiers) {
        shift = (pModifiers & GLFW.GLFW_MOD_SHIFT) == GLFW.GLFW_MOD_SHIFT;
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (verticalAmount != 0)
            scroll = MathHelper.clamp(scroll + (verticalAmount > 0 ? -1 : 1), 0, code.size() - 1);
        if (horizontalAmount != 0)
            scrollHor = Math.max(scrollHor + (horizontalAmount > 0 ? -4 : 4), 0);
        return true;
    }

    @Override
    public void resize(@NotNull MinecraftClient minecraft, int width, int height) {
        super.resize(minecraft, width, height);
        codeLeft = getLeft() + 5;
    }

    @Override
    public String getName() {
        return name;
    }

    private static final MutableText EMPTY = Text.literal("UNHIGHLIGHTED").formatted(Formatting.RED, Formatting.BOLD);
    private static final MutableText FAILURE = Text.literal("HIGHLIGHTER FAILURE").formatted(Formatting.RED, Formatting.BOLD);
    public synchronized void setCode(String code) {
        this.code.clear();
        String[] lines = code.split("\n");
        for (int i = 0; i < lines.length; i++) {
            this.code.add(new Bi<>(lines[i], EMPTY));
        }
        for (int i = 0; i < lines.length; i++) {
            highlight(i);
        }
    }
    public synchronized void highlight(int line) {
        try {
            MutableText c = highlighter.highlight(line, code, code.get(line).getA());
            this.code.get(line).setB(c);
        } catch (Exception e) {
            CogwheelExecutor.log.error("Trying to highlight line \"{}\" with {}", code.get(line).getA(), highlighter.getClass().getCanonicalName(), e);
            this.code.get(line).setB(FAILURE);
        }
    }
}
