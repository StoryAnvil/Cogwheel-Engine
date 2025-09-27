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

package com.storyanvil.cogwheel.fabric;

import com.storyanvil.cogwheel.CogwheelHooks;
import com.storyanvil.cogwheel.EventBus;
import com.storyanvil.cogwheel.data.StoryCodec;
import com.storyanvil.cogwheel.data.StoryPacket;
import com.storyanvil.cogwheel.entity.NPC;
import com.storyanvil.cogwheel.fabric.data.StoryFabricPacket;
import com.storyanvil.cogwheel.network.devui.DevEarlySyncPacket;
import com.storyanvil.cogwheel.network.devui.editor.DevEditorSession;
import com.storyanvil.cogwheel.network.mc.AnimationDataBound;
import com.storyanvil.cogwheel.util.Bi;
import dev.architectury.event.events.client.ClientTickEvent;
import net.fabricmc.api.ModInitializer;

import com.storyanvil.cogwheel.CogwheelEngine;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;
import java.util.function.Consumer;

import static com.storyanvil.cogwheel.EventBus.*;

public final class CogwheelEngineFabric implements ModInitializer {
    public static final Logger PLATFORM_LOG = LoggerFactory.getLogger("STORYANVIL/COGWHEEL/FABRICMC");

    private static void onServerTick(MinecraftServer server) {
        synchronized (queue) {
            ServerWorld world = CogwheelHooks.getOverworldServer();
            try {
                for (int i = 0; i < queue.size(); i++) {
                    Bi<Consumer<ServerWorld>, Integer> e = queue.get(i);
                    if (e.getB() < 2) {
                        try {
                            e.getA().accept(world);
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
                EventBus.getStoryLevel().tick(world);
            } catch (Exception e) {
                CogwheelEngine.LOGGER.warn("StoryLevel onClientTick error", e);
            }
        }
    }

    private static void onClientTick(ClientWorld world) {
        if (!world.getDimensionEntry().getKey().get().getValue().equals(Identifier.of("minecraft", "overworld")))
            return;
        synchronized (clientQueue) {
            try {
                for (int i = 0; i < clientQueue.size(); i++) {
                    Bi<Consumer<ClientWorld>, Integer> e = clientQueue.get(i);
                    if (e.getB() < 2) {
                        e.getA().accept(world);
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

    private static void onBoundEvent(ServerPlayNetworkHandler netHandler, PacketSender sender, MinecraftServer server) {
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
        CogwheelHooks.sendPacket(new AnimationDataBound(sb.toString()), netHandler.player);
        DevEarlySyncPacket.syncFor(netHandler.player, true);
        DevEditorSession.boundColorFor(netHandler.player);
    }

    private static void onUnboundEvent(ServerPlayNetworkHandler handler, MinecraftServer server) {
        DevEditorSession.unboundColorFrom(handler.player);
    }

    private static void onServerStarting(MinecraftServer server) {
        CogwheelHooks.serverStart();
    }

    private static void onServerStopping(MinecraftServer server) {
        CogwheelHooks.serverStop();
    }

    @Override
    public void onInitialize() {
        CogwheelEngine.init(); // Common setup
        FabricRegistry.initialize();
        CommandRegistrationCallback.EVENT.register((d, ra, e) -> CogwheelHooks.commandRegistry(d::register));
        ServerTickEvents.END_SERVER_TICK.register(CogwheelEngineFabric::onServerTick);
        ClientTickEvent.CLIENT_LEVEL_POST.register(CogwheelEngineFabric::onClientTick);
        ServerPlayConnectionEvents.JOIN.register(CogwheelEngineFabric::onBoundEvent);
        ServerPlayConnectionEvents.DISCONNECT.register(CogwheelEngineFabric::onUnboundEvent);
        ServerLifecycleEvents.SERVER_STARTED.register(CogwheelEngineFabric::onServerStarting);
        ServerLifecycleEvents.SERVER_STOPPING.register(CogwheelEngineFabric::onServerStopping);
        FabricDefaultAttributeRegistry.register(FabricRegistry.NPC, NPC.createAttributes());
        CogwheelHooks.packetRegistry(new CogwheelHooks.PacketRegistrar() {
            @Override
            public <T extends StoryPacket<T>> void accept(String id, StoryCodec<T> codec, Class<T> clazz) {
                id = id.toLowerCase(Locale.ENGLISH);
                StoryFabricPacket<T> packet = new StoryFabricPacket<>(id, codec);
                // Register both Client->Server and Server->Client
                PayloadTypeRegistry.playC2S().register(packet.getType().id(), packet);
                PayloadTypeRegistry.playS2C().register(packet.getType().id(), packet);
                ClientPlayNetworking.registerGlobalReceiver(packet.getType().id(), packet::handleClient);
                ServerPlayNetworking.registerGlobalReceiver(packet.getType().id(), packet::handleServer);
                CogwheelEngineFabric.PLATFORM_LOG.debug("Registered packet {} with [id={},class={}]", packet, id, clazz);
            }
        });
    }
}
