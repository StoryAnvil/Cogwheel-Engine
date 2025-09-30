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

package com.storyanvil.cogwheel;

import com.google.gson.*;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.storyanvil.cogwheel.client.devui.PacketParcel;
import com.storyanvil.cogwheel.config.CogwheelConfig;
import com.storyanvil.cogwheel.data.StoryCodec;
import com.storyanvil.cogwheel.data.StoryPacket;
import com.storyanvil.cogwheel.infrastructure.props.CGPM;
import com.storyanvil.cogwheel.infrastructure.cog.CogPrimalType;
import com.storyanvil.cogwheel.infrastructure.env.CogScriptEnvironment;
import com.storyanvil.cogwheel.infrastructure.testing.TestManagement;
import com.storyanvil.cogwheel.mixinAccess.IStoryEntity;
import com.storyanvil.cogwheel.network.devui.*;
import com.storyanvil.cogwheel.network.mc.*;
import com.storyanvil.cogwheel.registry.CogwheelRegistries;
import com.storyanvil.cogwheel.registry.PlatformRegistry;
import com.storyanvil.cogwheel.util.DefaultCommandOutput;
import com.storyanvil.cogwheel.util.PlatformType;
import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Class for multiloader methods
 */
@ApiStatus.Internal
public class CogwheelHooks {
    @ExpectPlatform
    public static File getConfigFolder() {
        throw new AssertionError();
    }

    public static void putInt(Entity e, String s, int value) {
        ((IStoryEntity) e).storyEntity$putInt(s, value);
    }

    public static void putString(Entity e, String s, String value) {
        ((IStoryEntity) e).storyEntity$putString(s, value);
    }

    public static void putBoolean(Entity e, String s, boolean value) {
        ((IStoryEntity) e).storyEntity$putBoolean(s, value);
    }

    public static int getInt(Entity e, String s, int defaultValue) {
        return ((IStoryEntity) e).storyEntity$getInt(s, defaultValue);
    }

    public static String getString(Entity e, String s, String defaultValue) {
        return ((IStoryEntity) e).storyEntity$getString(s, defaultValue);
    }

    public static boolean getBoolean(Entity e, String s, boolean defaultValue) {
        return ((IStoryEntity) e).storyEntity$getBoolean(s, defaultValue);
    }

    @ExpectPlatform
    public static byte performVersionCheck() {
        throw new AssertionError();
    }

    public static void executeCommand(String command) {
        // Schedules server tick task that executed provided chat command
        CogwheelExecutor.scheduleTickEvent(world -> {
            world.getServer().getCommandManager().executeWithPrefix(new ServerCommandSource(new DefaultCommandOutput(), new Vec3d(0, 0, 0), new Vec2f(0, 0), world, 2, "CogwheelEngine", Text.literal("CogwheelEngine"), world.getServer(), null), command);
        });
    }

    @ExpectPlatform
    public static CGPM getLevelData(String key) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static void putLevelData(String key, CogPrimalType value) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static void sendPacket(StoryPacket<?> packet, ServerPlayerEntity plr) {
        throw new AssertionError();
    }

    public static void sendPacket(ServerPlayerEntity plr, StoryPacket<?> packet) {
        sendPacket(packet, plr);
    }

    @ExpectPlatform
    public static void sendPacketToEveryone(StoryPacket<?> packet) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static void sendPacketToServer(StoryPacket<?> packet) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static void setAnimationData(Identifier[] locations) {
        throw new AssertionError();
    }

    public static ServerWorld getOverworldServer() {
        return MinecraftClient.getInstance().getServer().getOverworld();
    }

    public static MinecraftServer getServer() {
        return MinecraftClient.getInstance().getServer();
    }

    public static void serverStart() {
        CogwheelExecutor.serverStart();
    }

    public static void serverStop() {
        CogwheelExecutor.serverStop();
    }

    public static void packetRegistry(PacketRegistrar registrar) {
        registrar.accept("DevConsoleCode", DevConsoleCode.CODEC, DevConsoleCode.class);
        registrar.accept("DevDeleteLine", DevDeleteLine.CODEC, DevDeleteLine.class);
        registrar.accept("DevEarlySyncPacket", DevEarlySyncPacket.CODEC, DevEarlySyncPacket.class);
        registrar.accept("DevEditorLine", DevEditorLine.CODEC, DevEditorLine.class);
        registrar.accept("DevEditorState", DevEditorState.CODEC, DevEditorState.class);
        registrar.accept("DevEditorUserDelta", DevEditorUserDelta.CODEC, DevEditorUserDelta.class);
        registrar.accept("DevEnterCallback", DevEnterCallback.CODEC, DevEnterCallback.class);
        registrar.accept("DevFlush", DevFlush.CODEC, DevFlush.class);
        registrar.accept("DevInsertLine", DevInsertLine.CODEC, DevInsertLine.class);
        registrar.accept("DevOpenFile", DevOpenFile.CODEC, DevOpenFile.class);
        registrar.accept("DevOpenViewer", DevOpenViewer.CODEC, DevOpenViewer.class);
        registrar.accept("DevResyncRequest", DevResyncRequest.CODEC, DevResyncRequest.class);
        registrar.accept("DevRunAndFlush", DevRunAndFlush.CODEC, DevRunAndFlush.class);
        registrar.accept("DevTypeCallback", DevTypeCallback.CODEC, DevTypeCallback.class);
        registrar.accept("AnimationBound", AnimationBound.CODEC, AnimationBound.class);
        registrar.accept("AnimationDataBound", AnimationDataBound.CODEC, AnimationDataBound.class);
        registrar.accept("CameraForceBound", CameraForceBound.CODEC, CameraForceBound.class);
        registrar.accept("CameraTransitionBound", CameraTransitionBound.CODEC, CameraTransitionBound.class);
        registrar.accept("DialogBound", DialogBound.CODEC, DialogBound.class);
        registrar.accept("DialogChoiceBound", DialogChoiceBound.CODEC, DialogChoiceBound.class);
        registrar.accept("DialogResponseBound", DialogResponseBound.CODEC, DialogResponseBound.class);
        registrar.accept("Notification", Notification.CODEC, Notification.class);
        registrar.accept("Parcel", PacketParcel.CODEC, PacketParcel.class);
    }

