package com.storyanvil.cogwheel;

import com.storyanvil.cogwheel.entity.NPC;
import com.storyanvil.cogwheel.registry.CogwheelEntities;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = CogwheelEngine.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModEventBus {
    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(CogwheelEntities.NPC.get(), NPC.createAttributes().build());
    }
}
