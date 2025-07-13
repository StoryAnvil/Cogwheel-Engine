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

package com.storyanvil.cogwheel.infrustructure;

import com.storyanvil.cogwheel.infrustructure.cog.CogActionQueue;
import com.storyanvil.cogwheel.registry.CogwheelRegistries;
import com.storyanvil.cogwheel.util.DoubleValue;
import com.storyanvil.cogwheel.util.ObjectMonitor;
import com.storyanvil.cogwheel.util.ScriptLineHandler;
import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.HashMap;

import static com.storyanvil.cogwheel.CogwheelExecutor.log;

public class DispatchedScript implements ObjectMonitor.IMonitored {
    private static final ObjectMonitor<DispatchedScript> MONITOR = new ObjectMonitor<>();

    private ArrayList<String> linesToExecute;
    private int executionDepth = 0;
    private boolean skipCurrentDepth = false;
    private HashMap<String, CogPropertyManager> storage;
    private String scriptName = "unknown-script";

    public DispatchedScript(ArrayList<String> linesToExecute) {
        MONITOR.register(this);
        this.linesToExecute = linesToExecute;
        this.storage = new HashMap<>();
        CogwheelRegistries.putDefaults(storage, this);
    }

    @ApiStatus.Internal
    public DispatchedScript setScriptName(String scriptName) {
        this.scriptName = scriptName;
        return this;
    }

    public String getScriptName() {
        return scriptName;
    }

    private boolean executeLine(String line) {
        int labelEnd = line.indexOf(":::");
        String label = null;
        if (labelEnd != -1) {
            label = line.substring(0, labelEnd);
            line = line.substring(labelEnd + 3);
        }
        for (ScriptLineHandler handler : CogwheelRegistries.getLineHandlers()) {
            try {
                DoubleValue<Boolean, Boolean> result = handler.handle(line, label, this);
                if (result.getA()) {
                    return result.getB();
                }
            } catch (Throwable e) {
                log.warn("{}: LineHandler {} failed with exception. Line: \"{}\"", getScriptName(), handler.getResourceLocation(), line, e);
            }

        }
        log.warn("{}: None of LineHandlers could handle line: \"{}\". Skipping the line", getScriptName(), line);
        return true;
    }

    public void lineDispatcher() {
        if (linesToExecute.isEmpty()) return;
        String line = linesToExecute.get(0).trim();
        linesToExecute.remove(0);
        if (executeLine(line)) {
            lineDispatcher();
        }
    }

    public void put(String key, CogPropertyManager o) {
        if (o == null) return;
        storage.put(key, o);
    }
    public CogPropertyManager get(String key) {
        return storage.get(key);
    }
    public boolean hasKey(String key) {
//        log.warn(storage.keySet().toString());
        return storage.containsKey(key);
    }
    public <T> CogActionQueue<T> getActionQueue(String key, Class<T> clazz) {
        return (CogActionQueue<T>) storage.get(key);
    }

    public void dataDump() {
        log.info("Data dump: {}", scriptName);
//        for (Map.Entry<String, Object> d: storage.entrySet()) {
//            log.info("{} = {}", d.getKey(), d.getValue());
//        }
    }

    @Override
    public void reportState(StringBuilder sb) {
        sb.append(scriptName).append(">");
        for (String line : linesToExecute) {
            sb.append('"').append(line).append("\" ");
        }
//        sb.append("| STORAGE>>");
//        for (Map.Entry<String, Object> d : storage.entrySet()) {
//            sb.append('"').append(d.getKey()).append("\"=\"").append(d.getValue()).append("\";");
//        }
    }

    public int getExecutionDepth() {
        return executionDepth;
    }

    public void setExecutionDepth(int executionDepth) {
        this.executionDepth = executionDepth;
    }

    public int pushDepth(boolean skip) {
        executionDepth++;
        skipCurrentDepth = skip;
        return executionDepth;
    }

    public int pullDepth(boolean skip) {
        executionDepth--;
        skipCurrentDepth = skip;
        return executionDepth;
    }

    public boolean isSkippingCurrentDepth() {
        return skipCurrentDepth;
    }

    public void setSkipCurrentDepth(boolean skipCurrentDepth) {
        this.skipCurrentDepth = skipCurrentDepth;
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
    public String peekLine() {
        if (linesToExecute.isEmpty()) return null;
        return linesToExecute.get(0);
    }

    public HashMap<String, CogPropertyManager> getStorage() {
        return storage;
    }
}
