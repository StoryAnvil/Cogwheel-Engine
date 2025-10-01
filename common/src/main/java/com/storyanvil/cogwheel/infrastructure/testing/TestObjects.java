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

package com.storyanvil.cogwheel.infrastructure.testing;

import com.storyanvil.cogwheel.infrastructure.ArgumentData;
import com.storyanvil.cogwheel.infrastructure.cog.PreventSubCalling;
import com.storyanvil.cogwheel.infrastructure.err.CogScriptException;
import com.storyanvil.cogwheel.infrastructure.props.CGPM;
import com.storyanvil.cogwheel.infrastructure.props.JWCGPM;
import com.storyanvil.cogwheel.infrastructure.script.DispatchedScript;
import com.storyanvil.cogwheel.util.EasyPropManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

public class TestObjects {
    private TestObjects() {}

    public static class TestJWCGPM extends JWCGPM<TestJWCGPM> {
        public static CGPM testMethod(TestJWCGPM me, ArgumentData args, DispatchedScript script) {
            return null;
        }
    }

    public static class TestEasyCGPM implements CGPM {
        public static final EasyPropManager MANAGER = new EasyPropManager("testing", manager -> {
            manager.reg("testMethod", (name, args, script, o) -> {
                return null;
            });
        });

        @Override
        public @Nullable CGPM getProperty(String name, ArgumentData args, DispatchedScript script) throws PreventSubCalling, CogScriptException {
            return MANAGER.get(name, args, script, this);
        }

        @Override
        public boolean equalsTo(CGPM o) {
            return o == this;
        }
    }
    public static class TestBasicCGPM implements CGPM {
        @Override
        public @Nullable CGPM getProperty(String name, ArgumentData args, DispatchedScript script) throws PreventSubCalling, CogScriptException {
            if (!name.equals("testMethod")) throw script.wrap(new RuntimeException("No such method!"));
            return null;
        }

        @Override
        public boolean equalsTo(CGPM o) {
            return o == this;
        }
    }

    public static class DuplicateList<T> extends ArrayList<T> {
        private T value;
        private int size;

        public DuplicateList(T value, int size) {
            super(0);
            this.value = value;
            this.size = size;
        }

        @Override
        public void trimToSize() {}

        @Override
        public void ensureCapacity(int minCapacity) {}

        @Override
        public int size() {
            return size;
        }

        @Override
        public boolean isEmpty() {
            return size <= 0;
        }

        @Override
        public boolean contains(Object o) {
            return size > 0 && o == value;
        }

        @Override
        public int indexOf(Object o) {
            return contains(o) ? 0 : -1;
        }

        @Override
        public int lastIndexOf(Object o) {
            return contains(o) ? size - 1 : -1;
        }

        @Override
        public Object clone() {
            return this;
        }

        @Override
        public Object @NotNull [] toArray() {
            Object[] ar = new Object[size];
            for (int i = 0; i < size; i++) {
                ar[i] = value;
            }
            return ar;
        }

        @Override
        public <T1> T1 @NotNull [] toArray(T1[] a) {
            return (T1[]) toArray();
        }

        @Override
        public T get(int index) {
            if (index < 0 || index >= size) throw new IndexOutOfBoundsException();
            return value;
        }

        @Override
        public T getFirst() {
            if (isEmpty()) throw new NoSuchElementException();
            return value;
        }

        @Override
        public T getLast() {
            if (isEmpty()) throw new NoSuchElementException();
            return value;
        }

        @Override
        public T set(int index, T element) {
            throw new RuntimeException("List is not modifiable");
        }

        @Override
        public boolean add(T t) {
            throw new RuntimeException("List is not modifiable");
        }

        @Override
        public void add(int index, T element) {
            throw new RuntimeException("List is not modifiable");
        }

        @Override
        public void addFirst(T element) {
            throw new RuntimeException("List is not modifiable");
        }

        @Override
        public void addLast(T element) {
            throw new RuntimeException("List is not modifiable");
        }

