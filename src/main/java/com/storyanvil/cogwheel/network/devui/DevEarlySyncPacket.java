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

import com.storyanvil.cogwheel.CogwheelExecutor;
import com.storyanvil.cogwheel.client.devui.DevUI;
import com.storyanvil.cogwheel.client.devui.DevUIScreen;
import com.storyanvil.cogwheel.config.CogwheelConfig;
import com.storyanvil.cogwheel.data.StoryCodec;
import com.storyanvil.cogwheel.data.StoryPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

import static com.storyanvil.cogwheel.data.StoryCodecBuilder.*;

public record DevEarlySyncPacket(boolean permitted, boolean silent) implements StoryPacket {
    public static final StoryCodec<DevEarlySyncPacket> CODEC = build(
            Bool(DevEarlySyncPacket::permitted),
            Bool(DevEarlySyncPacket::silent),
            DevEarlySyncPacket::new
    );

    public static void syncFor(ServerPlayer player, boolean silent) {
        DevNetwork.sendFromServer(player, new DevEarlySyncPacket(isDev(), silent));
    }
    public static void syncAll(boolean silent) {
        DevNetwork.sendFromServer(new DevEarlySyncPacket(isDev(), silent));
    }

    public static boolean isDev() {
        return CogwheelConfig.isDevEnvironment();
    }

    @Override
    public void onServerUnsafe(Supplier<NetworkEvent.Context> ctx) {
        syncFor(ctx.get().getSender(), this.silent);
    }

    @Override
    public void onClientUnsafe(Supplier<NetworkEvent.Context> ctx) {
        DevUI.permitted = permitted;
        if (!permitted) {
            CogwheelExecutor.scheduleTickEventClientSide(levelTickEvent -> {
                if (Minecraft.getInstance().screen instanceof DevUIScreen) {
                    Minecraft.getInstance().setScreen(null);
                    Minecraft.getInstance().getToasts().addToast(new SystemToast(
                            SystemToast.SystemToastIds.PERIODIC_NOTIFICATION, Component.translatable("ui.storyanvil_cogwheel.notif_block"), Component.translatable("ui.storyanvil_cogwheel.notif_block_msg")
                    ));
                }
            });
        }
        if (!silent) {
            Minecraft.getInstance().getToasts().addToast(new SystemToast(
                    SystemToast.SystemToastIds.PERIODIC_NOTIFICATION, Component.translatable("ui.storyanvil_cogwheel.notif_sync"), Component.translatable("ui.storyanvil_cogwheel.notif_sync_msg")
            ));
        }
    }

    @Override
    public String toString() {
        return "DevEarlySyncPacket{" +
                "permitted=" + permitted +
                ", silent=" + silent +
                '}';
    }
}
