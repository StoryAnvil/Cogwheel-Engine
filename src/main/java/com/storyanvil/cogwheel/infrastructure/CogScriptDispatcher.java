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

package com.storyanvil.cogwheel.infrastructure;

import com.storyanvil.cogwheel.CogwheelExecutor;
import com.storyanvil.cogwheel.infrastructure.env.CogScriptEnvironment;
import com.storyanvil.cogwheel.util.ScriptStorage;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.function.Consumer;

import static com.storyanvil.cogwheel.CogwheelExecutor.log;

public class CogScriptDispatcher {
    public static void dispatch(String scriptName, CogScriptEnvironment environment) {
        CogwheelExecutor.schedule(() -> dispatchUnsafe(scriptName, new ScriptStorage(), environment));
    }
    public static void dispatch(String scriptName, ScriptStorage storage, CogScriptEnvironment environment) {
        CogwheelExecutor.schedule(() -> dispatchUnsafe(scriptName, storage, environment));
    }
    public static void dispatch(String scriptName, Consumer<DispatchedScript> s, CogScriptEnvironment environment) {
        CogwheelExecutor.schedule(() -> s.accept(dispatchUnsafe(scriptName, new ScriptStorage(), environment)));
    }
    public static void dispatch(String scriptName, ScriptStorage storage, Consumer<DispatchedScript> s, CogScriptEnvironment environment) {
        CogwheelExecutor.schedule(() -> s.accept(dispatchUnsafe(scriptName, storage, environment)));
    }
    public static @Nullable DispatchedScript dispatchUnsafe(String scriptName, ScriptStorage storage, CogScriptEnvironment environment) {
        File script = new File(Minecraft.getInstance().gameDirectory, "config/" + scriptName);
        return dispatchUnsafe(script, storage, environment);
    }
    public static @Nullable DispatchedScript dispatchUnsafe(File script, ScriptStorage storage, CogScriptEnvironment environment) {;
        String scriptName = script.getName();
        if (!script.exists()) {
            log.error("Script {} does not exist. Dispatch ignored!", script);
            return null;
        }
        if (scriptName.endsWith(".sa") /* storyanvil */) {
            log.info("Script: {} dispatched", scriptName);
            return readAndDispatch(scriptName, storage, environment, script);
        } else {
            log.error("Script {} does not end with any known extension (known extensions are: \".sa\"). Dispatch ignored!", script);
        }
        return null;
    }

    private static @Nullable DispatchedScript readAndDispatch(String scriptName, ScriptStorage storage, CogScriptEnvironment environment, File script) {
        try (FileReader fr = new FileReader(script); Scanner sc = new Scanner(fr)) {
            ArrayList<String> lines = new ArrayList<>();
            while (sc.hasNextLine()) {
                lines.add(sc.nextLine().trim());
            }
            DispatchedScript s = new DispatchedScript(lines, storage, environment);
            CogwheelExecutor.schedule(s.setScriptName(scriptName)::lineDispatcher);
            return s;
        } catch (IOException e) {
            log.error("Script dispatch failed while file reading", e);
        }
        return null;
    }
}
