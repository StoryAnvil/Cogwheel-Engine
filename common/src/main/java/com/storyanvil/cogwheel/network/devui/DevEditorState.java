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
import com.storyanvil.cogwheel.network.devui.editor.DevEditorSession;
import net.minecraft.util.Identifier;

public record DevEditorState(Identifier lc, Byte state) implements StoryPacket<DevEditorState> {
    public static final StoryCodec<DevEditorState> CODEC = StoryCodecBldr.build(
            StoryCodecBldr.Prop(DevEditorState::lc, StoryCodecs.RESOURCE_LOC),
            StoryCodecBldr.Byte(DevEditorState::state),
            DevEditorState::new
    );

    @Override
    public StoryCodec<DevEditorState> getStoryCodec() {
        return CODEC;
    }

    @Override
    public void onServerUnsafe(IStoryPacketContext ctx) {
        DevEditorSession session = DevEditorSession.get(lc);
        if (session == null) {
            error(ctx, "Invalid session!");
            return;
        }
        if (state == -128) {
            session.closeConnection(ctx.getSender());
        } else if (state == -127) {
            session.resync(ctx.getSender());
        }
    }

    @Override
    public void onClientUnsafe(IStoryPacketContext ctx) {
        if (state == -128) {
            DWCodeEditor editor = DWCodeEditor.get(lc);
            if (editor == null) return;
            DWCodeEditor.delete(lc);
            clientError(ctx, "Server closed your edition session!");
        }
    }
}
