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

import com.storyanvil.cogwheel.CogwheelExecutor;
import com.storyanvil.cogwheel.util.Bi;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;

public class DWCodeEditor extends DWTabbedView.Tab {
    private static final HashMap<ResourceLocation, DWCodeEditor> editors = new HashMap<>();

    private ResourceLocation rl;
    private String name;

    public static DWCodeEditor getOrCreateEditor(ResourceLocation script) {
        if (editors.containsKey(script))
            return editors.get(script);
        DWCodeEditor editor = new DWCodeEditor(script);
        editors.put(script, editor);
        DevUI.instance.tabs.openAndSelect(editor);
        return editor;
    }

    private DWCodeEditor(ResourceLocation rl) {
        this.rl = rl;
        this.name = rl.toString();

        String path = rl.getPath();
        if (path.endsWith(".sad")) {
            highlighter = new DevHighlighters.StoryAnvilDialog();
        } else if (path.endsWith(".sa")) {
            highlighter = new DevHighlighters.CogScript();
        } else {
            highlighter = new DevHighlighters.Empty();
        }
    }

    private ArrayList<Bi<String, MutableComponent>> code = new ArrayList<>();
    private int scroll = 0;
    private int codeLeft = 0;
    private Highlighter highlighter;

    @Override
    public void renderS(@NotNull GuiGraphics g, int mouseX, int mouseY, float partialTick, boolean isHovered, float timeHovered, int top, int left, int right, int bottom) {
        int y = top;
        for (int i = scroll; i < code.size(); i++) {
            MutableComponent b = code.get(i).getB();
            draw(g, codeLeft + 5, y, b == null ? EMPTY : b, ui().font);
            y += ui().font.lineHeight;
        }
    }

    @Override
    public boolean mouseScrolled(double pMouseX, double pMouseY, double pDelta) {
        scroll = Mth.clamp(scroll + (pDelta > 0 ? -1 : 1), 0, code.size() - 1);
        return true;
    }

    @Override
    public void resize(@NotNull Minecraft minecraft, int width, int height) {
        super.resize(minecraft, width, height);
        codeLeft = getLeft() + 1;
    }

    @Override
    public String getName() {
        return name;
    }

    private static final MutableComponent EMPTY = Component.literal("UNHIGHLIGHTED").withStyle(ChatFormatting.RED, ChatFormatting.BOLD);
    private static final MutableComponent FAILURE = Component.literal("HIGHLIGHTER FAILURE").withStyle(ChatFormatting.RED, ChatFormatting.BOLD);
    public void setCode(String code) {
        this.code.clear();
        String[] lines = code.split("\n");
        for (int i = 0; i < lines.length; i++) {
            this.code.add(new Bi<>(lines[i], EMPTY));
        }
        for (int i = 0; i < lines.length; i++) {
            highlight(i);
        }
    }
    public void highlight(int line) {
        try {
            MutableComponent c = highlighter.highlight(line, code, code.get(line).getA());
            this.code.get(line).setB(c);
        } catch (Exception e) {
            CogwheelExecutor.log.error("Trying to highlight line \"{}\" with {}", code.get(line).getA(), highlighter.getClass().getCanonicalName(), e);
            this.code.get(line).setB(FAILURE);
        }
    }

    public abstract static class Highlighter {
        public abstract MutableComponent highlight(int lineNumber, ArrayList<Bi<String, MutableComponent>> code, String line);
    }
}
