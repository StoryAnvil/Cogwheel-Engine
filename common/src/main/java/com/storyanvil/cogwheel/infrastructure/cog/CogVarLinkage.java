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

import com.storyanvil.cogwheel.infrastructure.ArgumentData;
import com.storyanvil.cogwheel.infrastructure.CGPM;
import com.storyanvil.cogwheel.infrastructure.script.DispatchedScript;
import com.storyanvil.cogwheel.util.EasyPropManager;
import org.jetbrains.annotations.Nullable;

public class CogVarLinkage implements CGPM {
    private String variable;

    public CogVarLinkage(String variable) {
        this.variable = variable;
    }

    public void changeLinkage(String variable) {
        this.variable = variable;
    }

    private static final EasyPropManager MANAGER = new EasyPropManager("varlinkage", CogVarLinkage::registerProps);

    private static void registerProps(EasyPropManager manager) {
        manager.reg("get", (name, args, script, o) -> {
            CogVarLinkage linkage = (CogVarLinkage) o;
            return script.get(linkage.variable);
        });
        manager.reg("set", (name, args, script, o) -> {
            CogVarLinkage linkage = (CogVarLinkage) o;
            script.put(linkage.variable, args.get(0));
            return null;
        });
    }

    @Override
    public boolean hasOwnProperty(String name) {
        return MANAGER.hasOwnProperty(name);
    }

    @Override
    public @Nullable CGPM getProperty(String name, ArgumentData args, DispatchedScript script) throws PreventSubCalling {
        return MANAGER.get(name, args, script, this);
    }

    @Override
    public boolean equalsTo(CGPM o) {
        if (o instanceof CogVarLinkage linkage) {
            return linkage.variable.equals(this.variable);
        }
        return false;
    }
}
