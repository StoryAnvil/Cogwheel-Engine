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
import com.storyanvil.cogwheel.data.IStoryPacketContext;
import com.storyanvil.cogwheel.data.StoryCodec;
import com.storyanvil.cogwheel.data.StoryCodecBldr;
import com.storyanvil.cogwheel.data.StoryPacket;
import com.storyanvil.cogwheel.network.devui.editor.DevEditorSession;
import com.storyanvil.cogwheel.network.mc.Notification;
import net.minecraft.text.Text;


public record DevResyncRequest() implements StoryPacket<DevResyncRequest> {
    public static final StoryCodec<DevResyncRequest> CODEC = StoryCodecBldr.empty(
            DevResyncRequest::new
    );

    @Override
    public StoryCodec<DevResyncRequest> getStoryCodec() {
        return CODEC;
    }

    @Override
    public void onServerUnsafe(IStoryPacketContext ctx) {
        for (DevEditorSession session : DevEditorSession.getSessions()) {
            session.resync(ctx.getSender());
        }
        CogwheelHooks.sendPacket(new Notification(
                Text.translatable("ui.storyanvil_cogwheel.notif_fsync"), Text.translatable("ui.storyanvil_cogwheel.notif_fsync_msg")
        ), ctx.getSender());
    }
}
