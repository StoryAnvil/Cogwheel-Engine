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
import com.storyanvil.cogwheel.infrastructure.cog.CogBool;
import com.storyanvil.cogwheel.infrastructure.err.CogScriptException;
import com.storyanvil.cogwheel.infrastructure.props.JWCGPM;
import com.storyanvil.cogwheel.infrastructure.props.JWCGPM_Method;
import com.storyanvil.cogwheel.infrastructure.script.DispatchedScript;
import com.storyanvil.cogwheel.infrastructure.util.CogUtils;
import com.storyanvil.cogwheel.util.FriendlyWeakRef;
import net.minecraft.entity.Entity;
import org.jetbrains.annotations.NotNull;

public class CogEntity<T extends Entity> extends JWCGPM<CogEntity<T>> {
    private final FriendlyWeakRef<T> ref;

    public CogEntity(T ref) {
        this.ref = new FriendlyWeakRef<>(ref);
    }

    // ========== === [ JWCGPM METHODS START HERE ] === ========== \\
    @JWCGPM_Method(arguments = {})
    public @NotNull CogBool isValid(@NotNull ArgumentData data, @NotNull DispatchedScript script) throws CogScriptException {
        return CogUtils.makeCogBool(this.ref.isStillValid());
    }
}
