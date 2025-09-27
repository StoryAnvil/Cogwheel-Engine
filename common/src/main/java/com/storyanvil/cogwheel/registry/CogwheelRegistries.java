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

package com.storyanvil.cogwheel.registry;

import com.storyanvil.cogwheel.CogwheelEngine;
import com.storyanvil.cogwheel.CogwheelExecutor;
import com.storyanvil.cogwheel.api.Api;
import com.storyanvil.cogwheel.infrastructure.*;
import com.storyanvil.cogwheel.infrastructure.cog.*;
import com.storyanvil.cogwheel.infrastructure.env.CogScriptEnvironment;
import com.storyanvil.cogwheel.infrastructure.module.CogModule;
import com.storyanvil.cogwheel.infrastructure.script.DispatchedScript;
import com.storyanvil.cogwheel.util.*;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Function;

import static com.storyanvil.cogwheel.CogwheelEngine.MODID;

public class CogwheelRegistries {
    private static final List<Bi<String, Function<DispatchedScript, CGPM>>> defaultVariables = new ArrayList<>();
    private static final ArrayList<Bi<ScriptLineHandler, Boolean>> lineHandlers = new ArrayList<>();
    /**
     * Registries ScriptLineHandler
     * @apiNote Namespaces <code>storyanvil</code> and <code>storyanvil_cogwheel</code> reserved for internal purposes and cannot be used
     * @param factory factory that will be registered
     */
    @Api.Stable(since = "2.0.0") @Api.MixinsNotAllowed(where = "NOWHERE")
    public static void register(@NotNull ScriptLineHandler factory) {
        synchronized (lineHandlers) {
            Identifier id = factory.getIdentifier();
            if (id.getNamespace().equals("storyanvil") || id.getNamespace().equals(MODID))
                throw new IllegalArgumentException("ActionFactory with namespace \"" + id.getNamespace() + "\" cannot be registered as this namespace is reserved for internal purposes");
            lineHandlers.add(new Bi<>(Objects.requireNonNull(factory), true));
        }
    }

    @Api.Internal @ApiStatus.Internal @Api.MixinsNotAllowed(where = "NOWHERE")
    protected static void registerInternal(@NotNull ScriptLineHandler factory) {
        synchronized (lineHandlers) {
            lineHandlers.add(new Bi<>(Objects.requireNonNull(factory), true));
        }
    }
    /**
     * Registries Default Variable
     * @apiNote Name must not be "StoryAnvil" or "CogWheel"
     */
    @Api.Stable(since = "2.0.0") @Api.MixinsNotAllowed(where = "NOWHERE")
    public static void register(@NotNull String name, @NotNull Function<DispatchedScript, CGPM> f) {
        synchronized (defaultVariables) {
            if (name.equalsIgnoreCase("storyanvil") || name.equalsIgnoreCase("cogwheel")) throw new IllegalArgumentException("Name not permitted");
            defaultVariables.add(new Bi<>(name, f));
        }
    }

    @Api.Internal @ApiStatus.Internal @Api.MixinsNotAllowed(where = "NOWHERE")
    protected static void registerInternal(@NotNull String name, @NotNull Function<DispatchedScript, CGPM> f) {
        synchronized (defaultVariables) {
            defaultVariables.add(new Bi<>(name, f));
        }
    }

    @Contract(pure = true)
    @Api.Internal @ApiStatus.Internal @Api.MixinsNotAllowed(where = "NOWHERE")
    public static List<Bi<ScriptLineHandler, Boolean>> getLineHandlers() {
        return lineHandlers;
    }

