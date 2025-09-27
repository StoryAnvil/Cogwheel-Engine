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

import com.storyanvil.cogwheel.CogwheelExecutor;
import com.storyanvil.cogwheel.data.IStoryPacketContext;
import com.storyanvil.cogwheel.data.StoryCodec;
import com.storyanvil.cogwheel.data.StoryCodecBldr;
import com.storyanvil.cogwheel.data.StoryPacket;

import java.util.function.Consumer;

public record DialogResponseBound(String dialogId, int responseId) implements StoryPacket<DialogResponseBound> {
    public static final StoryCodec<DialogResponseBound> CODEC = StoryCodecBldr.build(
            StoryCodecBldr.String(DialogResponseBound::dialogId),
            StoryCodecBldr.Integer(DialogResponseBound::responseId),
            DialogResponseBound::new
    );

    @Override
    public StoryCodec<DialogResponseBound> getStoryCodec() {
        return CODEC;
    }

    @Override
    public void onServerUnsafe(IStoryPacketContext ctx) {
        CogwheelExecutor.schedule(() -> {
            Consumer<Integer> call = CogwheelExecutor.getDefaultEnvironment().getDialogs().get(dialogId);
            CogwheelExecutor.getDefaultEnvironment().getDialogs().remove(dialogId);
            call.accept(responseId);
        });
    }
}
