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

package com.storyanvil.cogwheel.infrustructure.cog;

import com.storyanvil.cogwheel.infrustructure.ArgumentData;
import com.storyanvil.cogwheel.infrustructure.CogPropertyManager;
import com.storyanvil.cogwheel.infrustructure.DispatchedScript;
import com.storyanvil.cogwheel.infrustructure.StoryAction;
import com.storyanvil.cogwheel.infrustructure.abilities.StoryActionQueue;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.Objects;

public class CogActionQueue<T> implements CogPropertyManager {
    private WeakReference<StoryActionQueue<T>> actionQueue;

    public CogActionQueue(StoryActionQueue<T> actionQueue) {
        this.actionQueue = new WeakReference<>(actionQueue);
    }

    @Override
    public boolean hasOwnProperty(String name) {
        return false;
    }

    @Override
    public @Nullable CogPropertyManager getProperty(String name, ArgumentData args, DispatchedScript script) {
        return null;
    }

    @Override
    public boolean equalsTo(CogPropertyManager o) {
        if (o instanceof CogActionQueue<?> other) {
            return other.actionQueue.equals(this.actionQueue);
        }
        return false;
    }

    public <R extends T> void addStoryAction(StoryAction<R> action) {
        Objects.requireNonNull(actionQueue.get(), "ActionQueue got unloaded").addStoryAction(action);
    }
}
