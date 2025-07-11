/*
 * StoryAnvil CogWheel Engine
 * Copyright (C) 2025 StoryAnvil
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.storyanvil.cogwheel.util;

import net.minecraft.world.entity.Entity;

public class DataStorage {
    public static void setInt(Entity e, String key, int value) {
        e.getPersistentData().putInt("flake_" + key, value);
    }
    public static void setString(Entity e, String key, String value) {
        e.getPersistentData().putString("flake_" + key, value);
    }
    public static void setBoolean(Entity e, String key, boolean value) {
        e.getPersistentData().putBoolean("flake_" + key, value);
    }
    public static int getInt(Entity e, String _key, int defaultValue) {
        String key = "flake_" + _key;
        return e.getPersistentData().contains(key) ? e.getPersistentData().getInt(key) : defaultValue;
    }
    public static String getString(Entity e, String _key, String defaultValue) {
        String key = "flake_" + _key;
        return e.getPersistentData().contains(key) ? e.getPersistentData().getString(key) : defaultValue;
    }
    public static boolean getBoolean(Entity e, String _key, boolean defaultValue) {
        String key = "flake_" + _key;
        return e.getPersistentData().contains(key) ? e.getPersistentData().getBoolean(key) : defaultValue;
    }
}
