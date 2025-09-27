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

package com.storyanvil.cogwheel.infrastructure.cog;

import com.storyanvil.cogwheel.infrastructure.CGPM;
import com.storyanvil.cogwheel.infrastructure.script.DispatchedScript;
import com.storyanvil.cogwheel.util.Bi;

public interface ForEachManager extends CGPM {
    /**
     * @return new object that will be used by your ForEachManager to track loop progress
     */
    Object createForEach(DispatchedScript script);

    /**
     * @param track ForEachManager specified object for tracking loop progress, see {@link ForEachManager#createForEach(DispatchedScript)}
     * @return element of for each loop and new track object. Return NULL to break the loop
     */
    Bi<CGPM, Object> getForEach(Object track);
}
