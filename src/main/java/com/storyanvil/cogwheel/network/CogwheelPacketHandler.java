package com.storyanvil.cogwheel.network;

import com.storyanvil.cogwheel.CogwheelEngine;
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
//        DELTA_BRIDGE.registerMessage(PACKET_ID.incrementAndGet(), GuiNode.class,
//                GuiNode::encode, GuiNode::decode, GuiNode::handle);
    }
}
