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

import com.storyanvil.cogwheel.util.CogwheelExecutor;
import com.storyanvil.cogwheel.data.IStoryPacketContext;
import com.storyanvil.cogwheel.data.StoryCodec;
import com.storyanvil.cogwheel.data.StoryPacket;

import static com.storyanvil.cogwheel.data.StoryCodecBldr.*;

public record DevConsoleCode(String code) implements StoryPacket<DevConsoleCode> {
    public static final StoryCodec<DevConsoleCode> CODEC = build(
            String(DevConsoleCode::code),
            DevConsoleCode::new
    );

    @Override
    public StoryCodec<DevConsoleCode> getStoryCodec() {
        return CODEC;
    }

    @Override
    public void onServerUnsafe(IStoryPacketContext ctx) {
        if (!DevEarlySyncPacket.isDev()) {
            error(ctx, "DevUI Disabled!");
            return;
        }
        CogwheelExecutor.getChatConsole().addLineRedirecting(code);
    }
}
