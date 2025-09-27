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

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

public class SyncArray<T> extends ArrayList<T> {
    private boolean frozen = false;
    public void freeze() {
        this.frozen = true;
    }
    @Override
    public synchronized T remove(int index) {
        if (frozen) throw new IllegalStateException("Array is frozen!");
        return super.remove(index);
    }

    @Override
    public synchronized void add(int index, T element) {
        if (frozen) throw new IllegalStateException("Array is frozen!");
        super.add(index, element);
    }

    @Override
    public synchronized boolean add(T t) {
        if (frozen) throw new IllegalStateException("Array is frozen!");
        return super.add(t);
    }

    @Override
    public synchronized T set(int index, T element) {
        if (frozen) throw new IllegalStateException("Array is frozen!");
        return super.set(index, element);
    }

    @Override
    public synchronized T get(int index) {
        return super.get(index);
    }

    @Override
    public synchronized void sort(Comparator<? super T> c) {
        if (frozen) throw new IllegalStateException("Array is frozen!");
        super.sort(c);
    }

    @Override
    public synchronized void replaceAll(UnaryOperator<T> operator) {
        if (frozen) throw new IllegalStateException("Array is frozen!");
        super.replaceAll(operator);
    }

    @Override
    public synchronized boolean removeIf(Predicate<? super T> filter) {
        if (frozen) throw new IllegalStateException("Array is frozen!");
        return super.removeIf(filter);
    }

    @Override
    public synchronized void forEach(Consumer<? super T> action) {
        super.forEach(action);
    }

    @Override
    public synchronized @NotNull List<T> subList(int fromIndex, int toIndex) {
        return super.subList(fromIndex, toIndex);
    }

    @Override
    protected synchronized void removeRange(int fromIndex, int toIndex) {
        if (frozen) throw new IllegalStateException("Array is frozen!");
        super.removeRange(fromIndex, toIndex);
    }

    @Override
    public synchronized boolean removeAll(Collection<?> c) {
        if (frozen) throw new IllegalStateException("Array is frozen!");
        return super.removeAll(c);
    }

    @Override
    public synchronized boolean retainAll(Collection<?> c) {
        if (frozen) throw new IllegalStateException("Array is frozen!");
        return super.retainAll(c);
    }

    @Override
    public synchronized boolean remove(Object o) {
        if (frozen) throw new IllegalStateException("Array is frozen!");
        return super.remove(o);
    }

    @Override
    public synchronized void clear() {
        if (frozen) throw new IllegalStateException("Array is frozen!");
        super.clear();
    }

    public synchronized void dispose() {
        super.clear();
    }

    @Override
    public synchronized Object @NotNull [] toArray() {
        return super.toArray();
    }

    @Override
    public synchronized <T1> T1 @NotNull [] toArray(T1[] a) {
        return super.toArray(a);
    }

    @Override
    public synchronized boolean contains(Object o) {
        return super.contains(o);
    }

    @Override
    public synchronized int indexOf(Object o) {
        return super.indexOf(o);
    }

    @Override
    public synchronized int lastIndexOf(Object o) {
        return super.lastIndexOf(o);
    }
}
