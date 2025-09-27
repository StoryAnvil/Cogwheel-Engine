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

import com.storyanvil.cogwheel.infrastructure.ArgumentData;
import com.storyanvil.cogwheel.infrastructure.CGPM;
import com.storyanvil.cogwheel.infrastructure.script.DispatchedScript;
import com.storyanvil.cogwheel.util.Bi;
import org.jetbrains.annotations.Nullable;

public class CogRange implements ForEachManager {
    private final int offset;
    private final int limit;

    public CogRange(int offset, int limit) {
        this.offset = offset;
        this.limit = limit;
    }

    public CogRange(int limit) {
        this.offset = 0;
        this.limit = limit;
    }

    @Override
    public Object createForEach(DispatchedScript script) {
        return 0;
    }

    @Override
    public Bi<CGPM, Object> getForEach(Object track) {
        Integer i = (Integer) track;
        if (i >= limit) return null;
        return new Bi<>(new CogInteger(offset + i), i + 1);
    }

    @Override
    public boolean hasOwnProperty(String name) {
        return false;
    }

    @Override
    public @Nullable CGPM getProperty(String name, ArgumentData args, DispatchedScript script) throws PreventSubCalling {
        return null;
    }

    @Override
    public boolean equalsTo(CGPM o) {
        return false;
    }
}
