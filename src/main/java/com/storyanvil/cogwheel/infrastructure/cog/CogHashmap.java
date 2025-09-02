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

package com.storyanvil.cogwheel.infrastructure.cog;

import com.storyanvil.cogwheel.api.Api;
import com.storyanvil.cogwheel.infrastructure.ArgumentData;
import com.storyanvil.cogwheel.infrastructure.CogPropertyManager;
import com.storyanvil.cogwheel.infrastructure.script.DispatchedScript;
import com.storyanvil.cogwheel.util.EasyPropManager;
import com.storyanvil.cogwheel.util.ScriptStorage;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CogHashmap implements CogPropertyManager {
    private static final EasyPropManager MANAGER = new EasyPropManager("hashmap", CogHashmap::registerProps);

    @Contract(pure = true) @Api.Stable(since = "2.0.0")
    public CogHashmap(ScriptStorage value) {
        this.value = value;
    }
    @Contract(pure = true) @Api.Stable(since = "2.0.0")
    public CogHashmap() {
        this.value = new ScriptStorage();
    }

    public ScriptStorage getValue() {
        return value;
    }

    private final ScriptStorage value;
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
