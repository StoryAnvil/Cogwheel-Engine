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
import com.storyanvil.cogwheel.infrustructure.CogScriptDispatcher;
import com.storyanvil.cogwheel.infrustructure.DispatchedScript;
import com.storyanvil.cogwheel.infrustructure.StoryAction;
import com.storyanvil.cogwheel.infrustructure.abilities.StoryActionQueue;
import com.storyanvil.cogwheel.infrustructure.abilities.StoryChatter;
import com.storyanvil.cogwheel.infrustructure.abilities.StoryNameHolder;
import com.storyanvil.cogwheel.infrustructure.abilities.StorySkinHolder;
import com.storyanvil.cogwheel.infrustructure.actions.ChatAction;
import com.storyanvil.cogwheel.infrustructure.actions.PathfindAction;
import com.storyanvil.cogwheel.infrustructure.actions.WaitForLabelAction;
import com.storyanvil.cogwheel.util.ActionFactory;
import com.storyanvil.cogwheel.util.DoubleValue;
import com.storyanvil.cogwheel.util.MethodLikeLineHandler;
import com.storyanvil.cogwheel.util.ScriptLineHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.AbortableIterationConsumer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.entity.EntityTypeTest;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import static com.storyanvil.cogwheel.CogwheelEngine.MODID;
import static com.storyanvil.cogwheel.CogwheelExecutor.log;

public class CogwheelRegistries {
    private static final HashMap<ResourceLocation, ActionFactory> factoryRegistry = new HashMap<>();
    private static final HashMap<String, MethodLikeLineHandler> methodLikes = new HashMap<>();
    private static final ArrayList<ScriptLineHandler> lineHandlers = new ArrayList<>();

    /**
     * Registries ActionFactory
     * @param id ResourceLocation of registry. Namespaces <code>storyanvil</code> and <code>storyanvil_cogwheel</code> reserved for internal purposes and cannot be used
     * @param factory factory that will be registered
     */
    public static void register(@NotNull ResourceLocation id, @NotNull ActionFactory factory) {
        synchronized (factoryRegistry) {
            if (id.getNamespace().equals("storyanvil") || id.getNamespace().equals(MODID))
                throw new IllegalArgumentException("ActionFactory with namespace \"" + id.getNamespace() + "\" cannot be registered as this namespace is reserved for internal purposes");
            if (factoryRegistry.containsKey(ResourceLocation.fromNamespaceAndPath(MODID, "__finalize__")))
                throw new IllegalStateException("Registry frozen! New ActionFactory cannot be registered!");
            if (factoryRegistry.containsKey(id))
                throw new IllegalStateException("ActionFactory with resource location \"" + id + "\" was registered already!");
            factoryRegistry.put(id, Objects.requireNonNull(factory));
        }
    }

    @ApiStatus.Internal
    protected static void register(@NotNull String name, @NotNull ActionFactory factory) {
        synchronized (factoryRegistry) {
            if (factoryRegistry.containsKey(ResourceLocation.fromNamespaceAndPath(MODID, "__finalize__")))
                throw new IllegalStateException("Registry frozen! New ActionFactory cannot be registered!");
            ResourceLocation id = ResourceLocation.fromNamespaceAndPath(MODID, name);
            if (factoryRegistry.containsKey(id))
                throw new IllegalStateException("ActionFactory with resource location \"" + id + "\" was registered already!");
            factoryRegistry.put(id, Objects.requireNonNull(factory));
        }
    }
    /**
     * Registries ScriptLineHandler
     * @apiNote Namespaces <code>storyanvil</code> and <code>storyanvil_cogwheel</code> reserved for internal purposes and cannot be used
     * @param factory factory that will be registered
     */
    public static void register(@NotNull ScriptLineHandler factory) {
        synchronized (factoryRegistry) {
            synchronized (lineHandlers) {
                ResourceLocation id = factory.getResourceLocation();
                if (id.getNamespace().equals("storyanvil") || id.getNamespace().equals(MODID))
                    throw new IllegalArgumentException("ActionFactory with namespace \"" + id.getNamespace() + "\" cannot be registered as this namespace is reserved for internal purposes");
                if (factoryRegistry.containsKey(ResourceLocation.fromNamespaceAndPath(MODID, "__finalize__")))
                    throw new IllegalStateException("Registry frozen! New ActionFactory cannot be registered!");
                lineHandlers.add(Objects.requireNonNull(factory));
            }
        }
    }

    @ApiStatus.Internal
    protected static void registerInternal(@NotNull ScriptLineHandler factory) {
        synchronized (factoryRegistry) {
            synchronized (lineHandlers) {
                if (factoryRegistry.containsKey(ResourceLocation.fromNamespaceAndPath(MODID, "__finalize__")))
                    throw new IllegalStateException("Registry frozen! New ActionFactory cannot be registered!");
                lineHandlers.add(Objects.requireNonNull(factory));
            }
        }
    }
    /**
     * Registries MethodLikeHandler
     * @apiNote Namespaces <code>storyanvil</code> and <code>storyanvil_cogwheel</code> reserved for internal purposes and cannot be used
     * @param factory factory that will be registered
     */
    public static void register(@NotNull MethodLikeLineHandler factory) {
        synchronized (factoryRegistry) {
            synchronized (methodLikes) {
                ResourceLocation id = factory.getResourceLocation();
                if (id.getNamespace().equals("storyanvil") || id.getNamespace().equals(MODID))
                    throw new IllegalArgumentException("ActionFactory with namespace \"" + id.getNamespace() + "\" cannot be registered as this namespace is reserved for internal purposes");
                if (factoryRegistry.containsKey(ResourceLocation.fromNamespaceAndPath(MODID, "__finalize__")))
                    throw new IllegalStateException("Registry frozen! New ActionFactory cannot be registered!");
                methodLikes.put(factory.getSub(), factory);
            }
        }
    }

