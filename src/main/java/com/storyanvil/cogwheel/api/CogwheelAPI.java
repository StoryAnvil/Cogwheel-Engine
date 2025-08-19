/*
 *
 *  * StoryAnvil CogWheel Engine
 *  * Copyright (C) 2025 StoryAnvil
 *  *
 *  * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *  *
 *  * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 *  *
 *  * You should have received a copy of the GNU Lesser General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.storyanvil.cogwheel.api;

import com.storyanvil.cogwheel.infrastructure.CogPropertyManager;
import com.storyanvil.cogwheel.infrastructure.DispatchedScript;
import com.storyanvil.cogwheel.infrastructure.env.CogScriptEnvironment;
import com.storyanvil.cogwheel.registry.CogwheelRegistries;
import com.storyanvil.cogwheel.util.ScriptLineHandler;
import com.storyanvil.cogwheel.util.ScriptStorage;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public class CogwheelAPI {
    /**
     * Fires event for all environments. ({@link com.storyanvil.cogwheel.infrastructure.cog.CogManifest} can be used in script to subscribe to events)
     * @param event resource location to identify event type
     * @param storage script storage (default variables) for script. see {@link com.storyanvil.cogwheel.infrastructure.cog.CogEventCallback}
     */
    @Api.Stable(since = "2.0.0")
    public static void fireEvent(ResourceLocation event, ScriptStorage storage) {
        CogScriptEnvironment.dispatchEventGlobal(event, storage);
    }

    /**
     * Runs CogScript(aka {@link com.storyanvil.cogwheel.infrastructure.DispatchedScript}) by ResourceLocation
     * <br>
     * To run script when you have environment ({@link CogScriptEnvironment}) already use {@link CogScriptEnvironment#dispatchScript(String)}
     * @param rl Namespace is used to identify script's environment, Path is used by environment to find the script
     */
    @Api.Stable(since = "2.0.0")
    public static void runScript(ResourceLocation rl) {
        CogScriptEnvironment.dispatchScriptGlobal(rl);
    }

    /**
     * Runs CogScript(aka {@link com.storyanvil.cogwheel.infrastructure.DispatchedScript}) by ResourceLocation.
     * <br>
     * To run script when you have environment ({@link CogScriptEnvironment}) already use {@link CogScriptEnvironment#dispatchScript(String, ScriptStorage)}
     * @param rl Namespace is used to identify script's environment, Path is used by environment to find the script
     * @param storage script storage (default variables) for script
     */
    @Api.Stable(since = "2.0.0")
    public static void runScript(ResourceLocation rl, ScriptStorage storage) {
        CogScriptEnvironment.dispatchScriptGlobal(rl, storage);
    }

    /**
     * Registries line handler for {@link DispatchedScript}s.
     * @apiNote Namespace must not be "StoryAnvil" or "Cogwheel"
     */
    @Api.Stable(since = "2.0.0")
    public static void registerScriptLineHandler(@NotNull ScriptLineHandler handler) {
        CogwheelRegistries.register(handler);
    }

    /**
     * Registries Default Variable for all {@link DispatchedScript}s.
     * @apiNote Name must not be "StoryAnvil" or "Cogwheel"
     */
    @Api.Stable(since = "2.0.0")
    public static void registerDefaultScriptVariables(@NotNull String name, @NotNull Function<DispatchedScript, CogPropertyManager> f) {
        CogwheelRegistries.register(name, f);
    }
}
