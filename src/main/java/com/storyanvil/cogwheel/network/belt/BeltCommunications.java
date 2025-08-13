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

import com.google.gson.*;
import com.storyanvil.cogwheel.CogwheelExecutor;
import com.storyanvil.cogwheel.EventBus;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.concurrent.CompletableFuture;

public class BeltCommunications {
    public static final Logger log = LoggerFactory.getLogger("STORYANVIL/COGWHEEL/BELT");

    private String host;
    private final HttpClient client;
    private String remoteServerName;
    private String token = null;

    public static BeltCommunications create(String host) {
        try {
            URI url = new URI(host);
            BeltCommunications com = new BeltCommunications();
            if (!url.getScheme().equals("belt") && !url.getScheme().equals("sbelt"))
                throw new RuntimeException("Failed to connect to Belt Protocol host: Invalid protocol scheme");
            com.host = (url.getScheme().startsWith("s") ? "https://" : "http://") + url.getAuthority();
            CogwheelExecutor.scheduleBelt(com::connect);
        } catch (URISyntaxException e) {
            throw new RuntimeException("Failed to connect to Belt Protocol host: Invalid host", e);
        }
        return null;
    }

    public BeltCommunications() {
        client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    private CompletableFuture<HttpResponse<String>> httpGet(String path) {
        HttpRequest.Builder request = HttpRequest.newBuilder()
                .uri(URI.create(host + path))
                .GET()
                .header("User-Agent", "Cogwheel-Engine")
                .header("X-StoryAnvil", "Cogwheel-Engine")
                .timeout(Duration.ofSeconds(10));
        if (token != null) {
            request.header("X-StoryAnvil-Secret", token);
        }
        return client.sendAsync(request.build(), HttpResponse.BodyHandlers.ofString());
    }
    private CompletableFuture<HttpResponse<String>> httpPost(String path, String body) {
        HttpRequest.Builder request = HttpRequest.newBuilder()
                .uri(URI.create(host + path))
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .header("User-Agent", "Cogwheel-Engine")
                .header("X-StoryAnvil", "Cogwheel-Engine")
                .timeout(Duration.ofSeconds(10));
        if (token != null) {
            request.header("X-StoryAnvil-Secret", token);
        }
        return client.sendAsync(request.build(), HttpResponse.BodyHandlers.ofString());
    }

    public void connect() {
        httpGet("/~belt").thenAccept(response -> {
            if (response.statusCode() != 200) {
                log.error("BELT HANDSHAKE FAILED! Invalid status code: {}", response.statusCode());
                dispose();
                return;
            }
            try {
                JsonObject handshake = JsonParser.parseString(response.body()).getAsJsonObject();
                if (!handshake.get("successful").getAsBoolean()) {
                    if (handshake.has("message"))
                        log.error("BELT HANDSHAKE FAILED! Server sent following message: {}", handshake.get("message").getAsString());
                    else
                        log.error("BELT HANDSHAKE FAILED! Server did not sent any explanation message");
                    dispose();
                    return;
                }
                remoteServerName = handshake.get("server").getAsString();
                token = handshake.get("token").getAsString();
                if (handshake.has("message"))
                    log.warn("BELT HANDSHAKE! \"{}\" Server connected and sent following message: {}", remoteServerName, handshake.get("message").getAsString());
                else
                    log.warn("BELT HANDSHAKE! \"{}\" Server connected and did not sent any additional message", remoteServerName);
                CogwheelExecutor.scheduleBelt(this::produceUpdateCheck, 250);
            } catch (JsonSyntaxException | IllegalStateException e) {
                log.error("BELT HANDSHAKE FAILED! Invalid response");
                throw new RuntimeException("BELT HANDSHAKE FAILED! Invalid response", e);
            }
        });
    }

    public @NotNull String getRemoteServerName() {
        if (remoteServerName == null) return "Unknown Belt Server";
        return remoteServerName;
    }

    public String getHost() {
        return host;
    }

    public String getUserLink(ServerPlayer player) {
        return host + "/~auth/" + Base64.getUrlEncoder().encodeToString(player.getScoreboardName().getBytes(StandardCharsets.UTF_8));
    }

    private final ArrayList<BeltPacket> packets = new ArrayList<>();
    public static final Object SYNC = new Object();
    private String generateUpdateBody() {
        synchronized (SYNC) {
            JsonObject jo = new JsonObject();
            JsonArray array = new JsonArray();
            for (BeltPacket packet : packets) {
                array.add(packet.toJSON());
            }
            jo.add("list", array);
            return jo.toString();
        }
    }

    private void produceUpdateCheck() {
        httpPost("/~belt/update", generateUpdateBody()).thenAccept(response -> {
            if (response.statusCode() == 200) {
                EventBus.beltCommunications = this;
                JsonObject obj = JsonParser.parseString(response.body()).getAsJsonObject();
                JsonArray list = obj.get("list").getAsJsonArray();
                for (int i = 0; i < list.size(); i++) {
                    BeltPacket packet = BeltPacket.parse(list.get(i).getAsJsonObject());
                    packet.getType().handler.accept(packet);
                }
                CogwheelExecutor.scheduleBelt(this::produceUpdateCheck, 250);
            } else {
                log.error("Update check failed. Connection will be closed");
                dispose();
            }
        });
    }

    private static void dispose() {
        EventBus.beltCommunications = null;
    }

    public static void pushPacket(BeltPacket packet) {
        if (EventBus.beltCommunications != null) {
            synchronized (SYNC) {
                EventBus.beltCommunications.packets.add(packet);
            }
        }
    }
}
