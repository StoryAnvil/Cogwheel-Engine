/*
 * StoryAnvil CogWheel Engine
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
import com.storyanvil.cogwheel.infrustructure.*;
import com.storyanvil.cogwheel.infrustructure.cog.*;
import com.storyanvil.cogwheel.util.*;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.storyanvil.cogwheel.CogwheelEngine.LOGGER;
import static com.storyanvil.cogwheel.CogwheelEngine.MODID;

public class CogwheelRegistries {
    private static final List<DoubleValue<String, Function<DispatchedScript, CogPropertyManager>>> defaultVariables = new ArrayList<>();
    private static final ArrayList<ScriptLineHandler> lineHandlers = new ArrayList<>();
    /**
     * Registries ScriptLineHandler
     * @apiNote Namespaces <code>storyanvil</code> and <code>storyanvil_cogwheel</code> reserved for internal purposes and cannot be used
     * @param factory factory that will be registered
     */
    public static void register(@NotNull ScriptLineHandler factory) {
        synchronized (lineHandlers) {
            ResourceLocation id = factory.getResourceLocation();
            if (id.getNamespace().equals("storyanvil") || id.getNamespace().equals(MODID))
                throw new IllegalArgumentException("ActionFactory with namespace \"" + id.getNamespace() + "\" cannot be registered as this namespace is reserved for internal purposes");
            lineHandlers.add(Objects.requireNonNull(factory));
        }
    }

    @ApiStatus.Internal
    protected static void registerInternal(@NotNull ScriptLineHandler factory) {
        synchronized (lineHandlers) {
            lineHandlers.add(Objects.requireNonNull(factory));
        }
    }
    /**
     * Registries Default Variable
     * @apiNote Name must not be "StoryAnvil" or "CogWheel"
     */
    public static void register(@NotNull String name, @NotNull Function<DispatchedScript, CogPropertyManager> f) {
        synchronized (defaultVariables) {
            if (name.equalsIgnoreCase("storyanvil") || name.equalsIgnoreCase("cogwheel")) throw new IllegalArgumentException("Name not permitted");
            defaultVariables.add(new DoubleValue<>(name, f));
        }
    }

    @ApiStatus.Internal
    protected static void registerInternal(@NotNull String name, @NotNull Function<DispatchedScript, CogPropertyManager> f) {
        synchronized (defaultVariables) {
            defaultVariables.add(new DoubleValue<>(name, f));
        }
    }

    @ApiStatus.Internal
    public static List<ScriptLineHandler> getLineHandlers() {
        return lineHandlers;
    }

    @ApiStatus.Internal
    public static void registerDefaultObjects() {
        registerInternal(new ScriptLineHandler() {
            @Override
            public @NotNull DoubleValue<Boolean, Boolean> handle(@NotNull String line, @Nullable String label, @NotNull DispatchedScript script) throws Exception {
                if (line.startsWith("#") || line.isEmpty()) return ScriptLineHandler.continueReading();
                return ScriptLineHandler.ignore();
            }

            @Override
            public @NotNull ResourceLocation getResourceLocation() {
                return ResourceLocation.fromNamespaceAndPath(MODID, "comment");
            }
        });
//        registerInternal(new ScriptLineHandler() {
//            @Override
//            public @NotNull DoubleValue<Boolean, Boolean> handle(@NotNull String _line, @Nullable String label, @NotNull DispatchedScript script) throws Exception {
//                if (!_line.startsWith("*")) return ScriptLineHandler.ignore();
//                String line = _line.substring(1);
//                int bracket = line.indexOf('(');
//                if (bracket == -1) return ScriptLineHandler.ignore();
//                String sub = line.substring(0, bracket + 1);
//                if (!methodLikes.containsKey(sub)) return ScriptLineHandler.ignore();
//                MethodLikeLineHandler handler = methodLikes.get(sub);
//                return handler.methodHandler(line.substring(sub.length(), line.length() - 1), label, script);
//            }
//
//            @Override
//            public @NotNull ResourceLocation getResourceLocation() {
//                return ResourceLocation.fromNamespaceAndPath(MODID, "method_like");
//            }
//        });
        registerInternal(new ScriptLineHandler() {
            @Override
            public @NotNull DoubleValue<Boolean, Boolean> handle(@NotNull String line, @Nullable String label, @NotNull DispatchedScript script) throws Exception {
                if (line.startsWith("if (") && line.endsWith("{")) {
                    int endBracket = line.lastIndexOf(')');
                    String expression = line.substring(4, endBracket);
                    DoubleValue<DoubleValue<Boolean, Boolean>, CogPropertyManager> out = expressionHandler(expression, script, false);
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
                        return ScriptLineHandler.continueReading();
                    } else throw new CogExpressionFailure("If expression returned non-CogBool");
                }
                return ScriptLineHandler.ignore();
            }

            @Override
            public @NotNull ResourceLocation getResourceLocation() {
                return ResourceLocation.fromNamespaceAndPath(MODID, "if");
            }
        });
        registerInternal(new ScriptLineHandler() {
            @Override
            public @NotNull DoubleValue<Boolean, Boolean> handle(@NotNull String _line, @Nullable String label, @NotNull DispatchedScript script) throws Exception {
                DoubleValue<DoubleValue<Boolean, Boolean>, CogPropertyManager> parseOutput = expressionHandler(_line, script, true);
                return parseOutput.getA();
            }

            @Override
            public @NotNull ResourceLocation getResourceLocation() {
                return ResourceLocation.fromNamespaceAndPath(MODID, "property_managers");
            }
        });
        registerInternal(new ScriptLineHandler() {
            @Override
            public @NotNull DoubleValue<Boolean, Boolean> handle(@NotNull String line, @Nullable String label, @NotNull DispatchedScript script) throws Exception {
                if (line.startsWith("*switch ")) {
                    CogPropertyManager var = script.get(line.substring(8));
                    if (var instanceof CogStringGen<?> gen) {
                        while (true) {
                            String l = script.peekLine();
                            int arrow = l.indexOf("->");
                            if (arrow == -1) break;
                            script.removeLine();
                            String left = l.substring(0, arrow).trim();
                            String right = l.substring(arrow + 2).trim();
                            if (left.equals("*")) {
                                skip("@marker " + right, script);
                                return ScriptLineHandler.continueReading();
                            }
                            CogStringGen<?> compareTo = gen.fromString(left);
                            if (compareTo == null) throw new RuntimeException("Unexpected switch left hand");
                            if (compareTo.equalsTo(gen)) {
                                skip("@marker " + right, script);
                                return ScriptLineHandler.continueReading();
                            }
                        }
                        return ScriptLineHandler.continueReading();
                    } else throw new RuntimeException("Switch variable");
                }
                return ScriptLineHandler.ignore();
            }

            @Override
            public @NotNull ResourceLocation getResourceLocation() {
                return ResourceLocation.fromNamespaceAndPath(MODID, "switch_case");
            }
        });
        registerInternal(new ScriptLineHandler() {
            @Override
            public @NotNull DoubleValue<Boolean, Boolean> handle(@NotNull String line, @Nullable String label, @NotNull DispatchedScript script) throws Exception {
                if (line.startsWith("@skipTo ")) {
                    skip("@marker " + line.substring(8), script);
                } else if (line.startsWith("@stop")) {
                    return ScriptLineHandler.blocking();
                }
                return ScriptLineHandler.ignore();
            }

            @Override
            public @NotNull ResourceLocation getResourceLocation() {
                return ResourceLocation.fromNamespaceAndPath(MODID, "marker_tasks");
            }
        });

        registerInternal("Cogwheel", script -> CogMaster.getInstance());
        registerInternal("true", script -> CogBool.TRUE);
        registerInternal("false", script -> CogBool.FALSE);


        // //////////////////////////////////// //
        // Method Likes
