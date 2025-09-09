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

import com.storyanvil.cogwheel.data.SyncArray;
import com.storyanvil.cogwheel.infrastructure.CogScriptDispatcher;
import com.storyanvil.cogwheel.infrastructure.cog.early.CogEarlyManager;
import com.storyanvil.cogwheel.infrastructure.cog.early.CogEarlyRegistry;
import com.storyanvil.cogwheel.infrastructure.script.DispatchedScript;
import com.storyanvil.cogwheel.util.ScriptStorage;

public class RegistryEnvironment extends CogScriptEnvironment {
    private final SyncArray<CogEarlyRegistry> registries;

    public RegistryEnvironment(SyncArray<CogEarlyRegistry> registries) {
        this.registries = registries;
    }

    @Override
    public void dispatchScript(String name) {
        CogScriptDispatcher.dispatch("cog/early/" + name, this);
    }

    @Override
    public void dispatchScript(String name, ScriptStorage storage) {
        CogScriptDispatcher.dispatch("cog/early/" + name, storage, this);
    }

    @Override
    public String getScript(String name) {
        return "cog/early/" + name;
    }

    @Override
    public String getUniqueIdentifier() {
        return "registry_environment";
    }

    @Override
    public void defaultVariablesFactory(ScriptStorage storage, DispatchedScript script) {
        storage.put("EarlyManager", new CogEarlyManager(registries));
    }
}
