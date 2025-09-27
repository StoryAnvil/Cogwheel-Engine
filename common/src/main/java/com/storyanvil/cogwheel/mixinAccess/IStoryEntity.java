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

import net.minecraft.nbt.NbtCompound;

public interface IStoryEntity {
    void storyEntity$putInt(String k, int v);
    void storyEntity$putString(String k, String v);
    void storyEntity$putBoolean(String k, boolean v);
    int storyEntity$getInt(String k, int defaultV);
    String storyEntity$getString(String k, String defaultV);
    boolean storyEntity$getBoolean(String k, boolean defaultV);
    NbtCompound storyEntity$get();
    void storyEntity$set(NbtCompound c);
}
