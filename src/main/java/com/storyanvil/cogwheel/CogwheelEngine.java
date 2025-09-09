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
import com.storyanvil.cogwheel.config.CogwheelClientConfig;
import com.storyanvil.cogwheel.data.SyncArray;
import com.storyanvil.cogwheel.entity.NPCRenderer;
import com.storyanvil.cogwheel.infrastructure.cog.PropertyHandler;
import com.storyanvil.cogwheel.infrastructure.cog.early.CogEarlyRegistry;
import com.storyanvil.cogwheel.infrastructure.env.RegistryEnvironment;
import com.storyanvil.cogwheel.infrastructure.script.DispatchedScript;
import com.storyanvil.cogwheel.network.devui.DevNetwork;
import com.storyanvil.cogwheel.network.mc.CogwheelPacketHandler;
import com.storyanvil.cogwheel.registry.*;
import com.storyanvil.cogwheel.util.PropertyLocation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.progress.ProgressMeter;
import net.minecraftforge.fml.loading.progress.StartupNotificationManager;
import org.jetbrains.annotations.ApiStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;

@Mod(CogwheelEngine.MODID)
public class CogwheelEngine {
    @Api.Internal @ApiStatus.Internal
    public static final String MODID = "storyanvil_cogwheel";
    @Api.Internal @ApiStatus.Internal
    public static final Logger LOGGER = LoggerFactory.getLogger("STORYANVIL/COGWHEEL");
    public static final Logger EARLY = LoggerFactory.getLogger("STORYANVIL/COGWHEEL/EARLY-LOGGER");

    public static HashMap<String, PropertyHandler> EARLY_MANAGER = new HashMap<>();

    public CogwheelEngine(FMLJavaModLoadingContext context) throws InterruptedException {
        ProgressMeter meter = StartupNotificationManager.addProgressBar("COGWHEEL ENGINE - Initializing", 3);
        EARLY.info("Initializing");

        CogwheelClientConfig.reload();
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

        meter.label("COGWHEEL ENGINE - Early Scripts");
        EARLY.info("Loading early scripts");
        meter.increment();
        SyncArray<CogEarlyRegistry> registries = new SyncArray<>();
        RegistryEnvironment environment = new RegistryEnvironment(registries);
        File scripts = new File(Minecraft.getInstance().gameDirectory, "config/cog/early");
        File[] candidates = scripts.listFiles();
        if (candidates != null) {
            ProgressMeter meter2 = StartupNotificationManager.addProgressBar("COGWHEEL ENGINE - Early Script Dispatch", candidates.length);
            CountDownLatch latch = new CountDownLatch(candidates.length);
            for (File candidate : candidates) {
                String sss = candidate.toString();
                String ssss = candidate.getName();
                StartupNotificationManager.addModMessage("COGWHEEL ENGINE - Dispatching " + candidate);
                EARLY.info("Dispatching {}", candidate);
                try (FileReader fr = new FileReader(candidate); Scanner sc = new Scanner(fr)) {
                    ArrayList<String> lines = new ArrayList<>();
                    while (sc.hasNextLine()) {
                        lines.add(sc.nextLine().trim());
                    }
                    CogwheelExecutor.schedule(new DispatchedScript(lines, environment){
                        @Override
                        public void onEnd() {
                            super.onEnd();
                            StartupNotificationManager.addModMessage("COGWHEEL ENGINE - Dispatched " + sss);
                            EARLY.info("Finished execution {}", sss);
                            latch.countDown();
                            meter2.increment();
                        }
                    }.setScriptName("early-" + ssss)::lineDispatcher);
                } catch (IOException e) {
                    StartupNotificationManager.addModMessage("COGWHEEL ENGINE - FAILED Dispatching " + candidate);
                    EARLY.error("Script dispatch failed while file reading", e);
                }
            }
            latch.await();
            meter2.complete();
            StartupNotificationManager.addModMessage("COGWHEEL ENGINE - Scripts executed");
        } else StartupNotificationManager.addModMessage("COGWHEEL ENGINE - No early scripts found");
        StartupNotificationManager.addModMessage("COGWHEEL ENGINE - Disposed early env");
        meter.increment();
        meter.label("COGWHEEL ENGINE - Finishing registry");
        EARLY.info("Finishing registries");
        registries.freeze();
        for (CogEarlyRegistry registry : registries) {
            registry.register(modEventBus);
        }
        registries.dispose();
        environment.dispose();
        EARLY.info("Disposing property managers");
        EARLY_MANAGER.clear();
        EARLY_MANAGER = null;
        meter.complete();
        StartupNotificationManager.addModMessage("COGWHEEL ENGINE - Finished");
    }

    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent @Api.Internal @ApiStatus.Internal
        public static void onClientSetup(FMLClientSetupEvent event) {
            EntityRenderers.register(CogwheelEntities.NPC.get(), NPCRenderer::new);
        }
    }
}
