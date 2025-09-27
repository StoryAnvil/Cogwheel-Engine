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

public record DevRunAndFlush(Identifier lc) implements StoryPacket<DevRunAndFlush> {
    public static final StoryCodec<DevRunAndFlush> CODEC = StoryCodecBldr.build(
            StoryCodecBldr.Prop(DevRunAndFlush::lc, StoryCodecs.RESOURCE_LOC),
            DevRunAndFlush::new
    );

    @Override
    public StoryCodec<DevRunAndFlush> getStoryCodec() {
        return CODEC;
    }

    @Override
    public void onServerUnsafe(IStoryPacketContext ctx) {
        DevEditorSession session = DevEditorSession.get(lc);
        if (session == null) {
            error(ctx, "Invalid session!");
            return;
        }
        session.flushAndDispatch(ctx.getSender());
    }
}
