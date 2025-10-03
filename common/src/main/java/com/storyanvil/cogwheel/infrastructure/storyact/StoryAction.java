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

package com.storyanvil.cogwheel.infrastructure.storyact;

import com.storyanvil.cogwheel.cog.obj.CogNullManager;
import com.storyanvil.cogwheel.infrastructure.cog.CogString;
import com.storyanvil.cogwheel.infrastructure.err.CogScriptException;
import com.storyanvil.cogwheel.infrastructure.props.JWCGPM_Method;
import com.storyanvil.cogwheel.util.CogwheelExecutor;
import com.storyanvil.cogwheel.EventBus;
import com.storyanvil.cogwheel.infrastructure.ArgumentData;
import com.storyanvil.cogwheel.infrastructure.cog.CogInvoker;
import com.storyanvil.cogwheel.infrastructure.cog.PreventChainCalling;
import com.storyanvil.cogwheel.infrastructure.props.CGPM;
import com.storyanvil.cogwheel.infrastructure.props.JWCGPM;
import com.storyanvil.cogwheel.infrastructure.script.DispatchedScript;
import com.storyanvil.cogwheel.infrastructure.util.CogUtils;
import org.jetbrains.annotations.NotNull;

public abstract class StoryAction<T> extends JWCGPM<StoryAction<T>> {
    private String actionLabel = null;
    public abstract void proceed(T myself);
    public abstract boolean freeToGo(T myself);
    private Runnable onExit;
    private boolean done = false;

    @Override
    public String toString() {
        return "StoryAction{" + this.getClass().getName() + "}";
    }

    public String getActionLabel() {
        return actionLabel;
    }

    public void setActionLabel(String actionLabel) {
        this.actionLabel = actionLabel;
    }

    public void onExit() {
        if (actionLabel != null) {
            EventBus.hitLabel(actionLabel, this);
        }
        if (onExit != null)
            onExit.run();
        done = true;
    };

    public boolean isDone() {
        return done;
    }

    public void setOnExit(Runnable onExit) {
        if (this.onExit != null) throw new RuntimeException("OnExit already set!");
        this.onExit = onExit;
    }

    // ========== === [ JWCGPM METHODS START HERE ] === ========== \\
    @JWCGPM_Method(arguments = {CogString.class})
    public @NotNull CogNullManager setLabel(ArgumentData data, DispatchedScript script) throws CogScriptException {
        this.setActionLabel(data.requireString(0));
        return CogUtils.nullObject;
    }
    @JWCGPM_Method(arguments = {})
    public @NotNull CogString getLabel(@NotNull ArgumentData data, @NotNull DispatchedScript script) {
        return CogUtils.makeCogString(this.getActionLabel());
    }
    @JWCGPM_Method(arguments = {})
    public @NotNull CGPM blocking(@NotNull ArgumentData data, @NotNull DispatchedScript script) {
        if (this.isDone()) return CogUtils.nullObject;
        throw new PreventChainCalling(variable -> {
            this.setOnExit(() -> CogwheelExecutor.schedule(script::lineDispatcher));
        });
    }
    @JWCGPM_Method(arguments = {CogInvoker.class})
    public @NotNull CGPM then(@NotNull ArgumentData data, @NotNull DispatchedScript script) throws CogScriptException {
        CogInvoker invoker = data.requireInvoker(0);
        if (this.isDone()) {
            invoker.safeRunnable(data, script).run();
        } else {
            this.setOnExit(invoker.safeRunnable(data, script));
        }
        return CogUtils.nullObject;
    }

}
