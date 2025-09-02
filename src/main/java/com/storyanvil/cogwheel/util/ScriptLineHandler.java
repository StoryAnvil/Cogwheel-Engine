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

import com.storyanvil.cogwheel.api.Api;
import com.storyanvil.cogwheel.infrastructure.script.DispatchedScript;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

@Api.Stable(since = "2.0.0")
public interface ScriptLineHandler {
    /**
     * Method for handler script's line
     *
     * @param line   line that needs to be executed
     * @param script script executing line
     * @return DoubleValue provided by one of static ScriptLineHandler methods
     */
    @Api.Stable(since = "2.0.0")
    byte handle(@NotNull String line, @NotNull DispatchedScript script) throws Exception;

    /**
     * @return resource location for this ScriptLineHandler. Return value of this method must be constant!
     */
    @Api.Stable(since = "2.0.0")
    @NotNull ResourceLocation getResourceLocation();

    /**
     * Use ScriptLineHandler#ignore method instead
     */
    @ApiStatus.Internal @Api.Internal
    byte ignore = 0x00;
    /**
     * Use ScriptLineHandler#continueReading method instead
     */
    @ApiStatus.Internal @Api.Internal
    byte continueReading = 0x01;
    /**
     * Use ScriptLineHandler#blocking method instead
     */
    @ApiStatus.Internal @Api.Internal
    byte blocking = 0x02;

    /**
     * Use this method if your ScriptLineHandler is not applicable for provided line of CogScript code
     * @return DoubleValue for returning in ScriptLineHandler#handle.
     */
    @Contract(pure = true) @Api.Stable(since = "2.0.0")
    static byte ignore() {
        return ignore;
    }
    /**
     * Use this method if your ScriptLineHandler is applicable for provided line of CogScript code and line was in fact handled so dispatched script can continue executing lines
     * @return DoubleValue for returning in ScriptLineHandler#handle.
     */
    @Contract(pure = true) @Api.Stable(since = "2.0.0")
    static byte continueReading() {
        return continueReading;
    }
    /**
     * Use this method if your ScriptLineHandler is applicable for provided line of CogScript code and line was in fact handled but dispatched script shall not continue line execution.
     * <br>If you are using this make sure to schedule DispatchedScript#lineDispatcher somehow so script will be able to continue execution
     * @return DoubleValue for returning in ScriptLineHandler#handle.
     */
    @Contract(pure = true) @Api.Stable(since = "2.0.0")
    static byte blocking() {
        return blocking;
    }
}
