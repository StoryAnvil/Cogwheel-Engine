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

package com.storyanvil.cogwheel.infrastructure.cog;

import com.storyanvil.cogwheel.CogwheelExecutor;
import com.storyanvil.cogwheel.CogwheelHooks;
import com.storyanvil.cogwheel.infrastructure.ArgumentData;
import com.storyanvil.cogwheel.infrastructure.CGPM;
import com.storyanvil.cogwheel.infrastructure.script.DispatchedScript;
import com.storyanvil.cogwheel.infrastructure.StoryAction;
import com.storyanvil.cogwheel.infrastructure.abilities.StoryActionQueue;
import com.storyanvil.cogwheel.infrastructure.abilities.StoryChatter;
import com.storyanvil.cogwheel.util.EasyPropManager;
import com.storyanvil.cogwheel.util.ObjectMonitor;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Queue;

/** TODO: Fully remove StoryLevel and its StoryActionQueue.
          Add CGPM for each {@link ServerWorld} instead
*/
public class StoryLevel implements StoryActionQueue<StoryLevel>, StoryChatter, ObjectMonitor.IMonitored {
    private static final ObjectMonitor<StoryLevel> MONITOR = new ObjectMonitor<>();
    @SuppressWarnings("unchecked")
    @Override
    public <R> void addStoryAction(StoryAction<R> action) {
        actionQueue.add((StoryAction<? extends StoryLevel>) action);
    }

    @Override
    public void chat(String text) {
        Text c = Text.literal(text);
        for (ServerPlayerEntity player : world.getPlayers()) {
            player.sendMessage(c);
        }
    }

    private final Queue<StoryAction<? extends StoryLevel>> actionQueue = new ArrayDeque<>();
    @SuppressWarnings("rawtypes")
    private StoryAction current;
    private ServerWorld world;
    @SuppressWarnings("unchecked")
    public void tick(ServerWorld level) {
        this.world = level;
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
        if (world != null)
            sb.append(" | ").append(world);
    }

    private static final EasyPropManager MANAGER = new EasyPropManager("level", StoryLevel::registerProps);

    private static void registerProps(@NotNull EasyPropManager manager) {
        manager.reg("runCommandWithAction", (name, args, script, o) -> {
            StoryLevel sl = (StoryLevel) o;
            return sl.addChained(new StoryAction.Instant<StoryLevel>() {
                @Override
                public void proceed(StoryLevel myself) {
                    CogwheelHooks.executeCommand(args.getString(0));
                }
            });
        });
        manager.reg("runCommand", (name, args, script, o) -> {
            CogwheelHooks.executeCommand(args.getString(0));
            return null;
        });
        manager.reg("getPlayers", (name, args, script, o) -> {//TODO: Add property to get players on server instead of specific world
            throw new PreventSubCalling(new SubCallPostPrevention() {
                @Override
                public void prevent(String variable) {
                    CogwheelExecutor.scheduleTickEvent(world -> {
                        ArrayList<CogPlayer> players = new ArrayList<>();
                        for (ServerPlayerEntity player : world.getPlayers()) {
                            players.add(new CogPlayer(new WeakReference<>(player)));
                        }
                        script.put(variable, CogArray.getInstance(players));
                        CogwheelExecutor.schedule(script::lineDispatcher);
                    });
                }
            });
        });
        manager.reg("put", (name, args, script, o) -> {//TODO: Move to CogMaster
            CogwheelHooks.putLevelData(args.getString(0), args.requirePrimal(1));
            return null;
        });
        manager.reg("get", (name, args, script, o) -> {//TODO: Move to CogMaster
            return CogwheelHooks.getLevelData(args.getString(0));
        });
    }

    @Override
    public boolean hasOwnProperty(String name) {
        return MANAGER.hasOwnProperty(name);
    }

    @Override
    public @Nullable CGPM getProperty(String name, ArgumentData args, DispatchedScript script) {
        return MANAGER.get(name).handle(name, args, script, this);
    }

    @Override
    public boolean equalsTo(CGPM o) {
        return o == this;
    }

    public ServerWorld getWorld() {
        return world;
    }
}
