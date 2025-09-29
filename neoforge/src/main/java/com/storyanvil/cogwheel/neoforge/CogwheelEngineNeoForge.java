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

package com.storyanvil.cogwheel.neoforge;

import com.storyanvil.cogwheel.CogwheelHooks;
import com.storyanvil.cogwheel.entity.NPC;
import com.storyanvil.cogwheel.neoforge.client.NPCRenderer;
import com.storyanvil.cogwheel.registry.CogwheelRegistries;
import net.minecraft.client.render.entity.EntityRenderers;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;

import com.storyanvil.cogwheel.CogwheelEngine;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.storyanvil.cogwheel.CogwheelEngine.MODID;

@Mod(MODID)
public final class CogwheelEngineNeoForge {
    public static final Logger PLATFORM_LOG = LoggerFactory.getLogger("STORYANVIL/COGWHEEL/NEOFORGE");
    public CogwheelEngineNeoForge(IEventBus bus, @SuppressWarnings("unused") ModContainer container) {
        CogwheelEngine.init(); // Common setup
        NeoRegistry.ATTACHMENT_TYPES.register(bus);
        CogwheelHooks.registryRegistry(registry -> {
            NeoPlatformRegistry platform = (NeoPlatformRegistry) registry;
            if (platform.ITEMS != null)
                platform.ITEMS.register(bus);
            if (platform.ENTITY_TYPES != null)
                platform.ENTITY_TYPES.register(bus);
        });
    }

    @EventBusSubscriber(modid = MODID, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            EntityRenderers.register(CogwheelRegistries.NPC.get(), NPCRenderer::new);
        }

        @SubscribeEvent
        public static void registerBindings(RegisterKeyMappingsEvent event) {
            event.register(NeoRegistry.OPEN_DEVUI.get());
        }
    }

    @EventBusSubscriber(modid = MODID)
    public static class ModEvents {
        @SubscribeEvent
        public static void onEntityAttributeCreation(EntityAttributeCreationEvent event) {
            event.put(CogwheelRegistries.NPC.get(), NPC.createAttributes().build());
        }
    }
}
