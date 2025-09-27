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
import com.storyanvil.cogwheel.data.StoryPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CogwheelNetwork {
    public static final Logger NETWORK_LOG = LoggerFactory.getLogger("STORYANVIL/COGWHEEL/NETWORK");

    @Deprecated
    public static void sendToServer(StoryPacket<?> msg) {
        if (msg == null) return;
        CogwheelHooks.sendPacketToServer(msg);
    }
    @Deprecated
    public static void sendFromServer(ServerPlayerEntity plr, StoryPacket<?> msg) {
        if (msg == null) return;
        CogwheelHooks.sendPacket(msg, plr);
    }
}
