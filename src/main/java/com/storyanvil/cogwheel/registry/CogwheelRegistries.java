/*
 * StoryAnvil CogWheel Engine
 * Copyright (C) 2025 StoryAnvil
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.storyanvil.cogwheel.registry;

import com.storyanvil.cogwheel.CogwheelExecutor;
import com.storyanvil.cogwheel.EventBus;
import com.storyanvil.cogwheel.entity.NPC;
import com.storyanvil.cogwheel.infrustructure.CogPropertyManager;
import com.storyanvil.cogwheel.infrustructure.CogScriptDispatcher;
import com.storyanvil.cogwheel.infrustructure.DispatchedScript;
import com.storyanvil.cogwheel.infrustructure.StoryAction;
import com.storyanvil.cogwheel.infrustructure.abilities.*;
import com.storyanvil.cogwheel.infrustructure.actions.ChatAction;
import com.storyanvil.cogwheel.infrustructure.cog.CogMaster;
import com.storyanvil.cogwheel.infrustructure.actions.PathfindAction;
import com.storyanvil.cogwheel.infrustructure.actions.WaitForLabelAction;
import com.storyanvil.cogwheel.infrustructure.cog.CogActionQueue;
import com.storyanvil.cogwheel.infrustructure.cog.CogInteger;
import com.storyanvil.cogwheel.infrustructure.cog.PreventSubCalling;
import com.storyanvil.cogwheel.util.*;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.AbortableIterationConsumer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.entity.EntityTypeTest;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;

import static com.storyanvil.cogwheel.CogwheelEngine.MODID;
import static com.storyanvil.cogwheel.CogwheelExecutor.log;

public class CogwheelRegistries {
    private static final List<DoubleValue<String, Function<DispatchedScript, CogPropertyManager>>> defaultVariables = new ArrayList<>();
    private static final HashMap<String, MethodLikeLineHandler> methodLikes = new HashMap<>();
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
     * Registries MethodLikeHandler
     * @apiNote Namespaces <code>storyanvil</code> and <code>storyanvil_cogwheel</code> reserved for internal purposes and cannot be used
     * @param factory factory that will be registered
     */
    public static void register(@NotNull MethodLikeLineHandler factory) {
        synchronized (methodLikes) {
            ResourceLocation id = factory.getResourceLocation();
            if (id.getNamespace().equals("storyanvil") || id.getNamespace().equals(MODID))
                throw new IllegalArgumentException("ActionFactory with namespace \"" + id.getNamespace() + "\" cannot be registered as this namespace is reserved for internal purposes");
            methodLikes.put(factory.getSub(), factory);
        }
    }

    @ApiStatus.Internal
    protected static void registerInternal(@NotNull MethodLikeLineHandler factory) {
        synchronized (methodLikes) {
            methodLikes.put(factory.getSub(), factory);
        }
    }
    /**
     * Registries Default Variable
     * @apiNote Name must not be "StoryAnvil" or "CogWheel"
     */
    public static void register(@NotNull String name, @NotNull Function<DispatchedScript, CogPropertyManager> f) {
        synchronized (methodLikes) {
            if (name.equalsIgnoreCase("storyanvil") || name.equalsIgnoreCase("cogwheel")) throw new IllegalArgumentException("Name not permitted");
            defaultVariables.add(new DoubleValue<>(name, f));
        }
    }

    @ApiStatus.Internal
    protected static void registerInternal(@NotNull String name, @NotNull Function<DispatchedScript, CogPropertyManager> f) {
        synchronized (methodLikes) {
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
        registerInternal(new ScriptLineHandler() {
            @Override
            public @NotNull DoubleValue<Boolean, Boolean> handle(@NotNull String _line, @Nullable String label, @NotNull DispatchedScript script) throws Exception {
                if (!_line.startsWith("*")) return ScriptLineHandler.ignore();
                String line = _line.substring(1);
                int bracket = line.indexOf('(');
                if (bracket == -1) return ScriptLineHandler.ignore();
                String sub = line.substring(0, bracket + 1);
                if (!methodLikes.containsKey(sub)) return ScriptLineHandler.ignore();
                MethodLikeLineHandler handler = methodLikes.get(sub);
                return handler.methodHandler(line.substring(sub.length(), line.length() - 1), label, script);
            }

            @Override
            public @NotNull ResourceLocation getResourceLocation() {
                return ResourceLocation.fromNamespaceAndPath(MODID, "method_like");
            }
        });
        registerInternal(new ScriptLineHandler() {
            @Override
            public @NotNull DoubleValue<Boolean, Boolean> handle(@NotNull String _line, @Nullable String label, @NotNull DispatchedScript script) throws Exception {
                int eq = _line.indexOf('=');
                String line;
                String variable = null;
                if (eq != -1) {
                    line = _line.substring(eq + 1);
                    variable = _line.substring(0, eq);
                } else {
                    line = _line;
                }


                int dot = line.indexOf('.');
                if (dot != -1) {
                    String sub = line.substring(0, dot);
                    if (script.hasKey(sub)) {
                        CogPropertyManager manager = CogPropertyManager.noNull(script.get(sub));
                        String[] props = line.substring(dot + 1).split("\\.");
                        for (int i = 0; i < props.length; i++) {
                            String linkedProperty = props[i];
                            if (!linkedProperty.endsWith(")")) throw new RuntimeException("Tail Bracket mismatch");
                            int bracket = linkedProperty.indexOf('(');
                            if (bracket == -1) throw new RuntimeException("Head Bracket mismatch");
                            String args = linkedProperty.substring(bracket + 1, linkedProperty.length() - 1);
                            String propName = linkedProperty.substring(0, bracket);
                            if (manager.hasOwnProperty(propName)) {
                                try {
                                    manager = manager.getProperty(propName, args, script);
                                } catch (PreventSubCalling preventSubCalling) {
                                    preventSubCalling.getPostPrevention().prevent(variable);
                                    return ScriptLineHandler.blocking();
//                                    break;
                                }
                            } else throw new RuntimeException(manager.getClass().getCanonicalName() + " Manager does not have property named: " + linkedProperty + " as " + propName + " with " + args);
                        }
                        if (variable != null) {
                            script.put(variable, manager);
                        }
                        return ScriptLineHandler.continueReading();
                    }
                }
                return ScriptLineHandler.ignore();
            }

            @Override
            public @NotNull ResourceLocation getResourceLocation() {
                return ResourceLocation.fromNamespaceAndPath(MODID, "property_managers");
            }
        });
        registerInternal("Cogwheel", script -> CogMaster.getInstance());


        // //////////////////////////////////// //
        // Method Likes
        // //////////////////////////////////// //
        registerInternal(new MethodLikeLineHandler("chat", MODID) {
            @Override
            public DoubleValue<Boolean, Boolean> methodHandler(@NotNull String args, @Nullable String label, @NotNull DispatchedScript script) throws Exception {
                int sep = args.indexOf(':');
                String variable = args.substring(0, sep);
                String msg = args.substring(sep + 1);

                script.getActionQueue(variable, StoryChatter.class).addStoryAction(new ChatAction(msg).setActionLabel(label));

                return ScriptLineHandler.continueReading();
            }
        });
        registerInternal(new MethodLikeLineHandler("skin", MODID) {
            @Override
            public DoubleValue<Boolean, Boolean> methodHandler(@NotNull String args, @Nullable String label, @NotNull DispatchedScript script) throws Exception {
                int sep = args.indexOf(':');
                String variable = args.substring(0, sep);
                String skin = args.substring(sep + 1);
                script.getActionQueue(variable, StorySkinHolder.class).addStoryAction(new StoryAction.Instant<StorySkinHolder>() {
                    @Override
                    public void proceed(StorySkinHolder myself) {
                        myself.setSkin(skin);
                    }
                }.setActionLabel(label));
                return ScriptLineHandler.continueReading();
            }
        });
        registerInternal(new MethodLikeLineHandler("name", MODID) {
            @Override
            public DoubleValue<Boolean, Boolean> methodHandler(@NotNull String args, @Nullable String label, @NotNull DispatchedScript script) throws Exception {
                int sep = args.indexOf(':');
                String variable = args.substring(0, sep);
                String name = args.substring(sep + 1);
                script.getActionQueue(variable, StoryNameHolder.class).addStoryAction(new StoryAction.Instant<StoryNameHolder>() {
                    @Override
                    public void proceed(StoryNameHolder myself) {
                        myself.setCogName(name);
                    }
                }.setActionLabel(label));
                return ScriptLineHandler.continueReading();
            }
        });
        registerInternal(new MethodLikeLineHandler("getLevel", MODID) {
            @Override
            public DoubleValue<Boolean, Boolean> methodHandler(@NotNull String args, @Nullable String label, @NotNull DispatchedScript script) throws Exception {
                labelUnsupported(label);
                script.put(args, new CogActionQueue<>(EventBus.getStoryLevel()));
                return ScriptLineHandler.continueReading();
            }
        });
        registerInternal(new MethodLikeLineHandler("dataDump", MODID) {
            @Override
            public DoubleValue<Boolean, Boolean> methodHandler(@NotNull String args, @Nullable String label, @NotNull DispatchedScript script) throws Exception {
                labelUnsupported(label);
                script.dataDump();
                return ScriptLineHandler.continueReading();
            }
        });
        registerInternal(new MethodLikeLineHandler("pathfind", MODID) {
            @Override
            public DoubleValue<Boolean, Boolean> methodHandler(@NotNull String args, @Nullable String label, @NotNull DispatchedScript script) throws Exception {
                int sep = args.indexOf(':');
                String variable = args.substring(0, sep);
                String a = args.substring(sep + 1);
                String[] pos = a.split(" ");
                if (pos.length != 3) throw new IllegalArgumentException("Invalid pos");
                BlockPos blockPos = new BlockPos(Integer.parseInt(pos[0]), Integer.parseInt(pos[1]), Integer.parseInt(pos[2]));
                script.getActionQueue(variable, StoryNavigator.class).addStoryAction(new PathfindAction(blockPos).setActionLabel(label));
                return ScriptLineHandler.continueReading();
            }
        });
        registerInternal(new MethodLikeLineHandler("wait", MODID) {
            @Override
            public DoubleValue<Boolean, Boolean> methodHandler(@NotNull String args, @Nullable String label, @NotNull DispatchedScript script) throws Exception {
                labelUnsupported(label);
                int sep = args.indexOf(':');
                String variable = args.substring(0, sep);
                String name = args.substring(sep + 1);
                script.getActionQueue(variable, Object.class).addStoryAction(new WaitForLabelAction(name));
                return ScriptLineHandler.continueReading();
            }
        });
        registerInternal(new MethodLikeLineHandler("suspendScript", MODID) {
            @Override
            public DoubleValue<Boolean, Boolean> methodHandler(@NotNull String args, @Nullable String label, @NotNull DispatchedScript script) throws Exception {
                EventBus.register(args, (label1, host) -> {
                    CogwheelExecutor.schedule(script::lineDispatcher);
                });
                return ScriptLineHandler.blocking();
            }
        });
        registerInternal(new MethodLikeLineHandler("dispatchScript", MODID) {
            @Override
            public DoubleValue<Boolean, Boolean> methodHandler(@NotNull String args, @Nullable String label, @NotNull DispatchedScript script) throws Exception {
                labelUnsupported(label);
                CogScriptDispatcher.dispatch(args);
                return ScriptLineHandler.blocking();
            }
        });
        registerInternal(new MethodLikeLineHandler("dialogChoices", MODID) {
            @Override
            public DoubleValue<Boolean, Boolean> methodHandler(@NotNull String args, @Nullable String label, @NotNull DispatchedScript script) throws Exception {
                labelUnsupported(label);
                int sep = args.indexOf(':');
                String variable = args.substring(0, sep);
                String[] choices = args.substring(sep + 1).split(",");
                Component[] components = new Component[choices.length];
                final String dialogID = UUID.randomUUID().toString();
                EventBus.registerDialog(dialogID, response -> {
                    script.put(variable, new CogInteger(response));
                    script.lineDispatcher();
                });
                for (int i = 0; i < choices.length; i++) {
                    final int finalI = i;
                    components[i] = Component.literal("[" + (i + 1) + "] ").withStyle(style -> style.withColor(ChatFormatting.GRAY))
                            .append(Component.literal(choices[i]).withStyle(style -> style.withClickEvent(
                                    new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/@storyclient dialog " + finalI + " " + dialogID)
                            )));
                }
                CogwheelExecutor.scheduleTickEvent(event -> {
                    StoryUtils.sendGlobalMessage((ServerLevel) event.level, components);
                });
                return ScriptLineHandler.blocking();
            }
        });
    }

    public static void putDefaults(HashMap<String, CogPropertyManager> storage, DispatchedScript script) {
        for (DoubleValue<String, Function<DispatchedScript, CogPropertyManager>> pair : defaultVariables) {
            storage.put(pair.getA(), pair.getB().apply(script));
        }
    }
}
