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

package com.storyanvil.cogwheel.network.mc;

import com.storyanvil.cogwheel.util.StoryUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class Notification {
    public Notification(Component title, Component text) {
        this.title = title;
        this.text = text;
    }

    private Component title = null;
    private Component text = null;

    public Component getTitle() {
        return title;
    }

    public void setTitle(Component title) {
        this.title = title;
    }

    public Component getText() {
        return text;
    }

    public void setText(Component text) {
        this.text = text;
    }

    public void encode(FriendlyByteBuf byteBuf) {
        StoryUtils.encodeString(byteBuf, Component.Serializer.toStableJson(title));
        StoryUtils.encodeString(byteBuf, Component.Serializer.toStableJson(text));
    }

    public static Notification decode(FriendlyByteBuf byteBuf) {
        return new Notification(
                Component.Serializer.fromJson(StoryUtils.decodeString(byteBuf)),
                Component.Serializer.fromJson(StoryUtils.decodeString(byteBuf))
        );
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        contextSupplier.get().enqueueWork(() -> {
            DistExecutor.unsafeCallWhenOn(Dist.CLIENT, () -> () -> CogwheelClientPacketHandler.notification(this, contextSupplier));
        });
        contextSupplier.get().setPacketHandled(true);
    }
}
