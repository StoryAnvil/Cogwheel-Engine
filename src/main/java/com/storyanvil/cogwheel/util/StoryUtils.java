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

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;

public class StoryUtils {
    public static void sendGlobalMessage(ServerLevel level, Component msg) {
        for (ServerPlayer player : level.players()) {
            player.sendSystemMessage(msg);
        }
    }
    public static void sendGlobalMessage(ServerLevel level, Component... msg) {
        for (ServerPlayer player : level.players()) {
            for (Component c : msg) {
                player.sendSystemMessage(c);
            }
        }
    }
    public static void encodeString(@NotNull FriendlyByteBuf buf, @NotNull String str) {
        byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
        buf.writeInt(bytes.length);
        buf.writeBytes(bytes);
    }
    public static  @NotNull String decodeString(@NotNull FriendlyByteBuf buf) {
        int length = buf.readInt();
        byte[] bytes = new byte[length];
        for (int i = 0; i < length; i++) {
            bytes[i] = buf.readByte();
        }
        return new String(bytes, StandardCharsets.UTF_8);
    }
}
