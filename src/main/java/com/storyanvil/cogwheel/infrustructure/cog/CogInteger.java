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

public class CogInteger implements CogPropertyManager, CogStringGen<CogInteger> {
    private static EasyPropManager MANAGER = new EasyPropManager("int", CogInteger::registerProps);

    private static void registerProps(EasyPropManager manager) {
        manager.logMe(o -> {
            return String.valueOf(((CogInteger) o).value);
        });
    }

    private int value;
    public CogInteger(int value) {
        this.value = value;
    }
    public CogInteger(String value) {
        this(Integer.parseInt(value));
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
    public boolean equalsTo(CogPropertyManager o) {
        if (o instanceof CogInteger other) {
            return other.value == this.value;
        }
        return false;
    }

    public int getValue() {
        return value;
    }

    @Override
    public String convertToString() {
        return String.valueOf(value);
    }

    @Override
    public @Nullable CogInteger fromString(@NotNull String s) {
        if (s.charAt(0) == '^') {
            return new CogInteger(s.substring(1));
        }
        return null;
    }
}
