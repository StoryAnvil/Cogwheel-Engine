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

import com.storyanvil.cogwheel.util.DoubleValue;
import io.netty.util.concurrent.DefaultThreadFactory;
import net.minecraftforge.event.TickEvent;
import org.jetbrains.annotations.ApiStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class CogwheelExecutor {
    public static final Logger log = LoggerFactory.getLogger("STORYANVIL/COGWHEEL/EXECUTOR");
    private static final ScheduledThreadPoolExecutor poolExecutor = new ScheduledThreadPoolExecutor(1, new DefaultThreadFactory("cogwheel-executor"));

    @ApiStatus.Internal
    protected static void init() {
        poolExecutor.setMaximumPoolSize(1);
    }

    /**
     * Schedules task to be executed as soon as possible on CogwheelExecutor thread
     */
    public static void schedule(Runnable task) {
        poolExecutor.execute(task);
    }
    /**
     * Schedules task to be executed as soon as possible after specified amount of milliseconds on CogwheelExecutor thread
     */
    public static void schedule(Runnable task, int ms) {
        poolExecutor.schedule(task, ms, TimeUnit.MILLISECONDS);
    }
    /**
     * Schedules task to be executed as soon as possible on CogwheelExecutor thread. CogwheelExecutor logger will be provided
     */
    public static void schedule(Consumer<Logger> task) {
        poolExecutor.execute(() -> task.accept(log));
    }
    /**
     * Schedules task to be executed as soon as possible after specified amount of milliseconds on CogwheelExecutor thread. CogwheelExecutor logger will be provided
     */
    public static void schedule(Consumer<Logger> task, int ms) {
        poolExecutor.schedule(() -> task.accept(log), ms, TimeUnit.MILLISECONDS);
    }

    /**
     * Schedules task to be executed as soon as possible on Minecraft's Server thread on nearest server-side level tick
     */
    public static void scheduleTickEvent(Consumer<TickEvent.LevelTickEvent> task) {
        EventBus.queue.add(new DoubleValue<>(task, 0));
    }
    /**
     * Schedules task to be executed after provided amount of ticks on Minecraft's Server thread on nearest server-side level tick
     */
    public static void scheduleTickEvent(Consumer<TickEvent.LevelTickEvent> task, int ticks) {
        EventBus.queue.add(new DoubleValue<>(task, ticks));
    }
}
