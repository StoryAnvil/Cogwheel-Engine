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

package com.storyanvil.cogwheel.infrastructure.script;

import com.storyanvil.cogwheel.CogwheelHooks;
import com.storyanvil.cogwheel.api.Api;
import com.storyanvil.cogwheel.infrastructure.CGPM;
import com.storyanvil.cogwheel.infrastructure.abilities.DialogTarget;
import com.storyanvil.cogwheel.infrastructure.cog.CogInteger;
import com.storyanvil.cogwheel.infrastructure.cog.CogVarLinkage;
import com.storyanvil.cogwheel.infrastructure.env.CogScriptEnvironment;
import com.storyanvil.cogwheel.util.B;
import com.storyanvil.cogwheel.util.CogExpressionFailure;
import com.storyanvil.cogwheel.util.ScriptStorage;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static com.storyanvil.cogwheel.CogwheelExecutor.log;

public class DialogScript extends StreamExecutionScript {
    private ArrayList<String> dialogsToExecute;
    public DialogScript(ArrayList<String> linesToExecute, ScriptStorage storage, CogScriptEnvironment environment) {
        super(storage, environment);
        dialogsToExecute = linesToExecute;
    }

    public DialogScript(ArrayList<String> linesToExecute, CogScriptEnvironment environment) {
        super(environment);
        dialogsToExecute = linesToExecute;
    }

    public void startDialog() {
        if (!Thread.currentThread().getName().contains("cogwheel-executor")) {
            RuntimeException e = new RuntimeException("Line dispatcher can only be run in cogwheel executor thread");
            e.printStackTrace();
            log.error("[!CRITICAL!] LINE DISPATCHER WAS CALLED FROM NON-EXECUTOR THREAD! THIS WILL CAUSE MEMORY LEAKS AND PREVENT SCRIPTS FOR PROPER EXECUTION! THIS CALL WAS DISMISSED, PROBABLY CAUSING A MEMORY LEAK!");
            throw e;
        }
        internalDialogStart();
    }

    private int executingLine = 0;
    private void internalDialogStart() {
        while (!dialogsToExecute.isEmpty()) {
            String line = dialogsToExecute.get(executingLine);
            executingLine++;
            try {
                if (handleLine(line)) break;
            } catch (Throwable e) {
                e.printStackTrace();
                throw e;
            }
        }
    }

    private int currentLevel = 0;
    private boolean endToStart = false;
    private List<Integer> optionLines = null;

    @Api.MixinsNotAllowed(where = "DialogScript#mixinEntrypoint")
    private boolean handleLine(String line) {
        int level = 0;
        for (int i = 0; i < line.length(); i++) {
            if (line.charAt(i) == ' ') {
                level++;
            } else break;
        }
        if (currentLevel == -1) currentLevel = level;
        if (level < currentLevel) throw new CogExpressionFailure("Level became smaller [you can ignore this message if dialog works as intended]: " + line);
        String code = line.substring(level);
        switch (code.charAt(0)) {
            case '#' -> {break;}
            case '+' -> {
                currentLevel++;
                break;
            }
            case '!' -> {
                String l = code.substring(1);
                if (addLine(l)) {
                    endToStart = true;
                    return true;
                }
                break;
            }
            case '/' -> {
                CogwheelHooks.executeCommand(code.substring(1));
            }
            case '@' -> {
                char act = ' ';
                int actPlace = -1;
                for (int i = 0; i < code.length(); i++) {
                    char c = code.charAt(i);
                    if (c == ':' || c == '?' || c == '!') {
                        act = c;
                        actPlace = i; break;
                    }
                }
                CGPM unchecked = super.get(code.substring(1, actPlace));
                if (unchecked instanceof DialogTarget dialogTarget) {
                    if (act == ':') {
                        B<Integer> ticks = new B<>();
                        String f = format(code.substring(actPlace + 1), ticks);
                        ticks.putIfNull(this.computeTicks(f));
                        dialogTarget.d$say(f, dialogTarget.d$name(), ticks.get(), this::startDialog);
                        return true;
                    } else if (act == '!') {
                        B<Integer> ticks = new B<>();
                        String f = super.fastExecute(code.substring(actPlace + 1)).convertToString();
                        ticks.putIfNull(this.computeTicks(f));
                        dialogTarget.d$say(f, dialogTarget.d$name(), ticks.get(), this::startDialog);
                        return true;
                    } else if (act == '?') {
                        ArrayList<String> optionsAvailable = new ArrayList<>();
                        optionLines = new ArrayList<>();
                        String mask = " ".repeat(level + 1) + "+ ";
                        for (int i = executingLine; i < dialogsToExecute.size(); i++) {
                            String L = dialogsToExecute.get(i);
                            if (L.startsWith(mask)) {
                                optionLines.add(i);
                                optionsAvailable.add(format(L.substring(mask.length()), null));
                            }
                        }
                        dialogTarget.d$ask(format(code.substring(actPlace + 1), null), dialogTarget.d$name(), optionsAvailable, this::d$response);
                        return true;
                    }
                } else throw new CogExpressionFailure("Dialog Action target is not valid: " + line);
                break;
            }
            default -> {
                if (!mixinEntrypoint(line, code, level))
                    throw new CogExpressionFailure("Unknown line type: " + line);
            }
        }
        return false;
    }

    /**
     * Allows adding custom handlers to dialog scripts
     * @param line Entire line of code
     * @param code Line of code without leading whitespaces
     * @param level Amount of leading whitespaces
     * @return true if line was handles. false if line was not recognised
     */
    @Api.MixinIntoHere
    public boolean mixinEntrypoint(String line, String code, int level) {
        return false;
    }

    public static int computeTicks(String text) {
        return MathHelper.clamp(15 + text.split(" ").length * 10, 15, 2000);
    }

    private @NotNull String format(@NotNull String raw, @Nullable B<Integer> ticks) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < raw.length(); i++) {
            char c = raw.charAt(i);
            if (c == '$' && i + 3 < raw.length() && raw.charAt(i + 1) == '{') {
                int level = 0;
                boolean quotes = false;
                StringBuilder expr = new StringBuilder();
                for (int j = i + 2; j < raw.length(); j++) {
                    char $ = raw.charAt(j);
                    if ($ == '"') {
                        quotes = !quotes;
                    } else if ($ == '{') {
                        level++;
                    } else if ($ == '}') {
                        level--;
                        if (level < 0) {
                            i = j;
                            break;
                        }
                    }
                    expr.append($);
                }
                super.put("ticks", new CogVarLinkage("$ticks"));
                CGPM m = super.fastExecute(expr.toString());
                sb.append(m.convertToString());
                if (ticks != null && super.get("$ticks") instanceof CogInteger t) {
                    ticks.set(t.getValue());
                }
                super.put("ticks", null);
                continue;
            }
            sb.append(c);
        }
        return sb.toString();
    }

    @Override
    public void startExecution() {
        startDialog();
    }

    @Override
    public void onEnd() {
        if (endToStart) {
            endToStart = false;
            startDialog();
        }
    }

    public void d$response(int id) {
        currentLevel = -1;
        executingLine = optionLines.get(id);
        optionLines = null;
        startDialog();
    }
}
