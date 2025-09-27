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

package com.storyanvil.cogwheel;

import com.storyanvil.cogwheel.api.Api;
import com.storyanvil.cogwheel.config.CogwheelConfig;
import com.storyanvil.cogwheel.infrastructure.env.DefaultEnvironment;
import com.storyanvil.cogwheel.infrastructure.env.LibraryEnvironment;
import com.storyanvil.cogwheel.infrastructure.env.WorldEnvironment;
import com.storyanvil.cogwheel.infrastructure.script.StreamExecutionScript;
import com.storyanvil.cogwheel.util.Bi;
import com.storyanvil.cogwheel.util.StoryUtils;
import io.netty.util.concurrent.DefaultThreadFactory;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.server.world.ServerWorld;
import org.jetbrains.annotations.ApiStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

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
    public static void scheduleTickEvent(Consumer<ServerWorld> task) {
        synchronized (EventBus.queue) {
            EventBus.queue.add(new Bi<>(task, 0));
        }
    }
    /**
     * Schedules task to be executed after provided amount of ticks on Minecraft's Server thread on nearest server-side level tick
     */
    @Api.Stable(since = "2.0.0")
    public static void scheduleTickEvent(Consumer<ServerWorld> task, int ticks) {
        synchronized (EventBus.queue) {
            EventBus.queue.add(new Bi<>(task, ticks));
        }
    }
    /**
     * Schedules task to be executed as soon as possible on Minecraft's Render thread on nearest client-side level tick
     */
    @Api.Stable(since = "2.0.0")
    public static void scheduleTickEventClientSide(Consumer<ClientWorld> task) {
        synchronized (EventBus.clientQueue) {
            EventBus.clientQueue.add(new Bi<>(task, 0));
        }
    }
    /**
     * Schedules task to be executed after provided amount of ticks on Minecraft's Render thread on nearest client-side level tick
     */
    @Api.Stable(since = "2.0.0")
    public static void scheduleTickEventClientSide(Consumer<ClientWorld> task, int ticks) {
        synchronized (EventBus.clientQueue) {
            EventBus.clientQueue.add(new Bi<>(task, ticks));
        }
    }

    private static DefaultEnvironment defaultEnvironment;
    private static HashMap<String, LibraryEnvironment> libraryEnvironments;
    private static WorldEnvironment worldEnvironment;
    private static StreamExecutionScript chatConsole;

    public static void serverStart() {
        log.info("Creating CogScript default environments...");
        defaultEnvironment = new DefaultEnvironment();
        worldEnvironment = new WorldEnvironment();
        if (libraryEnvironments != null) {
            libraryEnvironments.clear();
        }
        libraryEnvironments = new HashMap<>();
        File libs = new File(CogwheelHooks.getConfigFolder(), "cog-libs/");
        File scripts = new File(CogwheelHooks.getConfigFolder(), "cog/");
        File unpackedLibraries = new File(libs, ".cog");
        if (!libs.exists()) libs.mkdir();
        if (!scripts.exists()) scripts.mkdir();
        if (unpackedLibraries.exists()) {
            StoryUtils.deleteDirectory(unpackedLibraries);
        }
        unpackedLibraries.mkdir();
        CogwheelConfig.reload();
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
            LibraryEnvironment environment = new LibraryEnvironment(library);
            libraryEnvironments.put(library, environment);
        }

        log.info("Environments will be notified of initialization");
        defaultEnvironment.dispatchScript("init.sa");
        worldEnvironment.dispatchScript("init.sa");
        for (LibraryEnvironment environment : libraryEnvironments.values()) {
            if (!environment.init(new File(unpackedLibraries, environment.getUniqueIdentifier()))) {
                libraryEnvironments.remove(environment.getUniqueIdentifier());
                environment.dispose();
            }
            environment.dispatchScript("init.sa");
        }
        createNewConsole();
        switch (CogwheelHooks.performVersionCheck()) {
            case 0: {
                log.info("Update checker failed...");
                break;
            }
            case 1: {
                log.info("Cogwheel Engine is up-to-date");
                break;
            }
            case 2: {
                //noinspection LoggingSimilarMessage
                log.warn("============= [ COGWHEEL ENGINE ] =============");
                log.warn(" Your version of Cogwheel Engine is outdated   ");
                log.warn(" Check modrinth to find new updates.           ");
                log.warn(" https://modrinth.com/mod/cogwheel-engine      ");
                //noinspection LoggingSimilarMessage
                log.warn("============= [ COGWHEEL ENGINE ] =============");
                break;
            }
        }
    }

    public static void createNewConsole() {
        log.info("Created new console script");
        chatConsole = new StreamExecutionScript(defaultEnvironment);
        chatConsole.setScriptName("Console-" + UUID.randomUUID().toString().toUpperCase());
    }

    public static StreamExecutionScript getChatConsole() {
        return chatConsole;
    }

    public static void serverStop() {
        log.info("Disposing all CogScript environments...");
        defaultEnvironment.dispose();
        defaultEnvironment = null;
        worldEnvironment.dispose();
        worldEnvironment = null;
        for (LibraryEnvironment environment : libraryEnvironments.values()) {
            environment.dispose();
        }
        libraryEnvironments = null;
    }

    @Api.Stable(since = "2.0.0")
    public static DefaultEnvironment getDefaultEnvironment() {
        return defaultEnvironment;
    }

    @Api.Stable(since = "2.1.0")
    public static WorldEnvironment getWorldEnvironment() {
        return worldEnvironment;
    }

    @Api.Stable(since = "2.0.0")
    public static LibraryEnvironment getLibraryEnvironment(String namespace) {
        return libraryEnvironments.get(namespace);
    }

    @Api.Experimental(since = "2.0.0")
    public static Collection<LibraryEnvironment> getLibraryEnvironments() {
        return libraryEnvironments.values();
    }
}
