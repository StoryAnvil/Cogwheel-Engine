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

import com.storyanvil.cogwheel.registry.PlatformRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

import java.util.function.Function;
import java.util.function.Supplier;

public class FabricPlatformRegistry extends PlatformRegistry {
    private final String namespace;
    public FabricPlatformRegistry(String namespace) {
        this.namespace = namespace;
    }

    @Override @SuppressWarnings("unchecked")
    public <T extends Entity> Supplier<EntityType<T>> registerEntity(String path, Function<RegistryKey<EntityType<?>>, EntityType.Builder<T>> builder) {
        RegistryKey<EntityType<?>> key = RegistryKey.of(RegistryKeys.ENTITY_TYPE, Identifier.of(namespace, "npc"));
        EntityType<?> tp = builder.apply(key).build(key);
        EntityType<T> t = (EntityType<T>) Registry.register(Registries.ENTITY_TYPE, key, tp);
        return () -> t;
    }

    @Override @SuppressWarnings("unchecked")
    public <T extends Item> Supplier<T> registerItem(String path, Function<Item.Settings, T> factory, Function<RegistryKey<Item>, Item.Settings> sup) {
        RegistryKey<Item> itemKey = RegistryKey.of(RegistryKeys.ITEM, Identifier.of(namespace, path));
        Item item = factory.apply(sup.apply(itemKey).registryKey(itemKey));
        T t = (T) Registry.register(Registries.ITEM, itemKey, item);
        return () -> t;
    }
}
