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

package com.storyanvil.cogwheel.infrastructure.cog;

import com.storyanvil.cogwheel.api.Api;
import com.storyanvil.cogwheel.infrastructure.ArgumentData;
import com.storyanvil.cogwheel.infrastructure.CogPropertyManager;
import com.storyanvil.cogwheel.infrastructure.script.DispatchedScript;
import com.storyanvil.cogwheel.util.Bi;
import com.storyanvil.cogwheel.util.EasyPropManager;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class CogArray implements CogPropertyManager, ForEachManager {
    private static final EasyPropManager MANAGER = new EasyPropManager("array", CogArray::registerProps);

    private ArrayList<CogPropertyManager> list;

    @Contract(pure = true)
    private CogArray(ArrayList<CogPropertyManager> list) {
        this.list = list;
    }

    private static void registerProps(@NotNull EasyPropManager manager) {
        manager.reg("get", (name, args, script, o) -> {
            CogArray array = (CogArray) o;
            return array.list.get(args.requireInt(0));
        });
        manager.reg("remove", (name, args, script, o) -> {
            CogArray array = (CogArray) o;
            array.list.remove(args.requireInt(0));
            return null;
        });
        manager.reg("add", (name, args, script, o) -> {
            CogArray array = (CogArray) o;
            array.list.add(args.get(0));
            return null;
        });
        manager.reg("size", (name, args, script, o) -> {
            CogArray array = (CogArray) o;
            return new CogInteger(array.list.size());
        });
    }

    @Override
    public boolean hasOwnProperty(@NotNull String name) {
        if (name.startsWith("$")) {
            return true;
        }
        return MANAGER.hasOwnProperty(name);
    }

    @Override
    public @Nullable CogPropertyManager getProperty(@NotNull String name, ArgumentData args, DispatchedScript script) {
        if (name.startsWith("$")) {
            String sub = name.substring(1);
            for (CogPropertyManager t : list) {
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

    @Contract(value = "_ -> new", pure = true) @Api.Stable(since = "2.0.0")
    public static @NotNull CogArray getInstance(ArrayList<CogPropertyManager> t) {
        return new CogArray(t);
    }
    @Contract("_ -> new") @Api.Stable(since = "2.0.0")
    public static <Q extends CogPropertyManager> @NotNull CogArray getInstance(List<Q> t) {
        return new CogArray(new ArrayList<>(t));
    }
    @Contract("_ -> new") @Api.Stable(since = "2.0.0")
    public static <Q extends CogPropertyManager> @NotNull CogArray getInstance(@NotNull Iterable<Q> t) {
        ArrayList<CogPropertyManager> list = new ArrayList<>();
        for (Q q : t) {
            list.add(q);
        }
        return new CogArray(list);
    }
    @Contract("_ -> new") @Api.Stable(since = "2.0.0")
    public static <Q extends CogPropertyManager> @NotNull CogArray getInstance(Q q) {
        ArrayList<CogPropertyManager> list = new ArrayList<>();
        list.add(q);
        return new CogArray(list);
    }
    @Contract("_ -> new") @Api.Stable(since = "2.0.0")
    public static @NotNull CogArray convertInstance(@NotNull Iterable<String> s) {
        ArrayList<CogPropertyManager> list = new ArrayList<>();
        for (String q : s) {
            list.add(new CogString(q));
        }
        return new CogArray(list);
    }

    @Override
    public Object createForEach(DispatchedScript script) {
        return 0;
    }

    @Override
    public Bi<CogPropertyManager, Object> getForEach(Object track) {
        int i = (int) track;
        if (this.list.size() <= i) return null;
        return new Bi<>(this.list.get(i), i + 1);
    }
}
