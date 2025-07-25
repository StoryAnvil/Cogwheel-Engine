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
import com.storyanvil.cogwheel.infrustructure.CogStringGen;
import com.storyanvil.cogwheel.infrustructure.DispatchedScript;
import com.storyanvil.cogwheel.util.EasyPropManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CogString implements CogPropertyManager, CogStringGen<CogString> {
    private static EasyPropManager MANAGER = new EasyPropManager("string", CogString::registerProps);

    public CogString(char c) {
        this(String.valueOf(c));
    }

    private static void registerProps(EasyPropManager manager) {
        manager.logMe(o -> {
            return ((CogString) o).value;
        });
        manager.reg("append", (name, args, script, o) -> {
            CogString str = (CogString) o;
            for (int i = 0; i < args.size(); i++) {
                str.value += args.getString(i);
            }
            return str;
        });
        manager.reg("format", (name, args, script, o) -> {
            CogString str = (CogString) o;
            for (int i = 0; i < args.size(); i++) {
                str.value = str.value.replaceFirst("\\{}", args.getString(i));
            }
            return str;
        });
        manager.reg("toUpperCase", (name, args, script, o) -> {
            CogString str = (CogString) o;
            str.value = str.value.toUpperCase();
            return str;
        });
        manager.reg("toLowerCase", (name, args, script, o) -> {
            CogString str = (CogString) o;
            str.value = str.value.toLowerCase();
            return str;
        });
        manager.reg("charAt", (name, args, script, o) -> {
            CogString str = (CogString) o;
            return new CogString(str.value.charAt(args.requireInt(0)));
        });
        manager.reg("substring", (name, args, script, o) -> {
            CogString str = (CogString) o;
            if (args.size() == 1)
                return new CogString(str.value.substring(args.requireInt(0)));
            else return new CogString(str.value.substring(args.requireInt(0), args.requireInt(1)));
        });
        manager.reg("replace", (name, args, script, o) -> {
            CogString str = (CogString) o;
            str.value = str.value.replace(args.getString(0), args.getString(1));
            return str;
        });
        manager.reg("indexOf", (name, args, script, o) -> {
            CogString str = (CogString) o;
            return new CogInteger(str.value.indexOf(args.getString(0)));
        });
        manager.reg("clone", (name, args, script, o) -> {
            CogString str = (CogString) o;
            return new CogString(str.value);
        });
    }

    private String value;
    public CogString(String value) {
        this.value = value;
    }

    @Override
    public boolean hasOwnProperty(String name) {
        return MANAGER.hasOwnProperty(name);
    }

    @Override
    public @Nullable CogPropertyManager getProperty(String name, ArgumentData args, DispatchedScript script) {
        return MANAGER.get(name).handle(name, args, script, this);
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equalsTo(CogPropertyManager o) {
        if (o instanceof CogString other) {
            return other.value.equals(this.value);
        }
        return false;
    }

    @Override
    public String convertToString() {
        return value;
    }

    @Override
    public @Nullable CogString fromString(@NotNull String s) {
        char head = s.charAt(0);
        char tail = s.charAt(s.length() - 1);
        if (head == '"' && tail == '"') {
            return new CogString(s.substring(1, s.length() - 1));
        }
        return null;
    }
}
