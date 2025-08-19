/*
 * StoryAnvil CogWheel Engine
 * Copyright (C) 2025 StoryAnvil
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.storyanvil.cogwheel.util;

import com.storyanvil.cogwheel.api.Api;
import org.jetbrains.annotations.Contract;

import java.util.Objects;

@Api.Stable(since = "2.0.0")
public class Bi<A,B> {
    private A a;
    private B b;

    @Contract(pure = true) @Api.Stable(since = "2.0.0")
    public Bi(A a, B b) {
        this.a = a;
        this.b = b;
    }

    @Contract(pure = true) @Api.Stable(since = "2.0.0")
    public Bi() {
        a = null;
        b = null;
    }

    @Api.Stable(since = "2.0.0")
    public A getA() {
        return a;
    }

    @Api.Stable(since = "2.0.0")
    public void setA(A a) {
        this.a = a;
    }

    @Api.Stable(since = "2.0.0")
    public B getB() {
        return b;
    }

    @Api.Stable(since = "2.0.0")
    public void setB(B b) {
        this.b = b;
    }

    @Override @Api.Stable(since = "2.0.0")
    public int hashCode() {
        return Objects.hash(a, b);
    }

    @Override @Api.Stable(since = "2.0.0")
    public boolean equals(Object obj) {
        if (obj instanceof Bi<?,?> other) {
            return Objects.equals(other.a, a) && Objects.equals(other.b, b);
        }
        return false;
    }
}
