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

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

public class DialogChoiceBound {
    private boolean close = false;
    private String request = "";
    private String dialogId = "";
    private String[] options = new String[0];
    private String npcName = "";
    private String texture = "";

    public static DialogChoiceBound close() {
        DialogChoiceBound bound = new DialogChoiceBound();
        bound.close = true;
        bound.dialogId = "";
        return bound;
    }
    public static DialogChoiceBound choice(String dialogId, String request, String[] options, String npcName, String texture) {
        DialogChoiceBound bound = new DialogChoiceBound();
        bound.request = request;
        bound.options = options;
        bound.dialogId = dialogId;
        bound.npcName = npcName;
        bound.texture = texture.toLowerCase().replace(' ', '_');
        return bound;
    }
    public static DialogChoiceBound choice(String dialogId, String request, List<String> options, String npcName, String texture) {
        String[] o = new String[options.size()];
        for (int i = 0; i < options.size(); i++) {
            o[i] = options.get(i);
        }
        return choice(dialogId, request, o, npcName, texture);
    }

    public boolean isClose() {
        return close;
    }

    public String getRequest() {
        return request;
    }

    public String[] getOptions() {
        return options;
    }

    public String getDialogId() {
        return dialogId;
    }

    public String getNpcName() {
        return npcName;
    }

    public String getTexture() {
        return texture;
    }

    public void encode(FriendlyByteBuf byteBuf) {
        byteBuf.writeBoolean(close);
        StoryUtils.encodeString(byteBuf, dialogId);
        StoryUtils.encodeString(byteBuf, request);
        StoryUtils.encodeString(byteBuf, npcName);
        StoryUtils.encodeString(byteBuf, texture);
        byteBuf.writeInt(options.length);
        for (int i = 0; i < options.length; i++) {
            StoryUtils.encodeString(byteBuf, options[i]);
        }
    }

    public static DialogChoiceBound decode(FriendlyByteBuf byteBuf) {
        DialogChoiceBound bound = new DialogChoiceBound();
        bound.close = byteBuf.readBoolean();
        bound.dialogId = StoryUtils.decodeString(byteBuf);
        bound.request = StoryUtils.decodeString(byteBuf);
        bound.npcName = StoryUtils.decodeString(byteBuf);
        bound.texture = StoryUtils.decodeString(byteBuf);
        int optionsLength = byteBuf.readInt();
        String[] options = new String[optionsLength];
        for (int i = 0; i < optionsLength; i++) {
            options[i] = StoryUtils.decodeString(byteBuf);
        }
        bound.options = options;
        return bound;
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        contextSupplier.get().enqueueWork(() -> {
            DistExecutor.unsafeCallWhenOn(Dist.CLIENT, () -> () -> CogwheelClientPacketHandler.dialogChoiceBound(this, contextSupplier));
        });
        contextSupplier.get().setPacketHandled(true);
    }

    @Override
    public String toString() {
        return "DialogBound{" +
                "close=" + close +
                ", request='" + request + '\'' +
                ", dialogId='" + dialogId + '\'' +
                ", options=" + Arrays.toString(options) +
                '}';
    }
}
