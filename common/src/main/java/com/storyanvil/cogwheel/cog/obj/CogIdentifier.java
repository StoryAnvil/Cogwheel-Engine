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
import com.storyanvil.cogwheel.infrastructure.cog.CogString;
import com.storyanvil.cogwheel.infrastructure.err.CogScriptException;
import com.storyanvil.cogwheel.infrastructure.props.CGPM;
import com.storyanvil.cogwheel.infrastructure.props.JWCGPM;
import com.storyanvil.cogwheel.infrastructure.props.JWCGPM_Method;
import com.storyanvil.cogwheel.infrastructure.script.DispatchedScript;
import com.storyanvil.cogwheel.infrastructure.util.CogUtils;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

public class CogIdentifier extends JWCGPM<CogIdentifier> {
    private Identifier identifier;

    public CogIdentifier(Identifier identifier) {
        this.identifier = identifier;
    }

    public Identifier getValue() {
        return identifier;
    }

    @Override
    public boolean equalsTo(CGPM o) {
        if (!(o instanceof CogIdentifier other)) return false;
        return identifier.equals(other.identifier);
    }

    @Override
    public String convertToString() {
        return identifier.toString();
    }

    // ========== === [ JWCGPM METHODS START HERE ] === ========== \\
    @JWCGPM_Method(arguments = {})
    public @NotNull CogString getNamespace(@NotNull ArgumentData data, @NotNull DispatchedScript script) throws CogScriptException {
        return CogUtils.makeCogString(identifier.getNamespace());
    }
    @JWCGPM_Method(arguments = {})
    public @NotNull CogString getPath(@NotNull ArgumentData data, @NotNull DispatchedScript script) throws CogScriptException {
        return CogUtils.makeCogString(identifier.getPath());
    }
    @JWCGPM_Method(arguments = {CogString.class})
    public @NotNull CogNullManager setNamespace(@NotNull ArgumentData data, @NotNull DispatchedScript script) throws CogScriptException {
        identifier = Identifier.of(data.requireString(0), identifier.getPath());
        return CogUtils.NULL;
    }
    @JWCGPM_Method(arguments = {CogString.class})
    public @NotNull CogNullManager setPath(@NotNull ArgumentData data, @NotNull DispatchedScript script) throws CogScriptException {
        identifier = Identifier.of(identifier.getNamespace(), data.requireString(0));
        return CogUtils.NULL;
    }

    @JWCGPM_Method(arguments = {CogString.class, CogString.class})
    public @NotNull CogNullManager setAll(@NotNull ArgumentData data, @NotNull DispatchedScript script) throws CogScriptException {
        identifier = Identifier.of(data.requireString(0), data.requireString(1));
        return CogUtils.NULL;
    }
}
