package com.storyanvil.cogwheel;

import com.storyanvil.cogwheel.entity.NPCRenderer;
import com.storyanvil.cogwheel.infrustructure.StoryAction;
import com.storyanvil.cogwheel.network.CogwheelPacketHandler;
import com.storyanvil.cogwheel.registry.*;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.storyanvil.cogwheel.util.TagUtils.*;

@Mod(CogwheelEngine.MODID)
public class CogwheelEngine
{
    public static final String MODID = "storyanvil_cogwheel";
    public static final Logger LOGGER = LoggerFactory.getLogger("STORYANVIL/COGWHEEL");

    public CogwheelEngine(FMLJavaModLoadingContext context)
    {
        CogwheelExecutor.init();
        IEventBus modEventBus = context.getModEventBus();
        CogwheelBlocks.getBLOCKS().register(modEventBus);
        CogwheelItems.getITEMS().register(modEventBus);
        CogwheelSounds.getSoundEvents().register(modEventBus);
        CogwheelEntities.ENTITY_TYPES.register(modEventBus);
        CogwheelPacketHandler.init();
        CogwheelRegistries.registerDefaultObjects();

        StoryAction.registerAction("chat", tag ->
                new StoryAction.Chat(Component.literal(S(tag, 1))));
        StoryAction.registerAction("pathfind", tag ->
                new StoryAction.PathfindTo(new BlockPos(
                        I(tag, 1), I(tag, 2), I(tag, 3)
                )));
        StoryAction.registerAction("teleport", tag ->
                new StoryAction.TeleportTo(new BlockPos(
                        I(tag, 1), I(tag, 2), I(tag, 3)
                )));
        StoryAction.registerAction("waitFor", tag ->
                new StoryAction.WaitForLabelNPC(S(tag, 1)));
        StoryAction.registerAction("waitForCount", tag ->
                new StoryAction.WaitForLabelNPC(S(tag, 1), I(tag, 2)));
        StoryAction.registerAction("skin", tag ->
                new StoryAction.SetData("skin", S(tag, 1)));
        StoryAction.registerAction("name", tag ->
                new StoryAction.SetData("name", S(tag, 1)));
    }

    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents
    {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event)
        {
            EntityRenderers.register(CogwheelEntities.NPC.get(), NPCRenderer::new);
        }
    }
}
