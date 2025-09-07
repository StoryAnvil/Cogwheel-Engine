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

import com.storyanvil.cogwheel.CogwheelExecutor;
import com.storyanvil.cogwheel.util.StoryUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class DialogResponseBound {
    private final String dialogId;
    private final int responseId;

    public DialogResponseBound(String dialogId, int responseId) {
        this.dialogId = dialogId;
        this.responseId = responseId;
    }

    public void encode(FriendlyByteBuf byteBuf) {
        StoryUtils.encodeString(byteBuf, dialogId);
        byteBuf.writeInt(responseId);
    }

    public static DialogResponseBound decode(FriendlyByteBuf byteBuf) {
        DialogResponseBound bound = new DialogResponseBound(StoryUtils.decodeString(byteBuf), byteBuf.readInt());
        return bound;
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        contextSupplier.get().enqueueWork(() -> {
            CogwheelExecutor.schedule(() -> {
                Consumer<Integer> call = CogwheelExecutor.getDefaultEnvironment().getDialogs().get(dialogId);
                CogwheelExecutor.getDefaultEnvironment().getDialogs().remove(dialogId);
                call.accept(responseId);
            });
        });
        contextSupplier.get().setPacketHandled(true);
    }

    @Override
    public String toString() {
        return "DialogResponseBound{" +
                "dialogId='" + dialogId + '\'' +
                ", responseId=" + responseId +
                '}';
    }
}
