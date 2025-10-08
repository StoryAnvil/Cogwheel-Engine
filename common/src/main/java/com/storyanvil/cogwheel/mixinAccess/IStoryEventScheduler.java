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

package com.storyanvil.cogwheel.mixinAccess;

import net.minecraft.server.world.ServerWorld;

import java.util.function.Consumer;

public interface IStoryEventScheduler<T> {
    void storyEvent$scheduleEvent(T event);
    void storyEvent$scheduleEvent(T event, int loops);

    public static IStoryEventScheduler<Consumer<ServerWorld>> getScheduler(ServerWorld world) {
        //noinspection unchecked // Provided by mixin
        return (IStoryEventScheduler<Consumer<ServerWorld>>) world;
    }

    public static class Task<T> {
        private int timeLeft;
        private final T event;

        public Task(T event, int timeLeft) {
            this.event = event;
            this.timeLeft = timeLeft;
        }
        public Task(T event) {
            this.event = event;
            this.timeLeft = -1;
        }

        /**
         * @return <code>TRUE</code> if Task is waited enough to be executed. <code>FALSE</code> otherwise
         */
        public boolean decrement() {
            timeLeft--;
            return timeLeft < 1;
        }

        public void setTimeLeft(int timeLeft) {
            this.timeLeft = timeLeft;
        }

        public int getTimeLeft() {
            return timeLeft;
        }

        public T getEvent() {
            return event;
        }
    }
}
