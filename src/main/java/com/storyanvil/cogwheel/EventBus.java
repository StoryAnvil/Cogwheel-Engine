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

import com.mojang.brigadier.arguments.StringArgumentType;
import com.storyanvil.cogwheel.infrustructure.CogScriptDispatcher;
import com.storyanvil.cogwheel.util.DoubleValue;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Mod.EventBusSubscriber(modid = CogwheelEngine.MODID)
public class EventBus {
    @SubscribeEvent
    public static void registerCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("@").requires(css -> css.hasPermission(1))
                .then(Commands.literal("dispatch-script")
                        .then(Commands.argument("name", StringArgumentType.greedyString())
                                .executes(ctx -> {
                                    ctx.getSource().sendSystemMessage(Component.literal("Script execution will be dispatched"));
                                    CogScriptDispatcher.dispatch(StringArgumentType.getString(ctx, "name"));
                                    return 0;
                                })
                        )
                )
        );
    }

    protected static List<DoubleValue<Consumer<TickEvent.LevelTickEvent>, Integer>> queue = new ArrayList<>();
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

    public static StoryLevel getStoryLevel() {
        return level;
    }
}
