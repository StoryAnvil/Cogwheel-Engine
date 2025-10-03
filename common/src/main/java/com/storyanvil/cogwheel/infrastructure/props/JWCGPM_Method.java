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

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.*;

/**
 * This annotation is used to mark methods that are executed by {@link JWCGPM}
 */
@Target(ElementType.METHOD) @Documented @Retention(RetentionPolicy.RUNTIME)
public @interface JWCGPM_Method {
    @NotNull Class<? extends CGPM>[] arguments() default {};
}
