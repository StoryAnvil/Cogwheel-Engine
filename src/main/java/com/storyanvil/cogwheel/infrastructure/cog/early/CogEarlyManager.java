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

package com.storyanvil.cogwheel.infrastructure.cog.early;

import com.storyanvil.cogwheel.CogwheelEngine;
import com.storyanvil.cogwheel.data.SyncArray;
import com.storyanvil.cogwheel.infrastructure.ArgumentData;
import com.storyanvil.cogwheel.infrastructure.CogPropertyManager;
import com.storyanvil.cogwheel.infrastructure.cog.PreventSubCalling;
import com.storyanvil.cogwheel.infrastructure.script.DispatchedScript;
import com.storyanvil.cogwheel.util.EasyPropManager;
import org.jetbrains.annotations.Nullable;

public class CogEarlyManager implements CogPropertyManager {
    private static final EasyPropManager MANAGER = new EasyPropManager("early", manager -> {
        manager.reg("createRegistry", (name, args, script, o) -> {
            CogEarlyRegistry registry = new CogEarlyRegistry(args.getString(0));
            ((CogEarlyManager) o).registries.add(registry);
            return registry;
        });
    }, CogwheelEngine.EARLY_MANAGER);

    private final SyncArray<CogEarlyRegistry> registries;
    public CogEarlyManager(SyncArray<CogEarlyRegistry> registries) {
        this.registries = registries;
    }

    @Override
    public boolean hasOwnProperty(String name) {
        return MANAGER.hasOwnProperty(name);
    }

    @Override
    public @Nullable CogPropertyManager getProperty(String name, ArgumentData args, DispatchedScript script) throws PreventSubCalling {
        return MANAGER.get(name).handle(name, args, script, this);
    }

    @Override
    public boolean equalsTo(CogPropertyManager o) {
        return o instanceof CogEarlyManager;
    }
}
