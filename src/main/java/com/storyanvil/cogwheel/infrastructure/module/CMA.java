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
import com.storyanvil.cogwheel.infrastructure.script.DispatchedScript;
import com.storyanvil.cogwheel.infrastructure.cog.PreventSubCalling;
import com.storyanvil.cogwheel.util.EasyPropManager;
import com.storyanvil.cogwheel.util.ScriptStorage;
import org.jetbrains.annotations.Nullable;

/**
 * CogModuleAccessor
 */
public class CMA implements CogPropertyManager {
    private static final EasyPropManager MANAGER = new EasyPropManager("cma", CMA::registerProps);

    private static void registerProps(EasyPropManager manager) {
        manager.reg("put", (name, args, script, o) -> {
            CMA cma = (CMA) o;
            cma.storage.put(args.getString(0), args.get(1));
            return null;
        });
        manager.reg("get", (name, args, script, o) -> {
            CMA cma = (CMA) o;
            return cma.storage.get(args.getString(0));
        });
    }

    private final CogModule parent;
    private final ScriptStorage storage;

    public CMA(CogModule parent) {
        this.parent = parent;
        this.storage = new ScriptStorage();
    }

    @Override
    public boolean hasOwnProperty(String name) {
        return MANAGER.hasOwnProperty(name, parent::_hasOwnProperty);
    }

    @Override
    public @Nullable CogPropertyManager getProperty(String name, ArgumentData args, DispatchedScript script) throws PreventSubCalling {
        return MANAGER.get(name, args, script, this, () -> parent._getProperty(name, args, script, this));
    }

    @Override
    public boolean equalsTo(CogPropertyManager o) {
        return o == this;
    }

    @Override
    public String toString() {
        return "CMA{" +
                "parent=" + parent +
                ", storage=" + storage +
                '}';
    }

    public CogPropertyManager call(String propertyName, ArgumentData args, DispatchedScript script) {
        return parent._getProperty(propertyName, args, script, this);
    }
}
