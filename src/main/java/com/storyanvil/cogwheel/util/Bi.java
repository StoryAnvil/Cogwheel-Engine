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

import org.jetbrains.annotations.Contract;

import java.util.Objects;

public class Bi<A,B> {
    private A a;
    private B b;

    @Contract(pure = true)
    public Bi(A a, B b) {
        this.a = a;
        this.b = b;
    }

    @Contract(pure = true)
    public Bi() {
        a = null;
        b = null;
    }

    public A getA() {
        return a;
    }

    public void setA(A a) {
        this.a = a;
    }

    public B getB() {
        return b;
    }

    public void setB(B b) {
        this.b = b;
    }

    @Override
    public int hashCode() {
        return Objects.hash(a, b);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Bi<?,?> other) {
            return Objects.equals(other.a, a) && Objects.equals(other.b, b);
        }
        return false;
    }
}
