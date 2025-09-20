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

package com.storyanvil.cogwheel.config;

import com.google.gson.*;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonWriter;
import com.storyanvil.cogwheel.api.Api;
import net.minecraft.client.Minecraft;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class CogwheelClientConfig {
    private static final Logger log = LoggerFactory.getLogger("STORYANVIL/COGWHEEL/CLIENT");
    private static JsonObject json = null;

    private static boolean getBool(String name, boolean defauld) {
        JsonElement e = json.get(name);
        if (e == null || !e.isJsonPrimitive()) {
            json.addProperty(name, defauld);
            return defauld;
        }
        try {
            return e.getAsBoolean();
        } catch (UnsupportedOperationException | IllegalStateException ignored) {
            json.addProperty(name, defauld);
            return defauld;
        }
    }
    private static List<String> getStringArray(String name, List<String> defauld) {
        JsonElement e = json.get(name);
        if (e == null || !e.isJsonArray()) {
            JsonArray a = new JsonArray();
            for (String s : defauld) {
                a.add(new JsonPrimitive(s));
            }
            json.add(name, a);
            return defauld;
        }
        JsonArray array = e.getAsJsonArray();
        ArrayList<String> arrayList = new ArrayList<>();
        for (int i = 0; i < array.size(); i++) {
            arrayList.add(array.get(i).getAsString());
        }
        return arrayList;
    }

    private static boolean disableDevUI = false;
    private static boolean disableQuestUI = false;

    @Api.Stable(since = "2.10.2") @Api.MixinsNotAllowed(where = "CogwheelClientConfig#mixinsEntrypoint")
    public static synchronized void reload() {
        File config = new File(Minecraft.getInstance().gameDirectory, "config/cog/config-client.json");
        try (FileReader fr = new FileReader(config); Scanner sc = new Scanner(fr)) {
            StringBuilder buffer = new StringBuilder();
            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                buffer.append(line).append('\n');
            }
            json = JsonParser.parseString(buffer.toString()).getAsJsonObject();
        } catch (FileNotFoundException e) {
            log.error("[CFG] Failed to load config-client! Config files does not exist");
            try {
                if (!config.createNewFile()) throw new IOException("Failed to create config-client for unknown reasons");
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        } catch (IOException e) {
            log.error("[CFG] Failed to load config-client! Default values will be used!", e);
        } catch (IllegalStateException | JsonParseException e) {
            log.error("[CFG] Failed to load config-client! Invalid format! Default values will be used!", e);
        }
        if (json == null) {
            json = new JsonObject();
        }

        // Load settings
        // Client configs are still loaded on serverside but most of the options don't matter there
        json.addProperty("wiki", "https://storyanvil.github.io/wiki/wiki.html?p=wiki/projects/cogwheel/clientConfig");
        disableDevUI = getBool("disableDevUI", false);
        disableQuestUI = getBool("disableQuestUI", false);

        mixinsEntrypoint(json);

        try (FileWriter fw = new FileWriter(config);) {
            try {
                StringWriter stringWriter = new StringWriter();
                JsonWriter jsonWriter = new JsonWriter(stringWriter);
                jsonWriter.setLenient(true);
                jsonWriter.setIndent("    ");
                Streams.write(json, jsonWriter);
                fw.write(stringWriter.toString());
            } catch (IOException e) {
                throw new AssertionError(e);
            }
            fw.flush();
        } catch (IOException e) {
            log.error("[CFG] Failed to updated config-client!", e);
        }
        json = null;
        log.info("[CFG] Config reloaded!");
    }

    public static boolean isDisableDevUI() {
        return disableDevUI;
    }

    public static boolean isDisableQuestUI() {
        return disableQuestUI;
    }

    /**
     * Can be used to mixin into CogwheelConfigs. This allows mods to have their own config in config-main.json.
     * Should only be used for mods that expand Cogwheel Engine's functionality. Make sure to add some unique prefix
     * to your config entries to prevent name collisions. Please use @Inject(at = @At("HEAD"), cancellable = false) with this method
     */
    @Api.Stable(since = "2.10.2") @Api.MixinIntoHere
    public static void mixinsEntrypoint(JsonObject config) {}
}
