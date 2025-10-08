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

package com.storyanvil.cogwheel.cog.obj;

import com.storyanvil.cogwheel.infrastructure.ArgumentData;
import com.storyanvil.cogwheel.infrastructure.err.CogScriptException;
import com.storyanvil.cogwheel.infrastructure.props.CGPM;
import com.storyanvil.cogwheel.infrastructure.props.JWCGPM;
import com.storyanvil.cogwheel.infrastructure.script.DispatchedScript;
import com.storyanvil.cogwheel.infrastructure.util.CogUtils;
import org.jetbrains.annotations.NotNull;

public class CogNullManager extends JWCGPM<CogNullManager> {
    @Override
    public boolean isNull() {
        return true;
    }

    @Override
    public boolean equalsTo(CGPM o) {
        return o instanceof CogNullManager;
    }

    @Override
    public String convertToString() {
        return "null";
    }

    @Override
    public @NotNull CGPM isNull(@NotNull ArgumentData data, @NotNull DispatchedScript script) throws CogScriptException {
        return CogUtils.NULL;
    }
}
