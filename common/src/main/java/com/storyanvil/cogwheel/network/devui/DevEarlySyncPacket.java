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

package com.storyanvil.cogwheel.network.devui;

import com.storyanvil.cogwheel.CogwheelExecutor;
import com.storyanvil.cogwheel.CogwheelHooks;
import com.storyanvil.cogwheel.client.devui.DevUI;
import com.storyanvil.cogwheel.client.devui.DevUIScreen;
import com.storyanvil.cogwheel.config.CogwheelConfig;
import com.storyanvil.cogwheel.data.IStoryPacketContext;
import com.storyanvil.cogwheel.data.StoryCodec;
import com.storyanvil.cogwheel.data.StoryPacket;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import static com.storyanvil.cogwheel.data.StoryCodecBldr.*;

public record DevEarlySyncPacket(boolean permitted, boolean silent) implements StoryPacket<DevEarlySyncPacket> {
    public static final StoryCodec<DevEarlySyncPacket> CODEC = build(
            Bool(DevEarlySyncPacket::permitted),
            Bool(DevEarlySyncPacket::silent),
            DevEarlySyncPacket::new
    );

    @Override
    public StoryCodec<DevEarlySyncPacket> getStoryCodec() {
        return CODEC;
    }

    public static void syncFor(ServerPlayerEntity player, boolean silent) {
        CogwheelHooks.sendPacket(new DevEarlySyncPacket(isDev(), silent), player);
    }

    public static boolean isDev() {
        return CogwheelConfig.isDevEnvironment();
    }

    @Override
    public void onServerUnsafe(IStoryPacketContext ctx) {
        syncFor(ctx.getSender(), this.silent);
    }

    @Override
    public void onClientUnsafe(IStoryPacketContext ctx) {
        DevUI.permitted = permitted;
        if (!permitted) {
            CogwheelExecutor.scheduleTickEventClientSide(levelTickEvent -> {
                if (MinecraftClient.getInstance().currentScreen instanceof DevUIScreen) {
                    MinecraftClient.getInstance().setScreen(null);
                    MinecraftClient.getInstance().getToastManager().add(new SystemToast(
                            SystemToast.Type.PERIODIC_NOTIFICATION, Text.translatable("ui.storyanvil_cogwheel.notif_block"), Text.translatable("ui.storyanvil_cogwheel.notif_block_msg")
                    ));
                }
            });
        }
        if (!silent) {
            MinecraftClient.getInstance().getToastManager().add(new SystemToast(
                    SystemToast.Type.PERIODIC_NOTIFICATION, Text.translatable("ui.storyanvil_cogwheel.notif_block"), Text.translatable("ui.storyanvil_cogwheel.notif_block_msg")
            ));
        }
    }
}
