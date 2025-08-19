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

import com.storyanvil.cogwheel.infrastructure.env.CogScriptEnvironment;
import com.storyanvil.cogwheel.registry.CogwheelRegistries;
import com.storyanvil.cogwheel.util.ObjectMonitor;
import com.storyanvil.cogwheel.util.ScriptLineHandler;
import com.storyanvil.cogwheel.util.ScriptStorage;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;

import static com.storyanvil.cogwheel.CogwheelExecutor.log;

public class DispatchedScript implements ObjectMonitor.IMonitored {
    @ApiStatus.Internal
    public static final ObjectMonitor<DispatchedScript> MONITOR = new ObjectMonitor<>();

    private final ArrayList<String> linesToExecute;
    private ScriptStorage storage;
    private HashMap<Integer, ScriptLineHandler> additionalLineHandlers;
    private String scriptName = "unknown-script";
    private CogScriptEnvironment environment;
    private boolean doNotRemoveLines = false;
    private int lineSkipped = 0;
    private int executionLine = 0;

    public DispatchedScript(ArrayList<String> linesToExecute, CogScriptEnvironment environment) {
        MONITOR.register(this);
        this.linesToExecute = linesToExecute;
        this.storage = new ScriptStorage();
        this.environment = environment;
        this.additionalLineHandlers = new HashMap<>();
        CogwheelRegistries.putDefaults(storage, this);
    }
    public DispatchedScript(ArrayList<String> linesToExecute, ScriptStorage storage, CogScriptEnvironment environment) {
        MONITOR.register(this);
        this.linesToExecute = linesToExecute;
        this.storage = storage;
        this.environment = environment;
        this.additionalLineHandlers = new HashMap<>();
        CogwheelRegistries.putDefaults(this.storage, this);
    }

    @ApiStatus.Internal
    public DispatchedScript setScriptName(String scriptName) {
        this.scriptName = scriptName;
        return this;
    }

    public String getScriptName() {
        return scriptName;
    }

    private boolean executeLine(@NotNull String line) {
//        int labelEnd = line.indexOf(":::");
//        String label = null;
//        if (labelEnd != -1) {
//            label = line.substring(0, labelEnd);
//            line = line.substring(labelEnd + 3);
//        }
        for (ScriptLineHandler handler : CogwheelRegistries.getLineHandlers()) {
            try {
                byte result = handler.handle(line, this);
                if (result == ScriptLineHandler.ignore()) continue;
                return result == ScriptLineHandler.continueReading();
            } catch (Throwable e) {
                log.warn("{}: LineHandler {} failed with exception. Line: \"{}\"", getScriptName(), handler.getResourceLocation(), line, e);
            }

        }
        log.warn("{}: None of LineHandlers could handle line: \"{}\". Skipping the line", getScriptName(), line);
        return true;
    }

    public void lineDispatcher() {
        if (!Thread.currentThread().getName().contains("cogwheel-executor")) {
            RuntimeException e = new RuntimeException("Line dispatcher can only be run in cogwheel executor thread");
            e.printStackTrace();
            log.error("[!CRITICAL!] LINE DISPATCHER WAS CALLED FROM NON-EXECUTOR THREAD! THIS WILL CAUSE MEMORY LEAKS AND PREVENT SCRIPTS FOR PROPER EXECUTION! THIS CALL WAS DISMISSED, PROBABLY CAUSING A MEMORY LEAK!");
            throw e;
        }
        lineDispatcherInternal();
    }
    private void lineDispatcherInternal() {
        try {
            while (!linesToExecute.isEmpty()) {
                String line;
                if (doNotRemoveLines) {
                    line = linesToExecute.get(lineSkipped);
                    lineSkipped++;
                } else {
                    line = linesToExecute.get(0).trim();
                    linesToExecute.remove(0);
                }
                executionLine++;
                if (!executeLine(line)) {
                    break;
                }
                if (additionalLineHandlers.containsKey(executionLine)) {
                    try {
                        byte res = additionalLineHandlers.get(executionLine).handle(line, this);
                        additionalLineHandlers.remove(executionLine);
                        if (res == ScriptLineHandler.blocking()) break;
                    } catch (Throwable e) {
                        log.warn("{}: Planned LineHandler {} failed with exception. Line: \"{}\"", getScriptName(), additionalLineHandlers.get(executionLine).getResourceLocation(), line, e);
                    }
                }
            }
        } catch (Throwable e) {
            RuntimeException a = new RuntimeException("Exception in lineDispatcher reflected:", e);
            log.error("FATAL LINE DISPATCHER FAILURE!", a);
            throw a;
        }
    }

    public void stopLineUnloading() {
        doNotRemoveLines = true;
        lineSkipped = 0;
    }
    public void continueUnloadingLines() {
        doNotRemoveLines = false;
    }
    public void removeUnloadedLines() {
        for (int i = 0; i < lineSkipped; i++)
            this.removeLine(0);
    }
    public void plantHandler(ScriptLineHandler handler, int lineAhead) {
        additionalLineHandlers.put(executionLine + lineAhead, handler);
    }

    public void put(String key, CogPropertyManager o) {
        if (o == null) return;
        storage.put(key, o);
    }
    public @Nullable CogPropertyManager get(String key) {
        return storage.get(key);
    }
    public boolean hasKey(String key) {
        return storage.containsKey(key);
    }

    @Override
    public void reportState(@NotNull StringBuilder sb) {
        sb.append(scriptName).append(">");
        for (String line : linesToExecute) {
            sb.append('"').append(line).append("\" ");
        }
    }

    public String pullLine() {
        if (linesToExecute.isEmpty()) return null;
        String l = linesToExecute.get(0);
        linesToExecute.remove(0);
        return l;
    }
    public void removeLine() {
        if (linesToExecute.isEmpty()) return;
        linesToExecute.remove(0);
    }
    public void removeLine(int line) {
        linesToExecute.remove(line);
    }
    public String peekLine() {
        if (linesToExecute.isEmpty()) return null;
        return linesToExecute.get(0);
    }
    public String peekLine(int line) {
        if (line >= linesToExecute.size()) return null;
        return linesToExecute.get(line);
    }
    public int linesLeft() {
        return linesToExecute.size();
    }

    public ScriptStorage getStorage() {
        return storage;
    }

    public CogScriptEnvironment getEnvironment() {
        return environment;
    }

    /**
     * Removes this script from its environment, clears and releases its storage and removes all lines for execution
     */
    public void haltExecution() {
        log.info("Script {} is being forcefully stopped!", getScriptName());
        linesToExecute.clear();
        environment = null;
        storage.clear();
        storage = null;
    }

    @Override
    public String toString() {
        return "DispatchedScript{" +
                "scriptName='" + scriptName + '\'' +
                ", environment=" + environment +
                '}';
    }
}
