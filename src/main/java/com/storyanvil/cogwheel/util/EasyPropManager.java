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

package com.storyanvil.cogwheel.util;

import com.storyanvil.cogwheel.api.Api;
import com.storyanvil.cogwheel.infrastructure.ArgumentData;
import com.storyanvil.cogwheel.infrastructure.CogPropertyManager;
import com.storyanvil.cogwheel.infrastructure.script.DispatchedScript;
import com.storyanvil.cogwheel.infrastructure.cog.CogBool;
import com.storyanvil.cogwheel.infrastructure.cog.PropertyHandler;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;

import java.util.HashMap;
import java.util.function.Function;
import java.util.function.Supplier;

@Api.Internal @ApiStatus.Internal
public class EasyPropManager {
    private static final HashMap<String, PropertyHandler> handlers = new HashMap<>();
    private String manager;
    private final Registrar registrar;

    @Contract(pure = true) @Api.Internal @ApiStatus.Internal
    public EasyPropManager(String manager, Registrar registrar) {
        this.manager = ">" + manager;
        this.registrar = registrar;
    }

    @Api.Internal @ApiStatus.Internal
    public boolean hasOwnProperty(String name) {
        if (this.manager.charAt(0) != '<') {
            this.manager = "<" + this.manager;
            this.reg("toString", (__, args, script, o) -> {
                return ((CogPropertyManager) o).convertToCogString();
            });
            this.reg("equals", (__, args, script, o) -> {
                return CogBool.getInstance(((CogPropertyManager) o).equalsTo(args.get(0)));
            });
            registrar.register(this);
        }
        return handlers.containsKey(manager + name);
    }

    @Api.Internal @ApiStatus.Internal
    public boolean hasOwnProperty(String name, Function<String,Boolean> alt) {
        return this.hasOwnProperty(name) || alt.apply(name);
    }

    @Api.Internal @ApiStatus.Internal
    public PropertyHandler get(String name) {
        return handlers.get(manager + name);
    }
    @Api.Internal @ApiStatus.Internal
    public PropertyHandler get(String name, Function<String,PropertyHandler> alt) {
        PropertyHandler h = this.get(name);
        if (h != null) return h;
        return alt.apply(name);
    }
    @Api.Internal @ApiStatus.Internal
    public CogPropertyManager get(String name, ArgumentData args, DispatchedScript script, Object o) {
        PropertyHandler h = this.get(name);
        return h.handle(name, args, script, o);
    }
    @Api.Internal @ApiStatus.Internal
    public CogPropertyManager get(String name, ArgumentData args, DispatchedScript script, Object o, Supplier<CogPropertyManager> alt) {
        PropertyHandler h = this.get(name);
        if (h != null) return h.handle(name, args, script, o);
        return alt.get();
    }

    @Api.Internal @ApiStatus.Internal
    public void reg(String name, PropertyHandler handler) {
        handlers.put(manager + name, handler);
    }

    @Api.Internal @ApiStatus.Internal
    public interface Registrar {
        @Api.Internal @ApiStatus.Internal
        void register(EasyPropManager manager);
    }
}
