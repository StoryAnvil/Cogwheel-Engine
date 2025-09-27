/*
 *
 * StoryAnvil Cogwheel Engine
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
import com.storyanvil.cogwheel.infrastructure.CGPM;
import com.storyanvil.cogwheel.infrastructure.script.DispatchedScript;
import com.storyanvil.cogwheel.util.EasyPropManager;
import net.minecraft.nbt.NbtCompound;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public final class CogDouble implements CGPM, CogPrimalType {
    private static final EasyPropManager MANAGER = new EasyPropManager("double", CogDouble::registerProps);

    private static void registerProps(@NotNull EasyPropManager manager) {
        manager.reg("add", (name, args, script, o) -> {
            CogDouble i = (CogDouble) o;
            i.value += args.requireDouble(0);
            return i;
        });
        manager.reg("subtract", (name, args, script, o) -> {
            CogDouble i = (CogDouble) o;
            i.value -= args.requireDouble(0);
            return i;
        });
        manager.reg("multiply", (name, args, script, o) -> {
            CogDouble i = (CogDouble) o;
            i.value *= args.requireDouble(0);
            return i;
        });
        manager.reg("divide", (name, args, script, o) -> {
            CogDouble i = (CogDouble) o;
            i.value /= args.requireDouble(0);
            return i;
        });
        manager.reg("clamp", (name, args, script, o) -> {
            CogDouble i = (CogDouble) o;
            double min = args.requireDouble(0);
            double max = args.requireDouble(1);
            if (i.value < min) {
                i.value = min;
            }
            if (i.value > max) {
                i.value = max;
            }
            return i;
        });
        manager.reg("abs", (name, args, script, o) -> {
            CogDouble i = (CogDouble) o;
            i.value = Math.abs(i.value);
            return i;
        });
        manager.reg("toInt", (name, args, script, o) -> {
            CogDouble i = (CogDouble) o;
            return new CogInteger(Math.toIntExact(Math.round(i.value)));
        });
        manager.reg("clone", (name, args, script, o) -> {
            CogDouble i = (CogDouble) o;
            return new CogDouble(i.value);
        });
    }

    private double value;
    @Contract(pure = true)
    @Api.Stable(since = "2.0.0")
    public CogDouble(double value) {
        this.value = value;
    }
    @Api.Stable(since = "2.0.0")
    public CogDouble(String value) {
        this(Double.parseDouble(value));
    }

    @Override
    public boolean hasOwnProperty(String name) {
        return MANAGER.hasOwnProperty(name);
    }

    @Override
    public @Nullable CGPM getProperty(String name, ArgumentData args, DispatchedScript script) {
        return MANAGER.get(name).handle(name, args, script, this);
    }

    @Contract(value = "null -> false", pure = true)
    @Override
    public boolean equalsTo(CGPM o) {
        if (o instanceof CogDouble other) {
            return other.value == this.value;
        }
        return false;
    }

    @Contract(pure = true)
    public double getValue() {
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
        return (byte) -216;
    }

    @Override
    public void putPrimal(@NotNull NbtCompound tag, String key) {
        tag.putDouble(key, value);
    }

    @Override
    public Object getObjValue() {
        return value;
    }
}
