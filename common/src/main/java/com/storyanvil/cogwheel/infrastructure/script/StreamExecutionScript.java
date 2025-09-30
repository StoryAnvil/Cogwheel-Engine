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

import com.storyanvil.cogwheel.CogwheelExecutor;
import com.storyanvil.cogwheel.api.Api;
import com.storyanvil.cogwheel.infrastructure.err.CogScriptException;
import com.storyanvil.cogwheel.infrastructure.props.CGPM;
import com.storyanvil.cogwheel.infrastructure.env.CogScriptEnvironment;
import com.storyanvil.cogwheel.registry.CogwheelRegistries;
import com.storyanvil.cogwheel.util.ScriptStorage;

import java.util.ArrayList;

public class StreamExecutionScript extends DispatchedScript {
    public StreamExecutionScript(ArrayList<ScriptLine> linesToExecute, ScriptStorage storage, CogScriptEnvironment environment) {
        super(linesToExecute, storage, environment);
    }
    public StreamExecutionScript(ArrayList<ScriptLine> linesToExecute, CogScriptEnvironment environment) {
        super(linesToExecute, environment);
    }
    public StreamExecutionScript(ScriptStorage storage, CogScriptEnvironment environment) {
        super(new ArrayList<>(), storage, environment);
    }
    public StreamExecutionScript(CogScriptEnvironment environment) {
        super(new ArrayList<>(), environment);
    }

    @Api.Stable(since = "2.7.0")
    public boolean addLine(String line) {
        linesToExecute.add(new ScriptLine(line));
        return lineDispatcher();
    }

    @Api.Stable(since = "2.7.0")
    public void addLineFrozen(String line) {
        linesToExecute.add(new ScriptLine(line));
    }

    @Api.Stable(since = "2.7.0")
    public void addLineRedirecting(String line) {
        linesToExecute.add(new ScriptLine(line));
        CogwheelExecutor.schedule(this::lineDispatcher);
    }

    @Api.Stable(since = "2.7.0")
    public CGPM fastExecute(String line) throws CogScriptException {
        return CogwheelRegistries.expressionHandler(line, this, false).getB();
    }
}
