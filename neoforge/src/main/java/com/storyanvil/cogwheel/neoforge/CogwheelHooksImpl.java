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
import com.storyanvil.cogwheel.data.StoryPacket;
import com.storyanvil.cogwheel.infrastructure.props.CGPM;
import com.storyanvil.cogwheel.infrastructure.cog.CogPrimalType;
import com.storyanvil.cogwheel.neoforge.client.NPCModel;
import com.storyanvil.cogwheel.neoforge.data.StoryNeoParcel;
import com.storyanvil.cogwheel.registry.PlatformRegistry;
import com.storyanvil.cogwheel.util.PlatformType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.VersionChecker;
import net.neoforged.fml.loading.progress.StartupNotificationManager;
import net.neoforged.neoforge.network.PacketDistributor;
import org.apache.maven.artifact.versioning.ArtifactVersion;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

@SuppressWarnings("unused")
public class CogwheelHooksImpl {
    public static byte performVersionCheck() {
        Optional<? extends ModContainer> container = ModList.get().getModContainerById(CogwheelEngine.MODID);
        if (container.isEmpty()) return 0;
        switch (VersionChecker.getResult(container.get().getModInfo()).status()) {
            case UP_TO_DATE, BETA -> {
                return 1;
            }
            case OUTDATED, BETA_OUTDATED -> {
                return 2;
            }
            default -> {
                return 0;
            }
        }
    }
    
    public static CGPM getLevelData(String key) {
        throw new AssertionError();
    }

    
    public static void putLevelData(String key, CogPrimalType value) {
        throw new AssertionError();
    }

    public static File getConfigFolder() {
        try {
            return new File(MinecraftClient.getInstance().runDirectory.getCanonicalFile(), "config");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T extends StoryPacket<T>> void sendPacket(StoryPacket<T> packet, ServerPlayerEntity plr) {
        PacketDistributor.sendToPlayer(plr, new StoryNeoParcel<T>(packet.getAsData()));
    }
    public static <T extends StoryPacket<T>> void sendPacketToServer(StoryPacket<T> packet) {
        PacketDistributor.sendToServer(new StoryNeoParcel<T>(packet.getAsData()));
    }
    public static <T extends StoryPacket<T>> void sendPacketToEveryone(StoryPacket<T> packet) {
        PacketDistributor.sendToAllPlayers(new StoryNeoParcel<T>(packet.getAsData()));
    }

    public static KeyBinding getDevUIBind() {
        return NeoRegistry.OPEN_DEVUI.get();
    }

    public static void setAnimationData(Identifier[] locations) {
        NPCModel.animationSources = locations;
    }

    public static void startupMessage(String message) {
        StartupNotificationManager.addModMessage(message);
        CogwheelEngineNeoForge.PLATFORM_LOG.info("STARTUP - {}", message);
    }

    public static PlatformRegistry createRegistry(String modid) {
        return new NeoPlatformRegistry(modid);
    }

    public static PlatformType getPlatform() {
        return PlatformType.NEOFORGE;
    }

    public static String getVersion() {
        //noinspection OptionalGetWithoutIsPresent
        ArtifactVersion version = ModList.get().getModContainerById(CogwheelEngine.MODID).get().getModInfo().getVersion();
        return version.getMajorVersion() + "." + version.getMinorVersion() + "." + version.getIncrementalVersion() + ":" + version.getBuildNumber();
    }
}
