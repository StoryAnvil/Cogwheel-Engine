/*
 *
 *  * StoryAnvil CogWheel Engine
 *  * Copyright (C) 2025 StoryAnvil
 *  *
 *  * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *  *
 *  * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 *  *
 *  * You should have received a copy of the GNU Lesser General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.storyanvil.cogwheel.infrustructure.cog;

import com.storyanvil.cogwheel.CogwheelExecutor;
import com.storyanvil.cogwheel.infrustructure.ArgumentData;
import com.storyanvil.cogwheel.infrustructure.CogPropertyManager;
import com.storyanvil.cogwheel.infrustructure.DispatchedScript;
import com.storyanvil.cogwheel.util.EasyPropManager;
import org.jetbrains.annotations.Nullable;

public class CogTestCallback implements CogPropertyManager {
    private static final EasyPropManager MANAGER = new EasyPropManager("testcallback", CogTestCallback::registerProps);

    private boolean complete = false;
    private boolean successful = true;

    public boolean isComplete() {
        return complete;
    }

    public boolean isSuccessful() {
        return successful;
    }

    private static void registerProps(EasyPropManager manager) {
        manager.reg("done", (name, args, script, o) -> {
            CogTestCallback callback = (CogTestCallback) o;
            callback.complete = true;
            return null;
        });
        manager.reg("assertEquals", (name, args, script, o) -> {
            CogTestCallback callback = (CogTestCallback) o;
            if (callback.successful) {
                CogPropertyManager a = args.get(0);
                CogPropertyManager b = args.get(1);
                callback.successful = a.equalsTo(b);
                if (!callback.successful) {
                    CogwheelExecutor.log.warn("TEST ASSERTION FAILED! ASSERTED EQUALS, BUT {} != {}", a.convertToString(), b.convertToString());
                }
            }
            return null;
        });
        manager.reg("debugWait", (name, args, script, o) -> {
            try {
                Thread.sleep(args.requireInt(0));
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return null;
        });
    }

    @Override
    public boolean hasOwnProperty(String name) {
        return MANAGER.hasOwnProperty(name);
    }

    @Override
    public @Nullable CogPropertyManager getProperty(String name, ArgumentData args, DispatchedScript script) throws PreventSubCalling {
        return MANAGER.get(name).handle(name, args, script, this);
    }

    @Override
    public boolean equalsTo(CogPropertyManager o) {
        return false;
    }
}
