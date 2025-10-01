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

import com.storyanvil.cogwheel.infrastructure.ArgumentData;
import com.storyanvil.cogwheel.infrastructure.cog.PreventSubCalling;
import com.storyanvil.cogwheel.infrastructure.err.CogScriptException;
import com.storyanvil.cogwheel.infrastructure.script.DispatchedScript;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.AccessFlag;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * <h4>(J)ava (W)rapping (C)o(G)(P)roperty (M)anager</h4>
 * <br>
 * This class is used to create {@link CGPM}s using java methods.
 */
public abstract class JWCGPM<T extends JWCGPM<T>> implements CGPM {
    @Override
    public boolean isNull() {
        return false;
    }

    private final Class<T> clazz;
    public JWCGPM(Class<T> clazz) {
        this.clazz = clazz;
    }

    public JWCGPM() {
        //noinspection unchecked
        this.clazz = (Class<T>) getClass();
    }

    @Override
    public @Nullable CGPM getProperty(String name, ArgumentData args, DispatchedScript script) throws PreventSubCalling, CogScriptException {
        try {
            Method method = clazz.getMethod(name, clazz, ArgumentData.class, DispatchedScript.class);
            if (!method.accessFlags().contains(AccessFlag.STATIC))
                throw new NoSuchMethodException("Method is not static!");
            if (!method.accessFlags().contains(AccessFlag.PUBLIC))
                throw new NoSuchMethodException("Method is not public!");
            return (CGPM) method.invoke(null, this, args, script);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw script.wrap(new Exception("No property with name: \"" + name + "\" found for this [\"" + this + "\"] instance of " + clazz.getCanonicalName(), e));
        } catch (ClassCastException e) {
            throw script.wrap(new Exception("Property with name: \"" + name + "\" has invalid return type for this [\"" + this + "\"] instance of " + clazz.getCanonicalName(), e));
        }
    }
}