        @Override
        public T remove(int index) {
            throw new RuntimeException("List is not modifiable");
        }

        @Override
        public T removeFirst() {
            throw new RuntimeException("List is not modifiable");
        }

        @Override
        public T removeLast() {
            throw new RuntimeException("List is not modifiable");
        }

        @Override
        public boolean equals(Object o) {
            return o == this;
        }

        @Override
        public int hashCode() {
            return Objects.hash(value, size);
        }

        @Override
        public boolean remove(Object o) {
            throw new RuntimeException("List is not modifiable");
        }

        @Override
        public void clear() {
            throw new RuntimeException("List is not modifiable");
        }

        @Override
        public boolean addAll(Collection<? extends T> c) {
            throw new RuntimeException("List is not modifiable");
        }

        @Override
        public boolean addAll(int index, Collection<? extends T> c) {
            throw new RuntimeException("List is not modifiable");
        }

        @Override
        protected void removeRange(int fromIndex, int toIndex) {
            throw new RuntimeException("List is not modifiable");
        }

        @Override
        public boolean removeAll(Collection<?> c) {
            throw new RuntimeException("List is not modifiable");
        }

        @Override
        public boolean retainAll(Collection<?> c) {
            throw new RuntimeException("List is not modifiable");
        }

        @Override
        public @NotNull ListIterator<T> listIterator(int index) {
            int[] f = new int[]{index};
            return new ListIterator<T>() {
                private int index = f[0];
                @Override
                public boolean hasNext() {
                    return index < size;
                }

                @Override
                public T next() {
                    index++;
                    return value;
                }

                @Override
                public boolean hasPrevious() {
                    return index > 0;
                }

                @Override
                public T previous() {
                    index--;
                    return value;
                }

                @Override
                public int nextIndex() {
                    index++;
                    return index;
                }

                @Override
                public int previousIndex() {
                    index--;
                    return index;
                }

                @Override
                public void remove() {
                    throw new RuntimeException("List is not modifiable");
                }

                @Override
                public void set(T t) {
                    throw new RuntimeException("List is not modifiable");
                }

                @Override
                public void add(T t) {
                    throw new RuntimeException("List is not modifiable");
                }
            };
        }

        @Override
        public @NotNull ListIterator<T> listIterator() {
            return listIterator(0);
        }

        @Override
        public @NotNull Iterator<T> iterator() {
            return listIterator();
        }

        @Override
        public List<T> subList(int fromIndex, int toIndex) {
            return new DuplicateList<T>(value, toIndex - fromIndex);
        }

        @Override
        public void forEach(Consumer<? super T> action) {
            for (int i = 0; i < size; i++) {
                action.accept(value);
            }
        }

        @Override
        public @NotNull Spliterator<T> spliterator() {
            throw new RuntimeException("spkiterator is not supported");
        }

        @Override
        public boolean removeIf(Predicate<? super T> filter) {
            throw new RuntimeException("List is not modifiable");
        }

        @Override
        public void replaceAll(UnaryOperator<T> operator) {
            throw new RuntimeException("List is not modifiable");
        }

        @Override
        public void sort(Comparator<? super T> c) {}

        @Override
        public boolean containsAll(@NotNull Collection<?> c) {
            for (Object o : c) {
                if (o != value) return false;
            }
            return true;
        }

        @Override
        public String toString() {
            return "DuplicateList[" + value + "*" + size + "]";
        }

        @Override
        public List<T> reversed() {
            return this;
        }

        @Override
        public <T1> T1[] toArray(@NotNull IntFunction<T1[]> generator) {
            return (T1[]) this.toArray();
        }

        @Override
        public @NotNull Stream<T> stream() {
            return (Stream<T>) Stream.of(toArray());
        }

        @Override
        public @NotNull Stream<T> parallelStream() {
            return (Stream<T>) Stream.of(toArray()).parallel();
        }
    }
}
