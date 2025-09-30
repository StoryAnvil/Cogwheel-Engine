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

import com.storyanvil.cogwheel.infrastructure.props.CGPM;

import java.lang.annotation.*;

/**
 * This annotation is used to mark {@link CGPM} subtypes that
 * do not require in-jar docs
 */
@Retention(RetentionPolicy.RUNTIME) @Documented @Target(ElementType.TYPE)
public @interface TestIgnoreDocs {
}
