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

package com.storyanvil.cogwheel.infrustructure.actions;

import com.storyanvil.cogwheel.entity.NPC;
import com.storyanvil.cogwheel.infrustructure.StoryAction;
import com.storyanvil.cogwheel.infrustructure.abilities.StoryAnimator;
import com.storyanvil.cogwheel.network.mc.AnimationBound;
import com.storyanvil.cogwheel.network.mc.CogwheelPacketHandler;
import net.minecraftforge.network.PacketDistributor;

public class AnimationAction extends StoryAction<StoryAnimator> {
    private final String animation;
    private int ticks;

    public AnimationAction(String animationName, int ticks) {
        this.animation = animationName;
        this.ticks = ticks;
    }

    @Override
    public void proceed(StoryAnimator myself) {
        CogwheelPacketHandler.DELTA_BRIDGE.send(PacketDistributor.ALL.noArg(), new AnimationBound(myself.getAnimatorID(), animation));
    }

    @Override
    public boolean freeToGo(StoryAnimator myself) {
        ticks--;
        if (ticks <= 0) {
            CogwheelPacketHandler.DELTA_BRIDGE.send(PacketDistributor.ALL.noArg(), new AnimationBound(myself.getAnimatorID(), "null"));
            return true;
        }
        return false;
    }
}
