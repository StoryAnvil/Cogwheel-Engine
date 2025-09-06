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

package com.storyanvil.cogwheel;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.storyanvil.cogwheel.api.Api;
import com.storyanvil.cogwheel.config.CogwheelConfig;
import com.storyanvil.cogwheel.infrastructure.StoryAction;
import com.storyanvil.cogwheel.infrastructure.cog.CogTestCallback;
import com.storyanvil.cogwheel.infrastructure.cog.StoryLevel;
import com.storyanvil.cogwheel.infrastructure.env.CogScriptEnvironment;
import com.storyanvil.cogwheel.network.belt.BeltCommunications;
import com.storyanvil.cogwheel.network.belt.BeltPacket;
import com.storyanvil.cogwheel.network.mc.AnimationDataBound;
import com.storyanvil.cogwheel.network.mc.CogwheelPacketHandler;
import com.storyanvil.cogwheel.registry.CogwheelRegistries;
import com.storyanvil.cogwheel.util.*;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.*;
import java.util.function.Consumer;

import static com.storyanvil.cogwheel.CogwheelExecutor.log;

@Mod.EventBusSubscriber(modid = CogwheelEngine.MODID) @Api.Internal @ApiStatus.Internal
public class EventBus {

    @SubscribeEvent @Api.Internal @ApiStatus.Internal
    public static void dataInjector(AddPackFindersEvent event) {
//        // TODO: Inject datapacks and resourcepacks
//        switch (event.getPackType()) {
//            case SERVER_DATA, CLIENT_RESOURCES -> {
//                event.addRepositorySource(new CogRepository(event.getPackType()));
//            }
//            default -> {
//                CogwheelEngine.LOGGER.warn("DATA-INJECTOR FAILURE: UNKNOWN PACK TYPE \"{}\" RECEIVED", event.getPackType().name());
//            }
//        }
    }

