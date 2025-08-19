/*
 * StoryAnvil CogWheel Engine
 * Copyright (C) 2025 StoryAnvil
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.storyanvil.cogwheel;

import com.storyanvil.cogwheel.api.Api;
import com.storyanvil.cogwheel.infrastructure.env.CogScriptEnvironment;
import com.storyanvil.cogwheel.util.Bi;
import com.storyanvil.cogwheel.util.StoryUtils;
import io.netty.util.concurrent.DefaultThreadFactory;
import net.minecraft.client.Minecraft;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.ApiStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@Mod.EventBusSubscriber(modid = CogwheelEngine.MODID)
public class CogwheelExecutor {
    @Api.Internal @ApiStatus.Internal
    public static final Logger log = LoggerFactory.getLogger("STORYANVIL/COGWHEEL/EXECUTOR");
    private static final ScheduledThreadPoolExecutor poolExecutor = new ScheduledThreadPoolExecutor(2, new DefaultThreadFactory("cogwheel-executor"));
    private static final ScheduledThreadPoolExecutor beltThread = new ScheduledThreadPoolExecutor(1, new DefaultThreadFactory("cogwheel-belt-protocol"));

    @Api.Internal @ApiStatus.Internal
    protected static void init() {
        poolExecutor.setMaximumPoolSize(2);
        beltThread.setMaximumPoolSize(1);
    }

    /**
     * Schedules task to be executed as soon as possible on CogwheelExecutor thread
     */
    @Api.Stable(since = "2.0.0")
    public static void schedule(Runnable task) {
        poolExecutor.execute(task);
    }
    /**
     * Schedules task to be executed as soon as possible after specified amount of milliseconds on CogwheelExecutor thread
     */
    @Api.Stable(since = "2.0.0")
    public static void schedule(Runnable task, int ms) {
        poolExecutor.schedule(task, ms, TimeUnit.MILLISECONDS);
    }

    /**
     * Schedules task to be executed as soon as possible on Minecraft's Server thread on nearest server-side level tick
     */
    @Api.Stable(since = "2.0.0")
    public static void scheduleTickEvent(Consumer<TickEvent.LevelTickEvent> task) {
        synchronized (EventBus.queue) {
            EventBus.queue.add(new Bi<>(task, 0));
        }
    }
    /**
     * Schedules task to be executed after provided amount of ticks on Minecraft's Server thread on nearest server-side level tick
     */
    @Api.Stable(since = "2.0.0")
    public static void scheduleTickEvent(Consumer<TickEvent.LevelTickEvent> task, int ticks) {
        synchronized (EventBus.queue) {
            EventBus.queue.add(new Bi<>(task, ticks));
        }
    }
    /**
     * Schedules task to be executed as soon as possible on Minecraft's Render thread on nearest client-side level tick
     */
    @Api.Stable(since = "2.0.0")
    public static void scheduleTickEventClientSide(Consumer<TickEvent.LevelTickEvent> task) {
        synchronized (EventBus.clientQueue) {
            EventBus.clientQueue.add(new Bi<>(task, 0));
        }
    }
    /**
     * Schedules task to be executed after provided amount of ticks on Minecraft's Render thread on nearest client-side level tick
     */
    @Api.Stable(since = "2.0.0")
    public static void scheduleTickEventClientSide(Consumer<TickEvent.LevelTickEvent> task, int ticks) {
        synchronized (EventBus.clientQueue) {
            EventBus.clientQueue.add(new Bi<>(task, ticks));
        }
    }

    /**
     * Schedules task to be executed as soon as possible on Belt Protocol thread
     */
    @Api.Internal @ApiStatus.Internal
    public static void scheduleBelt(Runnable task) {
        beltThread.execute(task);
    }
    /**
     * Schedules task to be executed as soon as possible after specified amount of milliseconds on Belt Protocol thread
     */
    @Api.Internal @ApiStatus.Internal
    public static void scheduleBelt(Runnable task, int ms) {
        beltThread.schedule(task, ms, TimeUnit.MILLISECONDS);
    }

    private static CogScriptEnvironment.DefaultEnvironment defaultEnvironment;
    private static HashMap<String, CogScriptEnvironment.LibraryEnvironment> libraryEnvironments;

    @SubscribeEvent @Api.Internal @ApiStatus.Internal
    public static void serverStart(ServerStartingEvent event) {
        log.info("Creating CogScript default environment...");
        defaultEnvironment = new CogScriptEnvironment.DefaultEnvironment();
        if (libraryEnvironments != null) {
            libraryEnvironments.clear();
        }
        libraryEnvironments = new HashMap<>();
        File libs = new File(Minecraft.getInstance().gameDirectory, "config/cog-libs/");
        File scripts = new File(Minecraft.getInstance().gameDirectory, "config/cog/");
        File unpackedLibraries = new File(libs, ".cog");
        if (!libs.exists()) libs.mkdir();
        if (!scripts.exists()) scripts.mkdir();
        if (unpackedLibraries.exists()) {
            StoryUtils.deleteDirectory(unpackedLibraries);
        }
        unpackedLibraries.mkdir();
        File[] libFiles = libs.listFiles();
        ArrayList<String> libraryNames = new ArrayList<>();
        if (libFiles != null) {
            for (File lib : libFiles) {
                String name = lib.getName();
                if (name.endsWith(".salc") /* StoryAnvil Locomotive Car */) {
                    File unpacked = new File(unpackedLibraries, name);
                    if (!unpacked.mkdir()) throw new RuntimeException("Failed to create " + unpacked);
                    try {
                        StoryUtils.unpackZip(lib, unpacked);
                        libraryNames.add(name);
                    } catch (IOException e) {
                        throw new RuntimeException("Failed to unpack " + lib, e);
                    }
                } else {
                    log.info("File {} is not StoryAnvil Locomotive Car (aka Cogwheel Engine library)", lib);
                }
            }
        }
        for (String library : libraryNames) {
            CogScriptEnvironment.LibraryEnvironment environment = new CogScriptEnvironment.LibraryEnvironment(library);
            libraryEnvironments.put(library, environment);
        }

        log.info("Environments will be notified of initialization");
        defaultEnvironment.dispatchScript("init.sa");
        for (CogScriptEnvironment.LibraryEnvironment environment : libraryEnvironments.values()) {
            if (!environment.init(new File(unpackedLibraries, environment.getUniqueIdentifier()))) {
                libraryEnvironments.remove(environment.getUniqueIdentifier());
                environment.dispose();
            }
            environment.dispatchScript("init.sa");
        }
    }
    @SubscribeEvent @Api.Internal @ApiStatus.Internal
    public static void serverStop(ServerStoppingEvent event) {
        log.info("Disposing all CogScript environments...");
        defaultEnvironment.dispose();
        defaultEnvironment = null;
        for (CogScriptEnvironment.LibraryEnvironment environment : libraryEnvironments.values()) {
            environment.dispose();;
        }
        libraryEnvironments = null;
    }

    @Api.Stable(since = "2.0.0")
    public static CogScriptEnvironment.DefaultEnvironment getDefaultEnvironment() {
        return defaultEnvironment;
    }

    @Api.Stable(since = "2.0.0")
    public static CogScriptEnvironment.LibraryEnvironment getLibraryEnvironment(String namespace) {
        return libraryEnvironments.get(namespace);
    }

    @Api.Experimental(since = "2.0.0")
    public static Collection<CogScriptEnvironment.LibraryEnvironment> getLibraryEnvironments() {
        return libraryEnvironments.values();
    }
}
