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
import com.storyanvil.cogwheel.network.devui.DevEditorLine;
import com.storyanvil.cogwheel.network.devui.DevEditorState;
import com.storyanvil.cogwheel.network.devui.DevEditorUserDelta;
import com.storyanvil.cogwheel.network.devui.DevNetwork;
import com.storyanvil.cogwheel.util.Bi;
import com.storyanvil.cogwheel.util.StoryUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

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
        myName = Minecraft.getInstance().player.getScoreboardName();
    }

    private ArrayList<Bi<String, MutableComponent>> code = new ArrayList<>();
    private ArrayList<Cursor> cursors = new ArrayList<>();
    private int scroll = 0;
    private int codeLeft = 0;
    private Highlighter highlighter;
    private String myName;
    private Cursor mine = null;

    private float blinker = 0f;
    private boolean blink = false;

    public static DWCodeEditor get(ResourceLocation lc) {
        return editors.get(lc);
    }

    public static void delete(ResourceLocation lc) {
        DWCodeEditor editor = editors.get(lc);
        DevUI.instance.tabs.close(editor);
        editors.remove(lc);
    }

    @Override
    public void renderS(@NotNull GuiGraphics g, int mouseX, int mouseY, float partialTick, boolean isHovered, float timeHovered, int top, int left, int right, int bottom) {
        int y = top;
        for (int i = scroll; i < code.size(); i++) {
            MutableComponent b = code.get(i).getB();
            draw(g, codeLeft, y, b == null ? EMPTY : b, ui().font);
            y += ui().font.lineHeight;
        }
        blinker += partialTick;
        if (blinker > 10f) {blinker = 0f;blink =!blink;}
        if (blink) {
            for (int i = 0; i < cursors.size(); i++) {
                Cursor c = cursors.get(i);
                int t = ui().font.lineHeight * (c.line - scroll) + top;
                int drawingLeft = c.drawingLeft + codeLeft;
                fill(g, drawingLeft, t, drawingLeft + 1, t + ui().font.lineHeight, -939458816);
            }
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
        codeLeft = getLeft() + 5;
    }

    @Override
    public String getName() {
        return name;
    }

    private static final MutableComponent EMPTY = Component.literal("UNHIGHLIGHTED").withStyle(ChatFormatting.RED, ChatFormatting.BOLD);
    private static final MutableComponent NODATA = Component.literal("NO DATA").withStyle(ChatFormatting.RED, ChatFormatting.BOLD);
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
        for (int i = 0; i < cursors.size(); i++) {
            Cursor c = cursors.get(i);
            if (c.line == line) {
                c.compute(this.code.get(line));
            }
        }
    }

    private Cursor getCursor(String name) {
        for (int i = 0; i < cursors.size(); i++) {
            Cursor c = cursors.get(i);
            if (c.name.equals(name))
                return c;
        }
        return null;
    }

    public void handle(DevEditorLine devEditorLine) {
        if (devEditorLine.linesTotal() != code.size()) {
            if (code.size() > devEditorLine.linesTotal()) {
                DevNetwork.sendToServer(new DevEditorState(rl, (byte) -127));
                return;
            } else {
                while (code.size() < devEditorLine.linesTotal())
                    code.add(new Bi<>("", NODATA));
            }
        }
        Bi<String, MutableComponent> line = code.get(devEditorLine.lineNumber());
        if (line.getA().equals(devEditorLine.line())) return;
        line.setA(devEditorLine.line());
        highlight(devEditorLine.lineNumber());
    }

    public void handle(DevEditorUserDelta delta) {
        Cursor c = getCursor(delta.name());
        if (c == null) {
            c = new Cursor();
            c.editor = this;
            cursors.add(c);
            if (delta.name().equals(myName)) {
                mine = c;
            }
        }
        c.line = delta.line();
        c.pos = delta.pos();
        c.selectNextChars = delta.selected();
    }

    @Override
    public boolean keyPressed(int code, int scanCode, int mods) {
        if (code == GLFW.GLFW_KEY_LEFT) {
            mine.setPosSafe(mine.pos - 1);
            mine.wrapLeft();
        } else if (code == GLFW.GLFW_KEY_RIGHT) {
            mine.setPosSafe(mine.pos + 1);
            mine.wrapRight();
        } else if (code == GLFW.GLFW_KEY_UP) {
            mine.setLineSafe(mine.line - 1);
        } else if (code == GLFW.GLFW_KEY_DOWN) {
            mine.setLineSafe(mine.line + 1);
        }
        return false;
    }

    public abstract static class Highlighter {
        public abstract MutableComponent highlight(int lineNumber, ArrayList<Bi<String, MutableComponent>> code, String line);
    }
    public static class Cursor {
        private DWCodeEditor editor;
        private int line = 0;
        private int pos = 0;
        private int selectNextChars = 0;
        private String name;
        private Component sup;

        private boolean onEndOfLine = false;

        private int drawingLeft = 0;

        @Override
        public String toString() {
            return "Cursor{" +
                    "line=" + line +
                    ", pos=" + pos +
                    ", selectNextChars=" + selectNextChars +
                    ", sup=" + sup +
                    ", name='" + name + '\'' +
                    '}';
        }

        public void compute(Bi<String, MutableComponent> line) {
            sup = StoryUtils.subComponent(line.getB(), 0, pos);
            drawingLeft = editor.ui().font.width(sup);
            onEndOfLine = pos == line.getA().length() + 1;
        }

        public void wrapRight() {
            if (onEndOfLine) {
                pos = 0;
                setLineSafe(line + 1);
            }
        }
        public void wrapLeft() {
            if (pos == 0 && line > 0) {
                line--;
                Bi<String, MutableComponent> prv = editor.code.get(line);
                pos = prv.getA().length() + 1;
                compute(prv);
                sync();
            }
        }

        public void setPosSafe(int pos) {
            Bi<String, MutableComponent> line = editor.code.get(this.line);
            this.pos = Mth.clamp(pos, 0, line.getA().length() + 1);
            compute(line);
            sync();
        }

        public void setLineSafe(int line) {
            if (line < 0) return;
            if (line >= editor.code.size()) return;
            this.line = line;
            setPosSafe(this.pos);
        }

        public void sync() {
            editor.blink = true;
            editor.blinker = 0f;
            DevNetwork.sendToServer(new DevEditorUserDelta(editor.rl, line, pos, selectNextChars, editor.myName));
        }
    }
}
