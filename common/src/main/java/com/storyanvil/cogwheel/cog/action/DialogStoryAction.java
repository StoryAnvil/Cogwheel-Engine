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

package com.storyanvil.cogwheel.cog.action;

import com.storyanvil.cogwheel.CogwheelHooks;
import com.storyanvil.cogwheel.config.CogwheelConfig;
import com.storyanvil.cogwheel.entity.AbstractNPC;
import com.storyanvil.cogwheel.infrastructure.script.DialogScript;
import com.storyanvil.cogwheel.infrastructure.storyact.TickingStoryActionBase;
import com.storyanvil.cogwheel.network.mc.AnimationBound;
import com.storyanvil.cogwheel.network.mc.DialogBound;

public class DialogStoryAction extends TickingStoryActionBase<AbstractNPC<?>> {
    private final DialogBound bound;

    public DialogStoryAction(String npcPhrase, AbstractNPC<?> parent) {
        super(DialogScript.computeTicks(npcPhrase));
        this.bound = new DialogBound(npcPhrase, parent.npc$getName(), parent.npc$getDefaultPhoto());
    }
    public DialogStoryAction(String npcPhrase, AbstractNPC<?> parent, String npcPhoto) {
        super(DialogScript.computeTicks(npcPhrase));
        this.bound = new DialogBound(npcPhrase, parent.npc$getName(), npcPhoto);
    }

    @Override
    public void proceed(AbstractNPC<?> myself) {
        CogwheelHooks.sendPacketToEveryone(bound);
        if (CogwheelConfig.isNpcTalkingAnimationEnabled())
            CogwheelHooks.sendPacketToEveryone(new AnimationBound(myself.npc$getAnimatorID(), "animation.npc.talk"));
    }

    @Override
    public void onEnding(AbstractNPC<?> myself) {
        CogwheelHooks.sendPacketToEveryone(new DialogBound());
        if (CogwheelConfig.isNpcTalkingAnimationEnabled())
            CogwheelHooks.sendPacketToEveryone(new AnimationBound(myself.npc$getAnimatorID(), "null"));
    }
}
