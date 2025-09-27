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

package com.storyanvil.cogwheel.infrastructure;

import com.mojang.datafixers.util.Function4;
import com.mojang.datafixers.util.Function5;
import com.storyanvil.cogwheel.CogwheelExecutor;
import com.storyanvil.cogwheel.CogwheelHooks;
import com.storyanvil.cogwheel.api.Api;
import com.storyanvil.cogwheel.config.CogwheelConfig;
import com.storyanvil.cogwheel.infrastructure.env.CogScriptEnvironment;
import com.storyanvil.cogwheel.infrastructure.script.DialogScript;
import com.storyanvil.cogwheel.infrastructure.script.DispatchedScript;
import com.storyanvil.cogwheel.util.ScriptStorage;
import org.jetbrains.annotations.NotNull;
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
    @SuppressWarnings("DataFlowIssue") // Why does this even trigger.....
    public static @Nullable DispatchedScript dispatchUnsafe(String scriptName, ScriptStorage storage, CogScriptEnvironment environment) {
        try {
            File script = new File(CogwheelHooks.getConfigFolder(), scriptName).getCanonicalFile();
            return dispatchUnsafe(script, storage, environment, true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    @SuppressWarnings("DataFlowIssue") // Why does this even trigger.....
    public static @Nullable DispatchedScript dispatchUnsafe(String scriptName, ScriptStorage storage, CogScriptEnvironment environment, boolean execute) {
        try {
            File script = new File(CogwheelHooks.getConfigFolder(), scriptName).getCanonicalFile();
            return dispatchUnsafe(script, storage, environment, execute);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    @Api.MixinsNotAllowed(where = "CogScriptDispatcher#mixinEntrypoint")
    public static @Nullable DispatchedScript dispatchUnsafe(File script, ScriptStorage storage, CogScriptEnvironment environment, boolean execute) {;
        if (script.getName().equals("config-main.json")) {
            CogwheelConfig.reload();
            return null;
        }
        if (CogwheelConfig.isDisablingAllScripts()) {
            log.error("Script {} can not be dispatched. All scripts were disabled in config. Dispatch ignored!", script);
            return null;
        }
        String scriptName = script.getName();
        if (!script.exists()) {
            log.error("Script {} does not exist. Dispatch ignored!", script);
            return null;
        }
        if (scriptName.endsWith(".sa") /* storyanvil */) {
            log.info("Script: {} dispatched", scriptName);
            return readAndDispatch(scriptName, storage, environment, script, CogScriptDispatcher::cogScriptScript, true, execute);
        } else if (scriptName.endsWith(".sad") /* storyanvil dialog */) {
            log.info("Dialog Script: {} dispatched", scriptName);
            return readAndDispatch(scriptName, storage, environment, script, CogScriptDispatcher::dialog, false, execute);
        } else {
            if (mixinEntrypoint(script, storage, environment) == null)
                log.error("Script {} does not end with any known extension (known extensions are: \".sa\"). Dispatch ignored!", script);
        }
        return null;
    }

    @SuppressWarnings("unused") // Unused arguments are for mixins
    @Api.MixinIntoHere
    public static @Nullable DispatchedScript mixinEntrypoint(File script, ScriptStorage storage, CogScriptEnvironment environment) {
        return null;
    }

    public static @Nullable DispatchedScript readAndDispatch(String scriptName, ScriptStorage storage, CogScriptEnvironment environment, File script, Function5<String, ScriptStorage, CogScriptEnvironment, ArrayList<String>, Boolean, DispatchedScript> scriptFunction, boolean trim, boolean execute) {
        try (FileReader fr = new FileReader(script); Scanner sc = new Scanner(fr)) {
            ArrayList<String> lines = new ArrayList<>();
            while (sc.hasNextLine()) {
                if (trim)
                    lines.add(sc.nextLine().trim());
                else
                    lines.add(sc.nextLine());
            }
            return scriptFunction.apply(scriptName, storage, environment, lines, execute);
        } catch (IOException e) {
            log.error("Script dispatch failed while file reading", e);
        }
        return null;
    }

    private static @NotNull DispatchedScript cogScriptScript(String scriptName, ScriptStorage storage, CogScriptEnvironment environment, ArrayList<String> lines, boolean execute) {
        DispatchedScript s = new DispatchedScript(lines, storage, environment);
        if (execute)
            CogwheelExecutor.schedule(s.setScriptName(scriptName)::startExecution);
        return s;
    }
    private static @NotNull DispatchedScript dialog(String scriptName, ScriptStorage storage, CogScriptEnvironment environment, ArrayList<String> lines, boolean execute) {
        DispatchedScript s = new DialogScript(lines, storage, environment);
        if (execute)
            CogwheelExecutor.schedule(s.setScriptName(scriptName)::startExecution);
        return s;
    }
}
