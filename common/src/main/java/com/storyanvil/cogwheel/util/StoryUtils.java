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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.storyanvil.cogwheel.api.Api;
import net.minecraft.nbt.*;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static com.storyanvil.cogwheel.util.CogwheelExecutor.log;

public class StoryUtils {
    public static void requireCogwheelThread() {
        if (!Thread.currentThread().getThreadGroup().parentOf(CogwheelExecutor.executorGroup)) {
            RuntimeException e = new RuntimeException("Only CogwheelExecutor threads are allowed!");
            log.error("[!CRITICAL!] CODE EXECUTION WAS DISMISSED!", e);
            throw e;
        }
    }

    @Api.Stable(since = "2.0.0") @Deprecated(forRemoval = true)
    public static void sendGlobalMessage(@NotNull ServerWorld level, Text... msg) {
        for (ServerPlayerEntity player : level.getPlayers()) {
            for (Text c : msg) {
                player.sendMessage(c);
            }
        }
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

    public static void discoverDirectory(@NotNull File file, @NotNull BiConsumer<File, String> consumer) {
        discoverDirectory(file, consumer, "");
    }

    public static void discoverDirectory(@NotNull File file, @NotNull BiConsumer<File, String> consumer, @NotNull String path) {
        path = path + "/" + file.getName();
        if (!file.isDirectory()) {
            consumer.accept(file, path);
            return;
        }
        File[] files = file.listFiles();
        if (files == null) return;
        for (File f : files) {
            discoverDirectory(f, consumer, path);
        }
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
    public static MutableText subText(MutableText source, int from, int to) {
        MutableText c = Text.empty();
        int l = 0;
        AtomicInteger skip = new AtomicInteger(from);
        AtomicInteger left = new AtomicInteger(to - from - 1);

        source.visit((style, content) -> {
            int s = content.length();
            if (skip.get() > 0) {
                if (skip.get() >= s) {
                    skip.addAndGet(-s);
                } else {
                    c.append(Text.literal(content.substring(skip.get())).fillStyle(style));
                    skip.set(0);
                }
                return Optional.empty();
            }
            if (left.get() > 0) {
                int lef = left.get();
                if (lef > s) {
                    c.append(Text.literal(content).fillStyle(style));
                    left.addAndGet(-s);
                } else {
                    c.append(Text.literal(content.substring(0, lef)).fillStyle(style));
                    left.set(0);
                }
            }
            return Optional.empty();
        }, net.minecraft.text.Style.EMPTY);
        return c;
    }

    @Api.Stable(since = "2.10.1")
    public static JsonObject toCompoundJSON(NbtCompound tag) {
        JsonObject root = new JsonObject();
        for (String key : tag.getKeys()) {
            NbtElement t = tag.get(key);
            JsonElement e = toElement(t);
            if (e != null)
                root.add(key, e);
        }
        return root;
    }
    @Api.Stable(since = "2.10.1")
    public static JsonArray toCompoundJSON(AbstractNbtList<?> tag) {
        JsonArray root = new JsonArray();
        for (int i = 0; i < root.size(); i++) {
            JsonElement e = toElement(tag.get(i));
            if (e != null)
                root.add(e);
        }
        return root;
    }

    @Api.Stable(since = "2.10.1")
    public static JsonElement toElement(NbtElement t) {
        if (t instanceof AbstractNbtNumber) {
            switch (t) {
                case NbtShort T:
                    return toJSON(T);
                case NbtDouble T:
                    return toJSON(T);
                case NbtFloat T:
                    return toJSON(T);
                case NbtByte T:
                    return toJSON(T);
                case NbtInt T:
                    return toJSON(T);
                case NbtLong T:
                    return toJSON(T);
                default:
                    break;
            }
        }
        if (t instanceof NbtCompound T) {
            return toCompoundJSON(T);
        }
        if (t instanceof AbstractNbtList<?> T) {
            return toCompoundJSON(T);
        }
        if (t instanceof NbtString T) {
            return toJSON(T);
        }
        return null;
    }

    @Api.Stable(since = "2.10.1")
    public static JsonPrimitive toJSON(NbtString tag) {
        return new JsonPrimitive(tag.asString());
    }

    @Api.Stable(since = "2.10.1")
    public static JsonPrimitive toJSON(NbtShort tag) {
        return new JsonPrimitive(tag.shortValue());
    }

    @Api.Stable(since = "2.10.1")
    public static JsonPrimitive toJSON(NbtDouble tag) {
        return new JsonPrimitive(tag.doubleValue());
    }

    @Api.Stable(since = "2.10.1")
    public static JsonPrimitive toJSON(NbtFloat tag) {
        return new JsonPrimitive(tag.floatValue());
    }

    @Api.Stable(since = "2.10.1")
    public static JsonPrimitive toJSON(NbtByte tag) {
        return new JsonPrimitive(tag.byteValue());
    }

    @Api.Stable(since = "2.10.1")
    public static JsonPrimitive toJSON(NbtInt tag) {
        return new JsonPrimitive(tag.intValue());
    }

    @Api.Stable(since = "2.10.1")
    public static JsonPrimitive toJSON(NbtLong tag) {
        return new JsonPrimitive(tag.longValue());
    }

    @Api.Stable(since = "2.0.0")
    public static void encodeString(@NotNull net.minecraft.network.PacketByteBuf buf, @Nullable String str) {
        if (str == null) {
            str = "";
        }
        byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
        buf.writeInt(bytes.length);
        buf.writeBytes(bytes);
    }

    @Contract("_ -> new") @Api.Stable(since = "2.0.0")
    public static @NotNull String decodeString(@NotNull PacketByteBuf buf) {
        int length = buf.readInt();
        byte[] bytes = new byte[length];
        for (int i = 0; i < length; i++) {
            bytes[i] = buf.readByte();
        }
        return new String(bytes, StandardCharsets.UTF_8);
    }

    public static JsonArray arrayWith(JsonElement e) {
        JsonArray array = new JsonArray(1);
        array.add(e);
        return array;
    }
    public static JsonArray arrayWith(String e) {
        return arrayWith(new JsonPrimitive(e));
    }
    public static JsonArray arrayWith(int e) {
        return arrayWith(new JsonPrimitive(e));
    }
}
