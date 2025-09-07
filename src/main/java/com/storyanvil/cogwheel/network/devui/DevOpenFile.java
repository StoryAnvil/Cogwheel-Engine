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

package com.storyanvil.cogwheel.network.devui;

import com.storyanvil.cogwheel.client.devui.DWCodeEditor;
import com.storyanvil.cogwheel.data.StoryCodec;
import com.storyanvil.cogwheel.data.StoryCodecs;
import com.storyanvil.cogwheel.data.StoryPacket;
import com.storyanvil.cogwheel.network.devui.editor.DevEditorSession;
import com.storyanvil.cogwheel.network.mc.CogwheelPacketHandler;
import com.storyanvil.cogwheel.network.mc.Notification;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.io.IOException;
import java.util.function.Supplier;

import static com.storyanvil.cogwheel.data.StoryCodecBuilder.*;

public record DevOpenFile(ResourceLocation script) implements StoryPacket {
    public static final StoryCodec<DevOpenFile> CODEC = build(
            Prop(DevOpenFile::script, StoryCodecs.RESOURCE_LOC),
            DevOpenFile::new
    );

    @Override
    public void onServerUnsafe(Supplier<NetworkEvent.Context> ctx) {
        DevEditorSession session = DevEditorSession.createOrGet(script());
        try {
            session.read();
            session.addConnection(ctx.get().getSender());
        } catch (IOException e) {
            DevNetwork.log.error("Error while starting session", e);
            CogwheelPacketHandler.DELTA_BRIDGE.send(PacketDistributor.PLAYER.with(ctx.get()::getSender), new Notification(
                    Component.literal("File can't be opened!"), Component.literal("Unknown error on serverside!")
            ));
            session.dispose();
        }
    }

    @Override
    public void onClientUnsafe(Supplier<NetworkEvent.Context> ctx) {
        DWCodeEditor editor = DWCodeEditor.getOrCreateEditor(script);
        DevNetwork.sendToServer(new DevEditorState(script, (byte) -127));
    }

    @Override
    public String toString() {
        return "DevOpenFile{" +
                "script=" + script +
                '}';
    }
}
