package com.storyanvil.cogwheel;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.storyanvil.cogwheel.entity.NPC;
import com.storyanvil.cogwheel.infrustructure.CogScriptDispatcher;
import com.storyanvil.cogwheel.infrustructure.StoryAction;
import com.storyanvil.cogwheel.util.DoubleValue;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.SynchronousQueue;
import java.util.function.Consumer;

@Mod.EventBusSubscriber(modid = CogwheelEngine.MODID)
public class EventBus {
    @SubscribeEvent
    public static void registerCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("@").requires(css -> css.hasPermission(1))
                .then(Commands.literal("dispatch-script")
                        .then(Commands.argument("name", StringArgumentType.greedyString())
                                .executes(ctx -> {
                                    ctx.getSource().sendSystemMessage(Component.literal("Script execution will be dispatched"));
                                    CogScriptDispatcher.dispatch(StringArgumentType.getString(ctx, "name"));
                                    return 0;
                                })
                        )
                )
                .then(Commands.literal("check-queue")
                        .then(Commands.argument("npc", EntityArgument.entity())
                                .executes(ctx -> {
                                    NPC npc = (NPC) EntityArgument.getEntity(ctx, "npc");
                                    StringBuilder sb = new StringBuilder();
                                    for (StoryAction<NPC> action : npc.getActionQueue()) {
                                        sb.append(action.toString()).append("\n");
                                    }
                                    ctx.getSource().sendSystemMessage(Component.literal(sb.toString()));
                                    return 0;
                                })
                        )
                )
        );
    }

    protected static List<DoubleValue<Consumer<TickEvent.LevelTickEvent>, Integer>> queue = new ArrayList<>();
    @SubscribeEvent
    public static void tick(TickEvent.LevelTickEvent event) {
        if (event.level.isClientSide()) return;
        if (event.phase == TickEvent.Phase.END) return;
        try {
            int size = queue.size();
            for (int i = 0; i < size; i++) {
                DoubleValue<Consumer<TickEvent.LevelTickEvent>, Integer> e = queue.get(i);
                if (e.getB() < 2) {
                    e.getA().accept(event);
                    queue.remove(i);
                    i--;
                } else {
                    e.setB(e.getB() - 1);
                }
            }
        } catch (IndexOutOfBoundsException e) {
            CogwheelEngine.LOGGER.warn("Queue bound error");
        }
    }
}
