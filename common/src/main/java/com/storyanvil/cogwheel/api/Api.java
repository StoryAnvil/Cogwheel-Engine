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

package com.storyanvil.cogwheel.api;

import org.jetbrains.annotations.ApiStatus;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public final class Api {
    private Api() {throw new UnsupportedOperationException("Api should not be initialized"); }

    /**
     * This annotation is used to mark Stable API methods. This methods will last at least util next major release but probably more
     */
    @Documented @Retention(RetentionPolicy.CLASS)
    public @interface Stable {
        String since() default "UNSPECIFIED";
    }

    /**
     * This annotation is used to mark Unstable API methods. These methods will probably be supported for multiple releases. This API methods can be used for creating addons pretty safely
     */
    @Documented @Retention(RetentionPolicy.CLASS)
    public @interface Experimental {
        String since() default "UNSPECIFIED";
    }

    /**
     * This annotation is used to mark Internal methods. This methods must not be used by Cogwheel Engine addons at all costs because these methods can change any time or even break things inside of Cogwheel Engine.
     */
    @Documented @Retention(RetentionPolicy.CLASS) @ApiStatus.Internal
    public @interface Internal {}

    /**
     * This annotation is used to mark methods where mixins are welcomed.
     */
    @Documented @Retention(RetentionPolicy.CLASS)
    public @interface MixinIntoHere {}

    /**
     * This annotation is used to mark methods that should only be called by platform-specific implementations.
     */
    @Documented @Retention(RetentionPolicy.CLASS)
    public @interface PlatformTool {}

    /**
     * This annotation is used to mark methods where mixins are not allowed. See {@link MixinsNotAllowed#where()} to know where you can mixin to change this method
     */
    @Documented @Retention(RetentionPolicy.CLASS)
    public @interface MixinsNotAllowed {
        String where() default "UNSPECIFIED";
    }
}
