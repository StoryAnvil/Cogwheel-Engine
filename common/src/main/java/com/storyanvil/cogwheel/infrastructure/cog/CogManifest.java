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

import com.storyanvil.cogwheel.api.Api;
import com.storyanvil.cogwheel.infrastructure.ArgumentData;
import com.storyanvil.cogwheel.infrastructure.err.CogScriptException;
import com.storyanvil.cogwheel.infrastructure.props.CGPM;
import com.storyanvil.cogwheel.infrastructure.env.LibraryEnvironment;
import com.storyanvil.cogwheel.infrastructure.script.DispatchedScript;
import com.storyanvil.cogwheel.infrastructure.env.CogScriptEnvironment;
import com.storyanvil.cogwheel.infrastructure.err.CogExpressionFailure;
import com.storyanvil.cogwheel.util.EasyPropManager;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CogManifest implements CGPM {
    private static final EasyPropManager MANAGER = new EasyPropManager("manifest", CogManifest::registerProps);

    private static CogManifest instance = null;

    @Api.Experimental(since = "2.0.0")
    public static CogManifest getInstance() {
        if (instance == null) {
            instance = new CogManifest();
        }
        return instance;
    }

    private static void registerProps(@NotNull EasyPropManager manager) {
        manager.reg("subscribeEvent", (name, args, script, o) -> {
            script.getEnvironment().subscribeForEvent(Identifier.tryParse(args.getString(0)), args.getString(1));
            return null;
        });
        manager.reg("unsubscribeEvent", (name, args, script, o) -> {
            script.getEnvironment().unsubscribeFromEvent(Identifier.tryParse(args.getString(0)), args.getString(1));
            return null;
        });
        manager.reg("clearEvent", (name, args, script, o) -> {
            script.getEnvironment().unsubscribeAllFromEvent(Identifier.tryParse(args.getString(0)));
            return null;
        });
        manager.reg("requireLibrary", (name, args, script, o) -> {
            LibraryEnvironment env = CogScriptEnvironment.getLibEnvironment(args.getString(0));
            if (env == null) {
                script.haltExecution();
                throw script.wrap(new CogExpressionFailure("Library \"" + args.getString(0) + "\" required by " + script.getEnvironment().getUniqueIdentifier() + " environment but it does not present!"));
            }
            return null;
        });
    }

    @Override
    public boolean hasOwnProperty(String name) {
        return MANAGER.hasOwnProperty(name);
    }

    @Override
    public @Nullable CGPM getProperty(String name, ArgumentData args, DispatchedScript script) throws PreventSubCalling, CogScriptException {
        return MANAGER.get(name).handle(name, args, script, this);
    }

    @Override
    public boolean equalsTo(CGPM o) {
        return o == this;
    }
}
