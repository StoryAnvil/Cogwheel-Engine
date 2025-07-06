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

package com.storyanvil.cogwheel.infrustructure;

import com.storyanvil.cogwheel.EventBus;

public abstract class StoryAction<T> {
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

    public abstract static class Instant<T> extends StoryAction<T> {
        @Override
        public boolean freeToGo(T myself) {
            hitLabel();
            return true;
        }
    }
}
