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

import com.storyanvil.cogwheel.CogwheelEngine;
import com.storyanvil.cogwheel.util.CogwheelExecutor;
import com.storyanvil.cogwheel.data.StoryCodec;
import com.storyanvil.cogwheel.data.StoryPacket;
import com.storyanvil.cogwheel.neoforge.CogwheelEngineNeoForge;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public class StoryNeoPacket<T extends StoryPacket<T>> implements PacketCodec<PacketByteBuf, StoryNeoParcel<T>> {
    private final StoryCodec<T> parent;
    private final CustomPayload.Type<PacketByteBuf, StoryNeoParcel<T>> type;

    public StoryNeoPacket(@NotNull String id, @NotNull StoryCodec<T> parent) {
        this.parent = parent;
        this.type = new CustomPayload.Type<>(new CustomPayload.Id<>(Identifier.of(CogwheelEngine.MODID, id)), this);
        parent.setPlatformData(this);
    }

    @Override
    public StoryNeoParcel<T> decode(PacketByteBuf buf) {
        return new StoryNeoParcel<>(parent.decode(buf));
    }

    @Override
    public void encode(PacketByteBuf buf, StoryNeoParcel<T> value) {
        parent.encode(value.get(), buf);
    }

    public CustomPayload.Type<PacketByteBuf, StoryNeoParcel<T>> getType() {
        return type;
    }

    public <S extends StoryPacket<S>> void clientHandle(StoryNeoParcel<S> parcel, IPayloadContext ctx) {
        CogwheelExecutor.schedule(() -> {
            CogwheelEngineNeoForge.PLATFORM_LOG.debug("Received packet {} from server", parcel);
            parcel.get().onClientUnsafe(new StoryNeoPacketContext(ctx));
        });
    }

    public <S extends StoryPacket<S>> void serverHandle(StoryNeoParcel<S> parcel, IPayloadContext ctx) {
        CogwheelExecutor.schedule(() -> {
            CogwheelEngineNeoForge.PLATFORM_LOG.debug("Received packet {} from client: {}", parcel, ctx.player());
            parcel.get().onServerUnsafe(new StoryNeoPacketContext(ctx));
        });
    }

    @Override
    public String toString() {
        return "StoryNeoPacket{" +
                "parent=" + parent +
                ", type=" + type +
                '}';
    }
}
