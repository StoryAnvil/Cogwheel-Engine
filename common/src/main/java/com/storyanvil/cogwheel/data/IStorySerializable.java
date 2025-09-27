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

package com.storyanvil.cogwheel.data;

import net.minecraft.network.PacketByteBuf;

public interface IStorySerializable<T extends IStorySerializable<T>> extends IStoryCodec<T, PacketByteBuf> {
    StoryCodec<T> getStoryCodec();

    @Override
    default void encode(T t, PacketByteBuf buf) {
        getStoryCodec().encode(t, buf);
    }

    default void encodeThis(PacketByteBuf buf) {
        getStoryCodec().encode((T) this, buf);
    }

    @Override
    default T decode(PacketByteBuf buf) {
        return getStoryCodec().decode(buf);
    }
}
