package com.storyanvil.cogwheel.mixin;

import com.storyanvil.cogwheel.infrustructure.CogPropertyManager;
import com.storyanvil.cogwheel.infrustructure.cog.CogEventCallback;
import com.storyanvil.cogwheel.infrustructure.cog.CogPlayer;
import com.storyanvil.cogwheel.infrustructure.cog.CogString;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.lang.ref.WeakReference;
import java.util.HashMap;

@Mixin(Player.class)
public abstract class PlayerMixin extends LivingEntity {
    protected PlayerMixin(EntityType<? extends LivingEntity> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Inject(at = @At(value = "TAIL", shift = At.Shift.BEFORE), locals = LocalCapture.CAPTURE_FAILHARD,
            method = "Lnet/minecraft/world/entity/player/Player;eat")
    public void eat(Level pLevel, ItemStack pFood, CallbackInfoReturnable<?> callbackInfo) {
        if (!pLevel.isClientSide()) {
            LivingEntity entity = this;

            // IDE will report this as false positive for constantValue. PlayerMixin will transform into Player class at runtime
            //noinspection ConstantValue
            if (entity instanceof ServerPlayer player) {
                HashMap<String, CogPropertyManager> storage = new HashMap<>();
                CogEventCallback callback = new CogEventCallback();
                storage.put("internal_callback", callback);
                storage.put("event_player", new CogPlayer(new WeakReference<>(player)));
                storage.put("food_item", new CogString(
                        ForgeRegistries.ITEMS.getKey(pFood.getItem()).toString()
                ));
                EventType.dispatchEvent(EventType.PLAYER_ATE, storage);
            }
        }
    }
}
