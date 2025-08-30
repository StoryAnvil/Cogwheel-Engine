/*
 *
 *  * StoryAnvil CogWheel Engine
 *  * Copyright (C) 2025 StoryAnvil
 *  *
 *  * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *  *
 *  * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 *  *
 *  * You should have received a copy of the GNU Lesser General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.storyanvil.cogwheel.infrastructure.module;

import com.storyanvil.cogwheel.infrastructure.ArgumentData;
import com.storyanvil.cogwheel.infrastructure.CogPropertyManager;
import com.storyanvil.cogwheel.infrastructure.DispatchedScript;
import com.storyanvil.cogwheel.infrastructure.cog.PreventSubCalling;
import org.jetbrains.annotations.Nullable;

public class CogModuleInstance implements CogPropertyManager {
    private final CogModule parent;

    public CogModuleInstance(CogModule parent) {
        this.parent = parent;
    }

    @Override
    public boolean hasOwnProperty(String name) {
        return parent._hasOwnProperty(name);
    }

    @Override
    public @Nullable CogPropertyManager getProperty(String name, ArgumentData args, DispatchedScript script) throws PreventSubCalling {
        return parent._getProperty(name, args, script, this);
    }

    @Override
    public boolean equalsTo(CogPropertyManager o) {
        return o == this;
    }
}
