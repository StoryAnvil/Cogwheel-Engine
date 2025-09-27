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

package com.storyanvil.cogwheel.fabric;

import com.storyanvil.cogwheel.CogwheelEngine;
import com.storyanvil.cogwheel.entity.NPC;
import com.storyanvil.cogwheel.items.InspectorItem;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.UseCooldownComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public class FabricRegistry {
    public static final Item INSPECTOR = register("inspector", InspectorItem::new,
            new Item.Settings().maxCount(1).fireproof().rarity(Rarity.EPIC).component(DataComponentTypes.USE_COOLDOWN, new UseCooldownComponent(0.25f, Optional.of(Identifier.of(CogwheelEngine.MODID, "devtools")))));

    public static final EntityType<NPC> NPC = register("npc", () ->
            EntityType.Builder.create(NPC::new, SpawnGroup.MISC).dimensions(0.6f, 1.8f));

    public static Item register(String name, Function<Item.Settings, Item> itemFactory, Item.Settings settings) {
        RegistryKey<Item> itemKey = RegistryKey.of(RegistryKeys.ITEM, Identifier.of(CogwheelEngine.MODID, name));
        Item item = itemFactory.apply(settings.registryKey(itemKey));
        Registry.register(Registries.ITEM, itemKey, item);
        return item;
    }
    public static <T extends Entity> EntityType<T> register(String name, Supplier<EntityType.Builder<T>> sup) {
        RegistryKey<EntityType<?>> key = RegistryKey.of(RegistryKeys.ENTITY_TYPE, Identifier.of(CogwheelEngine.MODID, "npc"));
        EntityType<?> tp = sup.get().build(key);;
        return (EntityType<T>) Registry.register(Registries.ENTITY_TYPE, key, tp);
    }
    public static void initialize() {/* Used to run static initializer */}
}
