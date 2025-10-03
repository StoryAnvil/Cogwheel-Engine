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
import com.storyanvil.cogwheel.infrastructure.storyact.StoryAction;
import com.storyanvil.cogwheel.infrastructure.cog.StoryLevel;
import com.storyanvil.cogwheel.util.*;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;

import java.util.*;
import java.util.function.Consumer;

public class EventBus {
    @Api.Internal @ApiStatus.Internal
    public static final List<Bi<Consumer<ServerWorld>, Integer>> queue = new ArrayList<>();
    @Api.Internal @ApiStatus.Internal
    public static final List<Bi<Consumer<ClientWorld>, Integer>> clientQueue = new ArrayList<>();

    private static final StoryLevel level = new StoryLevel();


    @Api.Internal @ApiStatus.Internal
    public static ArrayList<Identifier> serverSideAnimations = new ArrayList<>();
    private static final HashMap<String, WeakList<LabelCloseable>> labelListeners = new HashMap<>();
    @Api.Internal @ApiStatus.Internal
    public static void hitLabel(String label, StoryAction<?> action) {
        if (labelListeners.containsKey(label)) {
            WeakList<LabelCloseable> c = labelListeners.get(label);
            //noinspection ForLoopReplaceableByForEach
            for (int i = 0; i < c.size(); i++) {
                LabelCloseable closeable = c.get(i);
                if (closeable != null)
                    closeable.close(label, action);
            }
        }
    }
    @Api.Experimental(since = "2.1.0")
    public static void register(String label, LabelCloseable closeable) {
        if (labelListeners.containsKey(label)) {
            labelListeners.get(label).add(closeable);
        } else {
            labelListeners.put(label, new WeakList<>(closeable));
        }
    }

    @Contract(pure = true) @Api.Experimental(since = "2.1.0")
    public static StoryLevel getStoryLevel() {
        return level;
    }

}
