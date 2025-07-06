package com.storyanvil.cogwheel.registry;

import com.storyanvil.cogwheel.CogwheelEngine;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class CogwheelSounds {
    private static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, CogwheelEngine.MODID);

//    public static final RegistryObject<SoundEvent> LOBOTOMY_SOUND_EFFECT = register("lobotomy");

    public static DeferredRegister<SoundEvent> getSoundEvents() {
        return SOUND_EVENTS;
    }
    private static RegistryObject<SoundEvent> register(String name) {
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(CogwheelEngine.MODID, name)));
    }
    private static RegistryObject<SoundEvent> registerFixed(String name) {
        return SOUND_EVENTS.register(name, () -> SoundEvent.createFixedRangeEvent(ResourceLocation.fromNamespaceAndPath(CogwheelEngine.MODID, name), 15));
    }
}
