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

package com.storyanvil.cogwheel.infrastructure.cog.early;

import com.storyanvil.cogwheel.CogwheelEngine;
import com.storyanvil.cogwheel.infrastructure.ArgumentData;
import com.storyanvil.cogwheel.infrastructure.err.CogScriptException;
import com.storyanvil.cogwheel.infrastructure.props.CGPM;
import com.storyanvil.cogwheel.infrastructure.cog.PreventChainCalling;
import com.storyanvil.cogwheel.infrastructure.script.DispatchedScript;
import com.storyanvil.cogwheel.util.EasyPropManager;
import org.jetbrains.annotations.Nullable;

public class CogEarlyManager implements CGPM {
    private static final EasyPropManager MANAGER = new EasyPropManager("early", manager -> {
        manager.reg("log", (name, args, script, o) -> {
            CogwheelEngine.EARLY.info("{}: {}", script.getScriptName(), args.get(0).convertToString());
            return null;
        });
    }, CogwheelEngine.EARLY_MANAGER);

    @Override
    public boolean hasOwnProperty(String name) {
        return MANAGER.hasOwnProperty(name);
    }

    @Override
    public @Nullable CGPM getProperty(String name, ArgumentData args, DispatchedScript script) throws PreventChainCalling, CogScriptException {
        return MANAGER.get(name).handle(name, args, script, this);
    }

    @Override
    public boolean equalsTo(CGPM o) {
        return o instanceof CogEarlyManager;
    }
}
