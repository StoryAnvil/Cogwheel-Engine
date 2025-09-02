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
import com.storyanvil.cogwheel.util.EasyPropManager;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public final class CogInteger implements CogPropertyManager, CogPrimalType {
    private static final EasyPropManager MANAGER = new EasyPropManager("int", CogInteger::registerProps);

    private static void registerProps(@NotNull EasyPropManager manager) {
        manager.reg("add", (name, args, script, o) -> {
            CogInteger i = (CogInteger) o;
            i.value += args.requireInt(0);
            return i;
        });
        manager.reg("subtract", (name, args, script, o) -> {
            CogInteger i = (CogInteger) o;
            i.value -= args.requireInt(0);
            return i;
        });
        manager.reg("multiply", (name, args, script, o) -> {
            CogInteger i = (CogInteger) o;
            i.value *= args.requireInt(0);
            return i;
        });
        manager.reg("divide", (name, args, script, o) -> {
            CogInteger i = (CogInteger) o;
            i.value /= args.requireInt(0);
            return i;
        });
        manager.reg("clamp", (name, args, script, o) -> {
            CogInteger i = (CogInteger) o;
            int min = args.requireInt(0);
            int max = args.requireInt(1);
            if (i.value < min) {
                i.value = min;
            }
            if (i.value > max) {
                i.value = max;
            }
            return i;
        });
        manager.reg("abs", (name, args, script, o) -> {
            CogInteger i = (CogInteger) o;
            i.value = Math.abs(i.value);
            return i;
        });
        manager.reg("toDouble", (name, args, script, o) -> {
            CogInteger i = (CogInteger) o;
            return new CogDouble(i.value);
        });
        manager.reg("clone", (name, args, script, o) -> {
            CogInteger str = (CogInteger) o;
            return new CogInteger(str.value);
        });
        manager.reg("smaller", (name, args, script, o) -> {
            CogInteger str = (CogInteger) o;
            return CogBool.getInstance(str.value < args.requireInt(0));
        });
        manager.reg("bigger", (name, args, script, o) -> {
            CogInteger str = (CogInteger) o;
            return CogBool.getInstance(str.value > args.requireInt(0));
        });
    }

    private int value;
    @Contract(pure = true) @Api.Stable(since = "2.0.0")
    public CogInteger(int value) {
        this.value = value;
    }
    @Api.Stable(since = "2.0.0")
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

    @Contract(value = "null -> false", pure = true)
    @Override
    public boolean equalsTo(CogPropertyManager o) {
        if (o instanceof CogInteger other) {
            return other.value == this.value;
        }
        return false;
    }

    @Contract(pure = true)
    public int getValue() {
        return value;
    }

    @Contract(pure = true)
    @Override
    public @NotNull String convertToString() {
        return String.valueOf(value);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }

    @Contract(pure = true)
    @Override
    public byte getPrimalID() {
        return (byte) -217;
    }

    @Override
    public void putPrimal(@NotNull CompoundTag tag, String key) {
        tag.putInt(key, value);
    }
}