    @ApiStatus.Internal
    protected static void registerInternal(@NotNull MethodLikeLineHandler factory) {
        synchronized (factoryRegistry) {
            synchronized (methodLikes) {
                if (factoryRegistry.containsKey(ResourceLocation.fromNamespaceAndPath(MODID, "__finalize__")))
                    throw new IllegalStateException("Registry frozen! New ActionFactory cannot be registered!");
                methodLikes.put(factory.getSub(), factory);
            }
        }
    }

    /**
     * @return Registered ActionFactory. Shorthand for <code>CogwheelRegistries#getFactory(ResourceLocation.fromNamespaceAndPath(CogwheelEngine.MODID, name));</code>
     */
    public static @Nullable ActionFactory getFactory(@NotNull String name) {
        return factoryRegistry.get(ResourceLocation.fromNamespaceAndPath(MODID, name));
    }

    /**
     * @return Registered ActionFactory. Null is returned if there isn't ActionFactory with specified resource location
     */
    public static @Nullable ActionFactory getFactory(@NotNull ResourceLocation id) {
        return factoryRegistry.get(id);
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
            public @NotNull DoubleValue<Boolean, Boolean> handle(@NotNull String line, @Nullable String label, @NotNull DispatchedScript script) throws Exception {
                int bracket = line.indexOf('(');
                if (bracket == -1) return ScriptLineHandler.ignore();
                String sub = line.substring(0, bracket + 1);
                if (!methodLikes.containsKey(sub)) return ScriptLineHandler.ignore();
                MethodLikeLineHandler handler = methodLikes.get(sub);
                return handler.methodHandler(line.substring(sub.length() + 1, line.length() - 1), label, script);
            }

            @Override
            public @NotNull ResourceLocation getResourceLocation() {
                return ResourceLocation.fromNamespaceAndPath(MODID, "method_like");
            }
        });


        // //////////////////////////////////// //
        // Method Likes
        // //////////////////////////////////// //
        registerInternal(new MethodLikeLineHandler("log", MODID) {
            @Override
            public DoubleValue<Boolean, Boolean> methodHandler(@NotNull String args, @Nullable String label, @NotNull DispatchedScript script) throws Exception {
                labelUnsupported(label);
                log.info("{}: {}", script.getScriptName(), args);
                return ScriptLineHandler.continueReading();
            }
        });
        registerInternal(new MethodLikeLineHandler("findTaggedNPC", MODID) {
            @Override
            public DoubleValue<Boolean, Boolean> methodHandler(@NotNull String args, @Nullable String label, @NotNull DispatchedScript script) throws Exception {
                labelUnsupported(label);
                String[] parts = args.split("=");
                if (parts.length != 2) throw new IllegalArgumentException("findTaggedNPC requires parameters in following format: \"variable name=npc tag\"");

                CogwheelExecutor.scheduleTickEvent(event -> {
                    getNPCByTag((ServerLevel) event.level, parts[0], parts[1], script);
                });
                return ScriptLineHandler.blocking();
            }

            public static void getNPCByTag(ServerLevel level, String variable, String tag, DispatchedScript notify) {
                final NPC[] npc = {null};
                level.getEntities().get(new EntityTypeTest<Entity, NPC>() {
                    @Override
                    public @Nullable NPC tryCast(@NotNull Entity entity) {
                        if (entity instanceof NPC npc) return npc;
                        return null;
                    }

                    @Override
                    public @NotNull Class<? extends Entity> getBaseClass() {
                        return NPC.class;
                    }
                }, new AbortableIterationConsumer<>() {
                    @Override
                    public @NotNull Continuation accept(@NotNull NPC value) {
                        if (value.getTags().contains(tag)) {
                            npc[0] = value;
                            return Continuation.ABORT;
                        }
                        return Continuation.CONTINUE;
                    }
                });
                notify.putWeak(variable, npc[0]);
                if (npc[0] == null) {
                    log.info("{}: No NPC with tag {} found!", notify.getScriptName(), tag);
                }
                CogwheelExecutor.schedule(notify::lineDispatcher);
            }
        });
        registerInternal(new MethodLikeLineHandler("chat", MODID) {
            @Override
            public DoubleValue<Boolean, Boolean> methodHandler(@NotNull String args, @Nullable String label, @NotNull DispatchedScript script) throws Exception {
                int sep = args.indexOf(':');
                String variable = args.substring(0, sep);
                String msg = args.substring(sep + 1);

                script.getWeak(variable, StoryActionQueue.class).addStoryAction(new ChatAction(msg).setActionLabel(label));

                return ScriptLineHandler.continueReading();
            }
        });
        registerInternal(new MethodLikeLineHandler("skin", MODID) {
            @Override
            public DoubleValue<Boolean, Boolean> methodHandler(@NotNull String args, @Nullable String label, @NotNull DispatchedScript script) throws Exception {
                int sep = args.indexOf(':');
                String variable = args.substring(0, sep);
                String skin = args.substring(sep + 1);
                script.getWeak(variable, StoryActionQueue.class).addStoryAction(new StoryAction.Instant<StorySkinHolder>() {
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
                script.getWeak(variable, StoryActionQueue.class).addStoryAction(new StoryAction.Instant<StoryNameHolder>() {
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
                script.putWeak(args, EventBus.getStoryLevel());
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
                script.getWeak(variable, StoryActionQueue.class).addStoryAction(new PathfindAction(blockPos).setActionLabel(label));
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
                script.getWeak(variable, StoryActionQueue.class).addStoryAction(new WaitForLabelAction(name));
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
    }
}