    @Api.Internal @ApiStatus.Internal @Api.MixinsNotAllowed(where = "NOWHERE")
    public static void registerDefaultObjects() {
        registerInternal(new ScriptLineHandler() {
            @Override
            public byte handle(@NotNull String line, @NotNull DispatchedScript script) {
                if (line.startsWith("#") || line.isEmpty()) return ScriptLineHandler.continueReading();
                return ScriptLineHandler.ignore();
            }

            @Override
            public @NotNull Identifier getIdentifier() {
                return Identifier.of(MODID, "comment");
            }
        });
        registerInternal(new ScriptLineHandler() {
            @Override
            public byte handle(@NotNull String line, @NotNull DispatchedScript script) {
                if (line.startsWith("if (") && line.endsWith("{")) {
                    int endBracket = line.lastIndexOf(')');
                    String expression = line.substring(4, endBracket);
                    Bi<Byte, CGPM> out = expressionHandler(expression, script, false);
                    if (out.getB() instanceof CogBool bool) {
                        if (bool.getValue()) {
                            // Remove closing bracket of this IF
                            int level = 0;
                            for (int i = 0; i < script.linesLeft(); i++) {
                                String l = script.peekLine(i);
                                if (l.endsWith("{")) {
                                    level++;
                                } else if (l.equals("}")) {
                                    level--;
                                    if (level <= -1) {
                                        script.removeLine(i);
                                        break;
                                    }
                                }
                            }
                            return ScriptLineHandler.continueReading();
                        }
                        // Skip this IF
                        skipCurrentLevel(script);
                        return ScriptLineHandler.continueReading();
                    } else throw new CogExpressionFailure("If expression returned non-CogBool");
                }
                return ScriptLineHandler.ignore();
            }

            @Override
            public @NotNull Identifier getIdentifier() {
                return Identifier.of(MODID, "if");
            }
        });
        registerInternal(new ScriptLineHandler() {
            @Override
            public byte handle(@NotNull String line, @NotNull DispatchedScript script) {
                if (!line.startsWith("for (") || !line.endsWith(") {"))
                    return ScriptLineHandler.ignore();
                line = line.substring(5, line.length() - 3);
                int in = indexOfKeyword(line, "in");
                String left = line.substring(0, in).trim();
                String right = line.substring(in + 2).trim();
//                CogwheelExecutor.log.warn("{} >|{}|<  = {}:{}", line, in, left, right);
                CGPM arr = expressionHandler(right, script, false).getB();
                if (arr instanceof ForEachManager manager) {
                    Object track = manager.createForEach(script);
                    Bi<CGPM, Object> m = manager.getForEach(track);
                    if (m == null) {
                        skipCurrentLevel(script);
                        return ScriptLineHandler.continueReading();
                    }
                    track = m.getB();
                    script.put(left, m.getA());

                    int endLine = -1;
                    int level = 0;
                    for (int i = 0; i < script.linesLeft(); i++) {
                        String l = script.peekLine(i);
                        if (l.endsWith("{")) {
                            level++;
                        } else if (l.equals("}")) {
                            level--;
                            if (level <= -1) {
                                script.defuseLine(i);
                                endLine = i - 1;
                                break;
                            }
                        }
                    }
                    if (endLine == -1) throw new CogExpressionFailure("Foreach tail bracket mismatch!");
                    script.stopLineUnloading();
                    script.plantHandler(new ForEachInternal(track, manager, left, endLine), endLine);
                } else throw new CogExpressionFailure("Foreach argument is not a ForEachManager!");
                return ScriptLineHandler.continueReading();
            }

            @Override
            public @NotNull Identifier getIdentifier() {
                return Identifier.of(MODID, "foreach");
            }
        });
        registerInternal(new ScriptLineHandler() {
            @Override
            public byte handle(@NotNull String line, @NotNull DispatchedScript script) throws Exception {
                if (!line.startsWith("*")) return ScriptLineHandler.ignore();
                String library;
                boolean reload = false;
                if (line.startsWith("*import ")) {
                    library = line.substring(8);
                } else if (line.startsWith("*reimport ")) {
                    library = line.substring(10);
                    reload = true;
                } else if (line.startsWith("*return ")) {
                    String expression = line.substring(8);
                    CGPM exp = expressionHandler(expression, script, false).getB();
                    script.put("$", exp);
                    script.clearLines();
                    return ScriptLineHandler.continueReading();
                } else throw new CogExpressionFailure("STAR Expression is invalid");
                Identifier loc;
                try {
                    loc = Identifier.tryParse(library + ".sam");
                } catch (Throwable t) {throw new CogExpressionFailure("STAR Expression is invalid", t);}
                CogScriptEnvironment environment = CogScriptEnvironment.getEnvironment(loc);
                Identifier modLoc = CogwheelExecutor.getDefaultEnvironment().getModuleLoc(environment, loc.getPath());
                CogModule module = CogwheelExecutor.getDefaultEnvironment().getModule(modLoc);
                if (!reload) reload = module == null;
                if (reload) {
                    module = CogModule.build(environment, loc.getPath());
                    CogwheelExecutor.getDefaultEnvironment().putModule(modLoc, module);
                }
                String actualName = loc.getPath().substring(0, loc.getPath().length() - 4);
                script.put(actualName.substring(0, 1).toUpperCase() + actualName.substring(1), module);
                return ScriptLineHandler.continueReading();
            }

            @Override
            public @NotNull Identifier getIdentifier() {
                return Identifier.of(MODID, "star");
            }
        });
        registerInternal(new ScriptLineHandler() {
            @Override
            public byte handle(@NotNull String _line, @NotNull DispatchedScript script) {
                Bi<Byte, CGPM> parseOutput = expressionHandler(_line, script, true);
                return parseOutput.getA();
            }

            @Override
            public @NotNull Identifier getIdentifier() {
                return Identifier.of(MODID, "expression_handler");
            }
        });

        registerInternal("Cogwheel", script -> CogMaster.getInstance());
        registerInternal("MANIFEST", script -> CogManifest.getInstance());
        registerInternal("true", script -> CogBool.TRUE);
        registerInternal("false", script -> CogBool.FALSE);
    }

