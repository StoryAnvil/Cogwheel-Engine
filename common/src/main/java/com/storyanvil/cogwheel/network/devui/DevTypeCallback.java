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

import com.storyanvil.cogwheel.data.*;
import com.storyanvil.cogwheel.network.devui.editor.DevEditorSession;
import net.minecraft.util.Identifier;

public record DevTypeCallback(Identifier lc, String typed, DevEditorUserDelta delta) implements StoryPacket<DevTypeCallback> {
    public static final StoryCodec<DevTypeCallback> CODEC = StoryCodecBldr.build(
            StoryCodecBldr.Prop(DevTypeCallback::lc, StoryCodecs.RESOURCE_LOC),
            StoryCodecBldr.String(DevTypeCallback::typed),
            StoryCodecBldr.Prop(DevTypeCallback::delta, DevEditorUserDelta.CODEC),
            DevTypeCallback::new
    );

    @Override
    public StoryCodec<DevTypeCallback> getStoryCodec() {
        return CODEC;
    }

    @Override
    public void onServerUnsafe(IStoryPacketContext ctx) {
        DevEditorSession session = DevEditorSession.get(lc);
        if (session == null) {
            error(ctx, "Invalid session!");
            return;
        }
        session.typeCallback(ctx.getSender(), this);
    }
}
