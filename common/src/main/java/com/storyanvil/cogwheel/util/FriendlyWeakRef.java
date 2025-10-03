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

package com.storyanvil.cogwheel.util;

import com.storyanvil.cogwheel.infrastructure.err.CogScriptException;
import com.storyanvil.cogwheel.infrastructure.script.DispatchedScript;
import org.jetbrains.annotations.NotNull;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;

public class FriendlyWeakRef<T> extends WeakReference<T> {
    public FriendlyWeakRef(T referent) {
        super(referent);
    }

    public FriendlyWeakRef(T referent, ReferenceQueue<? super T> q) {
        super(referent, q);
    }

    public boolean isStillValid() {
        return !refersTo(null);
    }

    public @NotNull T getSafely(@NotNull DispatchedScript script) throws CogScriptException {
        T r = get();
        if (r == null)
            throw script.wrap(new NullPointerException("FriendlyWeakRef became invalid. This probably means object your script referring to got unloaded."));
        return r;
    }
}
