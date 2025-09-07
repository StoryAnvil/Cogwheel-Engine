/*
 *
 *  * StoryAnvil CogWheel Engine
 *  * Copyright (C) 2025 StoryAnvil
 *  *
 *  * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *  *
 *  * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 *  *
 *  * You should have received a copy of the GNU Lesser General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.storyanvil.cogwheel.data;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.storyanvil.cogwheel.util.StoryUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.ArrayList;
import java.util.List;

public class StoryCodecs {
    public static final StoryCodec<Boolean> BOOLEAN = new StoryCodec<>((a,b)->b.writeBoolean(a), FriendlyByteBuf::readBoolean);
    public static final StoryCodec<Byte> BYTE = new StoryCodec<>((a,b)->b.writeByte(a), FriendlyByteBuf::readByte);
    public static final StoryCodec<Integer> INTEGER = new StoryCodec<>((a,b)->b.writeInt(a), FriendlyByteBuf::readInt);
    public static final StoryCodec<Long> LONG = new StoryCodec<>((a,b)->b.writeLong(a), FriendlyByteBuf::readLong);
    public static final StoryCodec<Float> FLOAT = new StoryCodec<>((a,b)->b.writeFloat(a), FriendlyByteBuf::readFloat);
    public static final StoryCodec<Double> DOUBLE = new StoryCodec<>((a,b)->b.writeDouble(a), FriendlyByteBuf::readDouble);
    public static final StoryCodec<String> STRING = new StoryCodec<>((a,b)->StoryUtils.encodeString(b,a), StoryUtils::decodeString);
    public static final StoryCodec<JsonObject> JSON = new StoryCodec<>((a, b)->StoryUtils.encodeString(b,a.toString()), b-> JsonParser.parseString(StoryUtils.decodeString(b)).getAsJsonObject());
    public static final StoryCodec<ResourceLocation> RESOURCE_LOC = new StoryCodec<>((a, b)->{StoryUtils.encodeString(b,a.getNamespace());StoryUtils.encodeString(b,a.getPath());}, b->ResourceLocation.fromNamespaceAndPath(StoryUtils.decodeString(b),StoryUtils.decodeString(b)));
    public static final StoryCodec<Component> COMPONENT = new StoryCodec<>((a, b)->StoryUtils.encodeString(b, Component.Serializer.toStableJson(a)), b->Component.Serializer.fromJson(StoryUtils.decodeString(b)));
    public static final StoryCodec<Item> ITEM = getRegistryCodec(ForgeRegistries.ITEMS);
    public static final StoryCodec<Block> BLOCK = getRegistryCodec(ForgeRegistries.BLOCKS);
    public static final StoryCodec<Biome> BIOME = getRegistryCodec(ForgeRegistries.BIOMES);
    public static final StoryCodec<EntityType<?>> ENTITY_TYPE = getRegistryCodec(ForgeRegistries.ENTITY_TYPES);
    public static final StoryCodec<Attribute> ATTRIBUTE = getRegistryCodec(ForgeRegistries.ATTRIBUTES);
    public static final StoryCodec<Enchantment> ENCHANTMENT = getRegistryCodec(ForgeRegistries.ENCHANTMENTS);

    public static <T> StoryCodec<List<T>> getListCodec(StoryCodec<T> codec) {
        return new StoryCodec<>((ts, buf) -> {
            buf.writeInt(ts.size()); for (int i = 0; i < ts.size(); i++) codec.encode(ts.get(i), buf);
            }, buf -> {int l = buf.readInt(); ArrayList<T> q = new ArrayList<>(l); for (int i = 0; i < l; i++) q.add(codec.decode(buf)); return q;});
    }
    public static <T> StoryCodec<T> getRegistryCodec(IForgeRegistry<T> registry) {
        return new StoryCodec<>((a, b)->StoryUtils.encodeString(b, registry.getKey(a).toString()), b->registry.getValue(ResourceLocation.parse(StoryUtils.decodeString(b))));
    }
}
