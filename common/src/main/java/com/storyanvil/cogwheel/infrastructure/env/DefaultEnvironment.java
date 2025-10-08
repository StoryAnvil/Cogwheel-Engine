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

package com.storyanvil.cogwheel.infrastructure.env;

import com.storyanvil.cogwheel.api.Api;
import com.storyanvil.cogwheel.infrastructure.storyact.StoryAction;
import com.storyanvil.cogwheel.util.StoryUtils;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public final class DefaultEnvironment extends CogScriptEnvironment {
    @Api.Internal @ApiStatus.Internal
    public HashSet<Identifier> serverSideAnimations = new HashSet<>();
    private final HashMap<String, Consumer<Integer>> dialogs;
    private final HashMap<String, ArrayList<BiConsumer<String, StoryAction<?>>>> labelListeners = new HashMap<>();

    public DefaultEnvironment() {
        super();
        dialogs = new HashMap<>();
    }

    @Override
    public String getScript(String name) {
        return "cog/" + name;
    }

    @Override
    public String getUniqueIdentifier() {
        return "default_environment";
    }

    public void registerDialog(String id, Consumer<Integer> callback) {
        dialogs.put(id, callback);
    }

    public HashMap<String, Consumer<Integer>> getDialogs() {
        return dialogs;
    }

    @Override
    public boolean canBeEdited() {
        return true;
    }

    public void hitLabel(String label, StoryAction<?> action) {
        if (labelListeners.containsKey(label)) {
            ArrayList<BiConsumer<String, StoryAction<?>>> c = labelListeners.get(label);
            for (BiConsumer<String, StoryAction<?>> closeable : c) {
                if (closeable != null)
                    closeable.accept(label, action);
            }
        }
    }

    public void registerLabelListener(String label, BiConsumer<String, StoryAction<?>> closeable) {
        if (labelListeners.containsKey(label)) {
            labelListeners.get(label).add(closeable);
        } else {
            labelListeners.put(label, StoryUtils.arrayListWith(closeable));
        }
    }
}
