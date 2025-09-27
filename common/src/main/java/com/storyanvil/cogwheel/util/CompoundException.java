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

public class CompoundException extends RuntimeException {
    public CompoundException() {
        super("Multiple exceptions got thrown! See suppressed exceptions for more info", null, true, false);
    }
    public CompoundException(Throwable t) {
        this();
        addSuppressed(t);
    }

    /**
     * Adds throwable if not added yet
     */
    public void addUniqueException(Throwable t) {
        for (Throwable th : getSuppressed()) {
            if (th == t) return;
        }
        addSuppressed(t);
    }
}
