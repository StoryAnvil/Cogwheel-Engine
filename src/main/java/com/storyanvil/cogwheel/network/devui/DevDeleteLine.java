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
import com.storyanvil.cogwheel.data.StoryCodecBuilder;
import com.storyanvil.cogwheel.data.StoryCodecs;
import com.storyanvil.cogwheel.data.StoryPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record DevDeleteLine(ResourceLocation lc, int line) implements StoryPacket {
    public static final StoryCodec<DevDeleteLine> CODEC = StoryCodecBuilder.build(
            StoryCodecBuilder.Prop(DevDeleteLine::lc, StoryCodecs.RESOURCE_LOC),
            StoryCodecBuilder.Integer(DevDeleteLine::line),
            DevDeleteLine::new
    );

    @Override
    public void onClientUnsafe(Supplier<NetworkEvent.Context> ctx) {
        DWCodeEditor editor = DWCodeEditor.get(lc);
        if (editor == null) {
            DevNetwork.sendToServer(new DevEditorState(lc, (byte)-128));
            return;
        }
        editor.handle(this);
    }
}
