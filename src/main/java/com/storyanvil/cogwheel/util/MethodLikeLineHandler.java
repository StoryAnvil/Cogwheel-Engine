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

package com.storyanvil.cogwheel.util;

import com.storyanvil.cogwheel.CogwheelEngine;
import com.storyanvil.cogwheel.infrustructure.DispatchedScript;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class MethodLikeLineHandler {
    private final String methodName;
    private final String namespace;
    private final String sub;

    /**
     * Constructs ScriptLineHandler that filters line automatically.
     * <br>Only lines in following format will be passed to MethodLikeLineHandler#methodHandler:
     * <br><code>namespace.methodName(args)</code>
     */
    public MethodLikeLineHandler(String methodName, String namespace) {
        this.methodName = methodName;
        this.namespace = namespace;
        if (namespace.equals(CogwheelEngine.MODID)) {
            this.sub = "Cogwheel." + methodName + "(";
        } else {
            this.sub = namespace + "." + methodName + "(";
        }
    }

    public @NotNull ResourceLocation getResourceLocation() {
        return ResourceLocation.fromNamespaceAndPath(namespace, "method/" + methodName.toLowerCase());
    }

    public abstract Bi<Boolean, Boolean> methodHandler(@NotNull String args, @Nullable String label, @NotNull DispatchedScript script) throws Exception;

    public void labelUnsupported(@Nullable String label) {
        if (label != null) throw new IllegalArgumentException("Label unsupported for method: " + getResourceLocation());
    }

    public String getSub() {
        return sub;
    }
}
