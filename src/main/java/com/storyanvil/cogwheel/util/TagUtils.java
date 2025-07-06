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
