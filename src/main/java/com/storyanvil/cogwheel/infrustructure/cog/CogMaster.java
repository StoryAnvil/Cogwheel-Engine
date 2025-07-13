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

package com.storyanvil.cogwheel.infrustructure.cog;

import com.storyanvil.cogwheel.CogwheelExecutor;
import com.storyanvil.cogwheel.EventBus;
import com.storyanvil.cogwheel.entity.NPC;
import com.storyanvil.cogwheel.infrustructure.ArgumentData;
import com.storyanvil.cogwheel.infrustructure.CogPropertyManager;
import com.storyanvil.cogwheel.infrustructure.DispatchedScript;
import com.storyanvil.cogwheel.util.EasyPropManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.AbortableIterationConsumer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.entity.EntityTypeTest;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.storyanvil.cogwheel.CogwheelExecutor.log;

public class CogMaster implements CogPropertyManager {
    private static final EasyPropManager MANAGER = new EasyPropManager("master", CogMaster::register);
    private static CogMaster instance = null;

    public static CogMaster getInstance() {
        if (instance == null) {
            instance = new CogMaster();
        }
        return instance;
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
        return o instanceof CogMaster;
    }

    @SuppressWarnings("CodeBlock2Expr")
    private static void register(EasyPropManager manager) {
        manager.reg("log", (name, args, script, o) -> {
            CogwheelExecutor.log.info("{}: {}", script.getScriptName(), args.getString(0));
            return null;
        });
        manager.reg("getTaggedNPC", (name, args, script, o) -> {
            throw new PreventSubCalling(new SubCallPostPrevention() {
                @Override
                public void prevent(String variable) {
                    CogwheelExecutor.scheduleTickEvent(event -> {
                        getNPCByTag((ServerLevel) event.level, variable, args.getString(0), script);
                    });
                }
                public static void getNPCByTag(ServerLevel level, String variable, String tag, DispatchedScript notify) {
                    final NPC[] npc = {null};
                    level.getEntities().get(new EntityTypeTest<Entity, NPC>() {
                        @Override
                        public @Nullable NPC tryCast(@NotNull Entity entity) {
                            if (entity instanceof NPC npc) return npc;
                            return null;
                        }

                        @Override
                        public @NotNull Class<? extends Entity> getBaseClass() {
                            return NPC.class;
                        }
                    }, new AbortableIterationConsumer<>() {
                        @Override
                        public @NotNull Continuation accept(@NotNull NPC value) {
                            if (value.getTags().contains(tag)) {
                                npc[0] = value;
                                return Continuation.ABORT;
                            }
                            return Continuation.CONTINUE;
                        }
                    });
                    notify.put(variable, npc[0]);
                    if (npc[0] == null) {
                        log.info("{}: No NPC with tag {} found!", notify.getScriptName(), tag);
                    }
                    CogwheelExecutor.schedule(notify::lineDispatcher);
                }
            });
        });
        manager.reg("str", (name, args, script, o) -> {
            return args.getCogString(0);
        });
        manager.reg("int", (name, args, script, o) -> {
            return new CogInteger(args.requireInt(0));
        });
        manager.reg("double", (name, args, script, o) -> {
            return new CogDouble(args.requireInt(0));
        });
        manager.reg("true", (name, args, script, o) -> {
            return CogBool.TRUE;
        });
        manager.reg("false", (name, args, script, o) -> {
            return CogBool.FALSE;
        });
        manager.reg("getLevel", (name, args, script, o) -> {
            return EventBus.getStoryLevel();
        });
        manager.reg("disposeVariable", (name, args, script, o) -> {
            script.getStorage().remove(args.getString(0));
            return null;
        });
        // Variable internal_callback should not be accessed from CogScript directly. Instead, this method should be used
        manager.reg("getEvent", (name, args, script, o) -> {
            return script.get("internal_callback");
        });
    }

    @Override
    public String convertToString() {
        return "Cogwheel Object";
    }
}
