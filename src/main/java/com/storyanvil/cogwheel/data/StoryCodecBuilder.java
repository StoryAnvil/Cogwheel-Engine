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
import com.mojang.datafixers.util.*;
import net.minecraft.network.FriendlyByteBuf;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public final class StoryCodecBuilder {
    public static <R> StoryCodec<R> build(Supplier<R> decoder) {
        return new StoryCodec<>((r, buf) -> {}, buf -> decoder.get());
    }
    public static <T0, R> StoryCodec<R> build(P<R, T0> t0, Function<T0, R> decoder) {
        return new StoryCodec<>((r, buf) -> {
            t0.codec.encode(t0.get.apply(r), buf);
        }, buf -> decoder.apply(t0.codec.decode(buf)));
    }
    public static <T0,T1,R> StoryCodec<R> build(P<R,T0>t0, P<R,T1>t1,BiFunction<T0,T1,R>decoder) {
        return new StoryCodec<>((r, buf) -> {
            t0.encode(r, buf);t1.encode(r, buf);
        }, buf -> decoder.apply(t0.decode(buf), t1.decode(buf)));
    }
    public static <T0,T1,T2,R> StoryCodec<R> build(P<R,T0>t0,P<R,T1>t1,P<R,T2>t2,Function3<T0,T1,T2,R> decoder) {
        return new StoryCodec<>((r, buf) -> {
            t0.encode(r, buf);t1.encode(r, buf);t2.encode(r, buf);
        }, buf -> decoder.apply(t0.decode(buf), t1.decode(buf), t2.decode(buf)));
    }
    public static <T0,T1,T2,T3,R> StoryCodec<R> build(P<R,T0>t0,P<R,T1>t1,P<R,T2>t2,P<R,T3>t3,Function4<T0,T1,T2,T3,R> decoder) {
        return new StoryCodec<>((r, buf) -> {
            t0.encode(r, buf);t1.encode(r, buf);t2.encode(r, buf);t3.encode(r, buf);
        }, buf -> decoder.apply(t0.decode(buf), t1.decode(buf), t2.decode(buf), t3.decode(buf)));
    }
    public static <T0,T1,T2,T3,T4,R> StoryCodec<R> build(P<R,T0>t0,P<R,T1>t1,P<R,T2>t2,P<R,T3>t3,P<R,T4>t4,Function5<T0,T1,T2,T3,T4,R> decoder) {
        return new StoryCodec<>((r, buf) -> {
            t0.encode(r, buf);t1.encode(r, buf);t2.encode(r, buf);t3.encode(r, buf);t4.encode(r, buf);
        }, buf -> decoder.apply(t0.decode(buf), t1.decode(buf), t2.decode(buf), t3.decode(buf), t4.decode(buf)));
    }
    public static <T0,T1,T2,T3,T4,T5,R> StoryCodec<R> build(P<R,T0>t0,P<R,T1>t1,P<R,T2>t2,P<R,T3>t3,P<R,T4>t4,P<R,T5>t5,Function6<T0,T1,T2,T3,T4,T5,R> decoder) {
        return new StoryCodec<>((r, buf) -> {
            t0.encode(r, buf);t1.encode(r, buf);t2.encode(r, buf);t3.encode(r, buf);t4.encode(r, buf);t5.encode(r, buf);
        }, buf -> decoder.apply(t0.decode(buf), t1.decode(buf), t2.decode(buf), t3.decode(buf), t4.decode(buf), t5.decode(buf)));
    }

    public static <R> P<R, Boolean> Bool(Function<R, Boolean> getter) {
        return new P<>(getter, StoryCodecs.BOOLEAN);
    }
    public static <R> P<R, Byte> Byte(Function<R, Byte> getter) {
        return new P<>(getter, StoryCodecs.BYTE);
    }
    public static <R> P<R, Integer> Integer(Function<R, Integer> getter) {
        return new P<>(getter, StoryCodecs.INTEGER);
    }
    public static <R> P<R, Long> Long(Function<R, Long> getter) {
        return new P<>(getter, StoryCodecs.LONG);
    }
    public static <R> P<R, Float> Float(Function<R, Float> getter) {
        return new P<>(getter, StoryCodecs.FLOAT);
    }
    public static <R> P<R, Double> Double(Function<R, Double> getter) {
        return new P<>(getter, StoryCodecs.DOUBLE);
    }
    public static <R> P<R, String> String(Function<R, String> getter) {
        return new P<>(getter, StoryCodecs.STRING);
    }
    public static <R> P<R, JsonObject> JsonObject(Function<R, JsonObject> getter) {
        return new P<>(getter, StoryCodecs.JSON);
    }
    public static <R,T> P<R, T> Prop(Function<R, T> getter, StoryCodec<T> codec) {
        return new P<>(getter, codec);
    }

    public static class P<R, T> {
        public final Function<R, T> get;
        public final StoryCodec<T> codec;

        public void encode(R r, FriendlyByteBuf buf) {
            codec.encode(get.apply(r), buf);
        }
        public T decode(FriendlyByteBuf buf) {
            return codec.decode(buf);
        }
        private P(Function<R, T> get, StoryCodec<T> codec) {
            this.get = get;
            this.codec = codec;
        }
    }
}
