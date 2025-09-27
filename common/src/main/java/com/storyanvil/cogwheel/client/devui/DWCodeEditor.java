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
import com.storyanvil.cogwheel.network.devui.*;
import com.storyanvil.cogwheel.util.Bi;
import com.storyanvil.cogwheel.util.StoryUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.HashMap;

public class DWCodeEditor extends DWTabbedView.Tab {
    private static final HashMap<Identifier, DWCodeEditor> editors = new HashMap<>();

    private Identifier rl;
    private String name;

    public static DWCodeEditor getOrCreateEditor(Identifier script) {
        if (editors.containsKey(script))
            return editors.get(script);
        DWCodeEditor editor = new DWCodeEditor(script);
        editors.put(script, editor);
        DevUI.instance.tabs.openAndSelect(editor);
        return editor;
    }

    private DWCodeEditor(Identifier rl) {
        this.rl = rl;
        this.name = rl.toString();

        String path = rl.getPath();
        highlighter = getHighlighterByFileName(path);
        myName = MinecraftClient.getInstance().player.getNameForScoreboard();
    }

    public static Highlighter getHighlighterByFileName(String name) {
        if (name.endsWith(".sad")) {
            return new DevHighlighters.StoryAnvilDialog();
        } else if (name.endsWith(".sa")) {
            return new DevHighlighters.CogScript();
        } else if (name.endsWith(".json") || name.endsWith(".saui")) {
            return new DevHighlighters.JSON();
        } else {
            return new DevHighlighters.Empty();
        }
    }

    private ArrayList<Bi<String, MutableText>> code = new ArrayList<>();
    private ArrayList<Cursor> cursors = new ArrayList<>();
    private int scroll = 0;
    private int codeLeft = 0;
    private int scrollHor = 0;
    private boolean shift = false;
    private Highlighter highlighter;
    private String myName;
    private Cursor mine = null;

    private float blinker = 0f;
    private boolean blink = false;

    public static DWCodeEditor get(Identifier lc) {
        return editors.get(lc);
    }

