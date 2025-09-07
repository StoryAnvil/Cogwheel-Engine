/*
 * StoryAnvil CogWheel Engine
 * Copyright (C) 2025 StoryAnvil
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.storyanvil.cogwheel;

import com.storyanvil.cogwheel.api.Api;
import com.storyanvil.cogwheel.entity.NPCRenderer;
import com.storyanvil.cogwheel.network.devui.DevNetwork;
import com.storyanvil.cogwheel.network.mc.CogwheelPacketHandler;
import com.storyanvil.cogwheel.registry.*;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.jetbrains.annotations.ApiStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(CogwheelEngine.MODID)
public class CogwheelEngine
{
    @Api.Internal @ApiStatus.Internal
    public static final String MODID = "storyanvil_cogwheel";
    @Api.Internal @ApiStatus.Internal
    public static final Logger LOGGER = LoggerFactory.getLogger("STORYANVIL/COGWHEEL");

    public CogwheelEngine(FMLJavaModLoadingContext context)
    {
        CogwheelExecutor.init();
        IEventBus modEventBus = context.getModEventBus();
        CogwheelBlocks.getBLOCKS().register(modEventBus);
        CogwheelItems.getITEMS().register(modEventBus);
        CogwheelSounds.getSoundEvents().register(modEventBus);
        CogwheelEntities.ENTITY_TYPES.register(modEventBus);
        CogwheelUI.MENU.register(modEventBus);
        CogwheelPacketHandler.init();
        DevNetwork.init();
        CogwheelRegistries.registerDefaultObjects();
    }

    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents
    {
        @SubscribeEvent @Api.Internal @ApiStatus.Internal
        public static void onClientSetup(FMLClientSetupEvent event) {
            EntityRenderers.register(CogwheelEntities.NPC.get(), NPCRenderer::new);
        }
    }
}
