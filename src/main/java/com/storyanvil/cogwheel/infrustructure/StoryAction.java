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

package com.storyanvil.cogwheel.infrustructure;

import com.storyanvil.cogwheel.EventBus;
import com.storyanvil.cogwheel.infrustructure.cog.CogString;
import com.storyanvil.cogwheel.util.EasyPropManager;
import com.storyanvil.cogwheel.util.ObjectMonitor;
import org.jetbrains.annotations.Nullable;

public abstract class StoryAction<T> implements ObjectMonitor.IMonitored, CogPropertyManager {
    private static final ObjectMonitor<StoryAction<?>> MONITOR = new ObjectMonitor<>();
    private String actionLabel = null;
    public abstract void proceed(T myself);
    public abstract boolean freeToGo(T myself);
    @Override
    public String toString() {
        return this.getClass().getName() + "$ACT#";
    }

    public String getActionLabel() {
        return actionLabel;
    }

    public StoryAction<T> setActionLabel(String actionLabel) {
        this.actionLabel = actionLabel;
        return this;
    }

    public void hitLabel() {
        if (actionLabel == null) return;
        EventBus.hitLabel(actionLabel, this);
    };

    public StoryAction() {
        MONITOR.register(this);
    }

    @Override
    public void reportState(StringBuilder sb) {
        sb.append(this);
    }

    private static final EasyPropManager MANAGER = new EasyPropManager("storyAction", StoryAction::registerProps);

    private static void registerProps(EasyPropManager manager) {
        manager.reg("setLabel", (name, args, script, o) -> {
            StoryAction<?> action = (StoryAction<?>) o;
            action.setActionLabel(args.getString(0));
            return action;
        });
        manager.reg("getLabel", (name, args, script, o) -> {
            StoryAction<?> action = (StoryAction<?>) o;
            return new CogString(action.getActionLabel());
        });
    }

    @Override
    public boolean hasOwnProperty(String name) {
        return MANAGER.hasOwnProperty(name);
    }

    @Override
    public @Nullable CogPropertyManager getProperty(String name, ArgumentData args, DispatchedScript script) {
        return MANAGER.get(name).handle(name, args, script, this);
    }

    @Override
    public boolean equalsTo(CogPropertyManager o) {
        return o == this;
    }

    public abstract static class Instant<T> extends StoryAction<T> {
        public Instant() {
            super();
        }

        @Override
        public boolean freeToGo(T myself) {
            hitLabel();
            return true;
        }
    }
}
