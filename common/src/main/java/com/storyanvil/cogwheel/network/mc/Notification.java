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

package com.storyanvil.cogwheel.network.mc;

import com.storyanvil.cogwheel.data.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.text.Text;

public record Notification(Text title, Text text) implements StoryPacket<Notification> {
    public static final StoryCodec<Notification> CODEC = StoryCodecBldr.build(
            StoryCodecBldr.Prop(Notification::title, StoryCodecs.TEXT),
            StoryCodecBldr.Prop(Notification::text, StoryCodecs.TEXT),
            Notification::new
    );

    @Override
    public void onClientUnsafe(IStoryPacketContext ctx) {
        MinecraftClient.getInstance().getToastManager().add(new SystemToast(
                SystemToast.Type.PERIODIC_NOTIFICATION, title, text
        ));
    }

    @Override
    public StoryCodec<Notification> getStoryCodec() {
        return CODEC;
    }
}
