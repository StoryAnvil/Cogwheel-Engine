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

import java.util.Stack;

public class UnclearableStack<T> extends Stack<T> {
    public UnclearableStack(T initialElement) {
        super();
        push(initialElement);
    }

    @Override
    public T push(T item) {
        return super.push(item);
    }

    /**
     * Removes and returns top element of stack. If this stack has only one element it is not removed.
     */
    @Override
    public synchronized T pop() {
        if (super.size() == 1) return peek();
        return super.pop();
    }

    @Override
    public synchronized T peek() {
        return super.peek();
    }

    /**
     * @return <code>TRUE</code> only if stack contains 1 element. Unclearable stack can not contain 0 elements.
     */
    @Override
    public boolean empty() {
        return super.size() == 1;
    }

    @Override
    public synchronized int search(Object o) {
        return super.search(o);
    }
}