    @SubscribeEvent @Api.Internal @ApiStatus.Internal
    public static void registerCommands(@NotNull RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("@storyconsole").requires(css -> css.hasPermission(1))
                .then(Commands.argument("cmd", StringArgumentType.greedyString())
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
        event.getDispatcher().register(Commands.literal("@storyanvil").requires(css -> css.hasPermission(1))
                .then(Commands.literal("dispatch-script")
                        .then(Commands.argument("name", ResourceLocationArgument.id())
                                .executes(ctx -> {
                                    String name = ResourceLocationArgument.getId(ctx, "name").toString();
                                    if (CogwheelConfig.isDisablingAllScripts() && !name.contains("config-main.json")) {
                                        ctx.getSource().sendSystemMessage(Component.literal("Script execution is disabled from configs!").withStyle(ChatFormatting.RED));
                                        return 0;
                                    }
                                    ctx.getSource().sendSystemMessage(Component.literal("Script execution will be dispatched"));
                                    try {
                                        CogScriptEnvironment.dispatchScriptGlobal(name);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    return 0;
                                })
                        )
                )
                .then(Commands.literal("dump")
                        .executes(ctx -> {
                            StringBuilder sb = new StringBuilder("=== === === === === COGWHEEL ENGINE REPORT === === === === ===\n");
                            sb.append("DATE: ").append(new Date());
                            sb.append("\nMODDED: ").append(guessIfModded()).append("\n");
                            ObjectMonitor.dumpAll(sb);
                            CogwheelEngine.LOGGER.warn("\n{}", sb);
                            return 0;
                        })
                )
                .then(Commands.literal("run-tests").executes(ctx -> {
                    CogwheelExecutor.schedule(() -> {
                        CogScriptEnvironment.TestEnvironment environment = new CogScriptEnvironment.TestEnvironment();
                        File scripts = new File(Minecraft.getInstance().gameDirectory, "config/cog");
                        for (File f : Objects.requireNonNull(scripts.listFiles(), "No scripts available")) {
                            String name = f.getName();
                            if (name.startsWith("test.") && !name.startsWith("test..")) {
                                ScriptStorage s = new ScriptStorage();
                                CogTestCallback callback = new CogTestCallback();
                                s.put("TEST", callback);
                                environment.dispatchScript(name.substring(5), s);
                                while (!callback.isComplete()) {
                                    try {
                                        //noinspection BusyWait
                                        Thread.sleep(10);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }
                                if (callback.isSuccessful()) {
                                    ctx.getSource().sendSystemMessage(Component.literal("Test " + name + " completed!").withStyle(x -> x.withColor(TextColor.fromLegacyFormat(ChatFormatting.GREEN))));
                                } else {
                                    ctx.getSource().sendSystemMessage(Component.literal("Test " + name + " failed!").withStyle(x -> x.withColor(TextColor.fromLegacyFormat(ChatFormatting.RED))));
                                }
                            }
                        }
                        environment.dispose();
                        ctx.getSource().sendSystemMessage(Component.literal("All tests completed!"));
                    });
                    return 0;
                }))
                .then(Commands.literal("run-all-tests").executes(ctx -> {
                    CogwheelExecutor.schedule(() -> {
                        CogScriptEnvironment.TestEnvironment environment = new CogScriptEnvironment.TestEnvironment();
                        File scripts = new File(Minecraft.getInstance().gameDirectory, "config/cog");
                        for (File f : Objects.requireNonNull(scripts.listFiles(), "No scripts available")) {
                            String name = f.getName();
                            if (name.startsWith("test.")) {
                                ScriptStorage s = new ScriptStorage();
                                CogTestCallback callback = new CogTestCallback();
                                s.put("TEST", callback);
                                environment.dispatchScript(name.substring(5), s);
                                while (!callback.isComplete()) {
                                    try {
                                        //noinspection BusyWait
                                        Thread.sleep(10);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }
                                if (callback.isSuccessful()) {
                                    ctx.getSource().sendSystemMessage(Component.literal("Test " + name + " completed!").withStyle(x -> x.withColor(TextColor.fromLegacyFormat(ChatFormatting.GREEN))));
                                } else {
                                    ctx.getSource().sendSystemMessage(Component.literal("Test " + name + " failed!").withStyle(x -> x.withColor(TextColor.fromLegacyFormat(ChatFormatting.RED))));
                                }
                            }
                        }
                        environment.dispose();
                        ctx.getSource().sendSystemMessage(Component.literal("All tests completed!"));
                    });
                    return 0;
                }))
                .then(Commands.literal("cogwheel-belt")
                        .then(Commands.literal("start")
                                .then(Commands.argument("host", StringArgumentType.greedyString())
                                        .executes(ctx -> {
                                            try {
                                                beltCommunications = BeltCommunications.create(StringArgumentType.getString(ctx, "host"));
                                            } catch (Exception e) {
                                                log.warn("Failed to connect BELT PROTOCOL", e);
                                            }
                                            return 0;
                                        })
                                )
                        )
                        .then(Commands.literal("getLink")
                                .executes(ctx -> {
                                    if (beltCommunications == null) {
                                        ctx.getSource().getPlayerOrException().sendSystemMessage(Component.literal(
                                                "No belt server is connected"
                                        ));
                                        return 1;
                                    }
                                    String link = beltCommunications.getUserLink(ctx.getSource().getPlayerOrException());
                                    ctx.getSource().getPlayerOrException().sendSystemMessage(Component.literal(
                                            "Click to open " + link + " | This will authorize you on " + beltCommunications.getRemoteServerName()
                                    ).withStyle(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, link))));
                                    return 0;
                                })
                        )
                        .then(Commands.literal("auth")
                                .then(Commands.argument("code", StringArgumentType.greedyString())
                                        .executes(ctx -> {
                                            if (beltCommunications == null) {
                                                ctx.getSource().getPlayerOrException().sendSystemMessage(Component.literal(
                                                        "No belt server is connected"
                                                ));
                                                return 1;
                                            }
                                            String link = beltCommunications.getHost() + "/~";
                                            ctx.getSource().getPlayerOrException().sendSystemMessage(Component.literal(
                                                    "You will be authorized on " + beltCommunications.getRemoteServerName() + " as soon as possible."
                                            ).append("\nVisit " + link + " to check").withStyle(s -> s.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, link))));
                                            BeltPacket.createAuthCode(StringArgumentType.getString(ctx, "code"), ctx.getSource().getPlayerOrException().getScoreboardName());
                                            return 0;
                                        })
                                )
                        )
                )
        );
        event.getDispatcher().register(Commands.literal("@storyclient").requires(css -> css.hasPermission(0))
                .then(Commands.literal("dialog")
                        .then(Commands.argument("answer", IntegerArgumentType.integer())
                                .then(Commands.argument("dialog", StringArgumentType.greedyString())
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
        );
    }

    private static @NotNull StringBuilder guessIfModded() {
        StringBuilder modded = new StringBuilder();
        for (Bi<ScriptLineHandler, Boolean> handler : CogwheelRegistries.getLineHandlers()) {
            if (!handler.getA().getResourceLocation().getNamespace().equals(CogwheelEngine.MODID)) {
                modded.append(" DETECTED CUSTOM LINE HANDLERS");
            }
        }
        if (modded.isEmpty()) {
            modded.append("NO");
        } else modded.insert(0, "YES:");
        return modded;
    }

    @Api.Internal @ApiStatus.Internal
    protected static final List<Bi<Consumer<TickEvent.LevelTickEvent>, Integer>> queue = new ArrayList<>();
    @Api.Internal @ApiStatus.Internal
    protected static final List<Bi<Consumer<TickEvent.LevelTickEvent>, Integer>> clientQueue = new ArrayList<>();

    private static final StoryLevel level = new StoryLevel();
    @Api.Internal @ApiStatus.Internal
    public static BeltCommunications beltCommunications = null;
    @SubscribeEvent @Api.Internal @ApiStatus.Internal
    public static void tick(TickEvent.@NotNull LevelTickEvent event) {
        if (!event.level.dimension().location().equals(ResourceLocation.fromNamespaceAndPath("minecraft", "overworld"))) return;
        if (event.level.isClientSide()) {
            if (event.phase != TickEvent.Phase.END) return;
            synchronized (clientQueue) {
                try {
                    for (int i = 0; i < clientQueue.size(); i++) {
                        Bi<Consumer<TickEvent.LevelTickEvent>, Integer> e = clientQueue.get(i);
                        if (e.getB() < 2) {
                            e.getA().accept(event);
                            clientQueue.remove(i);
                            i--;
                        } else {
                            e.setB(e.getB() - 1);
                        }
                    }
                } catch (Exception e) {
                    CogwheelEngine.LOGGER.warn("Client Queue bound error", e);
                }
                return;
            }
        }
        if (event.phase == TickEvent.Phase.END) {
            level.tick((ServerLevel) event.level);
            return;
        }
        synchronized (queue) {
            try {
                for (int i = 0; i < queue.size(); i++) {
                    Bi<Consumer<TickEvent.LevelTickEvent>, Integer> e = queue.get(i);
                    if (e.getB() < 2) {
                        try {
                            e.getA().accept(event);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                        queue.remove(i);
                        i--;
                    } else {
                        e.setB(e.getB() - 1);
                    }
                }
            } catch (Exception e) {
                CogwheelEngine.LOGGER.warn("Queue bound error", e);
            }
        }
    }

    @SubscribeEvent @Api.Internal @ApiStatus.Internal
    public static void boundEvent(PlayerEvent.@NotNull PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            StringBuilder sb = new StringBuilder();
            boolean a = true;
            for (ResourceLocation loc : serverSideAnimations) {
                if (a) {
                    a = false;
                } else {
                    sb.append("|");
                }
                sb.append(loc.toString());
            }
            CogwheelPacketHandler.DELTA_BRIDGE.send(PacketDistributor.PLAYER.with(() -> player), new AnimationDataBound(sb.toString()));
        }
    }

    @Api.Internal @ApiStatus.Internal
    public static ArrayList<ResourceLocation> serverSideAnimations = new ArrayList<>();
    private static final HashMap<String, WeakList<LabelCloseable>> labelListeners = new HashMap<>();
    @Api.Internal @ApiStatus.Internal
    public static void hitLabel(String label, StoryAction<?> action) {
        if (labelListeners.containsKey(label)) {
            WeakList<LabelCloseable> c = labelListeners.get(label);
            //noinspection ForLoopReplaceableByForEach
            for (int i = 0; i < c.size(); i++) {
                LabelCloseable closeable = c.get(i);
                if (closeable != null)
                    closeable.close(label, action);
            }
        }
    }
    @Api.Experimental(since = "2.1.0")
    public static void register(String label, LabelCloseable closeable) {
        if (labelListeners.containsKey(label)) {
            labelListeners.get(label).add(closeable);
        } else {
            labelListeners.put(label, new WeakList<>(closeable));
        }
    }

    @Contract(pure = true) @Api.Experimental(since = "2.1.0")
    public static StoryLevel getStoryLevel() {
        return level;
    }

}
