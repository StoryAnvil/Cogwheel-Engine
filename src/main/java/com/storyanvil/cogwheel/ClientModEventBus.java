package com.storyanvil.cogwheel;

import com.storyanvil.cogwheel.entity.NPCModel;
import com.storyanvil.cogwheel.entity.NPCRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = CogwheelEngine.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientModEventBus {
    @SubscribeEvent
    public static void registerLayer(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(NPCRenderer.NPC_LAYER, NPCModel::createBodyLayer);
    }
}
