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

package com.storyanvil.cogwheel.cog.obj;

import com.storyanvil.cogwheel.CogwheelHooks;
import com.storyanvil.cogwheel.cog.action.*;
import com.storyanvil.cogwheel.entity.AbstractNPC;
import com.storyanvil.cogwheel.infrastructure.ArgumentData;
import com.storyanvil.cogwheel.infrastructure.cog.CogBool;
import com.storyanvil.cogwheel.infrastructure.cog.CogInteger;
import com.storyanvil.cogwheel.infrastructure.cog.CogString;
import com.storyanvil.cogwheel.infrastructure.cog.PreventChainCalling;
import com.storyanvil.cogwheel.infrastructure.err.CogScriptException;
import com.storyanvil.cogwheel.infrastructure.props.JWCGPM_Method;
import com.storyanvil.cogwheel.infrastructure.script.DispatchedScript;
import com.storyanvil.cogwheel.infrastructure.storyact.StoryAction;
import com.storyanvil.cogwheel.infrastructure.util.CogUtils;
import com.storyanvil.cogwheel.network.mc.DialogChoiceBound;
import com.storyanvil.cogwheel.util.CogwheelExecutor;
import com.storyanvil.cogwheel.util.FriendlyWeakRef;
import net.minecraft.entity.Entity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class CogNPC<T extends Entity & AbstractNPC<T>> extends CogEntity<T> {
    private final FriendlyWeakRef<T> ref;

    public CogNPC(T ref) {
        super(ref);
        this.ref = new FriendlyWeakRef<>(ref);
    }

    public CogNPC(AbstractNPC<T> npc) {
        //noinspection unchecked // Cast will always succsess
        this((T) npc);
    }

    // ========== === [ JWCGPM METHODS START HERE ] === ========== \\
    @JWCGPM_Method @Override
    public @NotNull CogBool isValid(@NotNull ArgumentData data, @NotNull DispatchedScript script) throws CogScriptException {
        return CogUtils.makeCogBool(this.ref.isStillValid() && super.isValid(data, script).getValue());
    }
    @JWCGPM_Method(arguments = {CogString.class})
    public @NotNull CogNullManager setSkin(@NotNull ArgumentData data, @NotNull DispatchedScript script) throws CogScriptException {
        this.ref.getSafely(script).npc$setSkin(data.requireString(0));
        return CogUtils.nullObject;
    }
    @JWCGPM_Method(arguments = {})
    public @NotNull CogString getSkin(@NotNull ArgumentData data, @NotNull DispatchedScript script) throws CogScriptException {
        return CogUtils.makeCogString(this.ref.getSafely(script).npc$getSkin());
    }
    @JWCGPM_Method(arguments = {CogString.class})
    public @NotNull CogNullManager setName(@NotNull ArgumentData data, @NotNull DispatchedScript script) throws CogScriptException {
        this.ref.getSafely(script).npc$setName(data.requireString(0));
        return CogUtils.nullObject;
    }
    @JWCGPM_Method(arguments = {})
    public @NotNull CogString getName(@NotNull ArgumentData data, @NotNull DispatchedScript script) throws CogScriptException {
        return CogUtils.makeCogString(this.ref.getSafely(script).npc$getName());
    }
    @JWCGPM_Method(arguments = {CogString.class})
    public @NotNull CogNullManager setModel(@NotNull ArgumentData data, @NotNull DispatchedScript script) throws CogScriptException {
        this.ref.getSafely(script).npc$setModel(data.requireString(0));
        return CogUtils.nullObject;
    }
    @JWCGPM_Method(arguments = {})
    public @NotNull CogString getModel(@NotNull ArgumentData data, @NotNull DispatchedScript script) throws CogScriptException {
        return CogUtils.makeCogString(this.ref.getSafely(script).npc$getModel());
    }
    @JWCGPM_Method(arguments = {CogString.class})
    public @NotNull StoryAction<T> chatAction(@NotNull ArgumentData data, @NotNull DispatchedScript script) throws CogScriptException {
        return this.ref.getSafely(script).addChainedChecked(new ChatStoryAction(Text.literal(data.requireString(0))));
    }
    @JWCGPM_Method(arguments = {CogInteger.class, CogInteger.class, CogInteger.class})
    public @NotNull StoryAction<T> pathfindAction(@NotNull ArgumentData data, @NotNull DispatchedScript script) throws CogScriptException {
        return this.ref.getSafely(script).addChainedChecked(new PathfindStoryAction(new BlockPos(
                data.requireInt(0), data.requireInt(1), data.requireInt(2)
        )));
    }
    @JWCGPM_Method(arguments = {CogString.class, CogInteger.class})
    public @NotNull StoryAction<T> waitForLabelAction(@NotNull ArgumentData data, @NotNull DispatchedScript script) throws CogScriptException {
        if (data.size() == 1)
            return this.ref.getSafely(script).addChainedChecked(new WaitForLabelAction(data.requireString(0)));
        else
            return this.ref.getSafely(script).addChainedChecked(new WaitForLabelAction(data.requireString(0), data.requireInt(1)));
    }
    @JWCGPM_Method(arguments = {CogString.class, CogInteger.class})
    public @NotNull StoryAction<T> tickAnimationAction(@NotNull ArgumentData data, @NotNull DispatchedScript script) throws CogScriptException {
        return this.ref.getSafely(script).addChainedChecked(new AnimationStoryAction(
                data.requireString(0), data.requireInt(1)
        ));
    }
    @JWCGPM_Method(arguments = {CogString.class, CogString.class})
    public @NotNull StoryAction<T> dialogAction(@NotNull ArgumentData data, @NotNull DispatchedScript script) throws CogScriptException {
        T npc = this.ref.getSafely(script);
        if (data.size() == 1) {
            return npc.addChainedChecked(new DialogStoryAction(data.requireString(0), npc));
        } else {
            return npc.addChainedChecked(new DialogStoryAction(data.requireString(0), npc, data.requireString(1)));
        }
    }
    @JWCGPM_Method(arguments = {})
    public @NotNull CogInteger dialogChoiceAction(@NotNull ArgumentData data, @NotNull DispatchedScript script) throws CogScriptException {
        T npc = this.ref.getSafely(script);
        final String dialogID = UUID.randomUUID().toString();
        int optionsLength = data.size() - 1;
        String[] options = new String[optionsLength - 1];
        for (int i = 0; i < optionsLength - 1; i++) {
            options[i] = data.getString(i + 1);
        }
        DialogChoiceBound bound = new DialogChoiceBound(dialogID, data.getString(0), options, npc.npc$getName(), data.requireString(optionsLength));
        npc.addStoryActionChecked(new InstantPacketStoryAction(bound));
        throw new PreventChainCalling(variable -> {
            // Dialogs can be registered only in default environment
            CogwheelExecutor.getDefaultEnvironment().registerDialog(dialogID, response -> {
                CogwheelExecutor.schedule(() -> {
                    // Send dialog close packet
                    CogwheelExecutor.scheduleTickEvent(levelTickEvent -> {
                        CogwheelHooks.sendPacketToEveryone(new DialogChoiceBound());
                    });
                    script.put(variable, CogUtils.makeCogInt(response));
                    CogwheelExecutor.schedule(script::lineDispatcher);
                });
            });
        });
    }
}
