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

package com.storyanvil.cogwheel.infrastructure.props;

import com.storyanvil.cogwheel.cog.obj.CogNullManager;
import com.storyanvil.cogwheel.infrastructure.ArgumentData;
import com.storyanvil.cogwheel.infrastructure.cog.CogLike;
import com.storyanvil.cogwheel.infrastructure.cog.CogString;
import com.storyanvil.cogwheel.infrastructure.cog.PreventChainCalling;
import com.storyanvil.cogwheel.infrastructure.err.CogScriptException;
import com.storyanvil.cogwheel.infrastructure.script.DispatchedScript;
import com.storyanvil.cogwheel.infrastructure.testing.TestIgnoreDocs;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * CGPM - CogPropertyManager. This class describes object available to CogScript code
 */
@TestIgnoreDocs
public interface CGPM extends CogLike {
    @Deprecated
    default boolean hasOwnProperty(String name) {
        throw new UnsupportedOperationException("hasOwnProperty is removed!");
    };
    @Nullable CGPM getProperty(String name, ArgumentData args, DispatchedScript script) throws PreventChainCalling, CogScriptException;
    boolean equalsTo(CGPM o);

    CogNullManager nullManager = new CogNullManager();
    @Contract(value = "!null -> param1", pure = true)
    static @NotNull CGPM noNull(@Nullable CGPM manager) {
        if (manager == null) return nullManager;
        return manager;
    }
    default String convertToString() {
        return toString();
    }
    default CogString convertToCogString() {
        return new CogString(convertToString());
    }
    @Override
    default CGPM asCogManager() {
        return this;
    }

    default boolean isNull() {
        return this instanceof CogNullManager;
    }

}
