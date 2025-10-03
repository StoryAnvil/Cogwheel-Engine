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

package com.storyanvil.cogwheel.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.storyanvil.cogwheel.util.StoryUtils;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.item.Item;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public class StoryCodecs {
    public static final StoryCodec<Boolean> BOOLEAN = new StoryCodec<>((a,b)->b.writeBoolean(a), PacketByteBuf::readBoolean);
    public static final StoryCodec<Byte> BYTE = new StoryCodec<>((a,b)->b.writeByte(a), PacketByteBuf::readByte);
    public static final StoryCodec<Integer> INTEGER = new StoryCodec<>((a,b)->b.writeInt(a), PacketByteBuf::readInt);
    public static final StoryCodec<Long> LONG = new StoryCodec<>((a,b)->b.writeLong(a), PacketByteBuf::readLong);
    public static final StoryCodec<Float> FLOAT = new StoryCodec<>((a,b)->b.writeFloat(a), PacketByteBuf::readFloat);
    public static final StoryCodec<Double> DOUBLE = new StoryCodec<>((a,b)->b.writeDouble(a), PacketByteBuf::readDouble);
    public static final StoryCodec<String> STRING = new StoryCodec<>((a,b)->StoryUtils.encodeString(b,a), StoryUtils::decodeString);
    public static final StoryCodec<JsonObject> JSON = new StoryCodec<>((a, b)->StoryUtils.encodeString(b,a.toString()), b-> JsonParser.parseString(StoryUtils.decodeString(b)).getAsJsonObject());
    public static final StoryCodec<JsonElement> JSON_UNIVERSAL = new StoryCodec<>((jsonElement, buf) -> {JsonObject o=new JsonObject();o.add("a",jsonElement);StoryCodecs.JSON.encode(o, buf);}, b -> StoryCodecs.JSON.decode(b).get("a"));
    public static final StoryCodec<Identifier> RESOURCE_LOC = new StoryCodec<>((a, b)->{StoryUtils.encodeString(b,a.getNamespace());StoryUtils.encodeString(b,a.getPath());}, b->Identifier.of(StoryUtils.decodeString(b),StoryUtils.decodeString(b)));
    public static final StoryCodec<NbtCompound> NBT_COMPOUND = new StoryCodec<>((compound, buf) -> {
        StoryCodecs.STRING.encode(NbtHelper.toNbtProviderString(compound), buf);
    }, buf -> {
        try {
            return NbtHelper.fromNbtProviderString(StoryCodecs.STRING.decode(buf));
        } catch (CommandSyntaxException e) {
            throw new RuntimeException(e);
        }
    });
    public static final StoryCodec<Identifier> IDENTIFIER = RESOURCE_LOC;
    public static final StoryCodec<CameraPos> CAMERA_POS = new StoryCodec<>(CameraPos::encode, CameraPos::decode);
    public static final StoryCodec<Item> ITEM = getRegistryCodec(Registries.ITEM);
    public static final StoryCodec<Block> BLOCK = getRegistryCodec(Registries.BLOCK);
    public static final StoryCodec<EntityType<?>> ENTITY_TYPE = getRegistryCodec(Registries.ENTITY_TYPE);
    public static final StoryCodec<EntityAttribute> ATTRIBUTE = getRegistryCodec(Registries.ATTRIBUTE);

    public static final StoryCodec<Text> TEXT = StoryCodecBldr.build(
            StoryCodecBldr.Prop(t -> Text.Serialization.toJsonString(t, MinecraftClient.getInstance().getNetworkHandler().getRegistryManager()), STRING),
            s -> Text.Serialization.fromJson(s, MinecraftClient.getInstance().getNetworkHandler().getRegistryManager())
    );

    public static <T> StoryCodec<List<T>> getListCodec(StoryCodec<T> codec) {
        return new StoryCodec<>((ts, buf) -> {
            buf.writeInt(ts.size()); for (int i = 0; i < ts.size(); i++) codec.encode(ts.get(i), buf);
            }, buf -> {int l = buf.readInt(); ArrayList<T> q = new ArrayList<>(l); for (int i = 0; i < l; i++) q.add(codec.decode(buf)); return q;});
    }
    public static <T> StoryCodec<T> getRegistryCodec(Registry<T> registry) {
        return StoryCodecBldr.build(
                StoryCodecBldr.String(o -> String.valueOf(registry.getId(o))),
                s -> registry.get(Identifier.of(s))
        );
    }
    public static <T> StoryCodec<T> getNullableCodec(StoryCodec<T> nonNullableCodec) {
        return new StoryCodec<T>((t, buf) -> {
            if (t == null) {
                buf.writeBoolean(false);
                return;
            }
            buf.writeBoolean(true);
            nonNullableCodec.encode(t, buf);
        }, buf -> {
            if (buf.readBoolean()) {
                return nonNullableCodec.decode(buf);
            } else return null;
        });
    }
}
