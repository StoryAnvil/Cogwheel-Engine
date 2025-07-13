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

package com.storyanvil.cogwheel.infrustructure.cog;

import com.storyanvil.cogwheel.infrustructure.ArgumentData;
import com.storyanvil.cogwheel.infrustructure.CogPropertyManager;
import com.storyanvil.cogwheel.infrustructure.DispatchedScript;
import com.storyanvil.cogwheel.util.EasyPropManager;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public class CogArray<T extends CogPropertyManager> implements CogPropertyManager {
    private static final EasyPropManager MANAGER = new EasyPropManager("array", CogArray::registerProps);

    private ArrayList<T> list;

    public CogArray(ArrayList<T> list) {
        this.list = list;
    }

    private static void registerProps(EasyPropManager manager) {
        manager.reg("get", (name, args, script, o) -> {
            CogArray<CogPropertyManager> array = (CogArray<CogPropertyManager>) o;
            return array.list.get(args.requireInt(0));
        });
        manager.reg("remove", (name, args, script, o) -> {
            CogArray<CogPropertyManager> array = (CogArray<CogPropertyManager>) o;
            array.list.remove(args.requireInt(0));
            return null;
        });
        manager.reg("add", (name, args, script, o) -> {
            CogArray<CogPropertyManager> array = (CogArray<CogPropertyManager>) o;
            array.list.add(args.get(0));
            return null;
        });
    }

    @Override
    public boolean hasOwnProperty(String name) {
        if (name.startsWith("$")) {
            return true;
        }
        return MANAGER.hasOwnProperty(name);
    }

    @Override
    public @Nullable CogPropertyManager getProperty(String name, ArgumentData args, DispatchedScript script) {
        if (name.startsWith("$")) {
            String sub = name.substring(1);
            for (T t : list) {
                if (t.hasOwnProperty(sub)) {
                    t.getProperty(sub, args, script);
                } else throw new RuntimeException("Array element does not have property " + sub);
            }
            return null;
        }
        return MANAGER.get(name).handle(name, args, script, this);
    }

    @Override
    public String convertToString() {
        return list.toString();
    }

    @Override
    public boolean equalsTo(CogPropertyManager o) {
        return false;
    }
}
