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

package com.storyanvil.cogwheel.util;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public class B<A> implements Supplier<A>, Consumer<A>, UnaryOperator<A> {
    private A a;

    public B(A a) {
        this.a = a;
    }

    public B() {
        this.a = null;
    }

    public void set(A a) {
        this.a = a;
    }

    @Override
    public String toString() {
        return "B{" + a +'}';
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(a);
    }

    @Override
    public A get() {
        return a;
    }

    @Override
    public void accept(A a) {
        this.a = a;
    }

    @Override
    public A apply(A a) {
        A old = this.a;
        this.a = a;
        return old;
    }

    public void putIfNull(A a) {
        if (this.a == null) this.a = a;
    }
    public void putNonNull(A a) {
        if (a != null) this.a = a;
    }
}