    public static void commandRegistry(Function<LiteralArgumentBuilder<ServerCommandSource>, LiteralCommandNode<ServerCommandSource>> register) {
        register.apply(CommandManager.literal("@storyconsole").requires(css -> css.hasPermissionLevel(1))
                .then(CommandManager.argument("cmd", StringArgumentType.greedyString())
                        .executes(ctx -> {
                            String text = StringArgumentType.getString(ctx, "cmd");
                            if (text.equals(":reset")) {
                                CogwheelExecutor.createNewConsole();
                            } else {
                                CogwheelExecutor.getChatConsole().addLineRedirecting(text);
                            }
                            return 1;
                        })
                )
        );
        register.apply(CommandManager.literal("@storyanvil").requires(css -> css.hasPermissionLevel(1))
                .then(CommandManager.literal("dispatch-script")
                        .then(CommandManager.argument("name", IdentifierArgumentType.identifier())
                                .executes(ctx -> {
                                    String name = IdentifierArgumentType.getIdentifier(ctx, "name").toString();
                                    if (CogwheelConfig.isDisablingAllScripts() && !name.contains("config-main.json")) {
                                        ctx.getSource().sendError(Text.literal("Script execution is disabled from configs!").formatted(Formatting.RED));
                                        return 0;
                                    }
                                    ctx.getSource().sendFeedback(() -> Text.literal("Script execution will be dispatched"), true);
                                    try {
                                        CogScriptEnvironment.dispatchScriptGlobal(name);
                                    } catch (Exception e) {
                                        CogwheelEngine.LOGGER.error("", e);
                                    }
                                    return 0;
                                })
                        )
                )
                .then(CommandManager.literal("dump")
                        .executes(ctx -> {
                            StringBuilder sb = new StringBuilder("=== === === === === COGWHEEL ENGINE REPORT === === === === ===\n");
                            sb.append("DATE: ").append(new Date());
                            CogwheelEngine.LOGGER.warn("\n{}", sb);
                            return 0;
                        })
                )
                .then(CommandManager.literal("run-tests").executes(ctx -> {
                    TestManagement.startTesting(true);
                    return 0;
                }))
                .then(CommandManager.literal("run-all-tests").executes(ctx -> {
                    // TODO: Run all tests
                    TestManagement.startTesting(false);
                    return 0;
                }))
        );
        register.apply(CommandManager.literal("@storyclient").requires(css -> css.hasPermissionLevel(0))
                .then(CommandManager.literal("dialog")
                        .then(CommandManager.argument("answer", IntegerArgumentType.integer())
                                .then(CommandManager.argument("dialog", StringArgumentType.greedyString())
                                        .executes(ctx -> {
                                            String dialogID = StringArgumentType.getString(ctx, "dialog");
                                            if (!CogwheelExecutor.getDefaultEnvironment().getDialogs().containsKey(dialogID)) return 1;
                                            Consumer<Integer> call = CogwheelExecutor.getDefaultEnvironment().getDialogs().get(dialogID);
                                            CogwheelExecutor.getDefaultEnvironment().getDialogs().remove(dialogID);
                                            call.accept(IntegerArgumentType.getInteger(ctx, "answer"));
                                            return 0;
                                        })
                                )
                        )
                )
                .then(CommandManager.literal("dev-resync").requires(ServerCommandSource::isExecutedByPlayer).executes(ctx -> {
                    DevEarlySyncPacket.syncFor(ctx.getSource().getPlayer(), false);
                    return 0;
                }))
        );
    }

    public static JsonObject readJarResource(String path) throws IOException {
        try (InputStream in = CogwheelEngine.class.getResourceAsStream("cgwhl/" + path)) {
            if (in == null) throw new IOException("Failed to get input stream for in-jar json resource: cgwhl/" + path);
            try {
                JsonElement e = JsonParser.parseReader(new InputStreamReader(in));
                return e.getAsJsonObject();
            } catch (IllegalStateException | JsonParseException ee) {
                throw new IOException("in-jar json resource: cgwhl/" + path + " is not JSON object");
            }
        }
    }

    @ExpectPlatform @Contract("-> !null") @SuppressWarnings("Contract")
    public static KeyBinding getDevUIBind() {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static PlatformRegistry createRegistry(String modid) {
        throw new AssertionError();
    }

    public static void registryRegistry(Consumer<PlatformRegistry> cons) {
        cons.accept(CogwheelRegistries.REGISTRY);
    }

    @ExpectPlatform
    public static void startupMessage(String message) {
        throw new AssertionError();
    }

    public static interface PacketRegistrar {
        <T extends StoryPacket<T>> void accept(String id, StoryCodec<T> codec, Class<T> clazz);
    }
    
    @ExpectPlatform @Contract("-> !null") @SuppressWarnings("Contract")
    public static PlatformType getPlatform() {
        throw new AssertionError();
    }

    @ExpectPlatform @Contract("-> !null") @SuppressWarnings("Contract")
    public static String getVersion() {
        throw new AssertionError();
    }
}