    private static void skipCurrentLevel(@NotNull DispatchedScript script) {
        int level = 0;
        String l = script.pullLine();
        while (l != null) {
            if (l.endsWith("{")) {
                level++;
            } else if (l.equals("}")) {
                level--;
                if (level <= -1) break;
            }
            l = script.pullLine();
        }
    }

    public static void putDefaults(ScriptStorage storage, DispatchedScript script) {
        for (Bi<String, Function<DispatchedScript, CGPM>> pair : defaultVariables) {
            storage.put(pair.getA(), pair.getB().apply(script));
        }
    }
    @Contract("_, _, _ -> new")
    @SuppressWarnings("ExtractMethodRecommender")
    public static @NotNull Bi<Byte, CGPM> expressionHandler(String line, DispatchedScript script, boolean allowBlocking) {
        // Return: DoubleValue<ScriptLineHandler.*(), ExpressionReturn>
        line = line.trim();
        int currentStart = 0;

        // Variable search
        StringBuilder variable = new StringBuilder();
        boolean hasVariable = false;
        for (int i = currentStart; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '=') {
                hasVariable = true;
                variable = new StringBuilder(variable.toString().trim());
                currentStart = i + 1;
                break;
            } else if (c == '.') {
                variable = null;
                break;
            } else {
                variable.append(c);
            }
        }
        if (!hasVariable) {
            variable = null;
        }

        // Check for string and integer creations
        String leftLine = line.substring(currentStart).stripLeading();
        CGPM cm = null;
        if (leftLine.startsWith("\"") && leftLine.endsWith("\"")) {
            cm = new CogString(leftLine.substring(1, leftLine.length() - 1));
        } else if (leftLine.startsWith("^")) {
            if (!leftLine.endsWith("L")) {
                cm = new CogInteger(leftLine.substring(1));
            } else {
                cm = new CogLong(leftLine.substring(1, leftLine.length() - 1));
            }
        }

        if (cm != null) {
            if (variable != null) {
                script.put(variable.toString(), cm);
            }
            return new Bi<>(ScriptLineHandler.continueReading(), cm);
        }

        // Chain call search
        StringBuilder currentName = new StringBuilder();
        int depth = 0;
        ArrayList<String> chainCalls = new ArrayList<>();
        for (int i = currentStart; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '.' && depth == 0) {
                chainCalls.add(currentName.toString());
                currentName = new StringBuilder();
            } else if (c == '(') {
                depth++;
                currentName.append(c);
            } else if (c == ')') {
                depth--;
                if (depth < 0) {
                    throw new CogExpressionFailure("Closing bracket mismatch! \"" + line + "\"");
                }
                currentName.append(c);
            } else {
                currentName.append(c);
            }
        }
        if (depth == 0) {
            chainCalls.add(currentName.toString());
        } else {
            throw new CogExpressionFailure("Tail closing bracket mismatch! \"" + line + "\"");
        }

        if (chainCalls.getFirst().endsWith(")")) {
            throw new CogExpressionFailure("Expression does not have first step! \"" + line + "\"");
        }
