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

import com.storyanvil.cogwheel.network.devui.*;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Contract;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class DWConsoleAutoComplete {
    private static final List<DWConsoleChoice> emptyImmutable = List.of();
    private static List<DWConsoleChoice> fullChoicePool = null;
    private static List<DWConsoleChoice> eraseChoicePool;
    private static List<DWConsoleChoice> nextChoicePool;
    private static String old;

    public static void init() {
        if (fullChoicePool != null) return;
        fullChoicePool = new ArrayList<>();
        eraseChoicePool = emptyImmutable;
        nextChoicePool = fullChoicePool;
        old = "";

        register("!", "<code>", "Executes CogScript code", Actions::cogScript);
        register("O", "<env>:<file name>", "Opens file", Actions::openFile);
        register("open", "<env>:<file name>", "Opens file", Actions::openFile);
        register("dispatch", "<env>:<file name>", "Dispatches file", Actions::dispatchFile);
        register("fullscreen", "Toggle fullscreen mode", Actions::fullscreen);
        register("resync", "DevUI Resync", Actions::resync);
        register("inspector", "Gives inspector tool", Actions::inspector);
        register("save", "Saves currently open file", Actions::save);
        register("run", "Executes currently open file", Actions::run);
        register("gmc", "Sets gamemode to creative", Actions::creative);
        register("gms", "Sets gamemode to survival", Actions::survival);
        register("gma", "Sets gamemode to adventure", Actions::adventure);
        register("closeall", "Closes all tabs", Actions::closeall);
    }

    public static void register(String mask, String args, String desc, Consumer<String> executor) {
        fullChoicePool.add(new DWConsoleChoice(mask, args, desc, executor));
    }
    public static void register(String mask, String desc, Consumer<String> executor) {
        fullChoicePool.add(new DWConsoleChoice(mask, desc, executor));
    }

    public static void empty() {
        eraseChoicePool = emptyImmutable;
        nextChoicePool = fullChoicePool;
        old = "";
        if (DevUI.instance != null && DevUI.instance.console != null && DevUI.instance.console.editBox != null)
            DevUI.instance.console.editBox.setValue("");
        rerenderChoices(old);
    }

    public static void execute(String value) {
        if (nextChoicePool.size() == 1) {
            DWConsoleChoice choice = nextChoicePool.get(0);
            if (!value.startsWith(choice.getMask())) return;
            int l = choice.getMask().length();
            choice.getExecutor().accept(value.substring(l > choice.getMask().length() ? l + 1 : l));
            DWConsoleAutoComplete.empty();
            DevUI.instance.drawConsole = false;
        }
    }

    public static void update(String value) {
        setEditable(false);
        int space = value.indexOf(' ');
        value = value.substring(0, space == -1 ? value.length() : space);
        if (value.isEmpty()) {
            empty();
            return;
        }
        int oldSize = old.length();
        int size = value.length();
        if (size - oldSize == 1 && value.startsWith(old)) {
            // One character got added
            char added = value.charAt(size - 1);
            int checkIndex = size - 1;
            ArrayList<DWConsoleChoice> applicable = new ArrayList<>();
            for (DWConsoleChoice choice : nextChoicePool) {
                if (choice.getMask().length() >= size && choice.getMask().charAt(checkIndex) == added) {
                    applicable.add(choice);
                }
            }
            eraseChoicePool = nextChoicePool;
            nextChoicePool = applicable;
        } else if (size < oldSize && oldSize - size == 1 && old.startsWith(value)) {
            // One character got removed
            char removed = old.charAt(size - 1);
            String checkFor = value.substring(0, value.length() - 1);
            ArrayList<DWConsoleChoice> applicable = new ArrayList<>();
            for (DWConsoleChoice choice : fullChoicePool) {
                if (choice.getMask().startsWith(checkFor)) {
                    applicable.add(choice);
                }
            }
            nextChoicePool = eraseChoicePool;
            eraseChoicePool = applicable;
        } else recompute(value);
//        recompute(value);
        rerenderChoices(value);
        old = value;
    }

    private static void setEditable(boolean pEnabled) {
        if (DevUI.instance == null || DevUI.instance.console == null) return;
        DevUI.instance.console.setEditable(pEnabled);
    }

    private static void recompute(String value) {
        ArrayList<DWConsoleChoice> nextApplicable = new ArrayList<>();
        ArrayList<DWConsoleChoice> eraseApplicable = new ArrayList<>();
        String eraseMask = value.substring(0, value.length() - 1);
        int checkPos = value.length() - 1;
        char check = value.charAt(checkPos);

        for (DWConsoleChoice choice : fullChoicePool) {
            if (choice.getMask().startsWith(eraseMask)) {
                eraseApplicable.add(choice);
                if (choice.getMask().length() > checkPos && choice.getMask().charAt(checkPos) == check) {
                    nextApplicable.add(choice);
                }
            }
        }
        nextChoicePool = nextApplicable;
        eraseChoicePool = eraseApplicable;
    }

    private static void rerenderChoices(String q) {
        if (DevUI.instance.console == null) return;
        List<Component> renders = DevUI.instance.console.getTips();
        renders.clear();
        if (nextChoicePool.isEmpty()) {
            renders.add(Component.literal(" ⚠ No results for this query!").withStyle(ChatFormatting.RED));
        } else {
            int start = q.length();
            int limit = 10;
            for (DWConsoleChoice choice : nextChoicePool) {
                limit--;
                if (limit == 0) {
                    renders.add(Component.literal("... and other " + (nextChoicePool.size() - 9) + " results").withStyle(ChatFormatting.GRAY));
                    break;
                }
                renders.add(Component.literal(q).withStyle(ChatFormatting.YELLOW).append(
                        Component.literal(choice.getMask().substring(Math.min(start, choice.getMask().length()))).withStyle(ChatFormatting.GRAY)
                ).append(
                        Component.literal(choice.getArguments()).withStyle(ChatFormatting.DARK_GRAY)
                ).append(
                        Component.literal(choice.getDescription()).withStyle(ChatFormatting.WHITE)
                ));
            }
        }
        setEditable(true);
    }

    @ParametersAreNonnullByDefault
    public static class Actions {
        @Contract(pure = true) private static DevUI ui() {return DevUI.instance;}

        public static void fullscreen(String s) {
            ui().fullscreen = !ui().fullscreen;
            ui().scheduleResize();
        }
        public static void resync(String s) {
            DevNetwork.sendToServer(new DevEarlySyncPacket(true, false));
            DevNetwork.sendToServer(new DevResyncRequest());
        }

        public static void inspector(String s) {
            DevNetwork.sendToServer(new DevConsoleCode("Cogwheel.executeCommand(\"give " + Minecraft.getInstance().player.getScoreboardName() + " storyanvil_cogwheel:inspector\")"));
        }

        public static void cogScript(String s) {
            DevNetwork.sendToServer(new DevConsoleCode(s));
        }

        public static void openFile(String s) {
            DevNetwork.sendToServer(new DevOpenFile(ResourceLocation.parse(s.trim())));
        }

        public static void save(String s) {
            if (ui().tabs.selected instanceof DWCodeEditor editor) {
                editor.save();
            }
        }

        public static void run(String s) {
            if (ui().tabs.selected instanceof DWCodeEditor editor) {
                editor.run();
            }
        }

        public static void creative(String s) {
            DevNetwork.sendToServer(new DevConsoleCode("Cogwheel.executeCommand(\"gamemode creative " + Minecraft.getInstance().player.getScoreboardName() + "\")"));
        }

        public static void survival(String s) {
            DevNetwork.sendToServer(new DevConsoleCode("Cogwheel.executeCommand(\"gamemode survival " + Minecraft.getInstance().player.getScoreboardName() + "\")"));
        }

        public static void adventure(String s) {
            DevNetwork.sendToServer(new DevConsoleCode("Cogwheel.executeCommand(\"gamemode adventure " + Minecraft.getInstance().player.getScoreboardName() + "\")"));
        }

        public static void closeall(String s) {
            ui().tabs.closeAll();
        }

        public static void dispatchFile(String s) {
            DevNetwork.sendToServer(new DevConsoleCode("Cogwheel.executeCommand(\"@storyanvil dispatch-script " + s.trim() + "\")"));
        }
    }
}
