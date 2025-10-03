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

package com.storyanvil.cogwheel.infrastructure.env;

import com.storyanvil.cogwheel.util.CogwheelExecutor;
import com.storyanvil.cogwheel.CogwheelHooks;
import com.storyanvil.cogwheel.api.Api;
import com.storyanvil.cogwheel.infrastructure.script.DispatchedScript;
import com.storyanvil.cogwheel.registry.CogwheelRegistries;
import com.storyanvil.cogwheel.util.ScriptStorage;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.storyanvil.cogwheel.util.CogwheelExecutor.log;

public abstract class CogScriptEnvironment {
    private final HashMap<Identifier, List<String>> eventSubscribers;

    @Contract(pure = true)
    public CogScriptEnvironment() {
        this.eventSubscribers = new HashMap<>();
    }

    @Api.Stable(since = "2.0.0")
    public static LibraryEnvironment getLibEnvironment(String string) {
        return CogwheelExecutor.getLibraryEnvironment(string);
    }

    @Api.Stable(since = "2.0.0")
    public void dispatchEvent(Identifier event, ScriptStorage storage) {
        if (eventSubscribers.containsKey(event)) {
            List<String> subscribedScripts = eventSubscribers.get(event);
            for (String script : subscribedScripts) {
                dispatchScript(script, storage);
            }
        }
    }
    @Api.Stable(since = "2.0.0")
    public void subscribeForEvent(Identifier event, String scriptName) {
        if (!eventSubscribers.containsKey(event)) {
            eventSubscribers.put(event, new ArrayList<>());
        }
        eventSubscribers.get(event).add(scriptName);
    }
    @Api.Stable(since = "2.0.0")
    public void unsubscribeFromEvent(Identifier event, String scriptName) {
        if (!eventSubscribers.containsKey(event)) {
            eventSubscribers.put(event, new ArrayList<>());
            return;
        }
        eventSubscribers.get(event).remove(scriptName);
    }
    @Api.Stable(since = "2.0.0")
    public void unsubscribeAllFromEvent(Identifier event) {
        eventSubscribers.remove(event);
    }
    @Api.Stable(since = "2.0.0")
    public static void dispatchEventGlobal(Identifier event, ScriptStorage storage) {
        CogwheelExecutor.getDefaultEnvironment().dispatchEvent(event, storage);
        for (CogScriptEnvironment environment : CogwheelExecutor.getLibraryEnvironments()) {
            environment.dispatchEvent(event, storage);
        }
    }

    /**
     * Halts execution of all scripts from this environment
     */
    @ApiStatus.Internal
    public void dispose() {
        log.info("Environment {} is disposing...", getUniqueIdentifier());
        eventSubscribers.clear();
    }
    public boolean canBeEdited() {return false;}
    public abstract void dispatchScript(String name);
    public abstract void dispatchScript(String name, ScriptStorage storage);
    public abstract String getScript(String name);
    public void defaultVariablesFactory(ScriptStorage storage, DispatchedScript script) {
        CogwheelRegistries.putDefaults(storage, script);
    }

    /**
     * Finds correct CogScriptEnvironment and dispatched script.
     * @param rl Identifier. Namespace determine what environment will be used. Path is the script name
     */
    @Api.Internal @ApiStatus.Internal
    public static void dispatchScriptGlobal(String rl) {
        Identifier loc = Identifier.tryParse(rl);
        dispatchScriptGlobal(loc);
    }

    @Api.Experimental(since = "2.0.0")
    public static void dispatchScriptGlobal(Identifier loc) {
        CogScriptEnvironment environment;
        environment = getEnvironment(loc);
        if (environment == null) throw new RuntimeException("Dispatch Failure! No environment found");
        environment.dispatchScript(loc.getPath());
    }

    public static CogScriptEnvironment getEnvironment(Identifier loc) {
        CogScriptEnvironment environment;
        if (loc.getNamespace().equals("def") || loc.getNamespace().equals("minecraft")) {
            environment = CogwheelExecutor.getDefaultEnvironment();
        } else if (loc.getNamespace().equals("wrld")) {
            environment = CogwheelExecutor.getWorldEnvironment();
        } else {
            environment = CogwheelExecutor.getLibraryEnvironment(loc.getNamespace());
        }
        return environment;
    }

    /**
     * Finds correct CogScriptEnvironment and dispatched script.
     * @param rl Identifier. Namespace determine what environment will be used. Path is the script name
     */
    @Api.Internal @ApiStatus.Internal
    public static void dispatchScriptGlobal(String rl, ScriptStorage storage) {
        Identifier loc = Identifier.tryParse(rl);
        dispatchScriptGlobal(loc, storage);
    }

    @Api.Experimental(since = "2.0.0")
    public static void dispatchScriptGlobal(Identifier loc, ScriptStorage storage) {
        CogScriptEnvironment environment = getEnvironment(loc);
        if (environment == null) throw new RuntimeException("Dispatch Failure! No environment found");
        environment.dispatchScript(loc.getPath(), storage);
    }

    @Api.Internal @ApiStatus.Internal
    public static File getScriptFile(Identifier rl) {
        CogScriptEnvironment environment = getEnvironment(rl);
        return new File(CogwheelHooks.getConfigFolder(), environment.getScript(rl.getPath()));
    }

    public abstract String getUniqueIdentifier();

    @Override
    public String toString() {
        return "CogScriptEnvironment{name=" + getUniqueIdentifier() + "}";
    }
}
