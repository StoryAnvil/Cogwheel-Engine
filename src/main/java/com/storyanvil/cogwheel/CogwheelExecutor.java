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
