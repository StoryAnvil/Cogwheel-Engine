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
import com.storyanvil.cogwheel.data.StoryPacket;
import com.storyanvil.cogwheel.fabric.client.CogwheelEngineFabricClient;
import com.storyanvil.cogwheel.fabric.client.NPCModel;
import com.storyanvil.cogwheel.fabric.data.StoryFabricParcel;
import com.storyanvil.cogwheel.infrastructure.props.CGPM;
import com.storyanvil.cogwheel.infrastructure.cog.CogPrimalType;
import com.storyanvil.cogwheel.registry.PlatformRegistry;
import com.storyanvil.cogwheel.util.PlatformType;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.io.File;

@SuppressWarnings("unused")
public class CogwheelHooksImpl {
    public static File getConfigFolder() {
        return FabricLoader.getInstance().getConfigDir().toFile();
    }

    public static byte performVersionCheck() {
        CogwheelEngineFabric.PLATFORM_LOG.info("Version checker is not available on fabric");
        return 0;
    }

    public static CGPM getLevelData(String key) {
        throw new AssertionError();
    }


    public static void putLevelData(String key, CogPrimalType value) {
        throw new AssertionError();
    }

    public static <T extends StoryPacket<T>> void sendPacket(StoryPacket<T> packet, ServerPlayerEntity plr) {
        ServerPlayNetworking.send(plr, new StoryFabricParcel<T>(packet.getAsData()));
    }
    public static <T extends StoryPacket<T>> void sendPacketToServer(StoryPacket<T> packet) {
        ClientPlayNetworking.send(new StoryFabricParcel<T>(packet.getAsData()));
    }
    public static <T extends StoryPacket<T>> void sendPacketToEveryone(StoryPacket<T> packet) {
        StoryFabricParcel<T> parcel = new StoryFabricParcel<T>(packet.getAsData());
        for (ServerPlayerEntity plr : CogwheelHooks.getOverworldServer().getServer().getPlayerManager().getPlayerList()) {
            ServerPlayNetworking.send(plr, parcel);
        }
    }

    public static KeyBinding getDevUIBind() {
        return CogwheelEngineFabricClient.devUI;
    }

    public static void setAnimationData(Identifier[] locations) {
        NPCModel.animationSources = locations;
    }

    public static void startupMessage(String message) {
        CogwheelEngineFabric.PLATFORM_LOG.info("STARTUP - {}", message);
    }

    public static PlatformRegistry createRegistry(String modid) {
        return new FabricPlatformRegistry(modid);
    }

    public static PlatformType getPlatform() {
        return PlatformType.FABRIC;
    }

    public static String getVersion() {
        return "unknown-version";
    }
}
