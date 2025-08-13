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
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class CogBool implements CogPropertyManager, CogPrimalType {
    private static final EasyPropManager MANAGER = new EasyPropManager("bool", CogBool::registerProps);
    public static final CogBool TRUE = new CogBool(true);
    public static final CogBool FALSE = new CogBool(false);
    @Contract(pure = true)
    public static CogBool getInstance(boolean value) {
        return value ? TRUE : FALSE;
    }

    private static void registerProps(@NotNull EasyPropManager manager) {
        manager.reg("not", (name, args, script, o) -> {
            CogBool bool = (CogBool) o;
            if (bool.value) return FALSE;
            else return TRUE;
        });
        manager.reg("and", (name, args, script, o) -> {
            CogBool bool = (CogBool) o;
            return getInstance(args.requireBoolean(0) && bool.value);
        });
        manager.reg("or", (name, args, script, o) -> {
            CogBool bool = (CogBool) o;
            return getInstance(args.requireBoolean(0) || bool.value);
        });
        manager.reg("xor", (name, args, script, o) -> {
            CogBool bool = (CogBool) o;
            return getInstance(args.requireBoolean(0) != bool.value);
        });
    }

    private boolean value;
    @Contract(pure = true)
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

    @Contract(pure = true)
    @Override
    public @NotNull String convertToString() {
        return value ? "TRUE" : "FALSE";
    }

    @Contract(pure = true)
    public boolean getValue() {
        return value;
    }

    @Contract(value = "null -> false", pure = true)
    @Override
    public boolean equalsTo(CogPropertyManager o) {
        if (o instanceof CogBool other) {
            return other.value == this.value;
        }
        return false;
    }

    @Contract(pure = true)
    @Override
    public int hashCode() {
        return value ? 0 : 1;
    }

    @Contract(pure = true)
    @Override
    public byte getPrimalID() {
        return (byte) -218;
    }

    @Override
    public void putPrimal(@NotNull CompoundTag tag, String key) {
        tag.putBoolean(key, value);
    }
}
