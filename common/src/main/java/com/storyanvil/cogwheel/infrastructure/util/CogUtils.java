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

package com.storyanvil.cogwheel.infrastructure.util;

import com.storyanvil.cogwheel.cog.obj.CogNPC;
import com.storyanvil.cogwheel.cog.obj.CogNullManager;
import com.storyanvil.cogwheel.entity.AbstractNPC;
import com.storyanvil.cogwheel.infrastructure.cog.CogBool;
import com.storyanvil.cogwheel.infrastructure.cog.CogInteger;
import com.storyanvil.cogwheel.infrastructure.cog.CogString;
import com.storyanvil.cogwheel.infrastructure.props.CGPM;
import net.minecraft.entity.Entity;

public class CogUtils {
    private CogUtils() {}

    public static final CogNullManager nullObject = CGPM.nullManager;

    public static CogString makeCogString(String s) {
        return new CogString(s);
    }
    public static CogBool makeCogBool(boolean value) {
        return value ? CogBool.TRUE : CogBool.FALSE;
    }

    public static <T extends Entity & AbstractNPC<T>> CogNPC<T> makeCogNPC(AbstractNPC<T> abstractNPC) {
        return new CogNPC<>(abstractNPC);
    }

    public static CGPM makeCogInt(int value) {
        return new CogInteger(value);
    }
}
