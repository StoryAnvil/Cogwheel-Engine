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

import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;
import java.util.*;

@SuppressWarnings("unchecked")
public class WeakList<T> implements List<T> {
    private final ArrayList<WeakReference<T>> list;

    public WeakList() {
        list = new ArrayList<>();
    }
    public WeakList(T value) {
        this();
        add(value);
    }

    @Override
    public int size() {
        return list.size();
    }

    @Override
    public boolean isEmpty() {
        return list.isEmpty();
    }

    /**
     * Removes unlinked elements
     */
    public void cleanUp() {
        for (int i = 0; i < list.size(); i++) {
            WeakReference<T> ref = list.get(i);
            if (ref.refersTo(null)) {
                list.remove(i);
                i--;
            }
        }
    }

    @Override
    public boolean contains(Object o) {
        for (int i = 0; i < list.size(); i++) {
            WeakReference<T> ref = list.get(i);
            if (ref.refersTo(null)) {
                list.remove(i);
                i--;
            } else if (ref.refersTo((T) o)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public @NotNull Iterator<T> iterator() {
        throw new UnsupportedOperationException();
//        return new Iterator<T>() {
//            private int i = 0;
//            @Override
//            public boolean hasNext() {
//                return !list.isEmpty();
//            }
//
//            @Override
//            public T next() {
//                return list.get(i).get();
//            }
//        };
    }

    @Override
    public @NotNull Object @NotNull [] toArray() {
        throw new UnsupportedOperationException();
    }

    @Override
    public @NotNull <T1> T1 @NotNull [] toArray(@NotNull T1 @NotNull[] a) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean add(T t) {
        return list.add(new WeakReference<>(t));
    }

    @Override
    public boolean remove(Object o) {
        for (int i = 0; i < list.size(); i++) {
            WeakReference<T> t = list.get(i);
            if (t.refersTo(null)) {
                i--;
                list.remove(i);
            } else if (t.refersTo((T) o)) {
                i--;
                list.remove(i);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean containsAll(@NotNull Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(@NotNull Collection<? extends T> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(int index, @NotNull Collection<? extends T> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(@NotNull Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(@NotNull Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        list.clear();
    }

    @Override
    public T get(int index) {
        return list.get(index).get();
    }

    public WeakReference<T> getRef(int index) {
        return list.get(index);
    }

    @Override
    public T set(int index, T element) {
        return list.set(index, new WeakReference<>(element)).get();
    }

    @Override
    public void add(int index, T element) {
        list.add(index, new WeakReference<>(element));
    }

    @Override
    public T remove(int index) {
        return list.remove(index).get();
    }

    @Override
    public int indexOf(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int lastIndexOf(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public @NotNull ListIterator<T> listIterator() {
        throw new UnsupportedOperationException();
    }

    @Override
    public @NotNull ListIterator<T> listIterator(int index) {
        throw new UnsupportedOperationException();
    }

    @Override
    public @NotNull List<T> subList(int fromIndex, int toIndex) {
        throw new UnsupportedOperationException();
    }
}
