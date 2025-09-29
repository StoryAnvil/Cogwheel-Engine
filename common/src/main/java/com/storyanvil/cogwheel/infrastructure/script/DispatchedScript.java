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

package com.storyanvil.cogwheel.infrastructure.script;

import com.storyanvil.cogwheel.api.Api;
import com.storyanvil.cogwheel.infrastructure.CGPM;
import com.storyanvil.cogwheel.infrastructure.env.CogScriptEnvironment;
import com.storyanvil.cogwheel.registry.CogwheelRegistries;
import com.storyanvil.cogwheel.util.Bi;
import com.storyanvil.cogwheel.util.ObjectMonitor;
import com.storyanvil.cogwheel.util.ScriptLineHandler;
import com.storyanvil.cogwheel.util.ScriptStorage;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.storyanvil.cogwheel.CogwheelExecutor.log;

public class DispatchedScript implements ObjectMonitor.IMonitored { //TODO: Stop deleting lines
    @ApiStatus.Internal @Api.Internal
    public static final ObjectMonitor<DispatchedScript> MONITOR = new ObjectMonitor<>();

    protected final ArrayList<String> linesToExecute;
    protected ScriptStorage storage;
    protected HashMap<Integer, ScriptLineHandler> additionalLineHandlers;
    protected String scriptName = "unknown-script";
    protected CogScriptEnvironment environment;
    protected boolean doNotRemoveLines = false;
    protected int lineSkipped = 0;
    protected int executionLine = 0;
    protected Runnable onEnd = () -> {};

    public DispatchedScript(ArrayList<String> linesToExecute, CogScriptEnvironment environment) {
        MONITOR.register(this);
        this.linesToExecute = linesToExecute;
        this.storage = new ScriptStorage();
        this.environment = environment;
        this.additionalLineHandlers = new HashMap<>();
        environment.defaultVariablesFactory(this.storage, this);
    }
    public DispatchedScript(ArrayList<String> linesToExecute, ScriptStorage storage, CogScriptEnvironment environment) {
        MONITOR.register(this);
        this.linesToExecute = linesToExecute;
        this.storage = storage;
        this.environment = environment;
        this.additionalLineHandlers = new HashMap<>();
        environment.defaultVariablesFactory(this.storage, this);
    }

    @ApiStatus.Internal
    public DispatchedScript setScriptName(String scriptName) {
        this.scriptName = scriptName;
        return this;
    }

    public Runnable getOnEnd() {
        return onEnd;
    }

    public void setOnEnd(Runnable onEnd) {
        this.onEnd = onEnd;
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
        for (Bi<ScriptLineHandler, Boolean> handler : CogwheelRegistries.getLineHandlers()) {
            if (!handler.getB().booleanValue()) continue;
            try {
                byte result = handler.getA().handle(line, this);
                if (result == ScriptLineHandler.ignore()) continue;
                return result == ScriptLineHandler.continueReading();
            } catch (Throwable e) {
                log.warn("{}: LineHandler {} failed with exception. Line: \"{}\"", getScriptName(), handler.getA().getIdentifier(), line, e);
            }

        }
        log.warn("{}: None of LineHandlers could handle line: \"{}\". Skipping the line", getScriptName(), line);
        return true;
    }

    public boolean lineDispatcher() {
        if (!Thread.currentThread().getName().contains("cogwheel-executor")) {
            RuntimeException e = new RuntimeException("Line dispatcher can only be run in cogwheel executor thread");
            log.error("[!CRITICAL!] LINE DISPATCHER WAS CALLED FROM NON-EXECUTOR THREAD! THIS WILL CAUSE MEMORY LEAKS AND PREVENT SCRIPTS FOR PROPER EXECUTION! THIS CALL WAS DISMISSED, PROBABLY CAUSING A MEMORY LEAK!", e);
            throw e;
        }
        return lineDispatcherInternal();
    }
    protected boolean lineDispatcherInternal() {
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
                    return true;
                }
                if (additionalLineHandlers.containsKey(executionLine)) {
                    try {
                        byte res = additionalLineHandlers.get(executionLine).handle(line, this);
                        additionalLineHandlers.remove(executionLine);
                        if (res == ScriptLineHandler.blocking()) break;
                    } catch (Throwable e) {
                        log.warn("{}: Planned LineHandler {} failed with exception. Line: \"{}\"", getScriptName(), additionalLineHandlers.get(executionLine).getIdentifier(), line, e);
                    }
                }
            }
        } catch (Throwable e) {
            RuntimeException a = new RuntimeException("Exception in lineDispatcher reflected:", e);
            log.error("FATAL LINE DISPATCHER FAILURE!", a);
            throw a;
        }
        if (linesToExecute.isEmpty())
            onEnd();
        return false;
    }

    public int getExecutionLine() {
        return executionLine;
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

    public void put(String key, CGPM o) {
        if (o == null) {
            storage.remove(key);
            return;
        }
        if (key == null) return;
        storage.put(key, o);
    }
    public @Nullable CGPM get(String key) {
        return CGPM.noNull(storage.get(key));
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
    public void defuseLine(int line) {
        linesToExecute.set(line, "");
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

    public void clearLines() {
        linesToExecute.clear();
    }

    public void startExecution() {
        lineDispatcher();
    }
    public void onEnd() {
        onEnd.run();
    }

    public List<String> getAllLines() {
        return linesToExecute;
    }
}
