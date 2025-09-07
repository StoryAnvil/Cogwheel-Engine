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
    public static <T0,T1,T2,T3,T4,T5,T6,R> StoryCodec<R> build(P<R,T0>t0,P<R,T1>t1,P<R,T2>t2,P<R,T3>t3,P<R,T4>t4,P<R,T5>t5,P<R,T6>t6,Function7<T0,T1,T2,T3,T4,T5,T6,R> decoder) {
        return new StoryCodec<>((r, buf) -> {
            t0.encode(r, buf);t1.encode(r, buf);t2.encode(r, buf);t3.encode(r, buf);t4.encode(r, buf);t5.encode(r, buf);t6.encode(r, buf);
        }, buf -> decoder.apply(t0.decode(buf), t1.decode(buf), t2.decode(buf), t3.decode(buf), t4.decode(buf), t5.decode(buf), t6.decode(buf)));
    }
    public static <T0,T1,T2,T3,T4,T5,T6,T7,R> StoryCodec<R> build(P<R,T0>t0,P<R,T1>t1,P<R,T2>t2,P<R,T3>t3,P<R,T4>t4,P<R,T5>t5,P<R,T6>t6,P<R,T7>t7,Function8<T0,T1,T2,T3,T4,T5,T6,T7,R> decoder) {
        return new StoryCodec<>((r, buf) -> {
            t0.encode(r, buf);t1.encode(r, buf);t2.encode(r, buf);t3.encode(r, buf);t4.encode(r, buf);t5.encode(r, buf);t6.encode(r, buf);t7.encode(r, buf);
        }, buf -> decoder.apply(t0.decode(buf), t1.decode(buf), t2.decode(buf), t3.decode(buf), t4.decode(buf), t5.decode(buf), t6.decode(buf), t7.decode(buf)));
    }
    public static <T0,T1,T2,T3,T4,T5,T6,T7,T8,R> StoryCodec<R> build(P<R,T0>t0,P<R,T1>t1,P<R,T2>t2,P<R,T3>t3,P<R,T4>t4,P<R,T5>t5,P<R,T6>t6,P<R,T7>t7,P<R,T8>t8,Function9<T0,T1,T2,T3,T4,T5,T6,T7,T8,R> decoder) {
        return new StoryCodec<>((r, buf) -> {
            t0.encode(r, buf);t1.encode(r, buf);t2.encode(r, buf);t3.encode(r, buf);t4.encode(r, buf);t5.encode(r, buf);t6.encode(r, buf);t7.encode(r, buf);t8.encode(r, buf);
        }, buf -> decoder.apply(t0.decode(buf), t1.decode(buf), t2.decode(buf), t3.decode(buf), t4.decode(buf), t5.decode(buf), t6.decode(buf), t7.decode(buf), t8.decode(buf)));
    }
    public static <T0,T1,T2,T3,T4,T5,T6,T7,T8,T9,R> StoryCodec<R> build(P<R,T0>t0,P<R,T1>t1,P<R,T2>t2,P<R,T3>t3,P<R,T4>t4,P<R,T5>t5,P<R,T6>t6,P<R,T7>t7,P<R,T8>t8,P<R,T9>t9,Function10<T0,T1,T2,T3,T4,T5,T6,T7,T8,T9,R> decoder) {
        return new StoryCodec<>((r, buf) -> {
            t0.encode(r, buf);t1.encode(r, buf);t2.encode(r, buf);t3.encode(r, buf);t4.encode(r, buf);t5.encode(r, buf);t6.encode(r, buf);t7.encode(r, buf);t8.encode(r, buf);t9.encode(r, buf);
        }, buf -> decoder.apply(t0.decode(buf), t1.decode(buf), t2.decode(buf), t3.decode(buf), t4.decode(buf), t5.decode(buf), t6.decode(buf), t7.decode(buf), t8.decode(buf), t9.decode(buf)));
    }
    public static <T0,T1,T2,T3,T4,T5,T6,T7,T8,T9,T10,R> StoryCodec<R> build(P<R,T0>t0,P<R,T1>t1,P<R,T2>t2,P<R,T3>t3,P<R,T4>t4,P<R,T5>t5,P<R,T6>t6,P<R,T7>t7,P<R,T8>t8,P<R,T9>t9,P<R,T10>t10,Function11<T0,T1,T2,T3,T4,T5,T6,T7,T8,T9,T10,R> decoder) {
        return new StoryCodec<>((r, buf) -> {
            t0.encode(r, buf);t1.encode(r, buf);t2.encode(r, buf);t3.encode(r, buf);t4.encode(r, buf);t5.encode(r, buf);t6.encode(r, buf);t7.encode(r, buf);t8.encode(r, buf);t9.encode(r, buf);t10.encode(r, buf);
        }, buf -> decoder.apply(t0.decode(buf), t1.decode(buf), t2.decode(buf), t3.decode(buf), t4.decode(buf), t5.decode(buf), t6.decode(buf), t7.decode(buf), t8.decode(buf), t9.decode(buf), t10.decode(buf)));
    }
    public static <T0,T1,T2,T3,T4,T5,T6,T7,T8,T9,T10,T11,R> StoryCodec<R> build(P<R,T0>t0,P<R,T1>t1,P<R,T2>t2,P<R,T3>t3,P<R,T4>t4,P<R,T5>t5,P<R,T6>t6,P<R,T7>t7,P<R,T8>t8,P<R,T9>t9,P<R,T10>t10,P<R,T11>t11,Function12<T0,T1,T2,T3,T4,T5,T6,T7,T8,T9,T10,T11,R> decoder) {
        return new StoryCodec<>((r, buf) -> {
            t0.encode(r, buf);t1.encode(r, buf);t2.encode(r, buf);t3.encode(r, buf);t4.encode(r, buf);t5.encode(r, buf);t6.encode(r, buf);t7.encode(r, buf);t8.encode(r, buf);t9.encode(r, buf);t10.encode(r, buf);t11.encode(r, buf);
        }, buf -> decoder.apply(t0.decode(buf), t1.decode(buf), t2.decode(buf), t3.decode(buf), t4.decode(buf), t5.decode(buf), t6.decode(buf), t7.decode(buf), t8.decode(buf), t9.decode(buf), t10.decode(buf), t11.decode(buf)));
    }
    public static <T0,T1,T2,T3,T4,T5,T6,T7,T8,T9,T10,T11,T12,R> StoryCodec<R> build(P<R,T0>t0,P<R,T1>t1,P<R,T2>t2,P<R,T3>t3,P<R,T4>t4,P<R,T5>t5,P<R,T6>t6,P<R,T7>t7,P<R,T8>t8,P<R,T9>t9,P<R,T10>t10,P<R,T11>t11,P<R,T12>t12,Function13<T0,T1,T2,T3,T4,T5,T6,T7,T8,T9,T10,T11,T12,R> decoder) {
        return new StoryCodec<>((r, buf) -> {
            t0.encode(r, buf);t1.encode(r, buf);t2.encode(r, buf);t3.encode(r, buf);t4.encode(r, buf);t5.encode(r, buf);t6.encode(r, buf);t7.encode(r, buf);t8.encode(r, buf);t9.encode(r, buf);t10.encode(r, buf);t11.encode(r, buf);t12.encode(r, buf);
        }, buf -> decoder.apply(t0.decode(buf), t1.decode(buf), t2.decode(buf), t3.decode(buf), t4.decode(buf), t5.decode(buf), t6.decode(buf), t7.decode(buf), t8.decode(buf), t9.decode(buf), t10.decode(buf), t11.decode(buf), t12.decode(buf)));
    }
    public static <T0,T1,T2,T3,T4,T5,T6,T7,T8,T9,T10,T11,T12,T13,R> StoryCodec<R> build(P<R,T0>t0,P<R,T1>t1,P<R,T2>t2,P<R,T3>t3,P<R,T4>t4,P<R,T5>t5,P<R,T6>t6,P<R,T7>t7,P<R,T8>t8,P<R,T9>t9,P<R,T10>t10,P<R,T11>t11,P<R,T12>t12,P<R,T13>t13,Function14<T0,T1,T2,T3,T4,T5,T6,T7,T8,T9,T10,T11,T12,T13,R> decoder) {
        return new StoryCodec<>((r, buf) -> {
            t0.encode(r, buf);t1.encode(r, buf);t2.encode(r, buf);t3.encode(r, buf);t4.encode(r, buf);t5.encode(r, buf);t6.encode(r, buf);t7.encode(r, buf);t8.encode(r, buf);t9.encode(r, buf);t10.encode(r, buf);t11.encode(r, buf);t12.encode(r, buf);t13.encode(r, buf);
        }, buf -> decoder.apply(t0.decode(buf), t1.decode(buf), t2.decode(buf), t3.decode(buf), t4.decode(buf), t5.decode(buf), t6.decode(buf), t7.decode(buf), t8.decode(buf), t9.decode(buf), t10.decode(buf), t11.decode(buf), t12.decode(buf), t13.decode(buf)));
    }
    public static <T0,T1,T2,T3,T4,T5,T6,T7,T8,T9,T10,T11,T12,T13,T14,R> StoryCodec<R> build(P<R,T0>t0,P<R,T1>t1,P<R,T2>t2,P<R,T3>t3,P<R,T4>t4,P<R,T5>t5,P<R,T6>t6,P<R,T7>t7,P<R,T8>t8,P<R,T9>t9,P<R,T10>t10,P<R,T11>t11,P<R,T12>t12,P<R,T13>t13,P<R,T14>t14,Function15<T0,T1,T2,T3,T4,T5,T6,T7,T8,T9,T10,T11,T12,T13,T14,R> decoder) {
        return new StoryCodec<>((r, buf) -> {
            t0.encode(r, buf);t1.encode(r, buf);t2.encode(r, buf);t3.encode(r, buf);t4.encode(r, buf);t5.encode(r, buf);t6.encode(r, buf);t7.encode(r, buf);t8.encode(r, buf);t9.encode(r, buf);t10.encode(r, buf);t11.encode(r, buf);t12.encode(r, buf);t13.encode(r, buf);t14.encode(r, buf);
        }, buf -> decoder.apply(t0.decode(buf), t1.decode(buf), t2.decode(buf), t3.decode(buf), t4.decode(buf), t5.decode(buf), t6.decode(buf), t7.decode(buf), t8.decode(buf), t9.decode(buf), t10.decode(buf), t11.decode(buf), t12.decode(buf), t13.decode(buf), t14.decode(buf)));
    }
    public static <T0,T1,T2,T3,T4,T5,T6,T7,T8,T9,T10,T11,T12,T13,T14,T15,R> StoryCodec<R> build(P<R,T0>t0,P<R,T1>t1,P<R,T2>t2,P<R,T3>t3,P<R,T4>t4,P<R,T5>t5,P<R,T6>t6,P<R,T7>t7,P<R,T8>t8,P<R,T9>t9,P<R,T10>t10,P<R,T11>t11,P<R,T12>t12,P<R,T13>t13,P<R,T14>t14,P<R,T15>t15,Function16<T0,T1,T2,T3,T4,T5,T6,T7,T8,T9,T10,T11,T12,T13,T14,T15,R> decoder) {
        return new StoryCodec<>((r, buf) -> {
            t0.encode(r, buf);t1.encode(r, buf);t2.encode(r, buf);t3.encode(r, buf);t4.encode(r, buf);t5.encode(r, buf);t6.encode(r, buf);t7.encode(r, buf);t8.encode(r, buf);t9.encode(r, buf);t10.encode(r, buf);t11.encode(r, buf);t12.encode(r, buf);t13.encode(r, buf);t14.encode(r, buf);t15.encode(r, buf);
        }, buf -> decoder.apply(t0.decode(buf), t1.decode(buf), t2.decode(buf), t3.decode(buf), t4.decode(buf), t5.decode(buf), t6.decode(buf), t7.decode(buf), t8.decode(buf), t9.decode(buf), t10.decode(buf), t11.decode(buf), t12.decode(buf), t13.decode(buf), t14.decode(buf), t15.decode(buf)));
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