//        registerInternal(new MethodLikeLineHandler("wait", MODID) {
//            @Override
//            public DoubleValue<Boolean, Boolean> methodHandler(@NotNull String args, @Nullable String label, @NotNull DispatchedScript script) throws Exception {
//                labelUnsupported(label);
//                int sep = args.indexOf(':');
//                String variable = args.substring(0, sep);
//                String name = args.substring(sep + 1);
//                script.getActionQueue(variable, Object.class).addStoryAction(new WaitForLabelAction(name));
//                return ScriptLineHandler.continueReading();
//            }
//        });
//        registerInternal(new MethodLikeLineHandler("suspendScript", MODID) {
//            @Override
//            public DoubleValue<Boolean, Boolean> methodHandler(@NotNull String args, @Nullable String label, @NotNull DispatchedScript script) throws Exception {
//                EventBus.register(args, (label1, host) -> {
//                    CogwheelExecutor.schedule(script::lineDispatcher);
//                });
//                return ScriptLineHandler.blocking();
//            }
//        });
//        registerInternal(new MethodLikeLineHandler("dispatchScript", MODID) {
//            @Override
//            public DoubleValue<Boolean, Boolean> methodHandler(@NotNull String args, @Nullable String label, @NotNull DispatchedScript script) throws Exception {
//                labelUnsupported(label);
//                CogScriptDispatcher.dispatch(args);
//                return ScriptLineHandler.blocking();
//            }
//        });
    }

    public static void putDefaults(HashMap<String, CogPropertyManager> storage, DispatchedScript script) {
        for (DoubleValue<String, Function<DispatchedScript, CogPropertyManager>> pair : defaultVariables) {
            storage.put(pair.getA(), pair.getB().apply(script));
        }
    }
    public static DoubleValue<DoubleValue<Boolean, Boolean>, CogPropertyManager> expressionHandler(String line, DispatchedScript script, boolean allowBlocking) {
        // Return: DoubleValue<ScriptLineHandler.*(), ExpressionReturn>

        int currentStart = 0;

        // Variable search
        String variable = "";
        boolean hasVariable = false;
        for (int i = currentStart; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '=') {
                hasVariable = true;
                variable = variable.trim();
                currentStart = i + 1;
                break;
            } else if (c == '.') {
                variable = null;
                break;
            } else {
                variable += c;
            }
        }
        if (!hasVariable) {
            variable = null;
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
        currentName = null;

        if (chainCalls.size() < 2) {
            throw new CogExpressionFailure("Expression does not have enough steps! \"" + line + "\"");
        }
        if (chainCalls.get(0).endsWith(")")) {
            throw new CogExpressionFailure("Expression does not have first step! \"" + line + "\"");
        }
        CogPropertyManager manager = script.get(chainCalls.get(0).stripLeading());
        for (int i = 1 /* do not take first step */; i < chainCalls.size(); i++) {
            String step = chainCalls.get(i);
            if (!step.endsWith(")")) {
                throw new CogExpressionFailure("Invalid step: \"" + step + "\"! \"" + line + "\"");
            }
            int firstBracket = step.indexOf('(');
            String propName = step.substring(0, firstBracket);
            ArgumentData argumentData = ArgumentData.createFromString(step.substring(firstBracket + 1, step.length() - 1), script);
            if (manager == null) {
                throw new CogExpressionFailure("Calling \"" + propName + "\" property is not possible as current object is NULL \"" + line + "\"");
            }
            if (!manager.hasOwnProperty(propName)) {
                throw new CogExpressionFailure(manager.getClass().getCanonicalName() + " object does not have property \"" + propName + "\"! \"" + line + "\"");
            }
            try {
                manager = manager.getProperty(propName, argumentData, script);
            } catch (PreventSubCalling preventSubCalling) {
                if (!allowBlocking) {
                    throw new CogExpressionFailure("SubCalling Prevention is not allowed in this context! \"" + line + "\"");
                }
                preventSubCalling.getPostPrevention().prevent(variable);
                return new DoubleValue<>(ScriptLineHandler.blocking(), manager);
            }
        }
        if (variable != null) {
            script.put(variable, manager);
        }
        return new DoubleValue<>(ScriptLineHandler.continueReading(), manager);
    }
    public static void skip(String label, DispatchedScript script) {
        String line = script.pullLine();
        while (!line.equals(label)) {
            line = script.pullLine();
            if (line == null) {
                throw new RuntimeException("Skip failed. No label found");
            }
        }
    }
}
