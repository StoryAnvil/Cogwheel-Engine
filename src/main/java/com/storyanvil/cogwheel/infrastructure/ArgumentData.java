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

import com.storyanvil.cogwheel.infrastructure.cog.*;
import com.storyanvil.cogwheel.infrastructure.script.DispatchedScript;
import com.storyanvil.cogwheel.registry.CogwheelRegistries;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;

public class ArgumentData {
    private final CogPropertyManager[] args;
    private final DispatchedScript script;
    private final String full;
    @Contract(pure = true)
    private ArgumentData(CogPropertyManager[] args, String full, DispatchedScript script) {
        this.full = full;
        this.args = args;
        this.script = script;
    }
    public CogPropertyManager get(int argument) {
        if (argument >= args.length) throw new RuntimeException("Not enough arguments: contains only " + args.length + " but " + argument + " is needed!");
        return args[argument];
    }
    public CogPrimalType requirePrimal(int argument) {
        CogPropertyManager m = get(argument);
        if (m instanceof CogPrimalType i) {
            return i;
        }
        throw new RuntimeException("Argument #" + argument + " is not primal type");
    }
    public int requireInt(int argument) {
        CogPropertyManager m = get(argument);
        if (m instanceof CogInteger i) {
            return i.getValue();
        }
        throw new RuntimeException("Argument #" + argument + " is not CogInteger");
    }
    public CogInvoker requireInvoker(int argument) {
        CogPropertyManager m = get(argument);
        if (m instanceof CogInvoker i) {
            return i;
        }
        throw new RuntimeException("Argument #" + argument + " is not CogInvoker");
    }
    public long requireLong(int argument) {
        CogPropertyManager m = get(argument);
        if (m instanceof CogLong i) {
            return i.getValue();
        }
        throw new RuntimeException("Argument #" + argument + " is not CogLong");
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
        if (m instanceof CogInteger i) {
            return i.getValue();
        }
        throw new RuntimeException("Argument #" + argument + " is not CogDouble");
    }
    public double requireDoubleOrInt(int argument) {
        CogPropertyManager m = get(argument);
        if (m instanceof CogDouble i) {
            return i.getValue();
        }
        if (m instanceof CogInteger i) {
            return i.getValue();
        }
        throw new RuntimeException("Argument #" + argument + " is not CogDouble/CogInteger");
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

    public DispatchedScript getScript() {
        return script;
    }

    public CogPropertyManager[] getArgs() {
        return args;
    }

    public String getFull() {
        return full;
    }

    public int size() {
        return args.length;
    }

    @Contract(value = "_, _ -> new", pure = false)
    public static @NotNull ArgumentData of(@NotNull String str, DispatchedScript script) {
        ArrayList<String> expressions = new ArrayList<>();
        boolean quotes = false;
        int level = 0;
        int start = 0;
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c == '"') {
                quotes = !quotes;
            } else if (!quotes) {
                if (c == '(') {
                    level++;
                } else if (c == ')') {
                    level--;
                } else if (c == ',' && level == 0) {
                    expressions.add(str.substring(start, i));
                    start = i + 1;
                }
            }
        }
        expressions.add(str.substring(start));
//        System.out.println(String.join("<|>", expressions) + "<---");
        CogPropertyManager[] managers = new CogPropertyManager[expressions.size()];
        for (int i = 0; i < managers.length; i++) {
            managers[i] = CogwheelRegistries.expressionHandler(expressions.get(i), script, false).getB();
        }
        return new ArgumentData(managers, str, script);
    }

    public static @NotNull ArgumentData of(DispatchedScript script, CogPropertyManager a1) {
        return new ArgumentData(new CogPropertyManager[]{a1}, null, script);
    }
    public static @NotNull ArgumentData of(DispatchedScript script, CogPropertyManager a1, CogPropertyManager a2) {
        return new ArgumentData(new CogPropertyManager[]{a1, a2}, null, script);
    }
    public static @NotNull ArgumentData of(DispatchedScript script, CogPropertyManager a1, CogPropertyManager a2, CogPropertyManager a3) {
        return new ArgumentData(new CogPropertyManager[]{a1, a2, a3}, null, script);
    }
    public static @NotNull ArgumentData of(DispatchedScript script, CogPropertyManager... a) {
        return new ArgumentData(a, null, script);
    }

    @Override
    public String toString() {
        return "ArgumentData{" +
                "args=" + Arrays.toString(args) +
                ", script=" + script +
                '}';
    }
}
