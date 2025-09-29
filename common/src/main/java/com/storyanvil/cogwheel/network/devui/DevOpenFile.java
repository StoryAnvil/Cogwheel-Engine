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
import com.storyanvil.cogwheel.network.mc.Notification;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.io.IOException;

import static com.storyanvil.cogwheel.data.StoryCodecBldr.*;

public record DevOpenFile(Identifier script) implements StoryPacket<DevOpenFile> {
    public static final StoryCodec<DevOpenFile> CODEC = build(
            Prop(DevOpenFile::script, StoryCodecs.RESOURCE_LOC),
            DevOpenFile::new
    );

    @Override
    public StoryCodec<DevOpenFile> getStoryCodec() {
        return CODEC;
    }

    @Override
    public void onServerUnsafe(IStoryPacketContext ctx) {
        DevEditorSession session = DevEditorSession.createOrGet(script());
        try {
            session.read();
            session.addConnection(ctx.getSender());
        } catch (IOException e) {
            CogwheelNetwork.NETWORK_LOG.error("Error while starting session", e);
            CogwheelHooks.sendPacket(new Notification(
                    Text.literal("File can't be opened!"), Text.literal("Unknown error on serverside!")
            ), ctx.getSender());
            session.dispose();
        }
    }

    @Override
    public void onClientUnsafe(IStoryPacketContext ctx) {
        DWCodeEditor editor = DWCodeEditor.getOrCreateEditor(script);
        CogwheelHooks.sendPacketToServer(new DevEditorState(script, (byte) -127));
    }
}
