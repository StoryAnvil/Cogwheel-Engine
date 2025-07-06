/*
 * StoryAnvil CogWheel Engine
 * Copyright (C) 2025 StoryAnvil
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.storyanvil.cogwheel.util;

import net.minecraft.nbt.CompoundTag;

public class TagUtils {
    public static int I(CompoundTag tag, String key) throws RuntimeException {
        if (!tag.contains(key)) throw new RuntimeException("No integer tag with key " + key);
        return tag.getInt(key);
    }
    public static String S(CompoundTag tag, String key) throws RuntimeException {
        if (!tag.contains(key)) throw new RuntimeException("No string tag with key " + key);
        return tag.getString(key);
    }

    public static int I(String[] data, int id) throws RuntimeException {
        if (id > data.length) throw new RuntimeException("No integer tag with id " + id);
        return Integer.parseInt(data[id]);
    }
    public static String S(String[] data, int id) throws RuntimeException {
        if (id > data.length) throw new RuntimeException("No string tag with id " + id);
        return data[id];
    }
}
