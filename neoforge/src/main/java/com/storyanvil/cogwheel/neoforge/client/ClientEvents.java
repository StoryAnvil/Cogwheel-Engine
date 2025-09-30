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

package com.storyanvil.cogwheel.neoforge.client;

import com.storyanvil.cogwheel.CogwheelEngine;
import com.storyanvil.cogwheel.client.devui.DevUI;
import com.storyanvil.cogwheel.client.devui.DevUIScreen;
import com.storyanvil.cogwheel.neoforge.NeoRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.text.Text;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@EventBusSubscriber(value = Dist.CLIENT, modid = CogwheelEngine.MODID)
public class ClientEvents {
    @SubscribeEvent
    public static void tick(ClientTickEvent.Post event) {
        while (NeoRegistry.OPEN_DEVUI.get().wasPressed()) {
            if (DevUI.permitted) {
                MinecraftClient.getInstance().setScreen(new DevUIScreen());
            } else {
                MinecraftClient.getInstance().getToastManager().add(new SystemToast(
                        SystemToast.Type.PERIODIC_NOTIFICATION, Text.translatable("ui.storyanvil_cogwheel.notif_ban"), Text.translatable("ui.storyanvil_cogwheel.notif_ban_msg")
                ));
            }
        }
    }
}
