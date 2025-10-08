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

package com.storyanvil.cogwheel.infrastructure.props;

/**
 * <h4>(J)ava (W)rapping (C)o(G)(P)roperty (M)anager</h4>
 * <br>
 * This class is used to create {@link CGPM}s using java methods.
 */
@JWCGPM_Class
public abstract class JWCGPM<T extends JWCGPM<T>> implements CGPM, ImplJWCGPM<T> {
    protected final Class<T> clazz;
    public JWCGPM(Class<T> clazz) {
        this.clazz = clazz;
    }

    public JWCGPM() {
        //noinspection unchecked
        this.clazz = (Class<T>) getClass();
    }

    @Override
    public Class<?> getThisClass() {
        return clazz;
    }

    @Override
    public boolean equalsTo(CGPM o) {
        return o.equals(this);
    }
}
