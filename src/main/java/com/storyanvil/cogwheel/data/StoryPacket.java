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

package com.storyanvil.cogwheel.data;

import com.storyanvil.cogwheel.api.Api;
import com.storyanvil.cogwheel.network.devui.DevNetwork;
import com.storyanvil.cogwheel.network.mc.CogwheelPacketHandler;
import com.storyanvil.cogwheel.network.mc.Notification;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.function.Supplier;

@Api.Experimental(since = "2.10.0")
public interface StoryPacket {
    default void handle(Supplier<NetworkEvent.Context> ctx) {
        try {
            ctx.get().enqueueWork(() -> {
                DevNetwork.log.debug("Handling {} as {}", this.toString(), this.getClass().getCanonicalName());
                if (ctx.get().getDirection().getReceptionSide().isServer()) onServerUnsafe(ctx);
                else onClientUnsafe(ctx);
            });
            ctx.get().setPacketHandled(true);
        } catch (Exception e) {
            DevNetwork.log.error("Exception while handling packet {}", this.getClass().getCanonicalName(), e);
            throw e;
        }
    };

    default void onClientUnsafe(Supplier<NetworkEvent.Context> ctx) {};
    default void onServerUnsafe(Supplier<NetworkEvent.Context> ctx) {};

    default void error(ServerPlayer plr, String err) {
        CogwheelPacketHandler.DELTA_BRIDGE.send(PacketDistributor.PLAYER.with(() -> plr), new Notification(
                Component.literal("Invalid Packet"), Component.literal(err)
        ));
    }
}
