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

package com.storyanvil.cogwheel.neoforge;

import com.storyanvil.cogwheel.CogwheelEngine;
import com.storyanvil.cogwheel.CogwheelHooks;
import com.storyanvil.cogwheel.EventBus;
import com.storyanvil.cogwheel.data.StoryCodec;
import com.storyanvil.cogwheel.data.StoryPacket;
import com.storyanvil.cogwheel.neoforge.data.StoryNeoPacket;
import com.storyanvil.cogwheel.neoforge.data.StoryNeoParcel;
import com.storyanvil.cogwheel.network.devui.DevEarlySyncPacket;
import com.storyanvil.cogwheel.network.devui.editor.DevEditorSession;
import com.storyanvil.cogwheel.network.mc.AnimationDataBound;
import com.storyanvil.cogwheel.network.mc.DialogBound;
import com.storyanvil.cogwheel.util.Bi;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.DirectionalPayloadHandler;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

import static com.storyanvil.cogwheel.EventBus.*;

@EventBusSubscriber(modid = CogwheelEngine.MODID)
public class NeoForgeEventBus {
    @SubscribeEvent
    public static void onRegisterPayloadHandlers(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar reg = event.registrar("Cog3");
        CogwheelHooks.packetRegistry(new CogwheelHooks.PacketRegistrar() {
            @Override
            public <T extends StoryPacket<T>> void accept(String id, StoryCodec<T> codec, Class<T> clazz) {
                id = id.toLowerCase(Locale.ENGLISH);
                StoryNeoPacket<T> neoPacket = new StoryNeoPacket<>(id, codec);
                CogwheelEngineNeoForge.PLATFORM_LOG.debug("Registered packet {} with [id={},class={}]", neoPacket, id, clazz);
                reg.playBidirectional(neoPacket.getType().id(), neoPacket, new DirectionalPayloadHandler<StoryNeoParcel<T>>(
                        neoPacket::clientHandle,
                        neoPacket::serverHandle
                ));
            }
        });
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CogwheelHooks.commandRegistry(event.getDispatcher()::register);
    }

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (!event.getLevel().getDimensionEntry().getKey().get().getValue().equals(Identifier.of("minecraft", "overworld"))) return;
        if (event.getLevel().isClient()) {
            synchronized (clientQueue) {
                try {
                    for (int i = 0; i < clientQueue.size(); i++) {
                        Bi<Consumer<ClientWorld>, Integer> e = clientQueue.get(i);
                        if (e.getB() < 2) {
                            e.getA().accept((ClientWorld) event.getLevel());
                            clientQueue.remove(i);
                            i--;
                        } else {
                            e.setB(e.getB() - 1);
                        }
                    }
                } catch (Exception e) {
                    CogwheelEngine.LOGGER.warn("Client Queue bound error", e);
                }
                return;
            }
        }
        synchronized (queue) {
            try {
                for (int i = 0; i < queue.size(); i++) {
                    Bi<Consumer<ServerWorld>, Integer> e = queue.get(i);
                    if (e.getB() < 2) {
                        try {
                            e.getA().accept((ServerWorld) event.getLevel());
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                        queue.remove(i);
                        i--;
                    } else {
                        e.setB(e.getB() - 1);
                    }
                }
            } catch (Exception e) {
                CogwheelEngine.LOGGER.warn("Queue bound error", e);
            }
            try {
                EventBus.getStoryLevel().tick((ServerWorld) event.getLevel());
            } catch (Exception e) {
                CogwheelEngine.LOGGER.warn("StoryLevel tick error", e);
            }
        }
    }

    @SubscribeEvent
    public static void boundEvent(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayerEntity player) {
            StringBuilder sb = new StringBuilder();
            boolean a = true;
            for (Identifier loc : serverSideAnimations) {
                if (a) {
                    a = false;
                } else {
                    sb.append("|");
                }
                sb.append(loc.toString());
            }
            CogwheelHooks.sendPacket(new AnimationDataBound(sb.toString()), player);
            DevEarlySyncPacket.syncFor(player, true);
            DevEditorSession.boundColorFor(player);
        }
    }
    @SubscribeEvent
    public static void unboundEvent(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayerEntity player) {
            DevEditorSession.unboundColorFrom(player);
        }
    }

    @SubscribeEvent
    public static void onServerStarting(ServerStartingEvent event) {
        CogwheelHooks.serverStart();
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        CogwheelHooks.serverStop();
    }
}
