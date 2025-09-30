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

package com.storyanvil.cogwheel.infrastructure.err;

import com.storyanvil.cogwheel.infrastructure.script.DispatchedScript;

public class CogScriptException extends Exception {
    private final DispatchedScript script;
    private final int lineNumber;

    public CogScriptException(DispatchedScript script, int lineNumber) {
        this.script = script;
        this.lineNumber = lineNumber;
    }

    public CogScriptException(String message, DispatchedScript script, int lineNumber) {
        super(message);
        this.script = script;
        this.lineNumber = lineNumber;
    }

    public CogScriptException(String message, Throwable cause, DispatchedScript script, int lineNumber) {
        super(message, cause);
        this.script = script;
        this.lineNumber = lineNumber;
    }

    public CogScriptException(Throwable cause, DispatchedScript script, int lineNumber) {
        super(cause);
        this.script = script;
        this.lineNumber = lineNumber;
    }

    public CogScriptException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace, DispatchedScript script, int lineNumber) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.script = script;
        this.lineNumber = lineNumber;
    }

    public DispatchedScript getScript() {
        return script;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    @Override
    public String getMessage() {
        return super.getMessage() + " [in script \"%s\"; line=%s]".formatted(script.getScriptName(), lineNumber);
    }
}
