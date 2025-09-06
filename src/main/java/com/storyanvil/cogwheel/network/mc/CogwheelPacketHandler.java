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

package com.storyanvil.cogwheel.network.mc;

import com.storyanvil.cogwheel.CogwheelEngine;
import com.storyanvil.cogwheel.network.devui.DevBoundRequest;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.concurrent.atomic.AtomicInteger;

public class CogwheelPacketHandler {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel DELTA_BRIDGE = NetworkRegistry.newSimpleChannel(
            ResourceLocation.fromNamespaceAndPath(CogwheelEngine.MODID, "delta-bridge"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static final AtomicInteger PACKET_ID = new AtomicInteger(0);
    public static void init() {
        DELTA_BRIDGE.registerMessage(PACKET_ID.incrementAndGet(), AnimationDataBound.class,
                AnimationDataBound::encode, AnimationDataBound::decode, AnimationDataBound::handle);
        DELTA_BRIDGE.registerMessage(PACKET_ID.incrementAndGet(), AnimationBound.class,
                AnimationBound::encode, AnimationBound::decode, AnimationBound::handle);
        DELTA_BRIDGE.registerMessage(PACKET_ID.incrementAndGet(), DialogChoiceBound.class,
                DialogChoiceBound::encode, DialogChoiceBound::decode, DialogChoiceBound::handle);
        DELTA_BRIDGE.registerMessage(PACKET_ID.incrementAndGet(), DialogResponseBound.class,
                DialogResponseBound::encode, DialogResponseBound::decode, DialogResponseBound::handle);
        DELTA_BRIDGE.registerMessage(PACKET_ID.incrementAndGet(), DialogBound.class,
                DialogBound::encode, DialogBound::decode, DialogBound::handle);
        DELTA_BRIDGE.registerMessage(PACKET_ID.incrementAndGet(), CameraForceBound.class,
                CameraForceBound::encode, CameraForceBound::decode, CameraForceBound::handle);
        DELTA_BRIDGE.registerMessage(PACKET_ID.incrementAndGet(), CameraTransitionBound.class,
                CameraTransitionBound::encode, CameraTransitionBound::decode, CameraTransitionBound::handle);
        DELTA_BRIDGE.registerMessage(PACKET_ID.incrementAndGet(), Notification.class,
                Notification::encode, Notification::decode, Notification::handle);
        DELTA_BRIDGE.registerMessage(PACKET_ID.incrementAndGet(), DevBoundRequest.class,
                DevBoundRequest::encode, DevBoundRequest::decode, DevBoundRequest::handle);
    }
}
