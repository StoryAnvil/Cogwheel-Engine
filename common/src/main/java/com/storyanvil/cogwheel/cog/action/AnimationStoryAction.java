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
import com.storyanvil.cogwheel.entity.AbstractNPC;
import com.storyanvil.cogwheel.infrastructure.storyact.StoryAction;
import com.storyanvil.cogwheel.network.mc.AnimationBound;
import org.jetbrains.annotations.NotNull;

public class AnimationStoryAction extends StoryAction<AbstractNPC<?>> {
    private final String animation;
    private int ticks;

    public AnimationStoryAction(String animationName, int ticks) {
        this.animation = animationName;
        this.ticks = ticks;
    }

    @Override
    public void proceed(@NotNull AbstractNPC<?> myself) {
        CogwheelHooks.sendPacketToEveryone(new AnimationBound(myself.npc$getAnimatorID(), animation));
    }

    @Override
    public boolean freeToGo(AbstractNPC<?> myself) {
        ticks--;
        if (ticks <= 0) {
            CogwheelHooks.sendPacketToEveryone(new AnimationBound(myself.npc$getAnimatorID(), "null"));
            return true;
        }
        return false;
    }
}
