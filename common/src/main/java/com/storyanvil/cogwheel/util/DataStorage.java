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

import com.storyanvil.cogwheel.CogwheelHooks;
import com.storyanvil.cogwheel.api.Api;
import net.minecraft.entity.Entity;

@Api.Stable(since = "2.0.0")
public class DataStorage {
    @Api.Stable(since = "2.0.0")
    public static void setInt(Entity e, String key, int value) {
        CogwheelHooks.putInt(e, "flake_" + key, value);
    }

    @Api.Stable(since = "2.0.0")
    public static void setString(Entity e, String key, String value) {
        CogwheelHooks.putString(e, "flake_" + key, value);
    }

    @Api.Stable(since = "2.0.0")
    public static void setBoolean(Entity e, String key, boolean value) {
        CogwheelHooks.putBoolean(e, "flake_" + key, value);
    }

    @Api.Stable(since = "2.0.0")
    public static int getInt(Entity e, String _key, int defaultValue) {
        return CogwheelHooks.getInt(e, "flake_" + _key, defaultValue);
    }

    @Api.Stable(since = "2.0.0")
    public static String getString(Entity e, String _key, String defaultValue) {
        return CogwheelHooks.getString(e, "flake_" + _key, defaultValue);
    }

    @Api.Stable(since = "2.0.0")
    public static boolean getBoolean(Entity e, String _key, boolean defaultValue) {
        return CogwheelHooks.getBoolean(e, "flake_" + _key, defaultValue);
    }
}
