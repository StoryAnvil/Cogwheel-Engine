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

package com.storyanvil.cogwheel.infrustructure.cog;

import com.storyanvil.cogwheel.infrustructure.ArgumentData;
import com.storyanvil.cogwheel.infrustructure.CogPropertyManager;
import com.storyanvil.cogwheel.infrustructure.DispatchedScript;
import com.storyanvil.cogwheel.util.EasyPropManager;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

public class CogHashmap implements CogPropertyManager {
    private static final EasyPropManager MANAGER = new EasyPropManager("hashmap", CogHashmap::registerProps);

    @Contract(pure = true)
    public CogHashmap(HashMap<String, CogPropertyManager> value) {
        this.value = value;
    }
    @Contract(pure = true)
    public CogHashmap() {
        this.value = new HashMap<>();
    }

    public HashMap<String, CogPropertyManager> getValue() {
        return value;
    }

    private final HashMap<String, CogPropertyManager> value;
    private static void registerProps(@NotNull EasyPropManager manager) {
        manager.reg("get", (name, args, script, o) -> {
            CogHashmap hashmap = (CogHashmap) o;
            return hashmap.value.get(args.get(0).convertToString());
        });
        manager.reg("put", (name, args, script, o) -> {
            CogHashmap hashmap = (CogHashmap) o;
            return hashmap.value.put(args.get(0).convertToString(), args.get(1));
        });
        manager.reg("clear", (name, args, script, o) -> {
            CogHashmap hashmap = (CogHashmap) o;
            hashmap.value.clear();
            return hashmap;
        });
        manager.reg("containsKey", (name, args, script, o) -> {
            CogHashmap hashmap = (CogHashmap) o;
            return CogBool.getInstance(hashmap.value.containsKey(args.get(0).convertToString()));
        });
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
        if (o instanceof CogHashmap other)
            return other.value.equals(value);
        return false;
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}
