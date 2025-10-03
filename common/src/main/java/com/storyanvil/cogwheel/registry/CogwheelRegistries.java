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

import com.storyanvil.cogwheel.util.CogwheelExecutor;
import com.storyanvil.cogwheel.CogwheelHooks;
import com.storyanvil.cogwheel.api.Api;
import com.storyanvil.cogwheel.entity.NPC;
import com.storyanvil.cogwheel.infrastructure.*;
import com.storyanvil.cogwheel.infrastructure.cog.*;
import com.storyanvil.cogwheel.infrastructure.env.CogScriptEnvironment;
import com.storyanvil.cogwheel.infrastructure.err.CogExpressionFailure;
import com.storyanvil.cogwheel.infrastructure.err.CogScriptException;
import com.storyanvil.cogwheel.infrastructure.module.CogModule;
import com.storyanvil.cogwheel.infrastructure.props.CGPM;
import com.storyanvil.cogwheel.infrastructure.script.DispatchedScript;
import com.storyanvil.cogwheel.infrastructure.script.ScriptLine;
import com.storyanvil.cogwheel.items.InspectorItem;
import com.storyanvil.cogwheel.util.*;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

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

    public static final PlatformRegistry REGISTRY = CogwheelHooks.createRegistry(MODID);
    public static final Supplier<Item> INSPECTOR = REGISTRY.registerItem("inspector", InspectorItem::new,
            k -> new Item.Settings().maxCount(1).fireproof().rarity(Rarity.EPIC));
    public static final Supplier<EntityType<NPC>> NPC = REGISTRY.registerEntity("npc",
            k -> EntityType.Builder.create(NPC::new, SpawnGroup.MISC)
                    .dimensions(0.6f, 1.8f));

    @Api.Internal @ApiStatus.Internal @Api.MixinsNotAllowed(where = "NOWHERE")
    public static void registerDefaultObjects() {
        registerInternal(new ScriptLineHandler() {
            @Override
            public byte handle(@NotNull ScriptLine scriptLine, @NotNull String line, @NotNull DispatchedScript script) throws Exception {
                if (!line.startsWith("*")) return ScriptLineHandler.ignore;
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
                    script.haltExecution();
                    return ScriptLineHandler.continueReading;
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
                script.put(actualName, module);
                return ScriptLineHandler.continueReading;
            }

            @Override
            public @NotNull Identifier getIdentifier() {
                return Identifier.of(MODID, "star");
            }
        });
        registerInternal(new ScriptLineHandler() {
            @Override
            public byte handle(@NotNull ScriptLine scriptLine, @NotNull String line, @NotNull DispatchedScript script) throws Exception {
                if (line.startsWith("if (") && line.endsWith(") {")) {
                    String expression = line.substring(4, line.length() - 3);
                    CGPM output = expressionHandler(expression, script, false).getB();
                    if (!(output instanceof CogBool bool))
                        throw script.wrap(new Exception("If Statement body returned non-boolean object!"));
                    if (bool.getValue()) {
                        script.pushFrame();
                        int $level = 0;
                        int $line = script.getExecutionLine() + 1;
                        ScriptLine l = script.getLinesToExecute().get($line);
                        while (l != null && $line < script.getLinesToExecute().size()) {
                            if (l.getLine().endsWith("{")) {
                                $level++;
                            } else if (l.getLine().equals("}")) {
                                $level--;
                                if ($level <= -1) break;
                            }
                            $line++;
                            l = script.getLinesToExecute().get($line);
                        }
                        if (l != null) {
                            l.setHandler(new Simple(Identifier.of(MODID, "if/tail")) {
                                @Override
                                public byte handle(@NotNull ScriptLine scriptLine, @NotNull String line, @NotNull DispatchedScript script) throws Exception {
                                    script.pullFrame();
                                    return ScriptLineHandler.continueReading;
                                }
                            });
                        }
                    } else {
                        skipCurrentLevel(script);
                    }
                    return ScriptLineHandler.continueReading;
                }
                return ScriptLineHandler.ignore;
            }

            @Override
            public @NotNull Identifier getIdentifier() {
                return Identifier.of(MODID, "if/head");
            }
        });
        registerInternal(new ScriptLineHandler() {
            @Override
            public byte handle(@NotNull ScriptLine scriptLine, @NotNull String _line, @NotNull DispatchedScript script) throws CogScriptException {
                return expressionHandler(_line, script, true).getA();
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
        String l = script.pullLine().getLine();
        while (l != null) {
            if (l.endsWith("{")) {
                level++;
            } else if (l.equals("}")) {
                level--;
                if (level <= -1) break;
            }
            l = script.pullLine().getLine();
        }
    }

    public static void putDefaults(ScriptStorage storage, DispatchedScript script) {
        for (Bi<String, Function<DispatchedScript, CGPM>> pair : defaultVariables) {
            storage.put(pair.getA(), pair.getB().apply(script));
        }
    }
    @Contract("_, _, _ -> new")
    @SuppressWarnings("ExtractMethodRecommender")
    public static @NotNull Bi<Byte, CGPM> expressionHandler(String line, DispatchedScript script, boolean allowBlocking) throws CogScriptException {
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

        String ccc = line.substring(currentStart).stripLeading();
        if (ccc.length() > 1) {
            String member = ccc;
            Bi<Byte, CGPM> r = null;
            if (member.startsWith("\"") && member.endsWith("\"")) {
                r = new Bi<>(ScriptLineHandler.continueReading, new CogString(member.substring(1, member.length() - 1)));
            } else if (member.startsWith("^")) {
                int endIndex = member.length() - 1;
                char end = member.charAt(endIndex);
                switch (end) {
                    case 'L' -> {
                        r = new Bi<>(ScriptLineHandler.continueReading, new CogLong(member.substring(1, endIndex)));
                        break;
                    }
                    case 'D' -> {
                        r = new Bi<>(ScriptLineHandler.continueReading, new CogDouble(member.substring(1, endIndex)));
                        break;
                    }
                    default -> {
                        r = new Bi<>(ScriptLineHandler.continueReading, new CogInteger(member.substring(1)));
                        break;
                    }
                }
            }
            if (r != null) {
                if (variable != null) {
                    script.put(variable.toString(), r.getB());
                }
                return r;
            }
        }

        // Chain call search
        StringBuilder currentName = new StringBuilder(); // TODO: Replace StringBuilder with one substring call for better optimization
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
                    throw script.wrap(new CogExpressionFailure("Closing bracket mismatch!"));
                }
                currentName.append(c);
            } else {
                currentName.append(c);
            }
        }
        if (depth == 0) {
            chainCalls.add(currentName.toString());
        } else {
            throw script.wrap(new CogExpressionFailure("Tail closing bracket mismatch!"));
        }

        if (chainCalls.getFirst().endsWith(")")) {
            throw script.wrap(new CogExpressionFailure("Expression does not have first step!"));
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
                throw script.wrap(new CogExpressionFailure("Invalid step: \"" + step + "\"! \"" + line + "\""));
            }
            int firstBracket = step.indexOf('(');
            String propName = step.substring(0, firstBracket);
            ArgumentData argumentData = ArgumentData.of(step.substring(firstBracket + 1, step.length() - 1), script);
            try {
                manager = CGPM.noNull(manager.getProperty(propName, argumentData, script));
            } catch (PreventChainCalling preventChainCalling) {
                if (!allowBlocking) {
                    throw script.wrap(new CogExpressionFailure("SubCalling Prevention is not allowed in this context! \"" + line + "\""));
                }
                preventChainCalling.getPostPrevention().prevent(variable != null ? variable.toString() : null);
                return new Bi<>(ScriptLineHandler.blocking, manager);
            } catch (RuntimeException | CogScriptException e) {
                throw new RuntimeException("Expression handler caught while getting property. ArgData: " + argumentData, e);
            }
        }
        if (variable != null) {
            script.put(variable.toString(), manager);
        }
        return new Bi<>(ScriptLineHandler.continueReading, manager);
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
}
