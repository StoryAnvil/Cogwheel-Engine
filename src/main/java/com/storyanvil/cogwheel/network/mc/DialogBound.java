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
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class DialogBound {
    private boolean close = false;
    private String request = "";
    private String npcName = "";
    private String texture = "";

    public static DialogBound close() {
        DialogBound bound = new DialogBound();
        bound.close = true;
        return bound;
    }
    public static DialogBound tell(String request, String npcName, String texture) {
        DialogBound bound = new DialogBound();
        bound.request = request;
        bound.npcName = npcName;
        bound.texture = texture.toLowerCase().replace(' ', '_');
        return bound;
    }

    public boolean isClose() {
        return close;
    }

    public String getRequest() {
        return request;
    }

    public String getNpcName() {
        return npcName;
    }

    public String getTexture() {
        return texture;
    }

    public void encode(FriendlyByteBuf byteBuf) {
        byteBuf.writeBoolean(close);
        StoryUtils.encodeString(byteBuf, request);
        StoryUtils.encodeString(byteBuf, npcName);
        StoryUtils.encodeString(byteBuf, texture);
    }

    public static DialogBound decode(FriendlyByteBuf byteBuf) {
        DialogBound bound = new DialogBound();
        bound.close = byteBuf.readBoolean();
        bound.request = StoryUtils.decodeString(byteBuf);
        bound.npcName = StoryUtils.decodeString(byteBuf);
        bound.texture = StoryUtils.decodeString(byteBuf);
        return bound;
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        contextSupplier.get().enqueueWork(() -> {
            DistExecutor.unsafeCallWhenOn(Dist.CLIENT, () -> () -> CogwheelClientPacketHandler.dialogBound(this, contextSupplier));
        });
        contextSupplier.get().setPacketHandled(true);
    }

    @Override
    public String toString() {
        return "DialogBound{" +
                "close=" + close +
                ", request='" + request + '\'' +
                '}';
    }
}
