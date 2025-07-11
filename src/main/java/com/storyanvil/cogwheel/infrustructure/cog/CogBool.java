/*
 * StoryAnvil CogWheel Engine
 * Copyright (C) 2025 StoryAnvil
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.storyanvil.cogwheel.infrustructure.cog;

import com.storyanvil.cogwheel.infrustructure.ArgumentData;
import com.storyanvil.cogwheel.infrustructure.CogPropertyManager;
import com.storyanvil.cogwheel.infrustructure.CogStringGen;
import com.storyanvil.cogwheel.infrustructure.DispatchedScript;
import com.storyanvil.cogwheel.util.EasyPropManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CogBool implements CogPropertyManager, CogStringGen<CogBool> {
    private static EasyPropManager MANAGER = new EasyPropManager("bool", CogBool::registerProps);
    public static final CogBool TRUE = new CogBool(true);
    public static final CogBool FALSE = new CogBool(false);
    public static CogBool getInstance(boolean value) {
        return value ? TRUE : FALSE;
    }

    private static void registerProps(EasyPropManager manager) {
        manager.reg("not", (name, args, script, o) -> {
            CogBool bool = (CogBool) o;
            if (bool.value) return FALSE;
            else return TRUE;
        });
        manager.logMe(o -> {
            return ((CogBool) o).value ? "TRUE" : "FALSE";
        });
    }

    private boolean value;
    private CogBool(boolean value) {
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

    @Override
    public String convertToString() {
        return value ? "TRUE" : "FALSE";
    }

    public boolean getValue() {
        return value;
    }

    @Override
    public boolean equalsTo(CogPropertyManager o) {
        if (o instanceof CogBool other) {
            return other.value == this.value;
        }
        return false;
    }

    @Override
    public @Nullable CogBool fromString(@NotNull String s) {
        if (s.equals("TRUE")) return TRUE;
        if (s.equals("FALSE")) return FALSE;
        return null;
    }
}
