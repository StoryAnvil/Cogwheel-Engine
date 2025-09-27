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

package com.storyanvil.cogwheel.network.mc;

import com.storyanvil.cogwheel.data.IStoryPacketContext;
import com.storyanvil.cogwheel.data.StoryCodec;
import com.storyanvil.cogwheel.data.StoryCodecBldr;
import com.storyanvil.cogwheel.data.StoryPacket;

public record AnimationBound(String animatorID, String animation) implements StoryPacket<AnimationBound> {
    public static final StoryCodec<AnimationBound> CODEC = StoryCodecBldr.build(
            StoryCodecBldr.String(AnimationBound::animatorID),
            StoryCodecBldr.String(AnimationBound::animation),
            AnimationBound::new
    );

    @Override
    public StoryCodec<AnimationBound> getStoryCodec() {
        return CODEC;
    }

    @Override
    public void onClientUnsafe(IStoryPacketContext ctx) {
        CogwheelClientPacketHandler.animationBound(this, ctx);
    }
}
