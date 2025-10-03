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

package com.storyanvil.cogwheel.infrastructure.storyact;

import net.minecraft.entity.Entity;

import java.util.ArrayDeque;
import java.util.Queue;

public class StoryActionQueue<T, Q extends T> implements StoryActionQueueAccessor<T> {
    private final Queue<StoryAction<Q>> actionQueue = new ArrayDeque<>();
    private StoryAction<Q> current;
    private final Q myself;

    public StoryActionQueue(Q myself) {
        this.myself = myself;
    }

    public static <S extends Entity> StoryActionQueue<S, ? extends S> importFromEntity(S entity) {
        return new StoryActionQueue<>(entity);
    }

    public void tick() {
        if (current != null) {
            if (current.freeToGo(myself)) {
                current.onExit();
                current = null;
            }
        } else {
            if (actionQueue.isEmpty()) return;
            current = actionQueue.remove();
            current.proceed(myself);
        }
    }

    @Override
    public <S extends T> void addStoryAction(StoryAction<S> action) {
        //noinspection unchecked // <Q> and <S> are the same
        actionQueue.add((StoryAction<Q>) action);
    }
}
