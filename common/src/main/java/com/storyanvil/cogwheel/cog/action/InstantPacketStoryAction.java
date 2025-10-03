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

package com.storyanvil.cogwheel.cog.action;

import com.storyanvil.cogwheel.CogwheelHooks;
import com.storyanvil.cogwheel.data.StoryPacket;
import com.storyanvil.cogwheel.infrastructure.storyact.InstantStoryActionBase;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Collection;

public class InstantPacketStoryAction extends InstantStoryActionBase<Object> {
    private final StoryPacket<?> packet;
    private final Collection<ServerPlayerEntity> players;

    public InstantPacketStoryAction(StoryPacket<?> packet, Collection<ServerPlayerEntity> players) {
        this.packet = packet;
        this.players = players;
    }
    public InstantPacketStoryAction(StoryPacket<?> packet) {
        this.packet = packet;
        this.players = null;
    }

    @Override
    public void proceed(Object myself) {
        if (players == null) {
            CogwheelHooks.sendPacketToEveryone(packet);
        } else {
            for (ServerPlayerEntity e : players) {
                CogwheelHooks.sendPacket(e, packet);
            }
        }
    }
}
