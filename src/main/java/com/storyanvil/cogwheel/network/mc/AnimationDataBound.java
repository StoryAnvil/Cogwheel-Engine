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

package com.storyanvil.cogwheel.network.mc;

import com.storyanvil.cogwheel.CogwheelExecutor;
import com.storyanvil.cogwheel.entity.NPCModel;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;

import java.util.Arrays;
import java.util.function.Supplier;

public class AnimationDataBound {
    private String sources;

    public AnimationDataBound(String sources) {
        this.sources = sources;
    }

    public AnimationDataBound() {
        sources = null;
    }

    public void encode(FriendlyByteBuf friendlyByteBuf) {
        int length = sources.length();
        friendlyByteBuf.writeInt(length);
        for (int i = 0; i < length; i++) {
            friendlyByteBuf.writeByte(sources.charAt(i));
        }
    }

    public static AnimationDataBound decode(FriendlyByteBuf friendlyByteBuf) {
        AnimationDataBound dataBound = new AnimationDataBound();
        int length = friendlyByteBuf.readInt();
        byte[] str = new byte[length];
        for (int i = 0; i < length; i++) {
            str[i] = friendlyByteBuf.readByte();
        }
        dataBound.sources = new String(str);
        return dataBound;
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        String[] locs = sources.split("\\|");
        ResourceLocation[] locations = new ResourceLocation[locs.length];
        for (int i = 0; i < locations.length; i++) {
            locations[i] = ResourceLocation.parse(locs[i]);
        }
        NPCModel.animationSources = locations;
        CogwheelExecutor.log.info("Animation source got updated: {}", Arrays.toString(NPCModel.animationSources));
        ctx.get().setPacketHandled(true);
    }
}
