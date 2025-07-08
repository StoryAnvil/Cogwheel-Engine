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

package com.storyanvil.cogwheel;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.storyanvil.cogwheel.infrustructure.CogScriptDispatcher;
import com.storyanvil.cogwheel.infrustructure.StoryAction;
import com.storyanvil.cogwheel.infrustructure.abilities.StoryActionQueue;
import com.storyanvil.cogwheel.registry.CogwheelRegistries;
import com.storyanvil.cogwheel.util.*;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

@Mod.EventBusSubscriber(modid = CogwheelEngine.MODID)
public class EventBus {
    @SubscribeEvent
    public static void registerCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("@storyanvil").requires(css -> css.hasPermission(1))
                .then(Commands.literal("dispatch-script")
                        .then(Commands.argument("name", StringArgumentType.greedyString())
                                .executes(ctx -> {
                                    ctx.getSource().sendSystemMessage(Component.literal("Script execution will be dispatched"));
                                    CogScriptDispatcher.dispatch(StringArgumentType.getString(ctx, "name"));
                                    return 0;
                                })
                        )
                )
                .then(Commands.literal("list")
                        .then(Commands.argument("e", EntityArgument.entity())
                                .executes(ctx -> {
                                    StoryActionQueue<?> actionQueue = (StoryActionQueue<?>) EntityArgument.getEntity(ctx, "e");
                                    StringBuilder sb = new StringBuilder();
                                    for (StoryAction<?> action : actionQueue.getActions()) {
                                        sb.append(action.toString()).append('\n');
                                    }
                                    ctx.getSource().sendSystemMessage(Component.literal(sb.toString()));
                                    return 0;
                                })
                        )
                )
                .then(Commands.literal("dump")
                        .executes(ctx -> {
                            StringBuilder sb = new StringBuilder("=== === === === === COGWHEEL ENGINE REPORT === === === === ===\n");
                            sb.append("DATE: ").append(new Date());
                            sb.append("\nMODDED: ").append(guessIfModded()).append("\n");
                            ObjectMonitor.dumpAll(sb);
                            CogwheelEngine.LOGGER.warn("\n{}", sb);
                            return 0;
                        })
                )
        );
        event.getDispatcher().register(Commands.literal("@storyclient").requires(css -> css.hasPermission(0))
                .then(Commands.literal("dialog")
                        .then(Commands.argument("answer", IntegerArgumentType.integer())
                                .then(Commands.argument("dialog", StringArgumentType.greedyString())
                                        .executes(ctx -> {
                                            String dialogID = StringArgumentType.getString(ctx, "dialog");
                                            if (!dialogResponses.containsKey(dialogID)) return 1;
                                            Consumer<Integer> call = dialogResponses.get(dialogID);
                                            dialogResponses.remove(dialogID);
                                            call.accept(IntegerArgumentType.getInteger(ctx, "answer"));
                                            return 0;
                                        })
                                )
                        )
                )
        );
    }

    private static @NotNull StringBuilder guessIfModded() {
        StringBuilder modded = new StringBuilder();
        for (ScriptLineHandler handler : CogwheelRegistries.getLineHandlers()) {
            if (!handler.getResourceLocation().getNamespace().equals(CogwheelEngine.MODID)) {
                modded.append(" DETECTED CUSTOM LINE HANDLERS");
            }
        }
        if (modded.isEmpty()) {
            modded.append("NO");
        } else modded.insert(0, "YES:");
        return modded;
    }

    protected static List<DoubleValue<Consumer<TickEvent.LevelTickEvent>, Integer>> queue = new ArrayList<>();
    private static HashMap<String, Consumer<Integer>> dialogResponses = new HashMap<>();
    private static StoryLevel level = new StoryLevel();
    @SubscribeEvent
    public static void tick(TickEvent.LevelTickEvent event) {
        if (event.level.isClientSide()) return;
        if (event.phase == TickEvent.Phase.END) {
            level.tick((ServerLevel) event.level);
            return;
        }
        if (!event.level.dimension().location().equals(ResourceLocation.fromNamespaceAndPath("minecraft", "overworld"))) return;
        try {
            int size = queue.size();
            for (int i = 0; i < size; i++) {
                DoubleValue<Consumer<TickEvent.LevelTickEvent>, Integer> e = queue.get(i);
                if (e.getB() < 2) {
                    e.getA().accept(event);
                    queue.remove(i);
                    i--;
                } else {
                    e.setB(e.getB() - 1);
                }
            }
        } catch (IndexOutOfBoundsException e) {
            CogwheelEngine.LOGGER.warn("Queue bound error");
        }
    }

    private static HashMap<String, WeakList<LabelCloseable>> labelListeners = new HashMap<>();
    public static void hitLabel(String label, StoryAction<?> action) {
        if (labelListeners.containsKey(label)) {
            WeakList<LabelCloseable> c = labelListeners.get(label);
            for (int i = 0; i < c.size(); i++) {
                LabelCloseable closeable = c.get(i);
                if (closeable != null)
                    closeable.close(label, action);
            }
        }
    }
    public static void register(String label, LabelCloseable closeable) {
        if (labelListeners.containsKey(label)) {
            labelListeners.get(label).add(closeable);
        } else {
            labelListeners.put(label, new WeakList<>(closeable));
        }
    }
    public static void registerDialog(String id, Consumer<Integer> callback) {
        dialogResponses.put(id, callback);
    }

    public static StoryLevel getStoryLevel() {
        return level;
    }
}
