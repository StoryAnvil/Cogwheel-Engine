/*
 *
 *  * StoryAnvil CogWheel Engine
 *  * Copyright (C) 2025 StoryAnvil
 *  *
 *  * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *  *
 *  * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 *  *
 *  * You should have received a copy of the GNU Lesser General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.storyanvil.cogwheel.network.devui;

import com.storyanvil.cogwheel.data.StoryCodec;
import com.storyanvil.cogwheel.data.StoryPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;

import static com.storyanvil.cogwheel.CogwheelEngine.MODID;

public class DevNetwork {
    public static final Logger log = LoggerFactory.getLogger("STORYANVIL/COGWHEEL/NETWORK");
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel DEV_BRIDGE = NetworkRegistry.newSimpleChannel(
            ResourceLocation.fromNamespaceAndPath(MODID, "dev-bridge"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );
    private static final AtomicInteger PACKET_ID = new AtomicInteger(0);
    public static void init() {
        register(DevEarlySyncPacket.class, DevEarlySyncPacket.CODEC);
        register(DevConsoleCode.class, DevConsoleCode.CODEC);
        register(DevOpenFile.class, DevOpenFile.CODEC);
        register(DevEditorLine.class, DevEditorLine.CODEC);
        register(DevEditorState.class, DevEditorState.CODEC);
        register(DevEditorUserDelta.class, DevEditorUserDelta.CODEC);
        register(DevResyncRequest.class, DevResyncRequest.CODEC);
    }

    private static <T extends StoryPacket> void register(@NotNull Class<T> clazz, @NotNull StoryCodec<T> codec) {
        DEV_BRIDGE.registerMessage(PACKET_ID.incrementAndGet(), clazz,
                codec.encoder(), codec.decoder(), T::handle);
    }

    public static void sendToServer(Object msg) {
        DEV_BRIDGE.sendToServer(msg);
    }
    public static void sendFromServer(Object msg) {
        DEV_BRIDGE.send(PacketDistributor.ALL.noArg(), msg);
    }
    public static void sendFromServer(ServerPlayer plr, Object msg) {
        DEV_BRIDGE.send(PacketDistributor.PLAYER.with(() -> plr), msg);
    }
}
