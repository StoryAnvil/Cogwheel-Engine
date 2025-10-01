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

import com.storyanvil.cogwheel.infrastructure.props.CGPM;
import com.storyanvil.cogwheel.infrastructure.env.CogScriptEnvironment;
import com.storyanvil.cogwheel.infrastructure.err.CogScriptException;
import com.storyanvil.cogwheel.registry.CogwheelRegistries;
import com.storyanvil.cogwheel.util.*;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.function.Consumer;

import static com.storyanvil.cogwheel.CogwheelExecutor.log;

public class DispatchedScript {
    public static final Runnable EMPTY = () -> {};
    public static final Consumer<CogScriptException> EMPTY_ERR = (err) -> {};
    protected final ArrayList<ScriptLine> linesToExecute;
    protected int executionLine = 0;
    protected ScriptStorage storage;
    protected String scriptName = "unknown-script";
    protected CogScriptEnvironment environment;
    protected Runnable onEnd = EMPTY;
    protected Consumer<CogScriptException> errorHandler = EMPTY_ERR;
    protected final UnclearableStack<ScriptExecutionFrame> frames;

    public DispatchedScript(ArrayList<ScriptLine> linesToExecute, CogScriptEnvironment environment) {
        this.linesToExecute = linesToExecute;
        this.storage = new ScriptStorage();
        this.environment = environment;
        this.frames = new UnclearableStack<>(new ScriptExecutionFrame());
        environment.defaultVariablesFactory(this.storage, this);
    }
    public DispatchedScript(ArrayList<ScriptLine> linesToExecute, ScriptStorage storage, CogScriptEnvironment environment) {
        this.linesToExecute = linesToExecute;
        this.storage = storage;
        this.environment = environment;
        this.frames = new UnclearableStack<>(new ScriptExecutionFrame());
        environment.defaultVariablesFactory(this.storage, this);
    }

    public DispatchedScript setScriptName(String scriptName) {
        this.scriptName = scriptName;
        return this;
    }

    public void setOnEnd(Runnable onEnd) {
        this.onEnd = onEnd;
    }

    public void setErrorHandler(Consumer<CogScriptException> errorHandler) {
        this.errorHandler = errorHandler;
    }

    public String getScriptName() {
        return scriptName;
    }

    /**
     * @return <code>TRUE</code> only if script should pause execution
     */
    private boolean executeLine(@NotNull ScriptLine line) {
        if (line.getHandler() != null) {
            try {
                byte result = line.getHandler().handle(line, line.getLine(), this);
                return result != ScriptLineHandler.continueReading;
            } catch (Throwable e) {
                log.warn("{}: Overwritten LineHandler {} failed with exception.", getScriptName(), line.getHandler().getIdentifier(), e);
                if (e instanceof CogScriptException scriptException)
                    fillScriptCrashReport(this, scriptException);
                return false;
            }
        }
        for (Bi<ScriptLineHandler, Boolean> handler : CogwheelRegistries.getLineHandlers()) {
            if (!handler.getB().booleanValue()) continue;
            try {
                byte result = handler.getA().handle(line, line.getLine(), this);
                if (result == ScriptLineHandler.ignore) continue;
                return result != ScriptLineHandler.continueReading;
            } catch (Throwable e) {
                log.warn("{}: LineHandler {} failed with exception.", getScriptName(), handler.getA().getIdentifier(), e);
                if (e instanceof CogScriptException scriptException)
                    fillScriptCrashReport(this, scriptException);
                return false;
            }
        }
        log.warn("{}: None of LineHandlers could handle line: \"{}\". Skipping the line", getScriptName(), line);
        return false;
    }

    public static void fillScriptCrashReport(DispatchedScript script, CogScriptException e) {
        //TODO
        script.errorHandler.accept(e);
        if (e.getScript() == script) {
            log.error("{}: Exception at line: {}", script.getScriptName(), e.getLineNumber(), e);
        } else {
            log.error("{}: Exception at line: {} [from external script: {}]", script.getScriptName(), e.getLineNumber(), e.getScript().getScriptName(), e);
        }
    }

    /**
     * @return <code>TRUE</code> if script needs to be dispatched again later. <code>FALSE</code> means this script is finished by now
     */
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
            while (executionLine < linesToExecute.size()) {
                ScriptLine line = linesToExecute.get(executionLine);
                executionLine++;
                if (executeLine(line)) {
                    return true;
                }
            }
        } catch (Exception e) {
            WrapperException t = new WrapperException(e);
            log.error("Critical line dispatcher failure", t);
            if (executionLine == linesToExecute.size())
                onEnd();
            throw t;
        }
        if (executionLine == linesToExecute.size())
            onEnd();
        return false;
    }

    public void put(String key, CGPM o) {
        if (o == null) {
            storage.remove(key);
            return;
        }
        if (key == null) return;
        storage.put(key, o);
    }
    public @NotNull CGPM get(String key) {
        return CGPM.noNull(storage.get(key));
    }
    public boolean hasKey(String key) {
        return storage.containsKey(key);
    }

    public ScriptStorage getStorage() {
        return storage;
    }

    public CogScriptEnvironment getEnvironment() {
        return environment;
    }

    public ArrayList<ScriptLine> getLinesToExecute() {
        return linesToExecute;
    }

    public void setExecutionLine(int executionLine) {
        this.executionLine = executionLine;
    }

    public int getExecutionLine() {
        return executionLine;
    }

    /**
     * Pushes and returns new {@link ScriptExecutionFrame} in this script
     */
    public ScriptExecutionFrame pushFrame() {
        ScriptExecutionFrame frame = new ScriptExecutionFrame();
        frames.push(frame);
        return frame;
    }

    /**
     * Removes top {@link ScriptExecutionFrame} from this script
     */
    public ScriptExecutionFrame pullFrame() {
        if (frames.empty()) return null;
        ScriptExecutionFrame frame = frames.pop();
        frame.onPulled(this);
        return frame;
    }

    /**
     * @return top {@link ScriptExecutionFrame} from this script
     */
    public ScriptExecutionFrame getFrame() {
        return frames.peek();
    }

    /**
     * Advances script one line further. Returns current line after advancing
     */
    public ScriptLine pullLine() {
        executionLine++;
        return linesToExecute.get(executionLine);
    }

    /**
     * Removes this script from its environment, clears and releases its storage and removes all lines for execution
     */
    public void haltExecution() {
        log.debug("Script {} is being forcefully stopped!", getScriptName());
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

    public void startExecution() {
        lineDispatcher();
    }

    /**
     * Left for overwrites
     */
    public void onEnd() {
        onEnd.run();
    }

    public CogScriptException wrapWithTrace(Throwable t) {
        return new CogScriptException(null, t, this, getExecutionLine());
    }
    public CogScriptException wrap(Throwable t) {
        return new CogScriptException(null, t, true, false, this, getExecutionLine());
    }
}