    public static void delete(Identifier lc) {
        DWCodeEditor editor = editors.get(lc);
        DevUI.instance.tabs.close(editor);
        editors.remove(lc);
    }

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
        blinker += partialTick;
        if (blinker > 10f) {blinker = 0f;blink =!blink;}
        for (int i = 0; i < cursors.size(); i++) {
            Cursor c = cursors.get(i);
            int t = ui().font.fontHeight * (c.line - scroll) + top;
            int drawingLeft = c.drawingLeft + codeLeft;
            int right1 = drawingLeft + 1;
            int bottom1 = t + ui().font.fontHeight;
            if (blink)
                fill(g, drawingLeft, t, right1, bottom1, c.color);
            if (StoryUtils.isHovering(mouseX, mouseY, drawingLeft - 2, right1 + 2, t - 2, bottom1 + 2)) {
                String tooltip = c.name;
                int tooltipWidth = ui().font.getWidth(tooltip) + 10;
                int tooltipHeight = ui().font.fontHeight + 10;
                fill(g, mouseX, mouseY, mouseX + tooltipWidth, mouseY + tooltipHeight, c.color);
                draw(g, mouseX + 5, mouseY + 5, tooltip, ui().font);
            }
        }
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
    private static final MutableText NODATA = Text.literal("NO DATA").formatted(Formatting.RED, Formatting.BOLD);
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
        recomputeCursors();
    }

    private synchronized Cursor getCursor(String name) {
        for (int i = 0; i < cursors.size(); i++) {
            Cursor c = cursors.get(i);
            if (c.name.equals(name))
                return c;
        }
        return null;
    }

    public synchronized void handle(DevEditorLine devEditorLine) {
        if (devEditorLine.linesTotal() != code.size()) {
            if (code.size() > devEditorLine.linesTotal()) {
                CogwheelNetwork.sendToServer(new DevEditorState(rl, (byte) -127));
                return;
            } else {
                while (code.size() < devEditorLine.linesTotal())
                    code.add(new Bi<>("", Text.empty()));
            }
        }
        Bi<String, MutableText> line = code.get(devEditorLine.lineNumber());
        if (line.getA().equals(devEditorLine.line())) return;
        line.setA(devEditorLine.line());
        highlight(devEditorLine.lineNumber());
        recomputeCursors();
    }

    public synchronized void handle(DevEditorUserDelta delta) {
        Cursor c = getCursor(delta.name());
        if (c == null) {
            c = new Cursor();
            c.editor = this;
            c.name = delta.name();
            cursors.add(c);
            if (delta.name().equals(myName)) {
                mine = c;
            }
        }
        c.line = delta.line();
        c.pos = net.minecraft.util.math.MathHelper.clamp(delta.pos(), 0, code.get(delta.line()).getA().length());
        c.selectNextChars = delta.selected();
        c.color = delta.color();
//        c.compute(code.get(c.line));
        recomputeCursors();
    }

    public synchronized void recomputeCursors() {
        for (int i = 0; i < cursors.size(); i++) {
            Cursor c = cursors.get(i);
            c.compute(code.get(c.line));
        }
    }

    @Override
    public boolean closingRequest() {
        CogwheelNetwork.sendToServer(new DevEditorState(rl, (byte) -128));
        return true;
    }

    @Override
    public boolean keyPressed(int code, int scanCode, int mods) {
        shift = (mods & GLFW.GLFW_MOD_SHIFT) == GLFW.GLFW_MOD_SHIFT;
        if (code == GLFW.GLFW_KEY_LEFT) {
            if (mine.wrapLeft())
                mine.setPosSafe(mine.pos - 1);
        } else if (code == GLFW.GLFW_KEY_RIGHT) {
            if (mine.wrapRight())
                mine.setPosSafe(mine.pos + 1);
        } else if (code == GLFW.GLFW_KEY_UP) {
            mine.setLineSafe(mine.line - 1);
        } else if (code == GLFW.GLFW_KEY_DOWN) {
            mine.setLineSafe(mine.line + 1);
        } else if (code == GLFW.GLFW_KEY_BACKSPACE) {
            CogwheelNetwork.sendToServer(new DevTypeCallback(rl, "<backspace>", mine.toDelta()));
        } else if (code == GLFW.GLFW_KEY_DELETE) {
            CogwheelNetwork.sendToServer(new DevTypeCallback(rl, "<delete>", mine.toDelta()));
        } else if (code == GLFW.GLFW_KEY_HOME) {
            mine.setPosSafe(0);
        } else if (code == GLFW.GLFW_KEY_END) {
            mine.setPosSafe(Integer.MAX_VALUE);
        } else if (code == GLFW.GLFW_KEY_PAGE_UP) {
            mine.setLineSafe(0);
            mine.setPosSafe(0);
        } else if (code == GLFW.GLFW_KEY_PAGE_DOWN) {
            mine.setLineSafe(Integer.MAX_VALUE);
            mine.setPosSafe(Integer.MAX_VALUE);
        } else if (code == GLFW.GLFW_KEY_S && (mods & GLFW.GLFW_MOD_CONTROL) == GLFW.GLFW_MOD_CONTROL) {
            save();
            return true;
        } else if (code == GLFW.GLFW_KEY_ENTER) {
            CogwheelNetwork.sendToServer(new DevEnterCallback(rl, "enter", mine.toDelta()));
        }
        return false;
    }

    @Override
    public boolean keyReleased(int pKeyCode, int pScanCode, int pModifiers) {
        shift = (pModifiers & GLFW.GLFW_MOD_SHIFT) != GLFW.GLFW_MOD_SHIFT;
        return false;
    }

    @Override
    public boolean charTyped(char c, int mods) {
        if (c == '`') return true;
        CogwheelNetwork.sendToServer(new DevTypeCallback(rl, ""+c, mine.toDelta()));
        return true;
    }

    public synchronized void handle(DevInsertLine delta) {
        code.add(delta.lineBefore() + 1, new Bi<>(delta.contents(), Text.empty()));
        highlight(delta.lineBefore() + 1);
        recomputeCursors();
    }

    public void handle(DevDeleteLine delta) {
        code.remove(delta.line());
        recomputeCursors();
    }

    public abstract static class Highlighter {
        public abstract MutableText highlight(int lineNumber, ArrayList<Bi<String, MutableText>> code, String line);
    }
    public static class Cursor {
        private DWCodeEditor editor;
        private int line = 0;
        private int pos = 0;
        private int selectNextChars = 0;
        private String name = ">huynya<";
        private Text sup;
        private int color = 0;

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

        public void compute(Bi<String, MutableText> line) {
            sup = StoryUtils.subText(line.getB(), 0, pos + 1);
            drawingLeft = editor.ui().font.getWidth(sup);
            onEndOfLine = pos == line.getA().length();
        }

        public boolean wrapRight() {
            if (onEndOfLine) {
                pos = 0;
                setLineSafe(line + 1);
                return false;
            }
            return true;
        }
        public boolean wrapLeft() {
            if (pos == 0 && line > 0) {
                line--;
                Bi<String, MutableText> prv = editor.code.get(line);
                pos = prv.getA().length();
                compute(prv);
                sync();
                return false;
            }
            return true;
        }

        public void setPosSafe(int pos) {
            Bi<String, MutableText> line = editor.code.get(this.line);
            this.pos = MathHelper.clamp(pos, 0, line.getA().length());
            compute(line);
            sync();
        }

        public void setLineSafe(int line) {
            line = MathHelper.clamp(line, 0, editor.code.size() - 1);
            this.line = line;
            setPosSafe(this.pos);
        }

        public void sync() {
            editor.blink = true;
            editor.blinker = 0f;
            CogwheelNetwork.sendToServer(new DevEditorUserDelta(editor.rl, line, pos, selectNextChars, editor.myName, color));
        }

        public DevEditorUserDelta toDelta() {
            return new DevEditorUserDelta(editor.rl, line, pos, selectNextChars, editor.myName, color);
        }
    }

    public void run() {
        CogwheelNetwork.sendToServer(new DevRunAndFlush(rl));
    }
    public void save() {
        CogwheelNetwork.sendToServer(new DevFlush(rl));
    }
}
