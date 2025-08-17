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

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.storyanvil.cogwheel.CogwheelExecutor;
import com.storyanvil.cogwheel.EventBus;
import com.storyanvil.cogwheel.infrustructure.CogPropertyManager;
import com.storyanvil.cogwheel.infrustructure.CogScriptDispatcher;
import com.storyanvil.cogwheel.infrustructure.DispatchedScript;
import com.storyanvil.cogwheel.infrustructure.cog.*;
import com.storyanvil.cogwheel.util.WeakList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static com.storyanvil.cogwheel.CogwheelExecutor.log;

public abstract class CogScriptEnvironment {
    private final HashMap<ResourceLocation, List<String>> eventSubscribers;
    private final long creationTime;

    @Contract(pure = true)
    public CogScriptEnvironment() {
        this.eventSubscribers = new HashMap<>();
        this.creationTime = System.currentTimeMillis();
    }

    public static LibraryEnvironment getLibEnvironment(String string) {
        return CogwheelExecutor.getLibraryEnvironment(string);
    }

    public void dispatchEvent(ResourceLocation event, HashMap<String, CogPropertyManager> storage) {
        if (eventSubscribers.containsKey(event)) {
            List<String> subscribedScripts = eventSubscribers.get(event);
            for (String script : subscribedScripts) {
                dispatchScript(script, storage);
            }
        }
    }
    public void subscribeForEvent(ResourceLocation event, String scriptName) {
        if (!eventSubscribers.containsKey(event)) {
            eventSubscribers.put(event, new ArrayList<>());
        }
        eventSubscribers.get(event).add(scriptName);
    }
    public void unsubscribeFromEvent(ResourceLocation event, String scriptName) {
        if (!eventSubscribers.containsKey(event)) {
            eventSubscribers.put(event, new ArrayList<>());
            return;
        }
        eventSubscribers.get(event).remove(scriptName);
    }
    public void unsubscribeAllFromEvent(ResourceLocation event) {
        eventSubscribers.remove(event);
    }
    public static void dispatchEventGlobal(ResourceLocation event, HashMap<String, CogPropertyManager> storage) {
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
    public abstract void dispatchScript(String name);
    public abstract void dispatchScript(String name, HashMap<String, CogPropertyManager> storage);

    /**
     * Finds correct CogScriptEnvironment and dispatched script.
     * @param rl ResourceLocation. Namespace determine what environment will be used. Path is the script name
     */
    public static void dispatchScriptGlobal(String rl) {
        ResourceLocation loc = ResourceLocation.parse(rl);
        CogScriptEnvironment environment = null;
        if (loc.getNamespace().equals("def") || loc.getNamespace().equals("minecraft")) {
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

    public static class DefaultEnvironment extends CogScriptEnvironment {
        private final HashMap<String, Consumer<Integer>> dialogs;

        public DefaultEnvironment() {
            super();
            dialogs = new HashMap<>();
            log.info("Default Environment {} initialized!", getUniqueIdentifier());
        }

        @Override
        public void dispatchScript(String name) {
            CogScriptDispatcher.dispatch("cog/" + name, this);
        }

        @Override
        public void dispatchScript(String name, HashMap<String, CogPropertyManager> storage) {
            CogScriptDispatcher.dispatch("cog/" + name, storage, this);
        }

        @Override
        public String getUniqueIdentifier() {
            return "default.environment";
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
            log.info("Library Environment {} initialized!", getUniqueIdentifier());
        }

        @ApiStatus.Internal
        public boolean init(File dotCog) {
            try {
                File manifest = new File(dotCog, "manifest.json");
                JsonObject obj = JsonParser.parseReader(new FileReader(manifest)).getAsJsonObject();
                String name = obj.get("name").getAsJsonPrimitive().getAsString();
                if (!name.equals(this.name)) throw new IllegalStateException("Library names does not match!");
            } catch (FileNotFoundException | IllegalStateException e) {
                log.info("Library \"{}\" does not have manifest.json or its manifest.json is invalid. Library won't be loaded", name);
                log.info("Exception for " + name, (Exception) e);
                return false;
            }
            return true;
        }

        @Override
        public void dispatchScript(String name) {
            CogScriptDispatcher.dispatch("cog-libs/.cog/" + this.name + "/" + name, this);
        }

        @Override
        public void dispatchScript(String name, HashMap<String, CogPropertyManager> storage) {
            CogScriptDispatcher.dispatch("cog-libs/.cog/" + this.name + "/" + name, storage, this);
        }

        @Override
        public String getUniqueIdentifier() {
            return name;
        }

        public String getName() {
            return name;
        }
    }
    public static class TestEnvironment extends LibraryEnvironment {
        public TestEnvironment() {
            super("test.environment");
        }

        @Override
        public void dispatchScript(String name) {
            CogScriptDispatcher.dispatch("cog/test." + name, this);
        }

        @Override
        public void dispatchScript(String name, HashMap<String, CogPropertyManager> storage) {
            CogScriptDispatcher.dispatch("cog/test." + name, storage, this);
        }
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
