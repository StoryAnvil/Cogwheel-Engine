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

import com.storyanvil.cogwheel.CogwheelHooks;
import com.storyanvil.cogwheel.data.IStoryPacketContext;
import com.storyanvil.cogwheel.data.StoryCodec;
import com.storyanvil.cogwheel.data.StoryCodecBldr;
import com.storyanvil.cogwheel.data.StoryPacket;
import net.minecraft.util.Identifier;

public record AnimationDataBound(String sources) implements StoryPacket<AnimationDataBound> {
    public static final StoryCodec<AnimationDataBound> CODEC = StoryCodecBldr.build(
            StoryCodecBldr.String(AnimationDataBound::sources),
            AnimationDataBound::new
    );

    @Override
    public StoryCodec<AnimationDataBound> getStoryCodec() {
        return CODEC;
    }

    @Override
    public void onClientUnsafe(IStoryPacketContext ctx) {
        String[] locs = sources.split("\\|");
        Identifier[] locations = new Identifier[locs.length];
        for (int i = 0; i < locations.length; i++) {
            locations[i] = Identifier.tryParse(locs[i]);
        }
        CogwheelHooks.setAnimationData(locations);
    }
}
