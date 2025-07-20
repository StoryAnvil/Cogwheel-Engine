package com.storyanvil.cogwheel;

import com.storyanvil.cogwheel.infrustructure.CogPropertyManager;
import com.storyanvil.cogwheel.infrustructure.cog.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.entity.living.LivingUseTotemEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Objects;

@Mod.EventBusSubscriber(modid = CogwheelEngine.MODID)
public class ScriptEventBus {
    @SubscribeEvent
    public static void blockPlaced(BlockEvent.EntityPlaceEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            HashMap<String, CogPropertyManager> storage = new HashMap<>();
            CogEventCallback callback = new CogEventCallback();
            storage.put("internal_callback", callback);
            storage.put("event_player", new CogPlayer(new WeakReference<>(player)));
            storage.put("event_x", new CogInteger(event.getPos().getX()));
            storage.put("event_y", new CogInteger(event.getPos().getY()));
            storage.put("event_z", new CogInteger(event.getPos().getZ()));
            storage.put("event_block", new CogString(
                    Objects.requireNonNull(ForgeRegistries.BLOCKS.getKey(event.getPlacedBlock().getBlock())).toString()
            ));
            EventType.dispatchEvent(EventType.BLOCK_PLACED, storage);

            if (callback.isCanceled()) event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void blockBroken(BlockEvent.BreakEvent event) {
        if (event.getPlayer() instanceof ServerPlayer player) {
            HashMap<String, CogPropertyManager> storage = new HashMap<>();
            CogEventCallback callback = new CogEventCallback();
            storage.put("internal_callback", callback);
            storage.put("event_player", new CogPlayer(new WeakReference<>(player)));
            storage.put("event_x", new CogInteger(event.getPos().getX()));
            storage.put("event_y", new CogInteger(event.getPos().getY()));
            storage.put("event_z", new CogInteger(event.getPos().getZ()));
            storage.put("event_block", new CogString(
                    Objects.requireNonNull(ForgeRegistries.BLOCKS.getKey(event.getState().getBlock())).toString()
            ));
            EventType.dispatchEvent(EventType.BLOCK_BROKEN, storage);

            if (callback.isCanceled()) event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void chatMessage(ServerChatEvent event) {
        HashMap<String, CogPropertyManager> storage = new HashMap<>();
        CogEventCallback callback = new CogEventCallback();
        storage.put("internal_callback", callback);
        storage.put("event_player", new CogPlayer(new WeakReference<>(event.getPlayer())));
        storage.put("event_message", new CogString(event.getRawText()));
        EventType.dispatchEvent(EventType.CHAT_MESSAGE, storage);

        if (callback.isCanceled()) event.setCanceled(true);
    }

    @SubscribeEvent
    public static void totemUsage(LivingUseTotemEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            HashMap<String, CogPropertyManager> storage = new HashMap<>();
            CogEventCallback callback = new CogEventCallback();
            storage.put("internal_callback", callback);
            storage.put("event_player", new CogPlayer(new WeakReference<>(player)));
            storage.put("event_hand", new CogString(event.getHandHolding().toString()));
            EventType.dispatchEvent(EventType.TOTEM_USAGE, storage);
            if (callback.isCanceled()) event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void blockRightClick(PlayerInteractEvent.RightClickBlock event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            HashMap<String, CogPropertyManager> storage = new HashMap<>();
            CogEventCallback callback = new CogEventCallback();
            storage.put("internal_callback", callback);
            storage.put("event_player", new CogPlayer(new WeakReference<>(player)));
            storage.put("event_x", new CogInteger(event.getPos().getX()));
            storage.put("event_y", new CogInteger(event.getPos().getY()));
            storage.put("event_z", new CogInteger(event.getPos().getZ()));
            EventType.dispatchEvent(EventType.BLOCK_RIGHT_CLICK, storage);
            if (callback.isCanceled()) event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void entityRightClick(PlayerInteractEvent.EntityInteract event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            HashMap<String, CogPropertyManager> storage = new HashMap<>();
            CogEventCallback callback = new CogEventCallback();
            storage.put("internal_callback", callback);
            storage.put("event_player", new CogPlayer(new WeakReference<>(player)));
            storage.put("event_x", new CogInteger(event.getPos().getX()));
            storage.put("event_y", new CogInteger(event.getPos().getY()));
            storage.put("event_z", new CogInteger(event.getPos().getZ()));
            storage.put("event_entity", new CogEntity(event.getTarget()));
            EventType.dispatchEvent(EventType.ENTITY_RIGHT_CLICK, storage);
            if (callback.isCanceled()) event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void playerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            HashMap<String, CogPropertyManager> storage = new HashMap<>();
            CogEventCallback callback = new CogEventCallback();
            storage.put("internal_callback", callback);
            storage.put("event_player", new CogPlayer(new WeakReference<>(player)));
            storage.put("event_endConquered", CogBool.getInstance(event.isEndConquered()));
            EventType.dispatchEvent(EventType.PLAYER_RESPAWN, storage);
            if (callback.isCanceled()) event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void entityAttacked(AttackEntityEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            HashMap<String, CogPropertyManager> storage = new HashMap<>();
            CogEventCallback callback = new CogEventCallback();
            storage.put("internal_callback", callback);
            storage.put("event_player", new CogPlayer(new WeakReference<>(player)));
            storage.put("event_entity", new CogEntity(event.getTarget()));
            EventType.dispatchEvent(EventType.ENTITY_ATTACKED, storage);
            if (callback.isCanceled()) event.setCanceled(true);
        }
    }
}
