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

import com.storyanvil.cogwheel.infrustructure.StoryAction;
import com.storyanvil.cogwheel.infrustructure.abilities.StoryActionQueue;
import com.storyanvil.cogwheel.infrustructure.abilities.StoryChatter;
import com.storyanvil.cogwheel.util.ObjectMonitor;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayDeque;
import java.util.Queue;

public class StoryLevel implements StoryActionQueue<StoryLevel>, StoryChatter, ObjectMonitor.IMonitored {
    private static final ObjectMonitor<StoryLevel> MONITOR = new ObjectMonitor<>();
    @Override
    public <R extends StoryLevel> void addStoryAction(StoryAction<R> action) {
        actionQueue.add(action);
    }

    @Override
    public Queue<StoryAction<? extends StoryLevel>> getActions() {
        return actionQueue;
    }

    @Override
    public void chat(String text) {
        Component c = Component.literal(text);
        for (ServerPlayer player : level.players()) {
            player.sendSystemMessage(c);
        }
    }

    private Queue<StoryAction<? extends StoryLevel>> actionQueue = new ArrayDeque<>();
    private StoryAction current;
    private ServerLevel level;
    public void tick(ServerLevel level) {
        this.level = level;
        if (current != null) {
            if (current.freeToGo(this)) {
                current = null;
            }
        } else {
            if (actionQueue.isEmpty()) return;
            current = actionQueue.remove();
            current.proceed(this);
        }
    }

    public StoryLevel() {
        MONITOR.register(this);
    }

    @Override
    public void reportState(StringBuilder sb) {
        for (StoryAction<?> action : actionQueue) {
            sb.append(action.toString());
        }
        sb.append(">").append(current.toString());
        sb.append(" | ").append(level.toString());
    }
}
