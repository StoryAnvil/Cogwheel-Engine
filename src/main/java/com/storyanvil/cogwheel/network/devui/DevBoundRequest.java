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

package com.storyanvil.cogwheel.network.devui;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.storyanvil.cogwheel.CogwheelExecutor;
import com.storyanvil.cogwheel.client.devui.DevTab;
import com.storyanvil.cogwheel.network.mc.CogwheelPacketHandler;
import com.storyanvil.cogwheel.network.mc.Notification;
import com.storyanvil.cogwheel.util.StoryUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.function.Supplier;

import static com.storyanvil.cogwheel.CogwheelExecutor.log;

public class DevBoundRequest {
    private @NotNull String type;
    private @NotNull String data;

    @Contract(pure = true)
    public DevBoundRequest() {
        type = "";
        data = "";
    }

    @Contract(pure = true)
    public DevBoundRequest(@NotNull String type) {
        this.type = type;
        this.data = "";
    }

    @Contract(pure = true)
    public DevBoundRequest(@NotNull String type, @NotNull String data) {
        this.type = type;
        this.data = data;
    }

    public @NotNull String getType() {
        return type;
    }

    public void encode(@NotNull FriendlyByteBuf friendlyByteBuf) {
        StoryUtils.encodeString(friendlyByteBuf, type);
        StoryUtils.encodeString(friendlyByteBuf, data);
    }

    public static @NotNull DevBoundRequest decode(FriendlyByteBuf friendlyByteBuf) {
        DevBoundRequest bound = new DevBoundRequest();
        bound.type = StoryUtils.decodeString(friendlyByteBuf);
        bound.data = StoryUtils.decodeString(friendlyByteBuf);
        return bound;
    }

    public void handle(@NotNull Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            try {
                boolean server = ctx.get().getDirection().getReceptionSide().isServer();
                log.debug("[DevUI Bound] {} | For server: {}", this, server);
                switch (type) {
                    case "full" -> {
                        if (!server) break;
                        if (!data.equals("silent")) {
                            CogwheelPacketHandler.DELTA_BRIDGE.send(PacketDistributor.PLAYER.with(() -> ctx.get().getSender()), new Notification(Component.translatable("ui.storyanvil_cogwheel.notif_sync"), Component.translatable("ui.storyanvil_cogwheel.notif_sync_msg")));
                        }
                        break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        ctx.get().setPacketHandled(true);
    }

    public JsonObject parse() {
        return JsonParser.parseString(data).getAsJsonObject();
    }

    @Override
    public String toString() {
        return "DevBoundRequest{" +
                "type='" + type + '\'' +
                ", data='" + data + '\'' +
                '}';
    }
}
