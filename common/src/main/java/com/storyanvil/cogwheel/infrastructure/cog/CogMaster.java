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
import com.storyanvil.cogwheel.EventBus;
import com.storyanvil.cogwheel.api.Api;
import com.storyanvil.cogwheel.config.CogwheelConfig;
import com.storyanvil.cogwheel.entity.NPC;
import com.storyanvil.cogwheel.infrastructure.ArgumentData;
import com.storyanvil.cogwheel.infrastructure.CGPM;
import com.storyanvil.cogwheel.infrastructure.script.DispatchedScript;
import com.storyanvil.cogwheel.infrastructure.env.CogScriptEnvironment;
import com.storyanvil.cogwheel.network.mc.AnimationDataBound;
import com.storyanvil.cogwheel.registry.CogwheelRegistries;
import com.storyanvil.cogwheel.util.EasyPropManager;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypeFilter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Random;

import static com.storyanvil.cogwheel.CogwheelExecutor.log;

public class CogMaster implements CGPM {
    private static final EasyPropManager MANAGER = new EasyPropManager("master", CogMaster::register);
    private static CogMaster instance = null;
    private static Random random = new Random();

    @Api.Experimental(since = "2.0.0")
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
    public @Nullable CGPM getProperty(String name, ArgumentData args, DispatchedScript script) {
        return MANAGER.get(name).handle(name, args, script, this);
    }

    @Override
    public boolean equalsTo(CGPM o) {
        return o instanceof CogMaster;
    }

