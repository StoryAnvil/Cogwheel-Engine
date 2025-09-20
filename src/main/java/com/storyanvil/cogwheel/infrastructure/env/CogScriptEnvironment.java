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

package com.storyanvil.cogwheel.infrastructure.env;

import com.storyanvil.cogwheel.CogwheelExecutor;
import com.storyanvil.cogwheel.EventBus;
import com.storyanvil.cogwheel.api.Api;
import com.storyanvil.cogwheel.infrastructure.CogScriptDispatcher;
import com.storyanvil.cogwheel.infrastructure.script.DispatchedScript;
import com.storyanvil.cogwheel.infrastructure.cog.*;
import com.storyanvil.cogwheel.registry.CogwheelRegistries;
import com.storyanvil.cogwheel.util.ScriptStorage;
import com.storyanvil.cogwheel.util.WeakList;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.storyanvil.cogwheel.CogwheelExecutor.log;

public abstract class CogScriptEnvironment {
    private final HashMap<ResourceLocation, List<String>> eventSubscribers;
    private final long creationTime;

    @Contract(pure = true)
    public CogScriptEnvironment() {
        this.eventSubscribers = new HashMap<>();
        this.creationTime = System.currentTimeMillis();
    }

    @Api.Stable(since = "2.0.0")
    public static LibraryEnvironment getLibEnvironment(String string) {
        return CogwheelExecutor.getLibraryEnvironment(string);
    }

    @Api.Stable(since = "2.0.0")
    public void dispatchEvent(ResourceLocation event, ScriptStorage storage) {
        if (eventSubscribers.containsKey(event)) {
            List<String> subscribedScripts = eventSubscribers.get(event);
            for (String script : subscribedScripts) {
                dispatchScript(script, storage);
            }
        }
    }
    @Api.Stable(since = "2.0.0")
    public void subscribeForEvent(ResourceLocation event, String scriptName) {
        if (!eventSubscribers.containsKey(event)) {
            eventSubscribers.put(event, new ArrayList<>());
        }
        eventSubscribers.get(event).add(scriptName);
    }
    @Api.Stable(since = "2.0.0")
    public void unsubscribeFromEvent(ResourceLocation event, String scriptName) {
        if (!eventSubscribers.containsKey(event)) {
            eventSubscribers.put(event, new ArrayList<>());
            return;
        }
        eventSubscribers.get(event).remove(scriptName);
    }
    @Api.Stable(since = "2.0.0")
    public void unsubscribeAllFromEvent(ResourceLocation event) {
        eventSubscribers.remove(event);
    }
    @Api.Stable(since = "2.0.0")
    public static void dispatchEventGlobal(ResourceLocation event, ScriptStorage storage) {
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
        WeakList<DispatchedScript> scripts = DispatchedScript.MONITOR.getObjects();
        if (scripts == null) return;
        for (int i = 0; i < scripts.size(); i++) {
            DispatchedScript script = scripts.get(i);
            if (script == null) {
                scripts.remove(i);
                i--;
                continue;
            }
            if (script.getEnvironment() == this) {
                script.haltExecution();
            }
        }
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
     * @param rl ResourceLocation. Namespace determine what environment will be used. Path is the script name
     */
    @Api.Internal @ApiStatus.Internal
    public static void dispatchScriptGlobal(String rl) {
        ResourceLocation loc = ResourceLocation.parse(rl);
        dispatchScriptGlobal(loc);
    }

    @Api.Experimental(since = "2.0.0")
    public static void dispatchScriptGlobal(ResourceLocation loc) {
        CogScriptEnvironment environment = null;
        environment = getEnvironment(loc);
        if (environment == null) throw new RuntimeException("Dispatch Failure! No environment found");
        environment.dispatchScript(loc.getPath());
    }

    public static CogScriptEnvironment getEnvironment(ResourceLocation loc) {
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
     * @param rl ResourceLocation. Namespace determine what environment will be used. Path is the script name
     */
    @Api.Internal @ApiStatus.Internal
    public static void dispatchScriptGlobal(String rl, ScriptStorage storage) {
        ResourceLocation loc = ResourceLocation.parse(rl);
        dispatchScriptGlobal(loc, storage);
    }

    @Api.Experimental(since = "2.0.0")
    public static void dispatchScriptGlobal(ResourceLocation loc, ScriptStorage storage) {
        CogScriptEnvironment environment = getEnvironment(loc);
        if (environment == null) throw new RuntimeException("Dispatch Failure! No environment found");
        environment.dispatchScript(loc.getPath(), storage);
    }

    @Api.Internal @ApiStatus.Internal
    public static File getScriptFile(ResourceLocation rl) {
        CogScriptEnvironment environment = getEnvironment(rl);
        return new File(Minecraft.getInstance().gameDirectory, "config/" + environment.getScript(rl.getPath()));
    }

    public @NotNull EnvironmentData getData() {
        return EventBus.getStoryLevel().getLevel().getDataStorage().computeIfAbsent(EnvironmentData::new, EnvironmentData::new, "cg" + this.getUniqueIdentifier());
    }

    public abstract String getUniqueIdentifier();

    public long getCreationTime() {
        return creationTime;
    }

    @Override
    public String toString() {
        return "CogScriptEnvironment{name=" + getUniqueIdentifier() + "}";
    }

    public static class EnvironmentData extends SavedData {
        private EnvironmentData() {
            super();
            data = new HashMap<>();
        }
        private EnvironmentData(@NotNull CompoundTag tag) {
            super();
            data = new HashMap<>();
            for (String key : tag.getAllKeys()) {
                CompoundTag t = (CompoundTag) tag.get(key);
                if (t == null) throw new RuntimeException("Failed to parse EnvironmentData for " + tag);
                byte type = t.getByte("t");
                CogPrimalType v = null;
                switch (type) {
                    case ((byte) -218) -> {
                        v = CogBool.getInstance(t.getBoolean("v"));
                    }
                    case ((byte) -217) -> {
                        v = new CogInteger(t.getInt("v"));
                    }
                    case ((byte) -216) -> {
                        v = new CogDouble(t.getDouble("v"));
                    }
                    case ((byte) -215) -> {
                        v = new CogString(t.getString("v"));
                    }
                }
                data.put(key, v);
            }
        }
        private final HashMap<String, CogPrimalType> data;
        @Override
        public @NotNull CompoundTag save(@NotNull CompoundTag tag) {
            for (Map.Entry<String, CogPrimalType> entry : data.entrySet()) {
                CompoundTag t = new CompoundTag();
                t.putByte("t", entry.getValue().getPrimalID());
                entry.getValue().putPrimal(t, "v");
                tag.put(entry.getKey(), t);
            }
            return tag;
        }
        public void put(String key, CogPrimalType value) {
            data.put(key, value);
            this.setDirty();
        }
        public CogPrimalType get(String key) {
            return data.get(key);
        }
        public void remove(String key) {
            data.remove(key);
            this.setDirty();
        }
    }
}
