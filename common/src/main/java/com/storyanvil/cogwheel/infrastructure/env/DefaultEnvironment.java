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

import com.storyanvil.cogwheel.infrastructure.CogScriptDispatcher;
import com.storyanvil.cogwheel.infrastructure.module.CogModule;
import com.storyanvil.cogwheel.util.ScriptStorage;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.function.Consumer;

import static com.storyanvil.cogwheel.CogwheelExecutor.log;

public final class DefaultEnvironment extends CogScriptEnvironment {
    private final HashMap<String, Consumer<Integer>> dialogs;
    private final HashMap<Identifier, CogModule> moduleMap = new HashMap<>();

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
    public void dispatchScript(String name, ScriptStorage storage) {
        CogScriptDispatcher.dispatch("cog/" + name, storage, this);
    }

    @Override
    public String getScript(String name) {
        return "cog/" + name;
    }

    @Override
    public String getUniqueIdentifier() {
        return "default_environment";
    }

    public void registerDialog(String id, Consumer<Integer> callback) {
        dialogs.put(id, callback);
    }

    public HashMap<String, Consumer<Integer>> getDialogs() {
        return dialogs;
    }

    public void putModule(Identifier loc, CogModule module) {
        moduleMap.put(loc, module);
    }

    public CogModule getModule(Identifier loc) {
        return moduleMap.get(loc);
    }

    public Identifier getModuleLoc(CogScriptEnvironment env, String script) {
        return Identifier.of(env.getUniqueIdentifier(), script);
    }

    @Override
    public boolean canBeEdited() {
        return true;
    }
}
