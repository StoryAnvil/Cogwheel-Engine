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
