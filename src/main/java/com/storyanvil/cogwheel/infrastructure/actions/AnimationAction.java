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

package com.storyanvil.cogwheel.infrastructure.actions;

import com.google.gson.JsonObject;
import com.storyanvil.cogwheel.infrastructure.StoryAction;
import com.storyanvil.cogwheel.infrastructure.abilities.StoryAnimator;
import com.storyanvil.cogwheel.network.mc.AnimationBound;
import com.storyanvil.cogwheel.network.mc.CogwheelPacketHandler;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

public class AnimationAction extends StoryAction<StoryAnimator> {
    private final String animation;
    private int ticks;

    public AnimationAction(String animationName, int ticks) {
        this.animation = animationName;
        this.ticks = ticks;
    }

    @Override
    public void proceed(@NotNull StoryAnimator myself) {
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

    @Override
    protected void toJSON(JsonObject obj) {
        super.toJSON(obj);
        obj.addProperty("ticks", ticks);
        obj.addProperty("animationName", animation);
    }
}
