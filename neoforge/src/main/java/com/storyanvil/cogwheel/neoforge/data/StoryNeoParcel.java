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

package com.storyanvil.cogwheel.neoforge.data;

import com.storyanvil.cogwheel.data.StoryPacket;
import com.storyanvil.cogwheel.util.B;
import net.minecraft.network.packet.CustomPayload;

public class StoryNeoParcel<T extends StoryPacket<T>> extends B<T> implements CustomPayload {
    public StoryNeoParcel(T decode) {
        super(decode);
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        //noinspection unchecked // Platform helper
        return ((StoryNeoPacket<T>) super.get().getStoryCodec().getPlatformData()).getType().id();
    }
}
