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
import com.storyanvil.cogwheel.infrastructure.cog.CogDouble;
import com.storyanvil.cogwheel.infrastructure.cog.CogInteger;
import com.storyanvil.cogwheel.infrastructure.cog.CogString;
import com.storyanvil.cogwheel.infrastructure.err.CogScriptException;
import com.storyanvil.cogwheel.infrastructure.props.CGPM;
import com.storyanvil.cogwheel.infrastructure.props.JWCGPM;
import com.storyanvil.cogwheel.infrastructure.props.JWCGPM_Method;
import com.storyanvil.cogwheel.infrastructure.script.DispatchedScript;
import com.storyanvil.cogwheel.infrastructure.util.CogUtils;
import com.storyanvil.cogwheel.mixinAccess.IStoryEntity;
import com.storyanvil.cogwheel.util.FriendlyWeakRef;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
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

    @JWCGPM_Method(arguments = {})
    public @NotNull CGPM getScoreboardName(@NotNull ArgumentData data, @NotNull DispatchedScript script) throws CogScriptException {
        return CogUtils.makeCogString(ref.getSafely(script).getNameForScoreboard());
    }

    @JWCGPM_Method(arguments = {})
    public @NotNull CogDouble getX(@NotNull ArgumentData data, @NotNull DispatchedScript script) throws CogScriptException {
        return CogUtils.makeCogDouble(ref.getSafely(script).getX());
    }

    @JWCGPM_Method(arguments = {})
    public @NotNull CogDouble getY(@NotNull ArgumentData data, @NotNull DispatchedScript script) throws CogScriptException {
        return CogUtils.makeCogDouble(ref.getSafely(script).getY());
    }

    @JWCGPM_Method(arguments = {})
    public @NotNull CogDouble getZ(@NotNull ArgumentData data, @NotNull DispatchedScript script) throws CogScriptException {
        return CogUtils.makeCogDouble(ref.getSafely(script).getZ());
    }

    @JWCGPM_Method(arguments = {CogString.class})
    public @NotNull CogBool hasTag(@NotNull ArgumentData data, @NotNull DispatchedScript script) throws CogScriptException {
        return CogUtils.makeCogBool(ref.getSafely(script).getCommandTags().contains(data.requireString(0)));
    }

    @JWCGPM_Method(arguments = {CogString.class})
    public @NotNull CogBool addTag(@NotNull ArgumentData data, @NotNull DispatchedScript script) throws CogScriptException {
        return CogUtils.makeCogBool(ref.getSafely(script).addCommandTag(data.requireString(0)));
    }

    @JWCGPM_Method(arguments = {CogString.class})
    public @NotNull CogBool removeTag(@NotNull ArgumentData data, @NotNull DispatchedScript script) throws CogScriptException {
        return CogUtils.makeCogBool(ref.getSafely(script).removeCommandTag(data.requireString(0)));
    }

    @JWCGPM_Method(arguments = {})
    public @NotNull CogNullManager kill(@NotNull ArgumentData data, @NotNull DispatchedScript script) throws CogScriptException {
        T entity = ref.getSafely(script);
        entity.kill((ServerWorld) entity.getWorld());
        ref.clear();
        return CogUtils.NULL;
    }

    @JWCGPM_Method(arguments = {CogString.class, CogInteger.class})
    public @NotNull CogNullManager putInt(@NotNull ArgumentData data, @NotNull DispatchedScript script) throws CogScriptException {
        ((IStoryEntity) ref.getSafely(script)).storyEntity$putInt("cg_" + data.requireString(0),
                data.requireInt(1)).storyEntity$syncIfOnServer();
        return CogUtils.NULL;
    }

    @JWCGPM_Method(arguments = {CogString.class, CogString.class})
    public @NotNull CogNullManager putString(@NotNull ArgumentData data, @NotNull DispatchedScript script) throws CogScriptException {
        ((IStoryEntity) ref.getSafely(script)).storyEntity$putString("cg_" + data.requireString(0),
                data.requireString(1)).storyEntity$syncIfOnServer();
        return CogUtils.NULL;
    }

    @JWCGPM_Method(arguments = {CogString.class, CogBool.class})
    public @NotNull CogNullManager putBool(@NotNull ArgumentData data, @NotNull DispatchedScript script) throws CogScriptException {
        ((IStoryEntity) ref.getSafely(script)).storyEntity$putBoolean("cg_" + data.requireString(0),
                data.requireBoolean(1)).storyEntity$syncIfOnServer();
        return CogUtils.NULL;
    }

    @JWCGPM_Method(arguments = {CogString.class, CogInteger.class})
    public @NotNull CogInteger getInt(@NotNull ArgumentData data, @NotNull DispatchedScript script) throws CogScriptException {
        return CogUtils.makeCogInt(((IStoryEntity) ref.getSafely(script)).storyEntity$getInt("cg_" + data.requireString(0), data.requireInt(1)));
    }

    @JWCGPM_Method(arguments = {CogString.class, CogString.class})
    public @NotNull CogString getString(@NotNull ArgumentData data, @NotNull DispatchedScript script) throws CogScriptException {
        return CogUtils.makeCogString(((IStoryEntity) ref.getSafely(script)).storyEntity$getString("cg_" + data.requireString(0), data.requireString(1)));
    }

    @JWCGPM_Method(arguments = {CogString.class, CogBool.class})
    public @NotNull CogBool getBoolean(@NotNull ArgumentData data, @NotNull DispatchedScript script) throws CogScriptException {
        return CogUtils.makeCogBool(((IStoryEntity) ref.getSafely(script)).storyEntity$getBoolean("cg_" + data.requireString(0), data.requireBoolean(1)));
    }
}
