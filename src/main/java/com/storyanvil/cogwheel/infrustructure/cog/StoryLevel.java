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

package com.storyanvil.cogwheel.infrustructure.cog;

import com.storyanvil.cogwheel.CogwheelExecutor;
import com.storyanvil.cogwheel.infrustructure.ArgumentData;
import com.storyanvil.cogwheel.infrustructure.CogPropertyManager;
import com.storyanvil.cogwheel.infrustructure.DispatchedScript;
import com.storyanvil.cogwheel.infrustructure.StoryAction;
import com.storyanvil.cogwheel.infrustructure.abilities.StoryActionQueue;
import com.storyanvil.cogwheel.infrustructure.abilities.StoryChatter;
import com.storyanvil.cogwheel.util.EasyPropManager;
import com.storyanvil.cogwheel.util.ObjectMonitor;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Queue;

public class StoryLevel implements StoryActionQueue<StoryLevel>, StoryChatter, ObjectMonitor.IMonitored {
    private static final ObjectMonitor<StoryLevel> MONITOR = new ObjectMonitor<>();
    @SuppressWarnings("unchecked")
    @Override
    public <R> void addStoryAction(StoryAction<R> action) {
        actionQueue.add((StoryAction<? extends StoryLevel>) action);
    }

    @Override
    public void chat(String text) {
        Component c = Component.literal(text);
        for (ServerPlayer player : level.players()) {
            player.sendSystemMessage(c);
        }
    }

    private final Queue<StoryAction<? extends StoryLevel>> actionQueue = new ArrayDeque<>();
    @SuppressWarnings("rawtypes")
    private StoryAction current;
    private ServerLevel level;
    @SuppressWarnings("unchecked")
    public void tick(ServerLevel level) {
        this.level = level;
        if (current != null) {
            if (current.freeToGo(this)) {
                current = null;
            }
        } else {
            if (actionQueue.isEmpty()) return;
            current = actionQueue.remove();
            current.proceed(this);
        }
    }

    public StoryLevel() {
        MONITOR.register(this);
    }

    @Override
    public void reportState(StringBuilder sb) {
        for (StoryAction<?> action : actionQueue) {
            sb.append(action.toString());
        }
        if (current != null)
            sb.append(">").append(current);
        if (level != null)
            sb.append(" | ").append(level);
    }

    private static final EasyPropManager MANAGER = new EasyPropManager("level", StoryLevel::registerProps);

    private static void registerProps(@NotNull EasyPropManager manager) {
        manager.reg("runCommand", (name, args, script, o) -> {
            StoryLevel sl = (StoryLevel) o;
            return sl.addChained(new StoryAction.Instant<StoryLevel>() {
                @Override
                public void proceed(StoryLevel myself) {
                    myself.level.getServer().getCommands().performPrefixedCommand(
                            new CommandSourceStack(CommandSource.NULL, new Vec3(0, 0, 0),
                                    new Vec2(0, 0), myself.level, 4, "COGWHEEL", Component.literal("COGWHEEL"), myself.level.getServer(), null),
                            args.getString(0));
                }
            });
        });
        manager.reg("getPlayers", (name, args, script, o) -> {
            throw new PreventSubCalling(new SubCallPostPrevention() {
                @Override
                public void prevent(String variable) {
                    CogwheelExecutor.scheduleTickEvent(event -> {
                        ArrayList<CogPlayer> players = new ArrayList<>();
                        for (ServerPlayer player : ((ServerLevel) event.level).players()) {
                            players.add(new CogPlayer(new WeakReference<>(player)));
                        }
                        script.put(variable, CogArray.getInstance(players));
                        CogwheelExecutor.schedule(script::lineDispatcher);
                    });
                }
            });
        });
        manager.reg("put", (name, args, script, o) -> {
            script.getEnvironment().getData().put(args.getString(0), args.requirePrimal(1));
            return null;
        });
        manager.reg("get", (name, args, script, o) -> {
            return script.getEnvironment().getData().get(args.getString(0));
        });
    }

    @Override
    public boolean hasOwnProperty(String name) {
        return MANAGER.hasOwnProperty(name);
    }

    @Override
    public @Nullable CogPropertyManager getProperty(String name, ArgumentData args, DispatchedScript script) {
        return MANAGER.get(name).handle(name, args, script, this);
    }

    @Override
    public boolean equalsTo(CogPropertyManager o) {
        return o == this;
    }

    public ServerLevel getLevel() {
        return level;
    }
}
