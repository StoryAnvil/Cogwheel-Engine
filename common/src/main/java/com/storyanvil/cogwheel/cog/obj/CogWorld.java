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

package com.storyanvil.cogwheel.cog.obj;

import com.storyanvil.cogwheel.entity.NPC;
import com.storyanvil.cogwheel.infrastructure.ArgumentData;
import com.storyanvil.cogwheel.infrastructure.cog.ChainCallPostPrevention;
import com.storyanvil.cogwheel.infrastructure.cog.CogString;
import com.storyanvil.cogwheel.infrastructure.cog.PreventChainCalling;
import com.storyanvil.cogwheel.infrastructure.err.CogScriptException;
import com.storyanvil.cogwheel.infrastructure.props.CGPM;
import com.storyanvil.cogwheel.infrastructure.props.JWCGPM;
import com.storyanvil.cogwheel.infrastructure.props.JWCGPM_Method;
import com.storyanvil.cogwheel.infrastructure.script.DispatchedScript;
import com.storyanvil.cogwheel.infrastructure.util.CogUtils;
import com.storyanvil.cogwheel.mixinAccess.IStoryEventScheduler;
import com.storyanvil.cogwheel.util.CogwheelExecutor;
import com.storyanvil.cogwheel.util.FriendlyWeakRef;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.TypeFilter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.storyanvil.cogwheel.util.CogwheelExecutor.log;

public class CogWorld extends JWCGPM<CogWorld> {
    private final FriendlyWeakRef<ServerWorld> ref;

    public CogWorld(ServerWorld world) {
        super(CogWorld.class);
        this.ref = new FriendlyWeakRef<>(world);
    }

    // ========== === [ JWCGPM METHODS START HERE ] === ========== \\
    @JWCGPM_Method(arguments = {CogString.class})
    public @NotNull CGPM getTaggedNPC(@NotNull ArgumentData data, @NotNull DispatchedScript script) throws CogScriptException {
        throw new PreventChainCalling(new ChainCallPostPrevention() {
            @Override
            public void prevent(String variable) throws CogScriptException {
                final String tag = data.requireString(0);
                IStoryEventScheduler.getScheduler(ref.getSafely(script))
                        .storyEvent$scheduleEvent(world -> {
                            getNPCByTag(world, variable, tag, script);
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
                notify.put(variable, CogUtils.makeCogNPC(npc[0]));
                if (npc[0] == null) {
                    log.info("{}: No NPC with tag {} found!", notify.getScriptName(), tag);
                }
                CogwheelExecutor.schedule(notify::lineDispatcher);
            }
        });
    }
}
