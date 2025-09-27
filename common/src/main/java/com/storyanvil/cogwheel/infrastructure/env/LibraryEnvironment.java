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

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.storyanvil.cogwheel.infrastructure.CogScriptDispatcher;
import com.storyanvil.cogwheel.util.ScriptStorage;
import org.jetbrains.annotations.ApiStatus;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

import static com.storyanvil.cogwheel.CogwheelExecutor.log;

public class LibraryEnvironment extends CogScriptEnvironment {
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
            log.info("Exception for {}", name, e);
            return false;
        }
        return true;
    }

    @Override
    public void dispatchScript(String name) {
        CogScriptDispatcher.dispatch("cog-libs/.cog/" + this.name + "/" + name, this);
    }

    @Override
    public void dispatchScript(String name, ScriptStorage storage) {
        CogScriptDispatcher.dispatch("cog-libs/.cog/" + this.name + "/" + name, storage, this);
    }

    @Override
    public String getScript(String name) {
        return "cog-libs/.cog/" + this.name + "/" + name;
    }

    @Override
    public String getUniqueIdentifier() {
        return name;
    }

    public String getName() {
        return name;
    }
}
