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

package com.storyanvil.cogwheel.infrustructure.env;

import com.storyanvil.cogwheel.CogwheelExecutor;
import com.storyanvil.cogwheel.infrustructure.CogPropertyManager;
import com.storyanvil.cogwheel.infrustructure.CogScriptDispatcher;
import com.storyanvil.cogwheel.infrustructure.DispatchedScript;
import com.storyanvil.cogwheel.util.WeakList;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;

import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

public abstract class CogScriptEnvironment {
    private final HashMap<ResourceLocation, List<String>> eventSubscribers;

    public CogScriptEnvironment() {
        this.eventSubscribers = new HashMap<>();
    }

    /**
     * Halts execution of all scripts from this environment
     */
    @ApiStatus.Internal
    public void dispose() {
        WeakList<DispatchedScript> scripts = DispatchedScript.MONITOR.getObjects();
        for (int i = 0; i < scripts.size(); i++) {
            DispatchedScript script = scripts.get(i);
            if (script == null) {
                i--;
                scripts.remove(i);
                continue;
            }
            if (script.getEnvironment() == this) {
                script.haltExecution();
            }
        }
    }
    public abstract void dispatchScript(String name);
    public abstract void dispatchScript(String name, HashMap<String, CogPropertyManager> storage);

    /**
     * Finds correct CogScriptEnvironment and dispatched script.
     * @param rl ResourceLocation. Namespace determine what environment will be used. Path is the script name
     */
    public static void dispatchScriptGlobal(String rl) {
        ResourceLocation loc = ResourceLocation.parse(rl);
        CogScriptEnvironment environment = null;
        if (loc.getNamespace().equalsIgnoreCase("DEF")) {
            environment = CogwheelExecutor.getDefaultEnvironment();
        } else {
            environment = CogwheelExecutor.getLibraryEnvironment(loc.getNamespace());
        }
        if (environment == null) throw new RuntimeException("Dispatch Failure! No environment found");
        environment.dispatchScript(loc.getPath());
    }
    /**
     * Finds correct CogScriptEnvironment and dispatched script.
     * @param rl ResourceLocation. Namespace determine what environment will be used. Path is the script name
     */
    public static void dispatchScriptGlobal(String rl, HashMap<String, CogPropertyManager> storage) {
        ResourceLocation loc = ResourceLocation.parse(rl);
        CogScriptEnvironment environment = null;
        if (loc.getNamespace().equalsIgnoreCase("DEF")) {
            environment = CogwheelExecutor.getDefaultEnvironment();
        } else {
            environment = CogwheelExecutor.getLibraryEnvironment(loc.getNamespace());
        }
        if (environment == null) throw new RuntimeException("Dispatch Failure! No environment found");
        environment.dispatchScript(loc.getPath(), storage);
    }

    public static class DefaultEnvironment extends CogScriptEnvironment {
        private final HashMap<String, Consumer<Integer>> dialogs;

        public DefaultEnvironment() {
            super();
            dialogs = new HashMap<>();
        }

        @Override
        public void dispatchScript(String name) {
            CogScriptDispatcher.dispatch("cog/" + name, this);
        }

        @Override
        public void dispatchScript(String name, HashMap<String, CogPropertyManager> storage) {
            CogScriptDispatcher.dispatch("cog/" + name, storage, this);
        }

        public void registerDialog(String id, Consumer<Integer> callback) {
            dialogs.put(id, callback);
        }
        public HashMap<String, Consumer<Integer>> getDialogs() {
            return dialogs;
        }
    }
    public static class LibraryEnvironment extends CogScriptEnvironment {
        private final String name;

        public LibraryEnvironment(String name) {
            super();
            this.name = name;
        }

        @Override
        public void dispatchScript(String name) {
            CogScriptDispatcher.dispatch("cog-libs/" + this.name + "/" + name, this);
        }

        @Override
        public void dispatchScript(String name, HashMap<String, CogPropertyManager> storage) {
            CogScriptDispatcher.dispatch("cog-libs/" + this.name + "/" + name, storage, this);
        }

        public String getName() {
            return name;
        }
    }
}
