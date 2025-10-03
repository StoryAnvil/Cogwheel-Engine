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

import com.storyanvil.cogwheel.api.Api;

public interface StoryActionQueueAccessor<T> {

    @Api.ServerSideOnly
    public <Q extends T> void addStoryAction(StoryAction<Q> action);

    /**
     * Only call if you 100% sure that provided {@link StoryAction} can be cast to {@code StoryAction<? extends T>} safely
     */
    @Api.ServerSideOnly
    default void addStoryActionChecked(StoryAction<?> action) {
        //noinspection unchecked
        addStoryAction((StoryAction<? extends T>) action);
    }

    @Api.ServerSideOnly
    default <Q extends T> StoryAction<Q> addChained(StoryAction<Q> action) {
        addStoryAction(action);
        return action;
    };

    /**
     * Only call if you 100% sure that provided {@link StoryAction} can be cast to {@code StoryAction<? extends T>} safely
     */
    @Api.ServerSideOnly
    default <Q extends T> StoryAction<Q> addChainedChecked(StoryAction<?> actionUncast) {
        //noinspection unchecked
        StoryAction<Q> action = (StoryAction<Q>) actionUncast;
        addStoryAction(action);
        return action;
    };
}
