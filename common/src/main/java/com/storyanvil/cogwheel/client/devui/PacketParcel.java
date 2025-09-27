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

package com.storyanvil.cogwheel.client.devui;

import com.storyanvil.cogwheel.CogwheelEngine;
import com.storyanvil.cogwheel.CogwheelHooks;
import com.storyanvil.cogwheel.data.IStoryPacketContext;
import com.storyanvil.cogwheel.data.StoryCodec;
import com.storyanvil.cogwheel.data.StoryCodecs;
import com.storyanvil.cogwheel.data.StoryPacket;

import java.lang.reflect.AccessFlag;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Used to pack multiple packets together preserving their order. Do not confuse with {@link com.storyanvil.cogwheel.neoforge.data.StoryNeoParcel}
 */
public class PacketParcel implements StoryPacket<PacketParcel> {
    private final ArrayList<StoryPacket<?>> packets;

    public static final StoryCodec<PacketParcel> CODEC = new StoryCodec<>((parcel, buf) -> {
        buf.writeInt(parcel.packets.size());
        for (StoryPacket<?> packet : parcel.packets) {
            StoryCodecs.STRING.encode(packet.getClass().getCanonicalName(), buf);
            getCodec(packet.getClass()).encode(packet, buf);
        }
    }, buf -> {
        int size = buf.readInt();
        ArrayList<StoryPacket<?>> packets = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            String clazz = StoryCodecs.STRING.decode(buf);
            packets.add(getCodec(clazz).decode(buf));
        }
        return new PacketParcel(packets);
    });

    private static StoryCodec<StoryPacket<?>> getCodec(String clazz) {
        try {
            return getCodec(Class.forName(clazz));
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Failed to find class to find Codec for parcel", e);
        }
    }

    private static StoryCodec<StoryPacket<?>> getCodec(Class<?> clazz) {
        try {
            Field field = clazz.getField("CODEC");
            if (!field.accessFlags().contains(AccessFlag.STATIC))
                throw new NoSuchFieldException("No public static CODEC in class: " + clazz);
            if (!field.accessFlags().contains(AccessFlag.PUBLIC))
                throw new NoSuchFieldException("No public static CODEC in class: " + clazz);
            return (StoryCodec<StoryPacket<?>>) field.get(null);
        } catch (NoSuchFieldException | IllegalAccessException | ClassCastException e) {
            throw new RuntimeException("Failed to get Codec for parcel!", e);
        }
    }

    private PacketParcel(ArrayList<StoryPacket<?>> packets) {
        this.packets = packets;
        for (int i = 0; i < this.packets.size(); i++) {
            if (this.packets.get(i) == null) {
                i--;
                this.packets.remove(i);
            }
        }
    }

    private PacketParcel() {
        this.packets = new ArrayList<>();
    }

    @Override
    public StoryCodec<PacketParcel> getStoryCodec() {
        return CODEC;
    }

    @Override
    public void onServerUnsafe(IStoryPacketContext ctx) {
        for (StoryPacket<?> packet : packets) {
            CogwheelHooks.debugLog(CogwheelEngine.LOGGER, "Handling server parcel {}", this);
            packet.onServerUnsafe(ctx);
        }
    }

    @Override
    public void onClientUnsafe(IStoryPacketContext ctx) {
        for (StoryPacket<?> packet : packets) {
            CogwheelHooks.debugLog(CogwheelEngine.LOGGER, "Handling client parcel {}", this);
            packet.onClientUnsafe(ctx);
        }
    }

    public static PacketParcel of(StoryPacket<?>... packets) {
        return new PacketParcel(new ArrayList<>(List.of(packets)));
    }
    public static PacketParcel of(ArrayList<StoryPacket<?>> packets) {
        return new PacketParcel(packets);
    }
    public static PacketParcel of(List<StoryPacket<?>> packets) {
        return new PacketParcel(new ArrayList<>(packets));
    }

    @Override
    public String toString() {
        return "PacketParcel{" + packets + '}';
    }
}
