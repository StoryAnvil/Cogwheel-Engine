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

import com.storyanvil.cogwheel.infrustructure.cog.CogBool;
import com.storyanvil.cogwheel.infrustructure.cog.CogDouble;
import com.storyanvil.cogwheel.infrustructure.cog.CogInteger;
import com.storyanvil.cogwheel.infrustructure.cog.CogString;

public class ArgumentData {
    private final String s;
    private final String[] args;
    private final DispatchedScript script;
    private ArgumentData(String s, DispatchedScript script) {
        this.s = s;
        this.args = s.split(",");
        this.script = script;
    }
    public CogPropertyManager get(int argument) {
        if (argument >= args.length) throw new RuntimeException("Not enough arguments: \"" + s + "\" contains only " + args.length + " but " + argument + " is needed!");
        String a = args[argument].trim();
        char head = a.charAt(0);
        char tail = a.charAt(a.length() - 1);
        if (head == '"' && tail == '"') {
            return new CogString(a.substring(1, a.length() - 1));
        } else if (head == '^') {
            return new CogInteger(a.substring(1));
        } else if (a.equals("true")) {
            return CogBool.TRUE;
        } else if (a.equals("false")) {
            return CogBool.FALSE;
        }
        return script.get(a);
    }
    public int requireInt(int argument) {
        CogPropertyManager m = get(argument);
        if (m instanceof CogInteger i) {
            return i.getValue();
        }
        throw new RuntimeException("Argument #" + argument + " is not CogInteger");
    }
    public boolean requireBoolean(int argument) {
        CogPropertyManager m = get(argument);
        if (m instanceof CogBool i) {
            return i.getValue();
        }
        throw new RuntimeException("Argument #" + argument + " is not CogBool");
    }
    public double requireDouble(int argument) {
        CogPropertyManager m = get(argument);
        if (m instanceof CogDouble i) {
            return i.getValue();
        }
        throw new RuntimeException("Argument #" + argument + " is not CogDouble");
    }
    public String getString(int argument) {
        CogPropertyManager m = get(argument);
        return m.convertToString();
    }
    public CogString getCogString(int argument) {
        CogPropertyManager m = get(argument);
        if (m instanceof CogString string) return string;
        return m.convertToCogString();
    }
    public String getAsString() {
        return s;
    }
    public int size() {
        return args.length;
    }

    public static ArgumentData createFromString(String s, DispatchedScript script) {
        return new ArgumentData(s.replace("\\.", "."), script);
    }
}
