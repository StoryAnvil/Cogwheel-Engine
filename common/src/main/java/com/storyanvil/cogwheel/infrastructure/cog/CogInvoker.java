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
import com.storyanvil.cogwheel.infrastructure.err.CogScriptException;
import com.storyanvil.cogwheel.infrastructure.props.CGPM;
import com.storyanvil.cogwheel.infrastructure.env.CogScriptEnvironment;
import com.storyanvil.cogwheel.infrastructure.module.CMA;
import com.storyanvil.cogwheel.infrastructure.script.DispatchedScript;
import com.storyanvil.cogwheel.util.EasyPropManager;
import com.storyanvil.cogwheel.util.ScriptStorage;
import com.storyanvil.cogwheel.util.WrapperException;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public class CogInvoker implements CGPM {
    private static final EasyPropManager MANAGER = new EasyPropManager("callable", CogInvoker::registerProps);

    private PropertyHandler invoker;

    private CogInvoker(PropertyHandler invoker) {
        this.invoker = invoker;
    }

    private static void registerProps(EasyPropManager manager) {
        manager.reg("invoke", (name, args, script, o) -> {
            CogInvoker cogCallable = (CogInvoker) o;
            if (cogCallable.invoker == null) return null;
            return cogCallable.invoker.handle(null, args, script, null);
        });
        manager.reg("unlink", (name, args, script, o) -> {
            CogInvoker cogCallable = (CogInvoker) o;
            cogCallable.invoker = null;
            return null;
        });
    }

    @Override
    public boolean hasOwnProperty(String name) {
        return MANAGER.hasOwnProperty(name);
    }

    @Override
    public @Nullable CGPM getProperty(String name, ArgumentData args, DispatchedScript script) throws PreventSubCalling, CogScriptException {
        return MANAGER.get(name, args, script, this);
    }

    @Override
    public boolean equalsTo(CGPM o) {
        return this == o;
    }


    public Runnable unsafeRunnable(ArgumentData args, DispatchedScript script) {
        return () -> {
            try {
                this.invoker.handle("invoke", args, script, this);
            } catch (CogScriptException e) {
                throw new WrapperException(e);
            }
        };
    }

    public static CogInvoker scriptInvoker(Identifier scriptName) {
        return new CogInvoker((name, args, script, o) -> {
            CogScriptEnvironment.dispatchScriptGlobal(scriptName);
            return null;
        });
    }
    public static CogInvoker eventScriptInvoker(Identifier scriptName) {
        return new CogInvoker((name, args, script, o) -> {
            CogScriptEnvironment.dispatchScriptGlobal(scriptName, (ScriptStorage) args.get(0));
            return null;
        });
    }
    public static CogInvoker cmaInvoker(CMA cma, String propertyName) {
        return new CogInvoker((name, args, script, o) -> {
            return cma.call(propertyName, args, script);
        });
    }
    public static CogInvoker genericInvoker(CGPM manager, String propertyName) {
        return new CogInvoker((name, args, script, o) -> {
            return manager.getProperty(propertyName, args, script);
        });
    }
    public static CogInvoker javaInvoker(PropertyHandler handler) {
        return new CogInvoker(handler);
    }
}
