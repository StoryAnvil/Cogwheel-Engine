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

import com.storyanvil.cogwheel.CogwheelHooks;
import com.storyanvil.cogwheel.client.devui.DWCodeEditor;
import com.storyanvil.cogwheel.data.IStoryPacketContext;
import com.storyanvil.cogwheel.data.StoryCodec;
import com.storyanvil.cogwheel.data.StoryCodecs;
import com.storyanvil.cogwheel.data.StoryPacket;
import com.storyanvil.cogwheel.network.devui.editor.DevEditorSession;
import net.minecraft.util.Identifier;

import static com.storyanvil.cogwheel.data.StoryCodecBldr.*;

public record DevEditorUserDelta(Identifier lc, int line, int pos, int selected, String name, int color) implements StoryPacket<DevEditorUserDelta> {
    public static final StoryCodec<DevEditorUserDelta> CODEC = build(
            Prop(DevEditorUserDelta::lc, StoryCodecs.RESOURCE_LOC),
            Integer(DevEditorUserDelta::line),
            Integer(DevEditorUserDelta::pos),
            Integer(DevEditorUserDelta::selected),
            String(DevEditorUserDelta::name),
            Integer(DevEditorUserDelta::color),
            DevEditorUserDelta::new
    );

    @Override
    public StoryCodec<DevEditorUserDelta> getStoryCodec() {
        return CODEC;
    }

    public void onClientUnsafe(IStoryPacketContext ctx) {
        DWCodeEditor editor = DWCodeEditor.get(lc);
        if (editor == null) {
            CogwheelHooks.sendPacketToServer(new DevEditorState(lc, (byte)-128));
            return;
        }
        editor.handle(this);
    }

    public void onServerUnsafe(IStoryPacketContext ctx) {
        DevEditorSession session = DevEditorSession.get(lc);
        if (session == null) {
            error(ctx, "Invalid session!");
            return;
        }
        session.updateConnection(ctx.getSender(), this);
    }
}
