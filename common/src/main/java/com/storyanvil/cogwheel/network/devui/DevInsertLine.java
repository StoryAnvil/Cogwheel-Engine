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

import com.storyanvil.cogwheel.client.devui.DWCodeEditor;
import com.storyanvil.cogwheel.data.*;
import net.minecraft.util.Identifier;

public record DevInsertLine(Identifier lc, int lineBefore, String contents) implements StoryPacket<DevInsertLine> {
    public static final StoryCodec<DevInsertLine> CODEC = StoryCodecBldr.build(
            StoryCodecBldr.Prop(DevInsertLine::lc, StoryCodecs.RESOURCE_LOC),
            StoryCodecBldr.Integer(DevInsertLine::lineBefore),
            StoryCodecBldr.String(DevInsertLine::contents),
            DevInsertLine::new
    );

    @Override
    public StoryCodec<DevInsertLine> getStoryCodec() {
        return CODEC;
    }

    @Override
    public void onClientUnsafe(IStoryPacketContext ctx) {
        DWCodeEditor editor = DWCodeEditor.get(lc);
        if (editor == null) {
            CogwheelNetwork.sendToServer(new DevEditorState(lc, (byte)-128));
            return;
        }
        editor.handle(this);
    }
}