    @SuppressWarnings("CodeBlock2Expr")
    private static void register(@NotNull EasyPropManager manager) {
        manager.reg("log", (name, args, script, o) -> {
            CogwheelExecutor.log.info("{}: {}", script.getScriptName(), args.getString(0));
            return null;
        });
        manager.reg("debug", (name, args, script, o) -> {
            if (CogwheelConfig.isDevEnvironment())
                CogwheelExecutor.log.info("[DBG] {}: {}", script.getScriptName(), args.getString(0));
            return null;
        });
        manager.reg("isDebugging", (name, args, script, o) -> {
            return CogBool.getInstance(CogwheelConfig.isDevEnvironment());
        });
        manager.reg("getTaggedNPC", (name, args, script, o) -> {
            throw new PreventSubCalling(new SubCallPostPrevention() {
                @Override
                public void prevent(String variable) { //TODO: Possibility to select specific world NPC will be searched in
                    CogwheelExecutor.scheduleTickEvent(world -> {
                        getNPCByTag(world, variable, args.getString(0), script);
                    });
                }
                public static void getNPCByTag(@NotNull ServerWorld level, String variable, String tag, @NotNull DispatchedScript notify) {
                    final NPC[] npc = {null};
                    level.getEntitiesByType(new TypeFilter<Entity, NPC>() {
                        @Override
                        public @Nullable NPC downcast(@NotNull Entity entity) {
                            if (entity instanceof NPC npc) return npc;
                            return null;
                        }

                        @Override
                        public @NotNull Class<? extends Entity> getBaseClass() {
                            return NPC.class;
                        }
                    }, npc1 -> {
                        if (npc1.getCommandTags().contains(tag)) {
                            npc[0] = npc1;
                            return false;
                        }
                        return true;
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
            return new CogDouble(args.requireDouble(0));
        });
        manager.reg("toInt", (name, args, script, o) -> {
            return new CogInteger(args.getString(0));
        });
        manager.reg("toDouble", (name, args, script, o) -> {
            return new CogDouble(args.getString(0));
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
        manager.reg("randomInt", (name, args, script, o) -> {
            return new CogInteger(random.nextInt(args.requireInt(0), args.requireInt(1)));
        });
        manager.reg("createList", (name, args, script, o) -> {
            return CogArray.getInstance((CGPM) args.get(0));
        });
        manager.reg("dispatchScript", (name, args, script, o) -> {
            if (args.size() == 1)
                script.getEnvironment().dispatchScript(args.getString(0));
            else
                script.getEnvironment().dispatchScript(args.getString(0), ((CogHashmap) args.get(1)).getValue());
            return null;
        });
        manager.reg("scriptInvoker", (name, args, script, o) -> {
            return CogInvoker.scriptInvoker(Identifier.tryParse(args.getString(0)));
        });
        manager.reg("scheduleScript", (name, args, script, o) -> {
            CogwheelExecutor.schedule(() -> {
                script.getEnvironment().dispatchScript(args.getString(0));
            }, args.requireInt(1));
            return null;
        });
        manager.reg("scheduleThis", (name, args, script, o) -> {
            throw new PreventSubCalling(new SubCallPostPrevention() {
                @Override
                public void prevent(String variable) {
                    CogwheelExecutor.schedule(script::lineDispatcher, args.requireInt(0));
                }
            });
        });
        manager.reg("dispatchScriptGlobal", (name, args, script, o) -> {
            if (args.size() == 1)
                CogScriptEnvironment.dispatchScriptGlobal(args.getString(0));
            else
                CogScriptEnvironment.dispatchScriptGlobal(args.getString(0), ((CogHashmap) args.get(1)).getValue());
            return null;
        });
        manager.reg("waitForStoryActionLabel", (name, args, script, o) -> {
            throw new PreventSubCalling(new SubCallPostPrevention() {
                @Override
                public void prevent(String variable) {
                    EventBus.register(args.getString(0), (label, host) -> {
                        CogwheelExecutor.schedule(script::lineDispatcher);
                    });
                }
            });
        });
        manager.reg("addAnimationSource", (name, args, script, o) -> {
            Identifier loc = Identifier.tryParse(args.getString(0) + ".json");
            String m = loc.toString();
            boolean found = false;
            for (Identifier r : EventBus.serverSideAnimations) {
                if (r.toString().equals(m)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                EventBus.serverSideAnimations.add(loc);

                StringBuilder sb = new StringBuilder();
                boolean a = true;
                for (Identifier locc : EventBus.serverSideAnimations) {
                    if (a) {
                        a = false;
                    } else {
                        sb.append("|");
                    }
                    sb.append(locc.toString());
                }
                CogwheelHooks.sendPacketToEveryone(new AnimationDataBound(sb.toString()));
            }
            return null;
        });
        manager.reg("removeAnimationSource", (name, args, script, o) -> {
            Identifier loc = Identifier.tryParse(args.getString(0) + ".json");
            String m = loc.toString();
            boolean found = false;
            for (int i = 0; i < EventBus.serverSideAnimations.size(); i++) {
                Identifier bad = EventBus.serverSideAnimations.get(i);
                if (bad.toString().equals(m)) {
                    EventBus.serverSideAnimations.remove(i);
                    found = true;
                    break;
                }
            }
            if (found) {
                StringBuilder sb = new StringBuilder();
                boolean a = true;
                for (Identifier locc : EventBus.serverSideAnimations) {
                    if (a) {
                        a = false;
                    } else {
                        sb.append("|");
                    }
                    sb.append(locc.toString());
                }
                CogwheelHooks.sendPacketToEveryone(new AnimationDataBound(sb.toString()));
            }
            return null;
        });
        manager.reg("clearAnimationSources", (name, args, script, o) -> {
            EventBus.serverSideAnimations.clear();
            CogwheelHooks.sendPacketToEveryone(new AnimationDataBound(""));
            return null;
        });
        manager.reg("createHashmap", (name, args, script, o) -> {
            return new CogHashmap();
        });
        manager.reg("time", (name, args, script, o) -> {
            return new CogLong(System.currentTimeMillis() - script.getEnvironment().getCreationTime());
        });
        manager.reg("dump", (name, args, script, o) -> {
            for (Map.Entry<String, CGPM> entry : script.getStorage().entrySet()) {
                log.info("{}: >{}< = {}", script.getScriptName(), entry.getKey(), entry.getValue().convertToString());
            }
            return null;
        });
        manager.reg("range", (name, args, script, o) -> {
            return new CogRange(0, args.requireInt(0));
        });
        manager.reg("range2", (name, args, script, o) -> {
            return new CogRange(args.requireInt(0), args.requireInt(1));
        });
        manager.reg("choose", (name, args, script, o) -> {
            boolean b = args.requireBoolean(0);
            return b ? args.get(1) : args.get(2);
        });
        manager.reg("map", (name, args, script, o) -> {
            int b = args.requireInt(0);
            return args.get(b + 1);
        });
        manager.reg("executeCommand", (name, args, script, o) -> {
            CogwheelHooks.executeCommand(args.getString(0));
            return null;
        });
    }

    @Override
    public String convertToString() {
        return "Cogwheel Object";
    }
}
