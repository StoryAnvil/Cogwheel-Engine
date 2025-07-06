/*
 * StoryAnvil CogWheel Engine
 * Copyright (C) 2025 StoryAnvil
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.storyanvil.cogwheel.infrustructure;

import com.storyanvil.cogwheel.registry.CogwheelRegistries;
import com.storyanvil.cogwheel.util.DoubleValue;
import com.storyanvil.cogwheel.util.ScriptLineHandler;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.storyanvil.cogwheel.CogwheelExecutor.log;

public class DispatchedScript {
    private ArrayList<String> linesToExecute;
    private int executionDepth = 0;
    private HashMap<String, WeakReference<Object>> weakStorage;
    private String scriptName = "unknown-script";

    public DispatchedScript(ArrayList<String> linesToExecute) {
        this.linesToExecute = linesToExecute;
        this.weakStorage = new HashMap<>();
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
                log.warn("{}: LineHandler {} failed with exception", getScriptName(), handler.getResourceLocation(), e);
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

    /**
     * @return object stored in weak storage. Null is returned if there isn't object with specified key of WeakReference to this object was cleared
     */
    public <T> @Nullable T getWeak(String key, Class<T> type) {
        WeakReference<Object> wr = weakStorage.get(key);
        if (wr == null) return null;
        Object o = wr.get();
        if (o == null) {
            log.info("{}: Object with key: {} got unloaded from weakStorage", getScriptName(), key);
            weakStorage.remove(key);
            return null;
        }
        return (T) o;
    }

    /**
     * Puts object in weak storage. Storing object in weak storage does not prevent java garbage collector from removing it!
     * @param key Key which will be used to access the object
     * @param o Object to store
     */
    public void putWeak(String key, Object o) {
        if (o == null) return;
        weakStorage.put(key, new WeakReference<>(o));
    }

    public void dataDump() {
        log.info("Data dump: {}", scriptName);
        for (Map.Entry<String, WeakReference<Object>> d: weakStorage.entrySet()) {
            log.info("{} = {}", d.getKey(), d.getValue().get());
        }
    }
}
