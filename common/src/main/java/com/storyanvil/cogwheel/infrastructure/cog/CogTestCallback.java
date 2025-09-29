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

package com.storyanvil.cogwheel.infrastructure.cog;

import com.storyanvil.cogwheel.infrastructure.ArgumentData;
import com.storyanvil.cogwheel.infrastructure.CGPM;
import com.storyanvil.cogwheel.infrastructure.script.DispatchedScript;
import com.storyanvil.cogwheel.infrastructure.testing.TestManagement;
import com.storyanvil.cogwheel.util.EasyPropManager;
import org.jetbrains.annotations.Nullable;

public class CogTestCallback implements CGPM {
    private final TestManagement.Result testResult;

    public CogTestCallback(TestManagement.Result testResult, TestManagement management) {
        this.testResult = testResult;
    }

    private static final EasyPropManager MANAGER = new EasyPropManager("testCallback", manager -> {
        manager.reg("assertEq", (name, args, script, o) -> {
            CogTestCallback callback = (CogTestCallback) o;
            CGPM actual = args.get(0);
            CGPM expected = args.get(1);
            if (!actual.equalsTo(expected)) {
                callback.testResult.failWith(new AssertionError(line(script) + "EQUALS ASSERTION FAILED! \"" + actual.convertToString() + "\" != \"" + expected.convertToString() + "\""));
            }
            return null;
        });
        manager.reg("assertNe", (name, args, script, o) -> {
            CogTestCallback callback = (CogTestCallback) o;
            CGPM actual = args.get(0);
            CGPM expected = args.get(1);
            if (actual.equalsTo(expected)) {
                callback.testResult.failWith(new AssertionError(line(script) + "NOTEQUALS ASSERTION FAILED! \"" + actual.convertToString() + "\" == \"" + expected.convertToString() + "\""));
            }
            return null;
        });
        manager.reg("assertNull", (name, args, script, o) -> {
            CogTestCallback callback = (CogTestCallback) o;
            CGPM actual = args.get(0);
            if (!actual.isNull()) {
                callback.testResult.failWith(new AssertionError(line(script) + "NULL ASSERTION FAILED! \"" + actual.convertToString() + "\" == \"NULL\""));
            }
            return null;
        });
        manager.reg("assertNonNull", (name, args, script, o) -> {
            CogTestCallback callback = (CogTestCallback) o;
            CGPM actual = args.get(0);
            if (actual.isNull()) {
                callback.testResult.failWith(new AssertionError(line(script) + "NONNULL ASSERTION FAILED! \"" + actual.convertToString() + "\" != \"NULL\""));
            }
            return null;
        });
        manager.reg("assertTrue", (name, args, script, o) -> {
            CogTestCallback callback = (CogTestCallback) o;
            CGPM actual = args.get(0);
            if (actual instanceof CogBool bool) {
                if (!bool.getValue()) {
                    callback.testResult.failWith(new AssertionError(line(script) + "TRUTH ASSERTION FAILED! \"" + actual.convertToString() + "\" == FALSE"));
                }
            } else {
                callback.testResult.failWith(new AssertionError(line(script) + "TRUTH ASSERTION FAILED! \"" + actual.convertToString() + "\" is not a CogBool"));
            }
            return null;
        });
        manager.reg("assertFalse", (name, args, script, o) -> {
            CogTestCallback callback = (CogTestCallback) o;
            CGPM actual = args.get(0);
            if (actual instanceof CogBool bool) {
                if (bool.getValue()) {
                    callback.testResult.failWith(new AssertionError(line(script) + "FALSE ASSERTION FAILED! \"" + actual.convertToString() + "\" == FALSE"));
                }
            } else {
                callback.testResult.failWith(new AssertionError(line(script) + "FALSE ASSERTION FAILED! \"" + actual.convertToString() + "\" is not a CogBool"));
            }
            return null;
        });
        manager.reg("assertJavaClass", (name, args, script, o) -> {
            CogTestCallback callback = (CogTestCallback) o;
            CGPM actual = args.get(0);
            Class<?> clazz = actual.getClass();
            if (!clazz.getCanonicalName().equals(args.getString(1))) {
                callback.testResult.failWith(new AssertionError(line(script) + "JAVACLASS ASSERTION FAILED! \"" + actual.convertToString() + "\"(" + clazz.getCanonicalName() + ") is not " + args.getString(1)));
            }
            return null;
        });
        manager.reg("fail", (name, args, script, o) -> {
            CogTestCallback callback = (CogTestCallback) o;
            callback.testResult.failWith(new AssertionError(line(script) + "TEST FAILED BY COGSCRIPT"));
            return null;
        });
    });

    private static String line(DispatchedScript script) {
        return "[LINE:" + script.getExecutionLine() + "] ";
    }

    @Override
    public boolean hasOwnProperty(String name) {
        return MANAGER.hasOwnProperty(name);
    }

    @Override
    public @Nullable CGPM getProperty(String name, ArgumentData args, DispatchedScript script) throws PreventSubCalling {
        return MANAGER.get(name, args, script, this);
    }

    @Override
    public boolean equalsTo(CGPM o) {
        return o == this;
    }
}
