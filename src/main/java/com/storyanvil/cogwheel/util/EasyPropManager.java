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

import com.storyanvil.cogwheel.infrustructure.CogPropertyManager;
import com.storyanvil.cogwheel.infrustructure.cog.CogBool;
import com.storyanvil.cogwheel.infrustructure.cog.PropertyHandler;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;

import java.util.HashMap;

@ApiStatus.Internal
public class EasyPropManager {
    private static final HashMap<String, PropertyHandler> handlers = new HashMap<>();
    private String manager;
    private final Registrar registrar;

    @Contract(pure = true)
    public EasyPropManager(String manager, Registrar registrar) {
        this.manager = ">" + manager;
        this.registrar = registrar;
    }

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
    public PropertyHandler get(String name) {
        return handlers.get(manager + name);
    }
    public void reg(String name, PropertyHandler handler) {
        handlers.put(manager + name, handler);
    }

    public interface Registrar {
        void register(EasyPropManager manager);
    }
}
