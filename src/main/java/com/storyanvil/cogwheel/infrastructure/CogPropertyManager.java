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

package com.storyanvil.cogwheel.infrastructure;

import com.storyanvil.cogwheel.infrastructure.cog.CogLike;
import com.storyanvil.cogwheel.infrastructure.cog.CogString;
import com.storyanvil.cogwheel.infrastructure.cog.JavaType;
import com.storyanvil.cogwheel.infrastructure.cog.PreventSubCalling;
import com.storyanvil.cogwheel.infrastructure.script.DispatchedScript;
import com.storyanvil.cogwheel.util.EasyPropManager;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface CogPropertyManager extends CogLike {
    boolean hasOwnProperty(String name);
    @Nullable CogPropertyManager getProperty(String name, ArgumentData args, DispatchedScript script) throws PreventSubCalling;
    boolean equalsTo(CogPropertyManager o);

    NullManager nullManager = new NullManager();
    @Contract(value = "!null -> param1", pure = true)
    static @NotNull CogPropertyManager noNull(@Nullable CogPropertyManager manager) {
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
    default CogPropertyManager asCogManager() {
        return this;
    }

    class NullManager implements CogPropertyManager {
        private static final EasyPropManager MANAGER = new EasyPropManager("nil", NullManager::registerProps);

        private static void registerProps(EasyPropManager manager) {
        }

        @Contract(pure = true)
        private NullManager() {}

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
            return o instanceof NullManager;
        }

        @Override
        public String convertToString() {
            return "NULL";
        }
    }
}
