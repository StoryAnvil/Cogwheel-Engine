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

import java.util.List;

public record DialogChoiceBound(boolean close, String request, String dialogId, List<String> options, String npcName, String texture) implements StoryPacket<DialogChoiceBound> {
    public static final StoryCodec<DialogChoiceBound> CODEC = StoryCodecBldr.build(
            StoryCodecBldr.Bool(DialogChoiceBound::close),
            StoryCodecBldr.String(DialogChoiceBound::request),
            StoryCodecBldr.String(DialogChoiceBound::dialogId),
            StoryCodecBldr.Prop(DialogChoiceBound::options, StoryCodecs.getListCodec(StoryCodecs.STRING)),
            StoryCodecBldr.String(DialogChoiceBound::npcName),
            StoryCodecBldr.String(DialogChoiceBound::texture),
            DialogChoiceBound::new
    );

    public DialogChoiceBound() {
        this(true, "", "", List.of(), "", "");
    }
    public DialogChoiceBound(String dialogId, String request, String[] options, String npcName, String texture) {
        this(false, request, dialogId, List.of(options), npcName, texture.replace(' ', '_'));
    }
    public DialogChoiceBound(String dialogId, String request, List<String> options, String npcName, String texture) {
        this(false, request, dialogId, options, npcName, texture.replace(' ', '_'));
    }

    @Override
    public StoryCodec<DialogChoiceBound> getStoryCodec() {
        return CODEC;
    }

    @Override
    public void onClientUnsafe(IStoryPacketContext ctx) {
        CogwheelClientPacketHandler.dialogChoiceBound(this, ctx);
    }
}
