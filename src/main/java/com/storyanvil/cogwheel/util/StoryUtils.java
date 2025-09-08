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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.storyanvil.cogwheel.api.Api;
import net.minecraft.nbt.*;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class StoryUtils {
    @Api.Stable(since = "2.0.0")
    public static void sendGlobalMessage(@NotNull ServerLevel level, Component msg) {
        for (ServerPlayer player : level.players()) {
            player.sendSystemMessage(msg);
        }
    }

    @Api.Stable(since = "2.0.0")
    public static void sendGlobalMessage(@NotNull ServerLevel level, Component... msg) {
        for (ServerPlayer player : level.players()) {
            for (Component c : msg) {
                player.sendSystemMessage(c);
            }
        }
    }

    @Api.Stable(since = "2.0.0")
    public static void encodeString(@NotNull FriendlyByteBuf buf, @Nullable String str) {
        if (str == null) {
            str = "";
        }
        byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
        buf.writeInt(bytes.length);
        buf.writeBytes(bytes);
    }

    @Contract("_ -> new") @Api.Stable(since = "2.0.0")
    public static @NotNull String decodeString(@NotNull FriendlyByteBuf buf) {
        int length = buf.readInt();
        byte[] bytes = new byte[length];
        for (int i = 0; i < length; i++) {
            bytes[i] = buf.readByte();
        }
        return new String(bytes, StandardCharsets.UTF_8);
    }

    @Api.Stable(since = "2.0.0")
    public static void deleteDirectory(@NotNull File dir) {
        if (!dir.exists()) return;
        if (dir.isDirectory()) {
            File[] childFiles = dir.listFiles();
            if (childFiles != null) {
                for (File f : childFiles) {
                    deleteDirectory(f);
                }
            }
        }
        if (!dir.delete()) throw new RuntimeException("Failed to delete: " + dir);
    }

    @Api.Experimental(since = "2.0.0")
    public static void unpackZip(File zip, File directory) throws IOException {
        byte[] buffer = new byte[1024];
        ZipInputStream zis = new ZipInputStream(new FileInputStream(zip));
        ZipEntry zipEntry = zis.getNextEntry();
        while (zipEntry != null) {
            File newFile = newFile(directory, zipEntry);
            if (zipEntry.isDirectory()) {
                if (!newFile.isDirectory() && !newFile.mkdirs()) {
                    throw new IOException("Failed to create directory " + newFile);
                }
            } else {
                // fix for Windows-created archives
                File parent = newFile.getParentFile();
                if (!parent.isDirectory() && !parent.mkdirs()) {
                    throw new IOException("Failed to create directory " + parent);
                }

                // write file content
                FileOutputStream fos = new FileOutputStream(newFile);
                int len;
                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }
                fos.close();
            }
            zipEntry = zis.getNextEntry();
        }

        zis.closeEntry();
        zis.close();
    }
    private static @NotNull File newFile(File destinationDir, @NotNull ZipEntry zipEntry) throws IOException {
        File destFile = new File(destinationDir, zipEntry.getName());

        String destDirPath = destinationDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();

        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
        }

        return destFile;
    }

    @Api.Stable(since = "2.6.0")
    public static boolean isHovering(int mouseX, int mouseY, int left, int right, int top, int bottom) {
        return mouseX >= left && mouseX <= right && mouseY >= top && mouseY <= bottom;
    }

    @Api.Stable(since = "2.9.0")
    public static MutableComponent subComponent(MutableComponent source, int from, int to) {
        MutableComponent c = Component.empty();
        int l = 0;
        AtomicInteger skip = new AtomicInteger(from);
        AtomicInteger left = new AtomicInteger(to - from - 1);

        source.visit((style, content) -> {
            int s = content.length();
            if (skip.get() > 0) {
                if (skip.get() >= s) {
                    skip.addAndGet(-s);
                } else {
                    c.append(Component.literal(content.substring(skip.get())).withStyle(style));
                    skip.set(0);
                }
                return Optional.empty();
            }
            if (left.get() > 0) {
                int lef = left.get();
                if (lef > s) {
                    c.append(Component.literal(content).withStyle(style));
                    left.addAndGet(-s);
                } else {
                    c.append(Component.literal(content.substring(0, lef)).withStyle(style));
                    left.set(0);
                }
            }
            return Optional.empty();
        }, Style.EMPTY);
        return c;
    }

    @Api.Stable(since = "2.10.1")
    public static JsonObject toCompoundJSON(CompoundTag tag) {
        JsonObject root = new JsonObject();
        for (String key : tag.getAllKeys()) {
            Tag t = tag.get(key);
            JsonElement e = toElement(t);
            if (e != null)
                root.add(key, e);
        }
        return root;
    }
    @Api.Stable(since = "2.10.1")
    public static JsonArray toCompoundJSON(CollectionTag<?> tag) {
        JsonArray root = new JsonArray();
        for (int i = 0; i < root.size(); i++) {
            JsonElement e = toElement(tag.get(i));
            if (e != null)
                root.add(e);
        }
        return root;
    }

    @Api.Stable(since = "2.10.1")
    public static JsonElement toElement(Tag t) {
        if (t instanceof NumericTag) {
            if (t instanceof ShortTag T) return toJSON(T);
            if (t instanceof DoubleTag T) return toJSON(T);
            if (t instanceof FloatTag T) return toJSON(T);
            if (t instanceof ByteTag T) return toJSON(T);
            if (t instanceof IntTag T) return toJSON(T);
            if (t instanceof LongTag T) return toJSON(T);
        } else if (t instanceof CompoundTag T) {
            return toCompoundJSON(T);
        } else if (t instanceof CollectionTag<?> T) {
            return toCompoundJSON(T);
        } else if (t instanceof StringTag T) {
            return toJSON(T);
        }
        return null;
    }

    @Api.Stable(since = "2.10.1")
    public static JsonPrimitive toJSON(StringTag tag) {
        return new JsonPrimitive(tag.getAsString());
    }

    @Api.Stable(since = "2.10.1")
    public static JsonPrimitive toJSON(ShortTag tag) {
        return new JsonPrimitive(tag.getAsShort());
    }

    @Api.Stable(since = "2.10.1")
    public static JsonPrimitive toJSON(DoubleTag tag) {
        return new JsonPrimitive(tag.getAsDouble());
    }

    @Api.Stable(since = "2.10.1")
    public static JsonPrimitive toJSON(FloatTag tag) {
        return new JsonPrimitive(tag.getAsFloat());
    }

    @Api.Stable(since = "2.10.1")
    public static JsonPrimitive toJSON(ByteTag tag) {
        return new JsonPrimitive(tag.getAsByte());
    }

    @Api.Stable(since = "2.10.1")
    public static JsonPrimitive toJSON(IntTag tag) {
        return new JsonPrimitive(tag.getAsInt());
    }

    @Api.Stable(since = "2.10.1")
    public static JsonPrimitive toJSON(LongTag tag) {
        return new JsonPrimitive(tag.getAsLong());
    }
}
