package com.storyanvil.cogwheel.mixin;

import com.storyanvil.cogwheel.infrastructure.cog.CogEventCallback;
import com.storyanvil.cogwheel.infrastructure.cog.CogPlayer;
import com.storyanvil.cogwheel.infrastructure.cog.CogString;
import com.storyanvil.cogwheel.infrastructure.env.CogScriptEnvironment;
import com.storyanvil.cogwheel.util.ScriptStorage;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.lang.ref.WeakReference;

import static com.storyanvil.cogwheel.ScriptEventBus.getEventLocation;

@Mixin(Player.class)
public abstract class PlayerMixin extends LivingEntity {
    protected PlayerMixin(EntityType<? extends LivingEntity> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Inject(at = @At(value = "TAIL", shift = At.Shift.BEFORE), locals = LocalCapture.CAPTURE_FAILHARD,
            method = "Lnet/minecraft/world/entity/player/Player;eat")
    public void eat(@NotNull Level pLevel, ItemStack pFood, CallbackInfoReturnable<?> callbackInfo) {
        if (!pLevel.isClientSide()) {
            LivingEntity entity = this;

            // IDE will report this as false positive for constantValue. PlayerMixin will transform into Player class at runtime
            //noinspection ConstantValue
            if (entity instanceof ServerPlayer player) {
                ScriptStorage storage = new ScriptStorage();
                CogEventCallback callback = new CogEventCallback();
                storage.put("internal_callback", callback);
                storage.put("event_player", new CogPlayer(new WeakReference<>(player)));
                storage.put("food_item", new CogString(
                        ForgeRegistries.ITEMS.getKey(pFood.getItem()).toString()
                ));
                CogScriptEnvironment.dispatchEventGlobal(getEventLocation("player/eat"), storage);
            }
        }
    }
}
