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

import com.storyanvil.cogwheel.client.saui.AbstractCogComponent;
import com.storyanvil.cogwheel.client.saui.SAUI;
import com.storyanvil.cogwheel.data.StoryCodec;
import com.storyanvil.cogwheel.infrastructure.CogPropertyManager;
import com.storyanvil.cogwheel.infrastructure.cog.*;
import com.storyanvil.cogwheel.infrastructure.script.DispatchedScript;
import com.storyanvil.cogwheel.infrastructure.env.CogScriptEnvironment;
import com.storyanvil.cogwheel.registry.CogwheelRegistries;
import com.storyanvil.cogwheel.util.ScriptLineHandler;
import com.storyanvil.cogwheel.util.ScriptStorage;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;
import java.util.function.Function;

/**
 * Class for most stable API methods.
 * @apiNote If there is possibility to do something with this class this is a better way
 */
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
     * Runs CogScript(aka {@link DispatchedScript}) by ResourceLocation
     * <br>
     * To run script when you have environment ({@link CogScriptEnvironment}) already use {@link CogScriptEnvironment#dispatchScript(String)}
     * @param rl Namespace is used to identify script's environment, Path is used by environment to find the script
     */
    @Api.Stable(since = "2.0.0")
    public static void runScript(ResourceLocation rl) {
        CogScriptEnvironment.dispatchScriptGlobal(rl);
    }

    /**
     * Runs CogScript(aka {@link DispatchedScript}) by ResourceLocation.
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

    /**
     * Registers CogComponent codec.
     * @apiNote `storyanvil_cogwheel` namespace is not allowed!
     */
    @Api.Stable(since = "2.8.0")
    public static void registerCogComponent(ResourceLocation loc, StoryCodec<AbstractCogComponent> codec) {
        SAUI.registerCogComponent(loc, codec);
    }

    /**
     * Converts objects to their CogScript versions
     */
    @Api.Stable(since = "2.8.0")
    public static CogBool cogify(boolean value) {
        return CogBool.getInstance(value);
    }
    /**
     * Converts objects to their CogScript versions
     */
    @Api.Stable(since = "2.8.0")
    public static CogBool cogify(Boolean value) {
        return value == null ? null : CogBool.getInstance(value);
    }
    /**
     * Converts objects to their CogScript versions
     */
    @Api.Stable(since = "2.8.0")
    public static CogInteger cogify(int value) {
        return new CogInteger(value);
    }
    /**
     * Converts objects to their CogScript versions
     */
    @Api.Stable(since = "2.8.0")
    public static CogInteger cogify(Integer value) {
        return value == null ? null : new CogInteger(value);
    }
    /**
     * Converts objects to their CogScript versions
     */
    @Api.Stable(since = "2.8.0")
    public static CogLong cogify(long value) {
        return new CogLong(value);
    }
    /**
     * Converts objects to their CogScript versions
     */
    @Api.Stable(since = "2.8.0")
    public static CogLong cogify(Long value) {
        return value == null ? null : new CogLong(value);
    }
    /**
     * Converts objects to their CogScript versions
     */
    @Api.Stable(since = "2.8.0")
    public static CogDouble cogify(double value) {
        return new CogDouble(value);
    }
    /**
     * Converts objects to their CogScript versions
     */
    @Api.Stable(since = "2.8.0")
    public static CogDouble cogify(Double value) {
        return value == null ? null : new CogDouble(value);
    }
    /**
     * Converts objects to their CogScript versions
     */
    @Api.Stable(since = "2.8.0")
    public static CogEntity cogify(Entity value) {
        return value == null ? null : new CogEntity(value);
    }
    /**
     * Converts objects to their CogScript versions
     */
    @Api.Stable(since = "2.8.0")
    public static CogPlayer cogify(ServerPlayer value) {
        return value == null ? null : new CogPlayer(new WeakReference<>(value));
    }
    /**
     * Converts objects to their CogScript versions
     */
    @Api.Stable(since = "2.8.0")
    public static CogPropertyManager cogify(CogLike value) {
        return value == null ? null : value.asCogManager();
    }
}
