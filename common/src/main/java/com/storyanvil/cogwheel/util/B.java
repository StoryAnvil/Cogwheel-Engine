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

package com.storyanvil.cogwheel.util;

import com.storyanvil.cogwheel.api.Api;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

@Api.Stable(since = "2.8.0")
public class B<A> implements Supplier<A>, Consumer<A>, UnaryOperator<A> {
    private A a;

    @Api.Stable(since = "2.8.0")
    public B(A a) {
        this.a = a;
    }

    @Api.Stable(since = "2.8.0")
    public B() {
        this.a = null;
    }

    @Api.Stable(since = "2.8.0")
    public void set(A a) {
        this.a = a;
    }

    @Override @Api.Stable(since = "2.8.0")
    public String toString() {
        return "B{" + a +'}';
    }

    @Override @Api.Stable(since = "2.8.0")
    public int hashCode() {
        return Objects.hashCode(a);
    }

    @Override @Api.Stable(since = "2.8.0")
    public A get() {
        return a;
    }

    @Override @Api.Stable(since = "2.8.0")
    public void accept(A a) {
        this.a = a;
    }

    @Override @Api.Stable(since = "2.8.0")
    public A apply(A a) {
        A old = this.a;
        this.a = a;
        return old;
    }

    @Api.Stable(since = "2.8.0")
    public void putIfNull(A a) {
        if (this.a == null) this.a = a;
    }

    @Api.Stable(since = "2.8.0")
    public void putNonNull(A a) {
        if (a != null) this.a = a;
    }
}
