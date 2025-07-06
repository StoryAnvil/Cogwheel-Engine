/*
 * StoryAnvil CogWheel Engine
 * Copyright (C) 2025 StoryAnvil
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.storyanvil.cogwheel.util;

import com.storyanvil.cogwheel.CogwheelEngine;
import com.storyanvil.cogwheel.infrustructure.DispatchedScript;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public abstract class MethodLikeLineHandler implements ScriptLineHandler {
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
            this.sub = methodName + "(";
        } else {
            this.sub = namespace + "." + methodName + "(";
        }
    }

    @Override
    public @NotNull ResourceLocation getResourceLocation() {
        return ResourceLocation.fromNamespaceAndPath(namespace, "method/" + methodName);
    }

    @Override
    public @NotNull DoubleValue<Boolean, Boolean> handle(@NotNull String line, @NotNull DispatchedScript script) throws Exception {
        if (line.startsWith(sub) && line.endsWith(")")) {
            return methodHandler(line.substring(sub.length(), line.length() - sub.length() - 1), script);
        }
        return ScriptLineHandler.ignore();
    }

    public abstract DoubleValue<Boolean, Boolean> methodHandler(@NotNull String args, @NotNull DispatchedScript script) throws Exception;
}
