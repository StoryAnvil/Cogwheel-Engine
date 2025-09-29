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

package com.storyanvil.cogwheel.config;

import com.google.gson.*;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonWriter;
import com.storyanvil.cogwheel.CogwheelHooks;
import com.storyanvil.cogwheel.api.Api;
import com.storyanvil.cogwheel.network.devui.DevEarlySyncPacket;
import com.storyanvil.cogwheel.registry.CogwheelRegistries;
import com.storyanvil.cogwheel.util.Bi;
import com.storyanvil.cogwheel.util.ScriptLineHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.io.*;
import java.util.*;

import static com.storyanvil.cogwheel.CogwheelExecutor.log;

public class CogwheelConfig {
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

    private static boolean disableAllScripts = false;
    private static boolean disableBelt = false;
    private static boolean npcTalkingAnimation = true;
    private static boolean monitorEnabled = false;
    private static boolean devEnvironment = true;

    @Api.Stable(since = "2.8.0") @Api.MixinsNotAllowed(where = "CogwheelConfig#mixinsEntrypoint")
    public static synchronized void reload() {
        File config = new File(CogwheelHooks.getConfigFolder(), "cog/config-main.json");
        try (FileReader fr = new FileReader(config); Scanner sc = new Scanner(fr)) {
            StringBuilder buffer = new StringBuilder();
            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                buffer.append(line).append('\n');
            }
            json = JsonParser.parseString(buffer.toString()).getAsJsonObject();
        } catch (FileNotFoundException e) {
            log.error("[CFG] Failed to load config-main! Config files does not exist");
            try {
                if (!config.createNewFile()) throw new IOException("Failed to create config-main for unknown reasons");
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        } catch (IOException e) {
            log.error("[CFG] Failed to load config-main! Default values will be used!", e);
        } catch (IllegalStateException | JsonParseException e) {
            log.error("[CFG] Failed to load config-main! Invalid format! Default values will be used!", e);
        }
        if (json == null) {
            json = new JsonObject();
        }

        // Load settings
        json.addProperty("wiki", "https://storyanvil.github.io/wiki/wiki.html?p=wiki/projects/cogwheel/config");
        disableAllScripts = getBool("disableAllScripts", false);
        disableBelt = getBool("disableBelt", false);
        npcTalkingAnimation = getBool("npcTalkingAnimation", true);
        monitorEnabled = getBool("monitorEnabled", false);
        devEnvironment = getBool("devEnvironment", true);
        List<String> disabledLineHandlers = getStringArray("disableLineHandles", List.of());
        HashSet<Identifier> disabledLH = new HashSet<>();
        for (String lh : disabledLineHandlers) {
            Identifier rl = Identifier.tryParse(lh);
            disabledLH.add(rl);
        }
        for (Bi<ScriptLineHandler, Boolean> lh : CogwheelRegistries.getLineHandlers()) {
            lh.setB(!disabledLH.contains(lh.getA().getIdentifier()));
        }

        mixinsEntrypoint(json);

        try (FileWriter fw = new FileWriter(config);) {
            try {
                StringWriter stringWriter = new StringWriter();
                JsonWriter jsonWriter = new JsonWriter(stringWriter);
                jsonWriter.setStrictness(Strictness.LENIENT);
                jsonWriter.setIndent("    ");
                Streams.write(json, jsonWriter);
                fw.write(stringWriter.toString());
            } catch (IOException e) {
                throw new AssertionError(e);
            }
            fw.flush();
        } catch (IOException e) {
            log.error("[CFG] Failed to updated config-main!", e);
        }
        json = null;
        if (CogwheelHooks.getServer() != null) {
            for (ServerPlayerEntity plr : CogwheelHooks.getOverworldServer().getServer().getPlayerManager().getPlayerList()) {
                DevEarlySyncPacket.syncFor(plr, true);
            }
        }
        log.info("[CFG] Config reloaded!");
    }

    @Api.Stable(since = "2.8.0")
    public static boolean isDisablingAllScripts() {
        return disableAllScripts;
    }

    @Api.Stable(since = "2.8.0")
    public static boolean isDisablingBelt() {
        return disableBelt;
    }

    @Api.Stable(since = "2.8.0")
    public static boolean isNpcTalkingAnimationEnabled() {
        return npcTalkingAnimation;
    }

    @Api.Stable(since = "2.8.0")
    public static boolean isMonitorDisabled() {
        return !monitorEnabled;
    }

    /**
     * @return true if script debugging features should be active
     */
    @Api.Stable(since = "2.8.0")
    public static boolean isDevEnvironment() {
        return devEnvironment;
    }

    /**
     * Can be used to mixin into CogwheelConfigs. This allows mods to have their own config in config-main.json.
     * Should only be used for mods that expand Cogwheel Engine's functionality. Make sure to add some unique prefix
     * to your config entries to prevent name collisions. Please use @Inject(at = @At("HEAD"), cancellable = false) with this method
     */
    @Api.Stable(since = "2.8.0") @Api.MixinIntoHere
    public static void mixinsEntrypoint(JsonObject config) {}
}
