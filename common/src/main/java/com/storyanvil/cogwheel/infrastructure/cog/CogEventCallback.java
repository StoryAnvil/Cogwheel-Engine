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

package com.storyanvil.cogwheel.infrastructure.cog;

import com.storyanvil.cogwheel.api.Api;
import com.storyanvil.cogwheel.infrastructure.ArgumentData;
import com.storyanvil.cogwheel.infrastructure.CGPM;
import com.storyanvil.cogwheel.infrastructure.script.DispatchedScript;
import com.storyanvil.cogwheel.util.EasyPropManager;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CogEventCallback implements CGPM {
    private static final EasyPropManager MANAGER = new EasyPropManager("event", CogEventCallback::registerProps);

    private static void registerProps(@NotNull EasyPropManager manager) {
        manager.reg("setCanceled", (name, args, script, o) -> {
            CogEventCallback callback = (CogEventCallback) o;
            callback.canceled = args.requireBoolean(0);
            return callback;
        });
    }

    private boolean canceled = false;

    @Contract(pure = true) @Api.Experimental(since = "2.0.0")
    public CogEventCallback() {
    }

    @Override
    public boolean hasOwnProperty(String name) {
        return true; // Props need to be checked dynamically
    }

    @Override
    public @Nullable CGPM getProperty(String name, ArgumentData args, DispatchedScript script) {
        if (MANAGER.hasOwnProperty(name))
            return MANAGER.get(name).handle(name, args, script, this);
        String key = "event_" + name; // do not access event variables directly. Use Cogwheel.getEvent().VariableName() instead
        if (script.hasKey(key))
            return script.get(key);
        throw new RuntimeException("Property " + name + " does not exist in CogEventCallback");
    }

    @Override
    public boolean equalsTo(CGPM o) {
        return false;
    }

    public boolean isCanceled() {
        return canceled;
    }
}
