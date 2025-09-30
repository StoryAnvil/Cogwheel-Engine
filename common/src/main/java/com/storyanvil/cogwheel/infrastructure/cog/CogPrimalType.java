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

import com.storyanvil.cogwheel.infrastructure.props.CGPM;
import net.minecraft.nbt.NbtCompound;
import org.jetbrains.annotations.Contract;

import java.util.Objects;

public sealed interface CogPrimalType extends CGPM, CogLike permits CogBool, CogString, CogInteger, CogDouble, CogLong {
    byte getPrimalID();
    void putPrimal(NbtCompound tag, String key);
    Object getObjValue();

    @Contract(value = "null -> false", pure = true)
    @Override
    default boolean equalsTo(CGPM o) {
        if (o instanceof CogPrimalType other) {
            return Objects.equals(other.getObjValue(), this.getObjValue());
        }
        return false;
    }
}
