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

package com.storyanvil.cogwheel.fabric.data;

import com.storyanvil.cogwheel.CogwheelEngine;
import com.storyanvil.cogwheel.CogwheelExecutor;
import com.storyanvil.cogwheel.data.StoryCodec;
import com.storyanvil.cogwheel.data.StoryPacket;
import com.storyanvil.cogwheel.fabric.CogwheelEngineFabric;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

public class StoryFabricPacket<T extends StoryPacket<T>> implements PacketCodec<PacketByteBuf, StoryFabricParcel<T>> {
    private final StoryCodec<T> parent;
    private final CustomPayload.Type<PacketByteBuf, StoryFabricParcel<T>> type;

    public StoryFabricPacket(@NotNull String id, @NotNull StoryCodec<T> parent) {
        this.parent = parent;
        this.type = new CustomPayload.Type<>(new CustomPayload.Id<>(Identifier.of(CogwheelEngine.MODID, id)), this);
        parent.setPlatformData(this);
    }

    @Override
    public StoryFabricParcel<T> decode(PacketByteBuf buf) {
        return new StoryFabricParcel<>(parent.decode(buf));
    }

    @Override
    public void encode(PacketByteBuf buf, StoryFabricParcel<T> value) {
        parent.encode(value.get(), buf);
    }

    public CustomPayload.Type<PacketByteBuf, StoryFabricParcel<T>> getType() {
        return type;
    }

    @Override
    public String toString() {
        return "StoryFabricPacket{" +
                "parent=" + parent +
                ", type=" + type +
                '}';
    }

    public <S extends StoryPacket<S>> void handleClient(StoryFabricParcel<S> parcel, ClientPlayNetworking.Context ctx) {
        CogwheelExecutor.schedule(() -> {
            CogwheelEngineFabric.PLATFORM_LOG.debug("Received packet {} from server", parcel);
            parcel.get().onClientUnsafe(new StoryFabricContext(ctx));
        });
    }

    public <S extends StoryPacket<S>> void handleServer(StoryFabricParcel<S> parcel, ServerPlayNetworking.Context ctx) {
        CogwheelExecutor.schedule(() -> {
            CogwheelEngineFabric.PLATFORM_LOG.debug("Received packet {} from client: {}", parcel, ctx.player());
            parcel.get().onClientUnsafe(new StoryFabricContext(ctx));
        });
    }
}
