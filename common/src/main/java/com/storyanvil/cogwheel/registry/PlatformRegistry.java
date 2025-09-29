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

package com.storyanvil.cogwheel.registry;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKey;

import java.util.function.Function;
import java.util.function.Supplier;

public abstract class PlatformRegistry {
    public abstract <T extends Entity> Supplier<EntityType<T>> registerEntity(String path, Function<RegistryKey<EntityType<?>>, EntityType.Builder<T>> builder);
    public abstract <T extends Item> Supplier<T> registerItem(String path, Function<Item.Settings, T> factory, Function<RegistryKey<Item>, Item.Settings> sup);
}
