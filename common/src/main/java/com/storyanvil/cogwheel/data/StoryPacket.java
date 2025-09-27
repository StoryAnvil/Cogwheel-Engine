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
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;

@Api.Experimental(since = "2.10.0")
public interface StoryPacket<T extends StoryPacket<T>> extends IStorySerializable<T> {
    default void onClientUnsafe(IStoryPacketContext ctx) {};
    default void onServerUnsafe(IStoryPacketContext ctx) {};

    default T getAsData() {
        return (T) this;
    }

    default void error(IStoryPacketContext ctx, String err) {
        //TODO
    }
    default void clientError(IStoryPacketContext ctx, String err) {
        MinecraftClient.getInstance().getToastManager().add(new SystemToast(
                SystemToast.Type.PERIODIC_NOTIFICATION, Text.literal("Server change!"), Text.literal(err)
        ));
    }
}
