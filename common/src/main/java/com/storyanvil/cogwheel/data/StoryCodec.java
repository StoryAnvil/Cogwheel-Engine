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

import com.storyanvil.cogwheel.api.Api;
import net.minecraft.network.PacketByteBuf;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;

@Api.Experimental(since = "2.10.0")
public final class StoryCodec<T> implements IStoryCodec<T, PacketByteBuf> {
    private final BiConsumer<T, PacketByteBuf> encoder;
    private final Function<PacketByteBuf, T> decoder;
    private Object platformData;

    public StoryCodec(BiConsumer<T, PacketByteBuf> encoder, Function<PacketByteBuf, T> decoder) {
        this.encoder = encoder;
        this.decoder = decoder;
    }

    @Api.Experimental(since = "2.10.0")
    public void encode(T v, PacketByteBuf buf) {
        encoder.accept(v, buf);
    }

    @Api.Experimental(since = "2.10.0")
    public T decode(PacketByteBuf buf) {
        return decoder.apply(buf);
    }

    @Api.Experimental(since = "2.10.0")
    public static <T> StoryCodec<T> fromIStoryCodec(IStoryCodec<T, PacketByteBuf> codec) {
        return new StoryCodec<>(codec::encode, codec::decode);
    }

    public StoryCodec<T> nullable() {
        return StoryCodecs.getNullableCodec(this);
    }
    @Api.PlatformTool
    public Object getPlatformData() {
        return platformData;
    }

    @Api.PlatformTool
    public void setPlatformData(Object platformData) {
        this.platformData = platformData;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (StoryCodec<?>) obj;
        return Objects.equals(this.encoder, that.encoder) &&
                Objects.equals(this.decoder, that.decoder);
    }

    @Override
    public int hashCode() {
        return Objects.hash(encoder, decoder);
    }

    @Override
    public String toString() {
        return "StoryCodec[" +
                "encoder=" + encoder + ", " +
                "decoder=" + decoder + ']';
    }
}