//        if (chainCalls.size() == 1) {
//            CogwheelEngine.EARLY.error("AAA!!! {} < {}", chainCalls, line);
//            cm = script.get(chainCalls.getFirst());
//            if (variable != null) {
//                script.put(variable.toString(), cm);
//            }
//            return new Bi<>(ScriptLineHandler.continueReading(), cm);
//        }
        @NotNull CGPM manager = CGPM.noNull(script.get(chainCalls.getFirst().stripLeading()));
        for (int i = 1 /* do not take first step */; i < chainCalls.size(); i++) {
            String step = chainCalls.get(i);
            if (!step.endsWith(")")) {
                throw new CogExpressionFailure("Invalid step: \"" + step + "\"! \"" + line + "\"");
            }
            int firstBracket = step.indexOf('(');
            String propName = step.substring(0, firstBracket);
            ArgumentData argumentData = ArgumentData.of(step.substring(firstBracket + 1, step.length() - 1), script);
            if (!manager.hasOwnProperty(propName)) {
                throw new CogExpressionFailure(manager.getClass().getCanonicalName() + " object does not have property \"" + propName + "\"! \"" + line + "\"");
            }
            try {
                manager = CGPM.noNull(manager.getProperty(propName, argumentData, script));
            } catch (PreventSubCalling preventSubCalling) {
                if (!allowBlocking) {
                    throw new CogExpressionFailure("SubCalling Prevention is not allowed in this context! \"" + line + "\"");
                }
                preventSubCalling.getPostPrevention().prevent(variable != null ? variable.toString() : null);
                return new Bi<>(ScriptLineHandler.blocking(), manager);
            } catch (RuntimeException e) {
                throw new RuntimeException("Expression handler caught while getting property. ArgData: " + argumentData, e);
            }
        }
        if (variable != null) {
            script.put(variable.toString(), manager);
        }
        return new Bi<>(ScriptLineHandler.continueReading(), manager);
    }
    public static void skip(String label, @NotNull DispatchedScript script) {
        String line = script.pullLine();
        while (!line.equals(label)) {
            line = script.pullLine();
            if (line == null) {
                throw new RuntimeException("Skip failed. No label found");
            }
        }
    }
    public static int indexOfKeyword(@NotNull String line, @NotNull String keyword) {
        boolean inQuotes = false;
        StringBuilder currentWord = new StringBuilder();
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ' ') {
                if (currentWord.toString().equals(keyword)) {
                    return i - currentWord.length();
                }
                currentWord = new StringBuilder();
            } else {
                currentWord.append(c);
            }
        }
        return -1;
    }

    @ApiStatus.Internal
    public static class ForEachInternal implements ScriptLineHandler {
        private final Object track;
        private final ForEachManager manager;
        private final String varName;
        private final int planningSchedule;

        @Contract(pure = true)
        public ForEachInternal(Object track, ForEachManager manager, String varName, int planningSchedule) {
            this.track = track;
            this.manager = manager;
            this.varName = varName;
            this.planningSchedule = planningSchedule;
        }

        @Override
        public byte handle(@NotNull String line, @NotNull DispatchedScript script) throws Exception {
            Bi<CGPM, Object> bi = manager.getForEach(track);
            if (bi == null) {
                script.continueUnloadingLines();
                script.removeUnloadedLines();
                return ScriptLineHandler.continueReading();
            }
            script.stopLineUnloading();
            script.put(varName, bi.getA());
            script.continueUnloadingLines();
            script.plantHandler(new ForEachInternal(bi.getB(), manager, varName, planningSchedule), planningSchedule);
            return ScriptLineHandler.continueReading();
        }

        @Override
        public @NotNull Identifier getIdentifier() {
            return Identifier.of(MODID, "foreach_internal/" + manager.getClass().getCanonicalName().toLowerCase() + "." + manager.hashCode() + "/" + track.getClass().getCanonicalName().toLowerCase() + "." + track.hashCode());
        }
    }
}
