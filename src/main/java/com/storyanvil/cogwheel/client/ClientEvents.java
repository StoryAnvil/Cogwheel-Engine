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

package com.storyanvil.cogwheel.client;

import com.storyanvil.cogwheel.CogwheelEngine;
import com.storyanvil.cogwheel.client.devui.DevUI;
import com.storyanvil.cogwheel.client.devui.DevUIScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = CogwheelEngine.MODID, value = Dist.CLIENT)
public class ClientEvents {
    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            while (DevUI.OPEN_DEVUI.get().consumeClick()) {
                if (DevUI.permitted) {
                    Minecraft.getInstance().setScreen(new DevUIScreen());
                } else {
                    Minecraft.getInstance().getToasts().addToast(new SystemToast(
                            SystemToast.SystemToastIds.PERIODIC_NOTIFICATION, Component.translatable("ui.storyanvil_cogwheel.notif_ban"), Component.translatable("ui.storyanvil_cogwheel.notif_ban_msg")
                    ));
                }
            }
        }
    }
}
