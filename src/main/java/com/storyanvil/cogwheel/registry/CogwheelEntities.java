package com.storyanvil.cogwheel.registry;

import com.storyanvil.cogwheel.CogwheelEngine;
import com.storyanvil.cogwheel.entity.NPC;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class CogwheelEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, CogwheelEngine.MODID);

    public static final RegistryObject<EntityType<NPC>> NPC =
            ENTITY_TYPES.register("npc", () -> EntityType.Builder.of(NPC::new, MobCategory.CREATURE)
                    .sized(0.6f, 1.8f).build("npc"));
}
