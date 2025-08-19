package com.storyanvil.cogwheel;

import com.storyanvil.cogwheel.api.Api;
import com.storyanvil.cogwheel.infrastructure.cog.*;
import com.storyanvil.cogwheel.infrastructure.env.CogScriptEnvironment;
import com.storyanvil.cogwheel.util.DataStorage;
import com.storyanvil.cogwheel.util.ScriptStorage;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;
import java.util.Objects;

@Mod.EventBusSubscriber(modid = CogwheelEngine.MODID) @Api.Internal @ApiStatus.Internal
public class ScriptEventBus {

    @SubscribeEvent @Api.Internal @ApiStatus.Internal
    public static void blockPlaced(BlockEvent.@NotNull EntityPlaceEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            ScriptStorage storage = new ScriptStorage();
            CogEventCallback callback = new CogEventCallback();
            storage.put("internal_callback", callback);
            storage.put("event_player", new CogPlayer(new WeakReference<>(player)));
            storage.put("event_x", new CogInteger(event.getPos().getX()));
            storage.put("event_y", new CogInteger(event.getPos().getY()));
            storage.put("event_z", new CogInteger(event.getPos().getZ()));
            storage.put("event_block", new CogString(
                    Objects.requireNonNull(ForgeRegistries.BLOCKS.getKey(event.getPlacedBlock().getBlock())).toString()
            ));
            CogScriptEnvironment.dispatchEventGlobal(getEventLocation("block/placed"), storage);

            if (callback.isCanceled()) event.setCanceled(true);
        }
    }

    @Contract("_ -> new") @Api.Internal @ApiStatus.Internal
    public static @NotNull ResourceLocation getEventLocation(String loc) {
        return ResourceLocation.fromNamespaceAndPath(CogwheelEngine.MODID, loc);
    }

    @SubscribeEvent @Api.Internal @ApiStatus.Internal
    public static void blockBroken(BlockEvent.@NotNull BreakEvent event) {
        if (event.getPlayer() instanceof ServerPlayer player) {
            ScriptStorage storage = new ScriptStorage();
            CogEventCallback callback = new CogEventCallback();
            storage.put("internal_callback", callback);
            storage.put("event_player", new CogPlayer(new WeakReference<>(player)));
            storage.put("event_x", new CogInteger(event.getPos().getX()));
            storage.put("event_y", new CogInteger(event.getPos().getY()));
            storage.put("event_z", new CogInteger(event.getPos().getZ()));
            storage.put("event_block", new CogString(
                    Objects.requireNonNull(ForgeRegistries.BLOCKS.getKey(event.getState().getBlock())).toString()
            ));
            CogScriptEnvironment.dispatchEventGlobal(getEventLocation("block/broken"), storage);

            if (callback.isCanceled()) event.setCanceled(true);
        }
    }

    @SubscribeEvent @Api.Internal @ApiStatus.Internal
    public static void chatMessage(@NotNull ServerChatEvent event) {
        ScriptStorage storage = new ScriptStorage();
        CogEventCallback callback = new CogEventCallback();
        storage.put("internal_callback", callback);
        storage.put("event_player", new CogPlayer(new WeakReference<>(event.getPlayer())));
        storage.put("event_message", new CogString(event.getRawText()));
        CogScriptEnvironment.dispatchEventGlobal(getEventLocation("chat_msg"), storage);

        if (callback.isCanceled()) event.setCanceled(true);
    }

    @SubscribeEvent @Api.Internal @ApiStatus.Internal
    public static void blockRightClick(PlayerInteractEvent.@NotNull RightClickBlock event) {
        if (event.getSide() == LogicalSide.CLIENT) return;
        if (event.getEntity() instanceof ServerPlayer player) {
            long ct = System.currentTimeMillis();
            long li = Long.parseLong(DataStorage.getString(player, "li", "0"));
            if (ct - li <= 1000) {
                return;
            }
            DataStorage.setString(player, "li", String.valueOf(ct));
            ScriptStorage storage = new ScriptStorage();
            CogEventCallback callback = new CogEventCallback();
            storage.put("internal_callback", callback);
            storage.put("event_player", new CogPlayer(new WeakReference<>(player)));
            storage.put("event_x", new CogInteger(event.getPos().getX()));
            storage.put("event_y", new CogInteger(event.getPos().getY()));
            storage.put("event_z", new CogInteger(event.getPos().getZ()));
            CogScriptEnvironment.dispatchEventGlobal(getEventLocation("block/use"), storage);
            if (callback.isCanceled()) event.setCanceled(true);
        }
    }

    @SubscribeEvent @Api.Internal @ApiStatus.Internal
    public static void entityRightClick(PlayerInteractEvent.@NotNull EntityInteract event) {
        if (event.getSide() == LogicalSide.CLIENT) return;
        if (event.getEntity() instanceof ServerPlayer player) {
            ScriptStorage storage = new ScriptStorage();
            CogEventCallback callback = new CogEventCallback();
            storage.put("internal_callback", callback);
            storage.put("event_player", new CogPlayer(new WeakReference<>(player)));
            storage.put("event_x", new CogInteger(event.getPos().getX()));
            storage.put("event_y", new CogInteger(event.getPos().getY()));
            storage.put("event_z", new CogInteger(event.getPos().getZ()));
            storage.put("event_entity", new CogEntity(event.getTarget()));
            CogScriptEnvironment.dispatchEventGlobal(getEventLocation("entity/use"), storage);
            if (callback.isCanceled()) event.setCanceled(true);
        }
    }

    @SubscribeEvent @Api.Internal @ApiStatus.Internal
    public static void playerRespawn(PlayerEvent.@NotNull PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            ScriptStorage storage = new ScriptStorage();
            CogEventCallback callback = new CogEventCallback();
            storage.put("internal_callback", callback);
            storage.put("event_player", new CogPlayer(new WeakReference<>(player)));
            storage.put("event_endConquered", CogBool.getInstance(event.isEndConquered()));
            CogScriptEnvironment.dispatchEventGlobal(getEventLocation("player/respawn"), storage);
            if (callback.isCanceled()) event.setCanceled(true);
        }
    }

    @SubscribeEvent @Api.Internal @ApiStatus.Internal
    public static void entityAttacked(@NotNull AttackEntityEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            ScriptStorage storage = new ScriptStorage();
            CogEventCallback callback = new CogEventCallback();
            storage.put("internal_callback", callback);
            storage.put("event_player", new CogPlayer(new WeakReference<>(player)));
            storage.put("event_entity", new CogEntity(event.getTarget()));
            CogScriptEnvironment.dispatchEventGlobal(getEventLocation("entity/attack"), storage);
            if (callback.isCanceled()) event.setCanceled(true);
        }
    }
}
