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

import com.storyanvil.cogwheel.util.Bi;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public class DevHighlighters {
    public static class ColorHelper {
        private String line;
        private ColorSup[] colors;

        public ColorHelper(String line) {
            this.line = line;
            this.colors = new ColorSup[line.length()];
            Arrays.fill(colors, CodeColor.WHITE);
        }

        public void color(int from, int to, ColorSup color) {
            for (int i = from; i < to && i < colors.length; i++) {
                colors[i] = color;
            }
        }
        public void colorSingle(int i, ColorSup colorSup) {
            colors[i] = colorSup;
        }
        public MutableComponent compile() {
            MutableComponent cp = Component.empty().withStyle(ChatFormatting.RESET);
            Integer color = null;
            ColorSup sup = null;
            int from = 0;
            for (int i = 0; i < colors.length; i++) {
                int c = colors[i].get();
                if (color == null) {
                    color = c;
                    sup = colors[i];
                    from = i;
                    continue;
                }
                if (color != c) {
                    MutableComponent n = Component.literal(line.substring(from, i));
                    Integer finalColor = color;
                    ColorSup finalSup = sup;
                    n.withStyle(s -> finalSup.apply(s).withColor(finalColor));
                    cp.append(Component.empty().withStyle(ChatFormatting.RESET)).append(n);
                    color = c;
                    from = i;
                    sup = colors[i];
                }
            }
            MutableComponent n = Component.literal(line.substring(from));
            if (sup != null) {
                Integer finalColor = color;
                ColorSup finalSup1 = sup;
                n.withStyle(s -> finalSup1.apply(s).withColor(finalColor));
            }
            cp.append(Component.empty().withStyle(ChatFormatting.RESET)).append(n);
            return cp;
        }
    }
    @SuppressWarnings("DataFlowIssue")
    public enum CodeColor implements ColorSup {
        WHITE(ChatFormatting.WHITE.getColor()),
        STRING(ChatFormatting.DARK_GREEN.getColor()),
        COMMENT(ChatFormatting.DARK_GRAY.getColor()),
        ERROR(ChatFormatting.RED.getColor()){
            @Override
            public Style apply(Style style) {
                return style.withStrikethrough(true);
            }
        },
        VARIABLE(ChatFormatting.BLUE.getColor()){
            @Override
            public Style apply(Style style) {
                return style.withUnderlined(true);
            }
        },
        KEYWORD(ChatFormatting.GOLD.getColor()),
        SPECIAL(ChatFormatting.AQUA.getColor()),
        NUMBER(ChatFormatting.DARK_AQUA.getColor());
        public final int color;
        CodeColor(int color) {
            this.color = color;
        }
        @Override
        public Integer get() {
            return color;
        }
    }
    public interface ColorSup extends Supplier<Integer>, UnaryOperator<Style> {
        @Override
        default Style apply(Style style) {
            return style;
        }
    }
    public static class Empty extends DWCodeEditor.Highlighter {
        @Override
        public MutableComponent highlight(int lineNumber, ArrayList<Bi<String, MutableComponent>> code, String line) {
            return Component.literal(line);
        }
    }
    public static class StoryAnvilDialog extends DWCodeEditor.Highlighter {
        @Override
        public MutableComponent highlight(int lineNumber, ArrayList<Bi<String, MutableComponent>> code, String line) {
            ColorHelper helper = new ColorHelper(line);
            if (line.isBlank()) {
                return helper.compile();
            }
            int offset = 0;
            while (offset < line.length() - 1 && line.charAt(offset) == ' ') offset++;
            char c = line.charAt(offset);
            if (c == '!') {
                CogScript.process(helper, offset + 1, line.length(), line);
            } else if (c == '#') {
                helper.color(offset, line.length(), CodeColor.COMMENT);
            } else if (c == '@') {
                int sep = offset;
                char sepC;
                int bound = line.length() - 1;
                while (sep < bound) {
                    sep++;
                    sepC = line.charAt(sep);
                    if (sepC == ':' || sepC == '?' || sepC == '!') break;
                }
                helper.color(offset, sep, CodeColor.VARIABLE);
                helper.color(sep, sep + 1, CodeColor.KEYWORD);
            } else if (c == '+') {
                helper.color(offset, offset + 1, CodeColor.SPECIAL);
            } else if (c == '/') {
            } else {
                helper.color(offset, line.length(), CodeColor.ERROR);
            }

            return helper.compile();
        }
    }
    public static class CogScript extends DWCodeEditor.Highlighter {
        @Override
        public MutableComponent highlight(int lineNumber, ArrayList<Bi<String, MutableComponent>> code, String line) {
            ColorHelper helper = new ColorHelper(line);
            process(helper, 0, line.length(), line);
            return helper.compile();
        }

        public static void process(ColorHelper helper, int from, int to, String fullLine) {
            String sub = fullLine.substring(from, to);
//            helper.color(from, to, CodeColor.ERROR);
        }
    }
    public static class JSON extends DWCodeEditor.Highlighter {
        @Override
        public MutableComponent highlight(int lineNumber, ArrayList<Bi<String, MutableComponent>> code, String line) {
            ColorHelper helper = new ColorHelper(line);
            char prv = ' ';
            for (int i = 0; i < line.length(); i++) {
                char c = line.charAt(i);
                if (c == '"' && prv != '\\') {
                    char prv2 = ' ';
                    int ii;
                    for (ii = i + 1; ii < line.length(); ii++) {
                        char c2 = line.charAt(ii);
                        if (c2 == '"' && prv2 != '\\') {
                            break;
                        }
                        prv2 = c2;
                    }
                    ii++;
                    helper.color(i, ii, CodeColor.STRING);
                    i = ii;
                    prv = c;
                    continue;
                } else if (Character.isDigit(c)) {
                    helper.colorSingle(i, CodeColor.NUMBER);
                } else if (c == 'l') {
                    if (line.substring(i - 4, i + 1).equals(" null")) {
                        helper.color(i - 4, i + 1, CodeColor.KEYWORD);
                    }
                }
                prv = c;
            }
            return helper.compile();
        }
    }
}
