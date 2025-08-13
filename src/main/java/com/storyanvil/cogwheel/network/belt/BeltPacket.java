/*
 *
 *  * StoryAnvil CogWheel Engine
 *  * Copyright (C) 2025 StoryAnvil
 *  *
 *  * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *  *
 *  * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 *  *
 *  * You should have received a copy of the GNU Lesser General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.storyanvil.cogwheel.network.belt;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class BeltPacket {
    public enum Type {
        AUTH_CODE(Handlers::none),
        RUN_COMMAND(Handlers::cmd),
        DISPATCH_SCRIPT(Handlers::dispatchScript),
        SCRIPT_MESSAGE(Handlers::scriptMessage),
        ;

        public final Consumer<BeltPacket> handler;

        @Contract(pure = true)
        Type(Consumer<BeltPacket> handler) {
            this.handler = handler;
        }
    }

    private final Type type;
    private final String[] data;

    @Contract(pure = true)
    private BeltPacket(Type type, String[] data) {
        this.type = type;
        this.data = data;
    }

    public Type getType() {
        return type;
    }

    public String[] getData() {
        return data;
    }

    public static void createAuthCode(String authCode, String playerName) {
        BeltPacket packet = new BeltPacket(Type.AUTH_CODE, new String[]{authCode, playerName});
        BeltCommunications.pushPacket(packet);
    }
    public static void createBeltMessage(String message) {
        BeltPacket packet = new BeltPacket(Type.SCRIPT_MESSAGE, new String[]{message});
        BeltCommunications.pushPacket(packet);
    }
    @Contract("_ -> new")
    public static @NotNull BeltPacket parse(@NotNull JsonObject o) {
        JsonArray a = o.get("data").getAsJsonArray();
        String[] array = new String[a.size()];
        for (int i = 0; i < array.length; i++) {
            array[i] = a.getAsString();
        }
        return new BeltPacket(Type.valueOf(o.get("type").getAsString()), array);
    }
    public JsonObject toJSON() {
        JsonObject obj = new JsonObject();
        JsonArray a = new JsonArray();
        obj.addProperty("type", type.toString());
        for (String s : data) {
            a.add(s);
        }
        obj.add("data", a);
        return obj;
    }
}
