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

import com.storyanvil.cogwheel.CogwheelExecutor;
import com.storyanvil.cogwheel.data.StoryCodec;
import com.storyanvil.cogwheel.data.StoryPacket;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

import static com.storyanvil.cogwheel.data.StoryCodecBuilder.*;

public record DevConsoleCode(String code) implements StoryPacket {
    public static final StoryCodec<DevConsoleCode> CODEC = build(
            String(DevConsoleCode::code),
            DevConsoleCode::new
    );

    @Override
    public void onServerUnsafe(Supplier<NetworkEvent.Context> ctx) {
        if (!DevEarlySyncPacket.isDev()) {
            error(ctx.get().getSender(), "DevUI Disabled!");
            return;
        }
        CogwheelExecutor.getChatConsole().addLineRedirecting(code);
    }

    @Override
    public String toString() {
        return "DevConsoleCode{" +
                "code='" + code + '\'' +
                '}';
    }
}
