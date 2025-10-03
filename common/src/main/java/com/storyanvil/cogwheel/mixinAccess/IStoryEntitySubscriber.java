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

package com.storyanvil.cogwheel.mixinAccess;

public interface IStoryEntitySubscriber {
    /**
     * Makes this entity update all its values to new ones.<br>
     * To get the values use {@link IStoryEntity#storyEntity$getInt(String, int)}, {@link IStoryEntity#storyEntity$getString(String, String)}, {@link IStoryEntity#storyEntity$getBoolean(String, boolean)} on yourself<br>
     * Remember to always call super implementation of this first!
     */
    default void storyEntity$accept() {};
}
